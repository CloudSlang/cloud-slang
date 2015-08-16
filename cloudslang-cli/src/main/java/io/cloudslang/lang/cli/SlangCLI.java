/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.cli;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import io.cloudslang.lang.cli.services.ScoreServices;
import io.cloudslang.lang.cli.utils.CompilerHelper;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.events.ScoreEventListener;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Input;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * @author lesant
 * @since 11/07/2014
 * @version $Id$
 */
@Component
public class SlangCLI implements CommandMarker {

    public static final String TRIGGERED_FLOW_MSG = "Triggered flow : ";
    public static final String WITH_EXECUTION_ID_MSG = "Execution id: ";
    public static final String FLOW_EXECUTION_TIME_TOOK = ", duration: ";
    private static final String CURRENTLY = "You are CURRENTLY running CloudSlang version: ";
    private final static Logger logger = Logger.getLogger(SlangCLI.class);

    @Autowired
    private ScoreServices scoreServices;

    @Autowired
    private CompilerHelper compilerHelper;

    @Value("${slang.version}")
    private String slangVersion;

    /**
     * This global param holds the state of the CLI, if flows need to run in ASYNC or in SYNC manner.
     */
    private Boolean triggerAsync = false;

    @CliCommand(value = "run", help = "triggers a CloudSlang flow")
    public String run(
            @CliOption(key = {"", "f", "file"}, mandatory = true, help = "Path to filename. e.g. cslang run --f C:\\CloudSlang\\flow.yaml") final File file,
            @CliOption(key = {"cp", "classpath"}, mandatory = false, help = "Classpath , a directory comma separated list to flow dependencies, by default it will take flow file dir") final List<String> classPath,
            @CliOption(key = {"i", "inputs"}, mandatory = false, help = "inputs in a key=value comma separated list") final Map<String,? extends Serializable> inputs,
            @CliOption(key = {"if", "input-file"}, mandatory = false, help = "comma separated list of input file locations") final List<String> inputFiles,
            @CliOption(key = {"", "q", "quiet"}, mandatory = false, help = "quiet", specifiedDefaultValue = "true",unspecifiedDefaultValue = "false") final Boolean quiet,
            @CliOption(key = {"spf", "system-property-file"}, mandatory = false, help = "comma separated list of system property file locations") final List<String> systemPropertyFiles) throws IOException {

        CompilationArtifact compilationArtifact = compilerHelper.compile(file.getAbsolutePath(), classPath);
        Map<String, ? extends Serializable> systemProperties = compilerHelper.loadSystemProperties(systemPropertyFiles);
        Map<String, ? extends Serializable> inputsFromFile = compilerHelper.loadInputsFromFile(inputFiles);
        Map<String, Serializable> mergedInputs = new HashMap<>();

        if(MapUtils.isNotEmpty(inputsFromFile)){
            mergedInputs.putAll(inputsFromFile);
        }
        if(MapUtils.isNotEmpty(inputs)) {
            mergedInputs.putAll(inputs);
        }

        Long id;
        if (!triggerAsync) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            id = scoreServices.triggerSync(compilationArtifact, mergedInputs, systemProperties, quiet);
            stopWatch.stop();
            return quiet ? StringUtils.EMPTY : triggerSyncMsg(id, stopWatch.toString());
        }
        id = scoreServices.trigger(compilationArtifact, mergedInputs, systemProperties);
        return quiet ? StringUtils.EMPTY : triggerAsyncMsg(id, compilationArtifact.getExecutionPlan().getName());
    }

    @CliCommand(value = "env", help = "Set environment var relevant to the CLI")
    public String setEnvVar(
            @CliOption(key = "setAsync", mandatory = true, help = "set the async") final boolean switchAsync) throws IOException {
        triggerAsync = switchAsync;
        return setEnvMessage(triggerAsync);
    }

    @CliCommand(value = "inputs", help = "Get flow inputs")
    public List<String> getFlowInputs(
            @CliOption(key = {"", "f", "file"}, mandatory = true, help = "Path to filename. e.g. cslang inputs --f C:\\CloudSlang\\flow.yaml") final File file,
            @CliOption(key = {"cp", "classpath"}, mandatory = false, help = "Classpath , a directory comma separated list to flow dependencies, by default it will take flow file dir") final List<String> classPath)
            throws IOException {
        CompilationArtifact compilationArtifact = compilerHelper.compile(file.getAbsolutePath(), classPath);
        List<Input> inputs = compilationArtifact.getInputs();
        List<String> inputsResult = new ArrayList<>();
        for (Input input : inputs) {
            if (input.isOverridable()) {
                inputsResult.add(input.getName());
            }
        }
        return inputsResult;
    }

    @CliCommand(value = "cslang --version", help = "Prints the CloudSlang version used")
    public String version() {
        return CURRENTLY + slangVersion;
    }

    public static String triggerSyncMsg(Long id, String duration) {
        return WITH_EXECUTION_ID_MSG + id + FLOW_EXECUTION_TIME_TOOK + duration;
    }

    public static String triggerAsyncMsg(Long id, String flowName) {
        return TRIGGERED_FLOW_MSG + flowName + WITH_EXECUTION_ID_MSG + id;
    }

    public static String setEnvMessage(boolean triggerAsync) {
        return "flow execution ASYNC execution was changed to : " + triggerAsync;
    }

    @PostConstruct
    private void registerEventHandlers() {
        Set<String> handlerTypes = new HashSet<>();
        handlerTypes.add(EventConstants.SCORE_FINISHED_EVENT);
        handlerTypes.add(EventConstants.SCORE_ERROR_EVENT);
        handlerTypes.add(EventConstants.SCORE_FAILURE_EVENT);
        handlerTypes.add(ScoreLangConstants.EVENT_ACTION_START);
        handlerTypes.add(ScoreLangConstants.EVENT_ACTION_END);
        handlerTypes.add(ScoreLangConstants.EVENT_ACTION_ERROR);
        handlerTypes.add(ScoreLangConstants.EVENT_TASK_START);
        handlerTypes.add(ScoreLangConstants.EVENT_INPUT_START);
        handlerTypes.add(ScoreLangConstants.EVENT_INPUT_END);
        handlerTypes.add(ScoreLangConstants.EVENT_OUTPUT_START);
        handlerTypes.add(ScoreLangConstants.EVENT_OUTPUT_END);
        handlerTypes.add(ScoreLangConstants.EVENT_BRANCH_START);
        handlerTypes.add(ScoreLangConstants.EVENT_BRANCH_END);
        handlerTypes.add(ScoreLangConstants.EVENT_SPLIT_BRANCHES);
        handlerTypes.add(ScoreLangConstants.EVENT_JOIN_BRANCHES_START);
        handlerTypes.add(ScoreLangConstants.EVENT_JOIN_BRANCHES_END);
        handlerTypes.add(ScoreLangConstants.SLANG_EXECUTION_EXCEPTION);
        handlerTypes.add(ScoreLangConstants.EVENT_EXECUTION_FINISHED);
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

}
