/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.tester.parallel;

import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.lang.tools.build.tester.TriggerTestCaseEventListener;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.events.ScoreEventListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;

import static io.cloudslang.lang.entities.ScoreLangConstants.EVENT_EXECUTION_FINISHED;
import static io.cloudslang.lang.entities.ScoreLangConstants.EVENT_OUTPUT_END;
import static io.cloudslang.score.events.EventConstants.SCORE_ERROR_EVENT;
import static io.cloudslang.score.events.EventConstants.SCORE_ERROR_LOG_MSG;
import static io.cloudslang.score.events.EventConstants.SCORE_ERROR_MSG;
import static io.cloudslang.score.events.EventConstants.SCORE_FAILURE_EVENT;
import static io.cloudslang.score.events.EventConstants.SCORE_FINISHED_EVENT;


public class MultiTriggerTestCaseEventListener implements ScoreEventListener {

    public static final String SYSTEM_CONTEXT = "systemContext";
    public static final String EXECUTION_ID_CONTEXT = "executionIdContext";
    private Map<Long, Boolean> flowFinishedMap;
    private Map<Long, String> errorMessageMap;
    private Map<Long, String> resultMap;
    private Map<Long, Map<String, Serializable>> outputsMap;

    public MultiTriggerTestCaseEventListener() {
        flowFinishedMap = new HashMap<>();
        errorMessageMap = new HashMap<>();
        resultMap = new HashMap<>();
        outputsMap = new HashMap<>();
    }

    public String getResultByExecutionId(Long executionId) {
        return resultMap.get(executionId);
    }

    public boolean isFlowFinishedByExecutionId(Long executionId) {
        Boolean flowFinished = flowFinishedMap.get(executionId);
        return (flowFinished != null) && flowFinished;
    }

    public String getErrorMessageByExecutionId(Long executionId) {
        return errorMessageMap.get(executionId);
    }

    @Override
    public synchronized void onEvent(ScoreEvent scoreEvent) throws InterruptedException {
        @SuppressWarnings("unchecked")
        Map<String, Serializable> data = (Map<String, Serializable>) scoreEvent.getData();
        LanguageEventData eventData;
        Long executionId = (data instanceof LanguageEventData) ? (((LanguageEventData) data).getExecutionId()) :
                (Long) ((Map) data.get(SYSTEM_CONTEXT)).get(EXECUTION_ID_CONTEXT);


        switch (scoreEvent.getEventType()) {
            case SCORE_FINISHED_EVENT:
                break;
            case SCORE_ERROR_EVENT:
            case SCORE_FAILURE_EVENT:
                String errorMessage = data.get(SCORE_ERROR_LOG_MSG) + " , " + data.get(SCORE_ERROR_MSG);
                errorMessageMap.put(executionId, errorMessage);
                flowFinishedMap.put(executionId, true);
                break;

            case EVENT_EXECUTION_FINISHED:
                eventData = (LanguageEventData) data;
                resultMap.put(executionId, eventData.getResult());
                flowFinishedMap.put(executionId, true);
                break;

            case EVENT_OUTPUT_END:
                eventData = (LanguageEventData) data;
                Map<String, Serializable> extractOutputs = TriggerTestCaseEventListener.extractOutputs(eventData);
                if (MapUtils.isNotEmpty(extractOutputs)) {
                    outputsMap.put(executionId, extractOutputs);
                }
                break;
            default:
                break;
        }
    }

    public Map<String, Serializable> getOutputsByExecutionId(Long executionId) {
        return outputsMap.get(executionId);
    }

}
