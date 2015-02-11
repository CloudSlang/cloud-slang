/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/
package org.openscore.lang.runtime.steps;

import com.hp.oo.sdk.content.annotations.Param;
import org.openscore.lang.entities.ResultNavigation;
import org.openscore.lang.entities.bindings.Input;
import org.openscore.lang.entities.bindings.Output;
import org.openscore.lang.runtime.bindings.InputsBinding;
import org.openscore.lang.runtime.bindings.OutputsBinding;
import org.openscore.lang.runtime.env.ParentFlowData;
import org.openscore.lang.runtime.env.ParentFlowStack;
import org.openscore.lang.runtime.env.ReturnValues;
import org.openscore.lang.runtime.env.RunEnvironment;
import org.openscore.lang.runtime.events.LanguageEventData;
import org.apache.commons.lang3.tuple.Pair;
import org.openscore.api.execution.ExecutionParametersConsts;
import org.openscore.lang.ExecutionRuntimeServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openscore.lang.entities.ScoreLangConstants.EVENT_OUTPUT_END;
import static org.openscore.lang.entities.ScoreLangConstants.EVENT_OUTPUT_START;
import static org.openscore.lang.entities.ScoreLangConstants.NEXT_STEP_ID_KEY;
import static org.openscore.lang.entities.ScoreLangConstants.NODE_NAME_KEY;
import static org.openscore.lang.entities.ScoreLangConstants.REF_ID;
import static org.openscore.lang.entities.ScoreLangConstants.RUN_ENV;
import static org.openscore.lang.entities.ScoreLangConstants.TASK_INPUTS_KEY;
import static org.openscore.lang.entities.ScoreLangConstants.TASK_NAVIGATION_KEY;
import static org.openscore.lang.entities.ScoreLangConstants.TASK_PUBLISH_KEY;
import static org.openscore.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;

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

        Map<String, Serializable> operationArguments = inputsBinding.bindInputs(taskInputs, flowContext, runEnv.getSystemProperties());

        //todo: hook

        sendBindingInputsEvent(taskInputs, operationArguments, runEnv, executionRuntimeServices, "Task inputs resolved",
                nodeName, LanguageEventData.levelName.TASK_NAME);

        updateCallArgumentsAndPushContextToStack(runEnv, flowContext, operationArguments);

        // request the score engine to switch to the execution plan of the given ref
        requestSwitchToRefExecutableExecutionPlan(runEnv, executionRuntimeServices, RUNNING_EXECUTION_PLAN_ID, refId, nextStepId);

        // set the start step of the given ref as the next step to execute (in the new running execution plan that will be set)
        runEnv.putNextStepPosition(executionRuntimeServices.getSubFlowBeginStep(refId));

    }

    public void endTask(@Param(RUN_ENV) RunEnvironment runEnv,
                        @Param(TASK_PUBLISH_KEY) List<Output> taskPublishValues,
                        @Param(TASK_NAVIGATION_KEY) Map<String, ResultNavigation> taskNavigationValues,
                        @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                        @Param(NODE_NAME_KEY) String nodeName) {

        Map<String, Serializable> flowContext = runEnv.getStack().popContext();

        ReturnValues executableReturnValues = runEnv.removeReturnValues();
        fireEvent(executionRuntimeServices, runEnv, EVENT_OUTPUT_START, "Output binding started",
                Pair.of(TASK_PUBLISH_KEY, (Serializable) taskPublishValues),
                Pair.of(TASK_NAVIGATION_KEY, (Serializable) taskNavigationValues),
                Pair.of("operationReturnValues", executableReturnValues),Pair.of(LanguageEventData.levelName.TASK_NAME.name(),nodeName));

        Map<String, String> publishValues = outputsBinding.bindOutputs(null, executableReturnValues.getOutputs(), taskPublishValues);

        flowContext.putAll(publishValues);

        //todo: hook

		// set the position of the next step - for the use of the navigation
		// find in the navigation values the correct next step position, according to the operation result, and set it
		ResultNavigation navigation = taskNavigationValues.get(executableReturnValues.getResult());
		if(navigation == null) {
            // should always have the executable response mapped to a navigation by the task, if not, it is an error
            throw new RuntimeException("Task: " + nodeName + " has no matching navigation for the executable result: " + executableReturnValues.getResult() );
        }

        Long nextPosition = navigation.getNextStepId();
        String presetResult = navigation.getPresetResult();
		runEnv.putNextStepPosition(nextPosition);

		HashMap<String, String> outputs = new HashMap<>();// todo - is this the right solution?
		for(Map.Entry<String, Serializable> entry : flowContext.entrySet()) {
			outputs.put(entry.getKey(), String.valueOf(entry.getValue()));
		}

		ReturnValues returnValues = new ReturnValues(outputs, presetResult != null ? presetResult : executableReturnValues.getResult());
		runEnv.putReturnValues(returnValues);
        fireEvent(executionRuntimeServices, runEnv, EVENT_OUTPUT_END, "Output binding finished",
                Pair.of(LanguageEventData.OUTPUTS, (Serializable) publishValues),
                Pair.of(LanguageEventData.RESULT, returnValues.getResult()),
                Pair.of(LanguageEventData.NEXT_STEP_POSITION, nextPosition),
                Pair.of(LanguageEventData.levelName.TASK_NAME.name(),nodeName));

        runEnv.getStack().pushContext(flowContext);
    }

    private void requestSwitchToRefExecutableExecutionPlan(RunEnvironment runEnv,
                                                           ExecutionRuntimeServices executionRuntimeServices,
                                                           Long RUNNING_EXECUTION_PLAN_ID,
                                                           String refId,
                                                           Long nextStepId) {
        // create ParentFlowData object containing the current running execution plan id and
        // the next step id to navigate to in the current execution plan,
        // and push it to the ParentFlowStack for future use (once we finish running the ref operation/flow)
        ParentFlowStack stack = runEnv.getParentFlowStack();
        stack.pushParentFlowData(new ParentFlowData(RUNNING_EXECUTION_PLAN_ID, nextStepId));
        // request the score engine to switch the execution plan to the one with the given refId once it can
        Long subFlowRunningExecutionPlanId = executionRuntimeServices.getSubFlowRunningExecutionPlan(refId);
        executionRuntimeServices.requestToChangeExecutionPlan(subFlowRunningExecutionPlanId);
    }

}
