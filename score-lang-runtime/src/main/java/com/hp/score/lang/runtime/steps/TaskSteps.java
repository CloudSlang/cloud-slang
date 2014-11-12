/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package com.hp.score.lang.runtime.steps;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.score.lang.ExecutionRuntimeServices;
import com.hp.score.lang.entities.bindings.Input;
import com.hp.score.lang.entities.bindings.Output;
import com.hp.score.lang.runtime.bindings.InputsBinding;
import com.hp.score.lang.runtime.bindings.OutputsBinding;
import com.hp.score.lang.runtime.env.ReturnValues;
import com.hp.score.lang.runtime.env.RunEnvironment;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private InputsBinding inputsBinding;

    @Autowired
    private OutputsBinding outputsBinding;


    public void beginTask(@Param(TASK_INPUTS_KEY) List<Input> taskInputs,
                          @Param(RUN_ENV) RunEnvironment runEnv,
                          @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices) {

        System.out.println("===========");
        System.out.println(" beginTask ");
        System.out.println("===========");
        runEnv.getExecutionPath().forward();
        runEnv.removeCallArguments();
        runEnv.removeReturnValues();

        Map<String, Serializable> flowContext = runEnv.getStack().popContext();

        Map<String, Serializable> operationArguments = inputsBinding.bindInputs(flowContext,taskInputs);

        //todo: hook

        sendBindingInputsEvent(taskInputs, operationArguments,runEnv, executionRuntimeServices,"Task inputs resolved");

        updateCallArgumentsAndPushContextToStack(runEnv, flowContext, operationArguments);
    }

    public void endTask(@Param(RUN_ENV) RunEnvironment runEnv,
                        @Param(TASK_PUBLISH_KEY) List<Output> taskPublishValues,
                        @Param(TASK_NAVIGATION_KEY) LinkedHashMap<String, Long> taskNavigationValues,
                        @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices) {

        System.out.println("=========");
        System.out.println(" endTask ");
        System.out.println("=========");

        Map<String, Serializable> flowContext = runEnv.getStack().popContext();

        ReturnValues operationReturnValues = runEnv.removeReturnValues();
        fireEvent(executionRuntimeServices, runEnv, EVENT_OUTPUT_START, "Output binding started",
                    Pair.of(TASK_PUBLISH_KEY, (Serializable)taskPublishValues),
                    Pair.of(TASK_NAVIGATION_KEY, taskNavigationValues),
                    Pair.of("operationReturnValues", operationReturnValues));

        Map<String, String> publishValues = outputsBinding.bindOutputs(null, operationReturnValues.getOutputs(), taskPublishValues);

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

    private Long calculateNextPosition(String result, LinkedHashMap<String, Long> taskNavigationValues) {
        //todo: implement
        return taskNavigationValues.get(result);
    }

}
