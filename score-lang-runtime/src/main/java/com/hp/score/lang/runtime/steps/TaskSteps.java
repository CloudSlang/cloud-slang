/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package com.hp.score.lang.runtime.steps;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.score.api.execution.ExecutionParametersConsts;
import com.hp.score.lang.ExecutionRuntimeServices;
import com.hp.score.lang.entities.bindings.Input;
import com.hp.score.lang.entities.bindings.Output;
import com.hp.score.lang.runtime.bindings.InputsBinding;
import com.hp.score.lang.runtime.bindings.OutputsBinding;
import com.hp.score.lang.runtime.env.ParentFlowData;
import com.hp.score.lang.runtime.env.ParentFlowStack;
import com.hp.score.lang.runtime.env.ReturnValues;
import com.hp.score.lang.runtime.env.RunEnvironment;
import com.hp.score.lang.runtime.events.LanguageEventData;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hp.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;
import static com.hp.score.lang.entities.ScoreLangConstants.*;
import static com.hp.score.lang.runtime.events.LanguageEventData.RETURN_VALUES;
import static com.hp.score.lang.runtime.events.LanguageEventData.NEXT_STEP_POSITION;

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
                          @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                          @Param(NODE_NAME_KEY) String nodeName,
                          @Param(ExecutionParametersConsts.RUNNING_EXECUTION_PLAN_ID) Long RUNNING_EXECUTION_PLAN_ID,
                          @Param(NEXT_STEP_ID_KEY) Long nextStepId,
                          @Param(REF_ID) String refId) {

        runEnv.getExecutionPath().forward();
        runEnv.removeCallArguments();
        runEnv.removeReturnValues();

        Map<String, Serializable> flowContext = runEnv.getStack().popContext();

        Map<String, Serializable> operationArguments = inputsBinding.bindInputs(flowContext, taskInputs);

        //todo: hook

        sendBindingInputsEvent(taskInputs, operationArguments, runEnv, executionRuntimeServices, "Task inputs resolved",
                nodeName, LanguageEventData.levelName.TASK_NAME);

        updateCallArgumentsAndPushContextToStack(runEnv, flowContext, operationArguments);

        // request the score engine to switch to the execution plan of the given ref
        requestSwitchToRefExecutableExecutionPlan(runEnv, executionRuntimeServices, RUNNING_EXECUTION_PLAN_ID, refId, nextStepId);

        // We set the start step of the given ref as the next step to execute (in the new running execution plan that will be set)
        runEnv.putNextStepPosition(executionRuntimeServices.getSubFlowBeginStep(refId));

    }

    public void endTask(@Param(RUN_ENV) RunEnvironment runEnv,
                        @Param(TASK_PUBLISH_KEY) List<Output> taskPublishValues,
                        @Param(TASK_NAVIGATION_KEY) Map<String, Long> taskNavigationValues,
                        @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                        @Param(NODE_NAME_KEY) String nodeName) {

        Map<String, Serializable> flowContext = runEnv.getStack().popContext();

        ReturnValues operationReturnValues = runEnv.removeReturnValues();
        fireEvent(executionRuntimeServices, runEnv, EVENT_OUTPUT_START, "Output binding started",
                Pair.of(TASK_PUBLISH_KEY, (Serializable) taskPublishValues),
                Pair.of(TASK_NAVIGATION_KEY, (Serializable) taskNavigationValues),
                Pair.of("operationReturnValues", operationReturnValues),Pair.of(LanguageEventData.levelName.TASK_NAME.name(),nodeName));

        Map<String, String> publishValues = outputsBinding.bindOutputs(null, operationReturnValues.getOutputs(), taskPublishValues);

        flowContext.putAll(publishValues);

        //todo: hook

        // set the position of the next step - for the use of the navigation
        Long nextPosition = setNextTaskPosition(runEnv, taskNavigationValues, operationReturnValues);

        HashMap<String, String> outputs = new HashMap<>();//todo - is this the right solution?
        for (Map.Entry<String, Serializable> entry : flowContext.entrySet()) {
            outputs.put(entry.getKey(), String.valueOf(entry.getValue()));
        }

        ReturnValues returnValues = new ReturnValues(outputs, operationReturnValues.getResult());
        runEnv.putReturnValues(returnValues);
        fireEvent(executionRuntimeServices, runEnv, EVENT_OUTPUT_END, "Output binding finished",
                Pair.of(RETURN_VALUES, returnValues),
                Pair.of(NEXT_STEP_POSITION, nextPosition),
                Pair.of(LanguageEventData.levelName.TASK_NAME.name(),nodeName));

        runEnv.getStack().pushContext(flowContext);
    }

    private void requestSwitchToRefExecutableExecutionPlan(RunEnvironment runEnv,
                                                           ExecutionRuntimeServices executionRuntimeServices,
                                                           Long RUNNING_EXECUTION_PLAN_ID,
                                                           String refId,
                                                           Long nextStepId) {
        // We create ParentFlowData object containing the current running execution plan id and
        // the next step id to navigate to in the current execution plan,
        // and we push it to the ParentFlowStack for future use (once we finish running the ref operation/flow)
        ParentFlowStack stack = runEnv.getParentFlowStack();
        stack.pushParentFlowData(new ParentFlowData(RUNNING_EXECUTION_PLAN_ID, nextStepId));
        // We request the score engine to switch the execution plan to the one of the given refId once it can
        Long subFlowRunningExecutionPlanId = executionRuntimeServices.getSubFlowRunningExecutionPlan(refId);
        executionRuntimeServices.requestToChangeExecutionPlan(subFlowRunningExecutionPlanId);
    }

    /**
     * Find in the navigation values the correct next step position, according to the operation result, and set it
     * @param runEnv
     * @param taskNavigationValues
     * @param operationReturnValues
     * @return the next step position
     */
    private Long setNextTaskPosition(RunEnvironment runEnv, Map<String, Long> taskNavigationValues, ReturnValues operationReturnValues) {
        Long nextPosition = taskNavigationValues.get(operationReturnValues.getResult());
        runEnv.putNextStepPosition(nextPosition);
        return nextPosition;
    }

}
