package io.cloudslang.lang.tools.build.tester.parallel;

import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.lang.tools.build.tester.TriggerTestCaseEventListener;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.events.ScoreEventListener;
import org.apache.commons.collections4.MapUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


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
        return errorMessageMap.get(executionId); // TODO see why special treatment of empty is required
    }

    public Map<String, Serializable> getOutputsByExecutionId(Long executionId) {
        return outputsMap.get(executionId);
    }

    @Override
    public synchronized void onEvent(ScoreEvent scoreEvent) throws InterruptedException {
        @SuppressWarnings("unchecked") Map<String, Serializable> data = (Map<String, Serializable>) scoreEvent.getData();
        LanguageEventData eventData;
        Long executionId =  (data instanceof LanguageEventData) ? (((LanguageEventData) data).getExecutionId()) :
                (Long) ((Map)data.get(SYSTEM_CONTEXT)).get(EXECUTION_ID_CONTEXT);

        switch (scoreEvent.getEventType()) {
            case EventConstants.SCORE_FINISHED_EVENT:
                break;
            case EventConstants.SCORE_ERROR_EVENT:
            case EventConstants.SCORE_FAILURE_EVENT:
                String errorMessage = data.get(EventConstants.SCORE_ERROR_LOG_MSG) + " , " + data.get(EventConstants.SCORE_ERROR_MSG);
                errorMessageMap.put(executionId, errorMessage);
                flowFinishedMap.put(executionId, true);
                break;

            case ScoreLangConstants.EVENT_EXECUTION_FINISHED:
                eventData = (LanguageEventData) data;
                resultMap.put(executionId, eventData.getResult());
                flowFinishedMap.put(executionId, true);
                break;

            case ScoreLangConstants.EVENT_OUTPUT_END:
                eventData = (LanguageEventData) data;
                Map<String, Serializable> extractOutputs = TriggerTestCaseEventListener.extractOutputs(eventData);
                if (MapUtils.isNotEmpty(extractOutputs)) {
                    outputsMap.put(executionId, extractOutputs);
                }
                break;
        }
    }

}
