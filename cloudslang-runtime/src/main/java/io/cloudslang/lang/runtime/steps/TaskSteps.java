/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.steps;

import com.hp.oo.sdk.content.annotations.Param;
import io.cloudslang.lang.entities.*;
import io.cloudslang.lang.runtime.bindings.LoopsBinding;
import io.cloudslang.lang.runtime.bindings.OutputsBinding;
import io.cloudslang.lang.runtime.env.*;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import org.apache.log4j.Logger;
import org.apache.commons.lang3.tuple.Pair;
import io.cloudslang.lang.entities.MapForLoopStatement;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.runtime.env.LoopCondition;
import io.cloudslang.score.api.execution.ExecutionParametersConsts;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.runtime.bindings.InputsBinding;
import io.cloudslang.lang.runtime.env.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.cloudslang.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;

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

    @Autowired
    private LoopsBinding loopsBinding;
    
    private static final Logger logger = Logger.getLogger(TaskSteps.class);

    public void beginTask(@Param(ScoreLangConstants.TASK_INPUTS_KEY) List<Input> taskInputs,
                          @Param(ScoreLangConstants.LOOP_KEY) LoopStatement loop,
                          @Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                          @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                          @Param(ScoreLangConstants.NODE_NAME_KEY) String nodeName,
                          @Param(ExecutionParametersConsts.RUNNING_EXECUTION_PLAN_ID) Long RUNNING_EXECUTION_PLAN_ID,
                          @Param(ScoreLangConstants.NEXT_STEP_ID_KEY) Long nextStepId,
                          @Param(ScoreLangConstants.REF_ID) String refId) {
        try {

            fireEvent(executionRuntimeServices, runEnv, ScoreLangConstants.EVENT_TASK_START,
                    "Task step started", LanguageEventData.StepType.TASK, nodeName);

            runEnv.removeCallArguments();
            runEnv.removeReturnValues();

            Context flowContext = runEnv.getStack().popContext();

            //loops
            if (loopStatementExist(loop)) {
                LoopCondition loopCondition = loopsBinding.getOrCreateLoopCondition(loop, flowContext, nodeName);
                if (!loopCondition.hasMore()) {
                    runEnv.putNextStepPosition(nextStepId);
                    runEnv.getStack().pushContext(flowContext);
                    return;
                }

                if (loopCondition instanceof ForLoopCondition) {
                    ForLoopCondition forLoopCondition = (ForLoopCondition) loopCondition;

                    if (loop instanceof ListForLoopStatement) {
                        // normal iteration
                        String varName = ((ListForLoopStatement) loop).getVarName();
                        loopsBinding.incrementListForLoop(varName, flowContext, forLoopCondition);
                    } else {
                        // map iteration
                        MapForLoopStatement mapForLoopStatement = (MapForLoopStatement) loop;
                        String keyName = mapForLoopStatement.getKeyName();
                        String valueName = mapForLoopStatement.getValueName();
                        loopsBinding.incrementMapForLoop(keyName, valueName, flowContext, forLoopCondition);
                    }
                }
            }

            Map<String, Serializable> flowVariables = flowContext.getImmutableViewOfVariables();

            sendStartBindingInputsEvent(taskInputs, runEnv, executionRuntimeServices,
                    "Pre Input binding for task", LanguageEventData.StepType.TASK, nodeName);

            Map<String, Serializable> operationArguments = inputsBinding.bindInputs(taskInputs, flowVariables, runEnv.getSystemProperties());

            //todo: hook

            sendEndBindingInputsEvent(taskInputs, operationArguments, runEnv, executionRuntimeServices, "Task inputs resolved",
                    LanguageEventData.StepType.TASK, nodeName);

            updateCallArgumentsAndPushContextToStack(runEnv, flowContext, operationArguments);

            // request the score engine to switch to the execution plan of the given ref
            requestSwitchToRefExecutableExecutionPlan(runEnv, executionRuntimeServices, RUNNING_EXECUTION_PLAN_ID, refId, nextStepId);

            // set the start step of the given ref as the next step to execute (in the new running execution plan that will be set)
            runEnv.putNextStepPosition(executionRuntimeServices.getSubFlowBeginStep(refId));
        } catch (RuntimeException e) {
            logger.error("There was an error running the begin task execution step of: \'" + nodeName + "\'. Error is: " + e.getMessage());
            throw new RuntimeException("Error running: " + nodeName + ": " + e.getMessage(), e);
        }
    }

    private boolean loopStatementExist(LoopStatement forLoopStatement) {
        return forLoopStatement != null;
    }

    public void endTask(@Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                        @Param(ScoreLangConstants.TASK_PUBLISH_KEY) List<Output> taskPublishValues,
                        @Param(ScoreLangConstants.TASK_NAVIGATION_KEY) Map<String, ResultNavigation> taskNavigationValues,
                        @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                        @Param(ScoreLangConstants.PREVIOUS_STEP_ID_KEY) Long previousStepId,
                        @Param(ScoreLangConstants.BREAK_LOOP_KEY) List<String> breakOn,
                        @Param(ScoreLangConstants.NODE_NAME_KEY) String nodeName,
                        @Param(ScoreLangConstants.ASYNC_LOOP_KEY) boolean async_loop) {

        try {
            Context flowContext = runEnv.getStack().popContext();
            Map<String, Serializable> flowVariables = flowContext.getImmutableViewOfVariables();

            ReturnValues executableReturnValues = runEnv.removeReturnValues();
            fireEvent(executionRuntimeServices, runEnv, ScoreLangConstants.EVENT_OUTPUT_START, "Output binding started",
                    LanguageEventData.StepType.TASK, nodeName,
                    Pair.of(ScoreLangConstants.TASK_PUBLISH_KEY, (Serializable) taskPublishValues),
                    Pair.of(ScoreLangConstants.TASK_NAVIGATION_KEY, (Serializable) taskNavigationValues),
                    Pair.of("operationReturnValues", executableReturnValues));

            Map<String, Serializable> publishValues = outputsBinding.bindOutputs(flowVariables, executableReturnValues.getOutputs(), taskPublishValues);

            flowContext.putVariables(publishValues);

            //loops
            Map<String, Serializable> langVariables = flowContext.getLangVariables();
            if (langVariables.containsKey(LoopCondition.LOOP_CONDITION_KEY)) {
                LoopCondition loopCondition = (LoopCondition) langVariables.get(LoopCondition.LOOP_CONDITION_KEY);
                if (!shouldBreakLoop(breakOn, executableReturnValues) && loopCondition.hasMore()) {
                    runEnv.putNextStepPosition(previousStepId);
                    runEnv.getStack().pushContext(flowContext);
                    throwEventOutputEnd(runEnv, executionRuntimeServices, nodeName,
                            (Serializable) publishValues, previousStepId,
                            new ReturnValues(publishValues, executableReturnValues.getResult()));
                    runEnv.getExecutionPath().forward();
                    return;
                } else {
                    flowContext.getLangVariables().remove(LoopCondition.LOOP_CONDITION_KEY);
                }
            }

            //todo: hook

            // if this is an end task method from a branch then next step position should ne null (end the flow)
            // and result should be the one from the executable (navigation is handled in join branches step)
            Long nextPosition = null;
            String executableResult = executableReturnValues.getResult();
            String presetResult = executableResult;

            if (!async_loop) {
                // set the position of the next step - for the use of the navigation
                // find in the navigation values the correct next step position, according to the operation result, and set it
                ResultNavigation navigation = taskNavigationValues.get(executableResult);
                if (navigation == null) {
                    // should always have the executable response mapped to a navigation by the task, if not, it is an error
                    throw new RuntimeException("Task: " + nodeName + " has no matching navigation for the executable result: " + executableReturnValues.getResult());
                }

                nextPosition = navigation.getNextStepId();
                presetResult = navigation.getPresetResult();
            }

            runEnv.putNextStepPosition(nextPosition);

            HashMap<String, Serializable> outputs = new HashMap<>(flowVariables);

            ReturnValues returnValues = new ReturnValues(outputs, presetResult != null ? presetResult : executableResult);
            runEnv.putReturnValues(returnValues);
            throwEventOutputEnd(runEnv, executionRuntimeServices, nodeName,
                    (Serializable) publishValues, nextPosition, returnValues);

            runEnv.getStack().pushContext(flowContext);
            runEnv.getExecutionPath().forward();
        } catch (RuntimeException e) {
            logger.error("There was an error running the end task execution step of: \'" + nodeName + "\'. Error is: " + e.getMessage());
            throw new RuntimeException("Error running: \'" + nodeName + "\': " + e.getMessage(), e);
        }
    }

    private void throwEventOutputEnd(RunEnvironment runEnv,
                                     ExecutionRuntimeServices executionRuntimeServices,
                                     String nodeName,
                                     Serializable publishValues,
                                     Long nextPosition,
                                     ReturnValues returnValues) {
        fireEvent(executionRuntimeServices, runEnv, ScoreLangConstants.EVENT_OUTPUT_END, "Output binding finished",
                LanguageEventData.StepType.TASK, nodeName,
                Pair.of(LanguageEventData.OUTPUTS, publishValues),
                Pair.of(LanguageEventData.RESULT, returnValues.getResult()),
                Pair.of(LanguageEventData.NEXT_STEP_POSITION, nextPosition));
    }

    private boolean shouldBreakLoop(List<String> breakOn, ReturnValues executableReturnValues) {
        return breakOn.contains(executableReturnValues.getResult());
    }

    private void requestSwitchToRefExecutableExecutionPlan(RunEnvironment runEnv,
                                                           ExecutionRuntimeServices executionRuntimeServices,
                                                           Long RUNNING_EXECUTION_PLAN_ID,
                                                           String refId,
                                                           Long nextStepId) {
        pushParentFlowDataOnStack(runEnv, RUNNING_EXECUTION_PLAN_ID, nextStepId);

        // request the score engine to switch the execution plan to the one with the given refId once it can
        Long subFlowRunningExecutionPlanId = executionRuntimeServices.getSubFlowRunningExecutionPlan(refId);
        executionRuntimeServices.requestToChangeExecutionPlan(subFlowRunningExecutionPlanId);
    }

}
