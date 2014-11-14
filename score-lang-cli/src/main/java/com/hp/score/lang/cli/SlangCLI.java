package com.hp.score.lang.cli;

import com.hp.score.api.TriggeringProperties;
import com.hp.score.events.EventConstants;
import com.hp.score.events.ScoreEvent;
import com.hp.score.events.ScoreEventListener;
import com.hp.score.lang.cli.services.ScoreServices;
import com.hp.score.lang.cli.utils.CompilerUtils;
import com.hp.score.lang.compiler.SlangCompiler;
import com.hp.score.lang.entities.CompilationArtifact;
import com.hp.score.lang.entities.ScoreLangConstants;
import com.hp.score.lang.runtime.env.RunEnvironment;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_ACTION_START;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_ACTION_END;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_ACTION_ERROR;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_INPUT_START;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_INPUT_END;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_OUTPUT_START;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_OUTPUT_END;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_STEP_ERROR;

/**
 * Date: 11/7/2014
 *
 * @author lesant
 */

@Component
public class SlangCLI implements CommandMarker {

    @Autowired
    private SlangCompiler compiler;

    @Autowired
    private ScoreServices scoreServices;

    private final static Logger logger = Logger.getLogger(SlangCLI.class);
    private static final String currently = "You are currently running Score version: ";
    private static final String scoreVersion = "0.1.229"; //todo get version

    @CliCommand(value = "slang run", help = "Runs a flow")
    public void run(
            @CliOption(key = "f", mandatory = true, help = "Path to filename. e.g. slang run --f C:\\Slang\\flow.yaml") final String filePath,
            @CliOption(key = "cp", mandatory = false, help = "Classpath") final String classPath,
            @CliOption(key = "sp", mandatory = false, help = "System property file location") final String systemProperty,
            @CliOption(key = "D", mandatory = false, help = "inputs in a key=value comma separated list") final Map<String, String> inputs) throws IOException {

        File file = CompilerUtils.getFile(filePath);
        File operation = CompilerUtils.getFile("yaml/operation.yaml"); //will solve with classpath

        List<File> path = new ArrayList<>();
        path.add(operation);

        CompilationArtifact compilationArtifact = compiler.compileFlow(file, path);

        trigger(compilationArtifact, inputs);
    }

    @CliCommand(value = "slang -version", help = "Prints the score version used")
    public String version() {
        return currently + scoreVersion;
    }

    public static String getVersion() {
        return scoreVersion;
    }

    @PostConstruct
    private void registerEventHandlers() {
        Set<String> handlerTypes = new HashSet<>();
        handlerTypes.add(EventConstants.SCORE_FINISHED_EVENT);
        handlerTypes.add(EventConstants.SCORE_ERROR_EVENT);
        handlerTypes.add(EventConstants.SCORE_FAILURE_EVENT);
        handlerTypes.add(EVENT_ACTION_START);
        handlerTypes.add(EVENT_ACTION_END);
        handlerTypes.add(EVENT_ACTION_ERROR);
        handlerTypes.add(EVENT_INPUT_START);
        handlerTypes.add(EVENT_INPUT_END);
        handlerTypes.add(EVENT_OUTPUT_START);
        handlerTypes.add(EVENT_OUTPUT_END);
        handlerTypes.add(EVENT_STEP_ERROR);
        scoreServices.subscribe(new ScoreEventListener() {
            @Override
            public void onEvent(ScoreEvent event) {
                logEvent(event);
            }
        }, handlerTypes);
    }

    private void logEvent(ScoreEvent event) {
        logger.info(("Event received: " + event.getEventType() + " Data is: " + event.getData()));
    }

    private void trigger(CompilationArtifact compilationArtifact, Map<String, String> inputs) {
        Map<String, Serializable> executionContext = new HashMap<>();
        executionContext.put(ScoreLangConstants.RUN_ENV, new RunEnvironment());
        executionContext.put(ScoreLangConstants.USER_INPUTS_KEY, (Serializable) inputs);

        TriggeringProperties triggeringProperties = TriggeringProperties
                .create(compilationArtifact.getExecutionPlan())
                .setDependencies(compilationArtifact.getDependencies())
                .setContext(executionContext);

        scoreServices.trigger(triggeringProperties);
    }
}
