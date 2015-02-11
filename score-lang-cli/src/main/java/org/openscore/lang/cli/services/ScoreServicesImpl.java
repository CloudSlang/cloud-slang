/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openscore.lang.cli.services;

import org.apache.commons.lang.StringUtils;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.openscore.events.EventConstants;
import org.openscore.events.ScoreEvent;
import org.openscore.events.ScoreEventListener;
import org.openscore.lang.api.Slang;
import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.lang.entities.ScoreLangConstants;
import org.openscore.lang.runtime.env.ExecutionPath;
import org.openscore.lang.runtime.events.LanguageEventData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.fusesource.jansi.Ansi.ansi;
import static org.openscore.lang.entities.ScoreLangConstants.*;
import static org.openscore.lang.runtime.events.LanguageEventData.EXCEPTION;
import static org.openscore.lang.runtime.events.LanguageEventData.RESULT;

/**
 * @author Bonczidai Levente
 * @since 11/13/2014
 * @version $Id$
 */
@Service
public class ScoreServicesImpl implements ScoreServices{
    public static final String SLANG_STEP_ERROR_MSG = "Slang Error : ";
    public static final String SCORE_ERROR_EVENT_MSG = "Score Error Event :";
    public static final String FLOW_FINISHED_WITH_FAILURE_MSG = "Flow finished with failure";

    @Autowired
    private Slang slang;
    private final static String TASK_PATH_PREFIX = "- ";

    public void subscribe(ScoreEventListener eventHandler, Set<String> eventTypes) {
        slang.subscribeOnEvents(eventHandler, eventTypes);
    }

    /**
     * This method will trigger the flow in an Async matter.
     * @param compilationArtifact the artifact to trigger
     * @param inputs : flow inputs
     * @return executionId
     */
    @Override
	public Long trigger(CompilationArtifact compilationArtifact, Map<String, ? extends Serializable> inputs, Map<String, ? extends Serializable> systemProperties) {
        return slang.run(compilationArtifact, inputs, systemProperties);
    }

    /**
     * This method will trigger the flow in a synchronize matter, meaning only one flow can run at a time.
     * @param compilationArtifact the artifact to trigger
     * @param inputs : flow inputs
     * @return executionId
     */
    @Override
	public Long triggerSync(CompilationArtifact compilationArtifact, Map<String, ? extends Serializable> inputs, Map<String, ? extends Serializable> systemProperties){
        //add start event
        Set<String> handlerTypes = new HashSet<>();
        handlerTypes.add(EventConstants.SCORE_FINISHED_EVENT);
        handlerTypes.add(EventConstants.SCORE_ERROR_EVENT);
        handlerTypes.add(EventConstants.SCORE_FAILURE_EVENT);
        handlerTypes.add(SLANG_EXECUTION_EXCEPTION);
        handlerTypes.add(EVENT_EXECUTION_FINISHED);
        handlerTypes.add(EVENT_INPUT_END);

        SyncTriggerEventListener scoreEventListener = new SyncTriggerEventListener();
        slang.subscribeOnEvents(scoreEventListener, handlerTypes);

        Long executionId = trigger(compilationArtifact, inputs, systemProperties);

        while(!scoreEventListener.isFlowFinished()){
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignore) {}
        }
        slang.unSubscribeOnEvents(scoreEventListener);

        String errorMessageFlowExecution = scoreEventListener.getErrorMessage();
        if (!errorMessageFlowExecution.isEmpty()) {
            // exception occurred during flow execution
            throw new RuntimeException(errorMessageFlowExecution);
        }

        return executionId;
    }

    private class SyncTriggerEventListener implements ScoreEventListener{

        private AtomicBoolean flowFinished = new AtomicBoolean(false);
        private AtomicReference<String> errorMessage = new AtomicReference<>("");

        public boolean isFlowFinished() {
            return flowFinished.get();
        }

        public String getErrorMessage() {
            return errorMessage.get();
        }

        @Override
        public synchronized void onEvent(ScoreEvent scoreEvent) throws InterruptedException {
            @SuppressWarnings("unchecked") Map<String,Serializable> data = (Map<String,Serializable>)scoreEvent.getData();
            switch (scoreEvent.getEventType()){
                case EventConstants.SCORE_FINISHED_EVENT :
                    flowFinished.set(true);
                    break;
                case EventConstants.SCORE_ERROR_EVENT :
                    errorMessage.set(SCORE_ERROR_EVENT_MSG + data.get(EventConstants.SCORE_ERROR_LOG_MSG) + " , " +
                            data.get(EventConstants.SCORE_ERROR_MSG));
                    break;
                case EventConstants.SCORE_FAILURE_EVENT :
                    printWithColor(Ansi.Color.RED,FLOW_FINISHED_WITH_FAILURE_MSG);
                    flowFinished.set(true);
                    break;
                case ScoreLangConstants.SLANG_EXECUTION_EXCEPTION:
                    errorMessage.set(SLANG_STEP_ERROR_MSG + data.get(EXCEPTION));
                    break;
                case ScoreLangConstants.EVENT_INPUT_END:
                    String taskName = (String)data.get(LanguageEventData.levelName.TASK_NAME.name());
                    if(StringUtils.isNotEmpty(taskName)){
                        String path = (String) data.get(LanguageEventData.PATH);
                        int matches = StringUtils.countMatches(path, ExecutionPath.PATH_SEPARATOR);
                        String prefix = StringUtils.repeat(TASK_PATH_PREFIX, matches);
                        printWithColor(Ansi.Color.YELLOW, prefix + taskName);
                    }
                    break;
                case EVENT_EXECUTION_FINISHED :
                    printFinishEvent(data);
                    break;
            }
        }

        private void printFinishEvent(Map<String, Serializable> data) {
            String flowResult = (String)data.get(RESULT);
            String flowName = (String)data.get(LanguageEventData.levelName.EXECUTABLE_NAME.toString());
            printWithColor(Ansi.Color.CYAN,"Flow : " + flowName + " finished with result : " + flowResult);
        }

        private void printWithColor(Ansi.Color color, String msg){
            AnsiConsole.out().print(ansi().fg(color).a(msg).newline());
            AnsiConsole.out().print(ansi().fg(Ansi.Color.WHITE));

        }
    }

}
