/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package com.hp.score.lang.runtime.steps;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.score.lang.ExecutionRuntimeServices;
import com.hp.score.lang.runtime.env.ReturnValues;
import com.hp.score.lang.runtime.env.RunEnvironment;

import static com.hp.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_OUTPUT_END;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_OUTPUT_START;
import static com.hp.score.lang.entities.ScoreLangConstants.RUN_ENV;
import static com.hp.score.lang.entities.ScoreLangConstants.TASK_INPUTS_KEY;
import static com.hp.score.lang.entities.ScoreLangConstants.TASK_NAVIGATION_KEY;
import static com.hp.score.lang.entities.ScoreLangConstants.TASK_PUBLISH_KEY;
import static com.hp.score.lang.runtime.events.LanguageEventData.RETURN_VALUES;

/**
 * User: stoneo
 * Date: 02/11/2014
 * Time: 10:23
 */
@Component
public class TaskSteps extends AbstractSteps {

    public void beginTask(@Param(TASK_INPUTS_KEY) LinkedHashMap<String, Serializable> taskInputs,
                          @Param(RUN_ENV) RunEnvironment runEnv,
                          @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices) {

        System.out.println("===========");
        System.out.println(" beginTask ");
        System.out.println("===========");
        runEnv.getExecutionPath().forward();
        runEnv.removeCallArguments();
        runEnv.removeReturnValues();

        Map<String, Serializable> flowContext = runEnv.getStack().popContext();

        Map<String, Serializable> operationArguments = createBindInputsMap(flowContext, taskInputs, executionRuntimeServices, runEnv);

        //todo: hook

        updateCallArgumentsAndPushContextToStack(runEnv, flowContext, operationArguments);
    }

    public void finishTask(@Param(RUN_ENV) RunEnvironment runEnv,
                           @Param(TASK_PUBLISH_KEY) LinkedHashMap<String, Serializable> taskPublishValues,
                           @Param(TASK_NAVIGATION_KEY) LinkedHashMap<String, Long> taskNavigationValues,
                           @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices) {

        System.out.println("============");
        System.out.println(" finishTask ");
        System.out.println("============");

        Map<String, Serializable> flowContext = runEnv.getStack().popContext();

        ReturnValues operationReturnValues = runEnv.removeReturnValues();
        fireEvent(executionRuntimeServices, runEnv, EVENT_OUTPUT_START, "Output binding started", Pair.of(TASK_PUBLISH_KEY, taskPublishValues), Pair.of(TASK_NAVIGATION_KEY, taskNavigationValues), Pair.of("operationReturnValues", operationReturnValues));
        Map<String, Serializable> publishValues = createBindOutputsContext(operationReturnValues.getOutputs(), taskPublishValues);
        flowContext.putAll(publishValues);
        printMap(flowContext, "flowContext");

        //todo: hook

        Long nextPosition = calculateNextPosition(operationReturnValues.getResult(), taskNavigationValues);
        runEnv.putNextStepPosition(nextPosition);
        ReturnValues returnValues = new ReturnValues(new HashMap<String, String>(), operationReturnValues.getResult());
        runEnv.putReturnValues(returnValues);
        fireEvent(executionRuntimeServices, runEnv, EVENT_OUTPUT_END, "Output binding finished", Pair.of(RETURN_VALUES, returnValues), Pair.of("nextPosition", nextPosition));
        printReturnValues(returnValues);
        System.out.println("next position: " + nextPosition);

        runEnv.getStack().pushContext(flowContext);
    }

    private Map<String, Serializable> createBindOutputsContext(Map<String, String> operationResultContext, LinkedHashMap<String, Serializable> taskOutputs) {
        Map<String, Serializable> tempContext = new LinkedHashMap<>();
        if (taskOutputs != null) {
            for (Map.Entry<String, Serializable> output : taskOutputs.entrySet()) {
                String outputKey = output.getKey();
                Serializable outputValue = output.getValue();
                String outputRetValue = null;
                if (outputValue != null) {
                    if (outputValue instanceof String) {
                        // assigning from another param
                        String paramName = (String) outputValue;
                        // TODO: missing - evaluate script
                        outputRetValue = operationResultContext.get(paramName);
                        if (outputRetValue == null)
                            outputRetValue = paramName;
                    } else {
                        tempContext.put(outputKey, outputValue);
                    }
                } else {
                    outputRetValue = operationResultContext.get(outputKey);
                }
                tempContext.put(outputKey, outputRetValue);
            }
        }
        return tempContext;
    }

    private Long calculateNextPosition(String result, LinkedHashMap<String, Long> taskNavigationValues) {
        //todo: implement
        return taskNavigationValues.get(result);
    }

}
