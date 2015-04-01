package io.cloudslang.lang.tools.build.tester;
/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/

import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.events.ScoreEventListener;
import io.cloudslang.lang.runtime.env.ReturnValues;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: stoneo
 * Date: 18/03/2015
 * Time: 14:13
 */
public class TriggerTestCaseEventListener implements ScoreEventListener {

    public static final String TEST_CASE_PASSED = "Passed test case: ";
    public static final String TEST_CASE_FAILED = "Failed running test case: ";
    public static final String EXEC_START_PATH = "0";

    private final static Logger log = Logger.getLogger(SlangTestRunner.class);

    private AtomicBoolean flowFinished = new AtomicBoolean(false);
    private AtomicReference<String> errorMessage = new AtomicReference<>("");
    private String testCaseName;
    private String expectedResult;
    private String result;
    private Map<String, Serializable> outputs;

    public TriggerTestCaseEventListener(String testCaseName, String expectedResult) {
        this.testCaseName = testCaseName;
        this.expectedResult = expectedResult;
    }

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
            case EventConstants.SCORE_FAILURE_EVENT :
                errorMessage.set(TEST_CASE_FAILED + testCaseName + ". " + data.get(EventConstants.SCORE_ERROR_LOG_MSG) + " , " +
                        data.get(EventConstants.SCORE_ERROR_MSG));
                flowFinished.set(true);
                result = ScoreLangConstants.FAILURE_RESULT;
                break;
            case ScoreLangConstants.SLANG_EXECUTION_EXCEPTION:
                errorMessage.set(TEST_CASE_FAILED + testCaseName + ". " + data.get(LanguageEventData.EXCEPTION));
                break;
            case ScoreLangConstants.EVENT_EXECUTION_FINISHED :
                result = (String)data.get(LanguageEventData.RESULT);
                printFinishEvent(data);
                break;
            case ScoreLangConstants.EVENT_OUTPUT_END:
                Map<String, Serializable> extractOutputs = extractOutputs(data);
                if(MapUtils.isNotEmpty(extractOutputs)) {
                    outputs = extractOutputs;
                }
                break;
        }
    }

    public ReturnValues getExecutionReturnValues(){
        if(StringUtils.isEmpty(result)){
            throw new RuntimeException("Result of executing the test " + testCaseName + " cannot be empty");
        }
        if (outputs == null){
            outputs = new HashMap<>();
        }
        return new ReturnValues(outputs, result);
    }

    private static Map<String, Serializable> extractOutputs(Map<String, Serializable> data) {

        Map<String, Serializable> outputsMap = new HashMap<>();

        boolean thereAreOutputsForRootPath =
                data.containsKey(LanguageEventData.OUTPUTS)
                && data.containsKey(LanguageEventData.PATH)
                && data.get(LanguageEventData.PATH).equals(EXEC_START_PATH);

        if (thereAreOutputsForRootPath) {
            @SuppressWarnings("unchecked") Map<String, Serializable> outputs =
                    (Map<String, Serializable>) data.get(LanguageEventData.OUTPUTS);
            if (MapUtils.isNotEmpty(outputs)) outputsMap.putAll(outputs);
        }

        return outputsMap;
    }

    private void printFinishEvent(Map<String, Serializable> data) {
        String flowResult = (String)data.get(LanguageEventData.RESULT);
        String flowName = (String)data.get(LanguageEventData.levelName.EXECUTABLE_NAME.toString());
        if(expectedResult == null || expectedResult.equals(flowResult)) {
            log.info(TEST_CASE_PASSED + testCaseName + ". Finished running: " + flowName + " with result: " + flowResult);
        } else {
            log.info(TEST_CASE_FAILED + testCaseName + ". Finished running: " + flowName + " with result: " + flowResult + " and expected result: " + expectedResult);
        }
    }
}
