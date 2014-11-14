package com.hp.score.lang.cli;

import com.hp.score.api.TriggeringProperties;
import com.hp.score.lang.cli.utils.CompilerUtils;
import com.hp.score.lang.compiler.SlangCompiler;
import com.hp.score.lang.entities.CompilationArtifact;
import com.hp.score.lang.entities.ScoreLangConstants;
import com.hp.score.lang.runtime.env.RunEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date: 11/7/2014
 *
 * @author lesant
 */

@Component
public class SlangCLI implements CommandMarker {

    @Autowired
    private SlangCompiler compiler;

    private static final String currently = "You are currently running Score version: ";
    private static final String scoreVersion = "0.1.229"; //todo get version

    @CliCommand(value = "slang run", help = "Runs a flow")
    public void run(
            @CliOption(key = "f", mandatory = true, help = "Path to filename. e.g. slang run --f C:\\Slang\\flow.yaml") final String filePath,
            @CliOption(key = "cp", mandatory = false, help = "Classpath") final String classPath,
            @CliOption(key = "sp", mandatory = false, help = "System property file location") final String systemProperty,
            @CliOption(key = "D", mandatory = false, help = "inputs in a key=value comma separated list") final Map<String, String> inputs) throws IOException {

        File file = CompilerUtils.getFile(filePath);
        File operation = CompilerUtils.getFile("yaml/firstOperation.yaml"); //will solve with classpath

        List<File> path = new ArrayList<>();
        path.add(operation);

        CompilationArtifact compilationArtifact = compiler.compileFlow(file, path);

        trigger(compilationArtifact, inputs);

    }
    private void trigger(CompilationArtifact compilationArtifact, Map<String, String> inputs){
        Map<String, Serializable> executionContext = new HashMap<>();
        executionContext.put(ScoreLangConstants.RUN_ENV, new RunEnvironment());
        executionContext.put(ScoreLangConstants.USER_INPUTS_KEY, (Serializable) inputs);

        TriggeringProperties triggeringProperties = TriggeringProperties
                .create(compilationArtifact.getExecutionPlan())
                .setDependencies(compilationArtifact.getDependencies())
                .setContext(executionContext);
    }

    @CliCommand(value = "slang -version", help = "Prints the score version used")
    public String version() {
        return currently + scoreVersion;
    }

    public static String getVersion() {
        return scoreVersion;
    }
}
