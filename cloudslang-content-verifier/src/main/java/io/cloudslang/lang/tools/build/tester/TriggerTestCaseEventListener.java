/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.tester;


import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.events.ScoreEventListener;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.collections4.MapUtils;

/**
 * User: stoneo
 * Date: 18/03/2015
 * Time: 14:13
 */
public class TriggerTestCaseEventListener implements ScoreEventListener {

    public static final String EXEC_START_PATH = "0";


    private AtomicBoolean flowFinished = new AtomicBoolean(false);
    private AtomicReference<String> errorMessage = new AtomicReference<>("");
    private String result;
    private Map<String, Serializable> outputs = new HashMap<>();

    public String getResult() {
        return result;
    }

    public Map<String, Serializable> getOutputs() {
        return outputs;
    }

    public boolean isFlowFinished() {
        return flowFinished.get();
    }

    public String getErrorMessage() {
        return errorMessage.get();
    }

    @Override
    public synchronized void onEvent(ScoreEvent scoreEvent) throws InterruptedException {
        @SuppressWarnings("unchecked")
        Map<String, Serializable> data = (Map<String, Serializable>) scoreEvent.getData();
        LanguageEventData eventData;
        switch (scoreEvent.getEventType()) {
            case EventConstants.SCORE_FINISHED_EVENT:
                break;
            case EventConstants.SCORE_ERROR_EVENT:
            case EventConstants.SCORE_FAILURE_EVENT:
                errorMessage.set(data.get(EventConstants.SCORE_ERROR_LOG_MSG) + " , " +
                        data.get(EventConstants.SCORE_ERROR_MSG));
                flowFinished.set(true);
                break;
            case ScoreLangConstants.EVENT_EXECUTION_FINISHED:
                eventData = (LanguageEventData) data;
                result = eventData.getResult();
                flowFinished.set(true);
                break;
            case ScoreLangConstants.EVENT_OUTPUT_END:
                eventData = (LanguageEventData) data;
                Map<String, Serializable> extractOutputs = extractOutputs(eventData);
                if (MapUtils.isNotEmpty(extractOutputs)) {
                    outputs = extractOutputs;
                }
                break;
            default:
                break;
        }
    }

    public static Map<String, Serializable> extractOutputs(LanguageEventData data) {

        Map<String, Serializable> outputsMap = new HashMap<>();

        boolean thereAreOutputsForRootPath = data.containsKey(LanguageEventData.OUTPUTS) &&
                data.containsKey(LanguageEventData.PATH) &&
                data.getPath().equals(EXEC_START_PATH);

        if (thereAreOutputsForRootPath) {
            Map<String, Serializable> outputs = data.getOutputs();
            if (MapUtils.isNotEmpty(outputs)) {
                outputsMap.putAll(outputs);
            }
        }

        return outputsMap;
    }
}
