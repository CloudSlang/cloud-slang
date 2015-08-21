/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package io.cloudslang.lang.cli.services;

import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.runtime.env.ExecutionPath;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.events.ScoreEventListener;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Date: 2/26/2015
 *
 * @author lesant
 */
public class SyncTriggerEventListener implements ScoreEventListener{
    public static final String SLANG_STEP_ERROR_MSG = "Slang Error : ";
    public static final String SCORE_ERROR_EVENT_MSG = "Score Error Event :";
    public static final String FLOW_FINISHED_WITH_FAILURE_MSG = "Flow finished with failure";
    public static final String EXEC_START_PATH = "0";
    public static final int OUTPUT_VALUE_LIMIT = 40;
    private final static String TASK_PATH_PREFIX = "- ";

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
                errorMessage.set(SLANG_STEP_ERROR_MSG + data.get(LanguageEventData.EXCEPTION));
                break;
            case ScoreLangConstants.EVENT_TASK_START:
                LanguageEventData eventData = (LanguageEventData) data;
                if(eventData.getStepType() == LanguageEventData.StepType.TASK){
                    String taskName = eventData.getStepName();
                    String path = eventData.getPath();
                    int matches = StringUtils.countMatches(path, ExecutionPath.PATH_SEPARATOR);
                    String prefix = StringUtils.repeat(TASK_PATH_PREFIX, matches);
                    printWithColor(Ansi.Color.YELLOW, prefix + taskName);
                }
                break;
            case ScoreLangConstants.EVENT_OUTPUT_END:
                Map<String, Serializable> outputs = extractOutputs(data);

                for (String key : outputs.keySet()) {
                    printWithColor(Ansi.Color.WHITE, "- " + key + " = " + outputs.get(key));
                }
                break;

            case ScoreLangConstants.EVENT_EXECUTION_FINISHED :
                flowFinished.set(true);
                printFinishEvent(data);
                break;
        }
    }

    public static Map<String, Serializable> extractOutputs(Map<String, Serializable> data) {
        if(data.containsKey(LanguageEventData.OUTPUTS)
                && data.containsKey(LanguageEventData.PATH)
                && data.get(LanguageEventData.PATH).equals(EXEC_START_PATH)) {

            @SuppressWarnings("unchecked") Map<String, Serializable> outputs = (Map<String, Serializable>) data.get(LanguageEventData.OUTPUTS);
            if (MapUtils.isNotEmpty(outputs)) {
                Iterator<Map.Entry<String,Serializable>> iterator = outputs.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String,Serializable> output = iterator.next();
                    if(output.getValue() == null || StringUtils.isEmpty(output.getValue().toString())){
                        iterator.remove();
                    } else {
                        outputs.put(output.getKey(), StringUtils.abbreviate(output.getValue().toString(), 0, OUTPUT_VALUE_LIMIT));
                    }

                }
            }
            return outputs;
        }
        return new HashMap<>();
    }

    private void printFinishEvent(Map<String, Serializable> data) {
        String flowResult = (String)data.get(LanguageEventData.RESULT);
        String flowName = (String)data.get(LanguageEventData.STEP_NAME);
        printWithColor(Ansi.Color.CYAN,"Flow : " + flowName + " finished with result : " + flowResult);
    }

    private void printWithColor(Ansi.Color color, String msg){
        AnsiConsole.out().print(ansi().fg(color).a(msg).newline());
        AnsiConsole.out().print(ansi().fg(Ansi.Color.WHITE));

    }
}

