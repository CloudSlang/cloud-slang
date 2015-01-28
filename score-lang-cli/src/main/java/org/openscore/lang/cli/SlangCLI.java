/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openscore.lang.cli;

import com.google.common.collect.Lists;

import org.openscore.lang.cli.services.ScoreServices;
import org.openscore.lang.cli.utils.CompilerHelper;
import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.lang.entities.ScoreLangConstants;
import org.openscore.lang.entities.bindings.Input;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.openscore.events.EventConstants;
import org.openscore.events.ScoreEvent;
import org.openscore.events.ScoreEventListener;
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

/**
 * @author lesant
 * @since 11/07/2014
 * @version $Id$
 */
@Component
public class SlangCLI implements CommandMarker {

    public static final String TRIGGERED_FLOW_MSG = "Triggered flow : ";
    public static final String WITH_EXECUTION_ID_MSG = " , with execution id : ";
    public static final String FLOW_EXECUTION_TIME_TOOK = "Flow execution time took  ";

    @Autowired
    private ScoreServices scoreServices;

    @Autowired
    private CompilerHelper compilerHelper;

    /**
     * This global param holds the state of the CLI, if flows need to run in ASYNC or in SYNC manner.
     */
    private Boolean triggerAsync = false;

    private final static Logger logger = Logger.getLogger(SlangCLI.class);

    private static final String currently = "You are currently running Score version: ";
    private static final String SCORE_VERSION = "0.1.229"; //todo get version

    @CliCommand(value = "run", help = "triggers a slang flow")
    public String run(
            @CliOption(key = {"", "f", "file"}, mandatory = true, help = "Path to filename. e.g. slang run --f C:\\Slang\\flow.yaml") final File file,
            @CliOption(key = {"cp", "classpath"}, mandatory = false, help = "Classpath , a directory comma separated list to flow dependencies, by default it will take flow file dir") final List<String> classPath,
            @CliOption(key = {"i", "inputs"}, mandatory = false, help = "inputs in a key=value comma separated list") final Map<String, Serializable> inputs,
            @CliOption(key = {"spf", "system-property-file"}, mandatory = false, help = "comma separated list of system property file locations") final List<String> systemPropertyFiles) throws IOException {

        CompilationArtifact compilationArtifact = compilerHelper.compile(file.getAbsolutePath(), null, classPath);
        Map<String, ? extends Serializable> systemProperties = compilerHelper.loadSystemProperties(systemPropertyFiles);
        Long id;
        if (!triggerAsync) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            id = scoreServices.triggerSync(compilationArtifact, inputs, systemProperties);
            stopWatch.stop();
            return triggerSyncMsg(id, stopWatch.toString());
        }
        id = scoreServices.trigger(compilationArtifact, inputs, systemProperties);
        return triggerAsyncMsg(id, compilationArtifact.getExecutionPlan().getName());
    }

    @CliCommand(value = "env", help = "Set environment var relevant to the CLI")
    public String setEnvVar(
            @CliOption(key = "setAsync", mandatory = true, help = "set the async") final boolean switchAsync) throws IOException {
        triggerAsync = switchAsync;
        return setEnvMessage(triggerAsync);
    }

    @CliCommand(value = "inputs", help = "Get flow inputs")
    public List<String> getFlowInputs(
            @CliOption(key = {"", "f", "file"}, mandatory = true, help = "Path to filename. e.g. slang inputs --f C:\\Slang\\flow.yaml") final File file,
            @CliOption(key = {"cp", "classpath"}, mandatory = false,
                    help = "Classpath , a directory comma separated list to flow dependencies, by default it will take flow file dir") final List<String> classPath)
            throws IOException {
        CompilationArtifact compilationArtifact = compilerHelper.compile(file.getAbsolutePath(), null, classPath);
        List<Input> inputs = compilationArtifact.getInputs();
        List<String> inputsResult = new ArrayList<>();
        for (Input input : inputs) {
            if (!input.isOverride()) {
                inputsResult.add(input.getName());
            }
        }
        return inputsResult;
    }

    @CliCommand(value = "slang --version", help = "Prints the score version used")
    public String version() {
        return currently + SCORE_VERSION;
    }

    public static String triggerSyncMsg(Long id, String duration) {
        return FLOW_EXECUTION_TIME_TOOK + duration + WITH_EXECUTION_ID_MSG + id;
    }

    public static String triggerAsyncMsg(Long id, String flowName) {
        return TRIGGERED_FLOW_MSG + flowName + WITH_EXECUTION_ID_MSG + id;
    }

    public static String setEnvMessage(boolean triggerAsync) {
        return "flow execution ASYNC execution was changed to : " + triggerAsync;
    }

    public static String getVersion() {
        return SCORE_VERSION;
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
        handlerTypes.add(ScoreLangConstants.EVENT_INPUT_START);
        handlerTypes.add(ScoreLangConstants.EVENT_INPUT_END);
        handlerTypes.add(ScoreLangConstants.EVENT_OUTPUT_START);
        handlerTypes.add(ScoreLangConstants.EVENT_OUTPUT_END);
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
