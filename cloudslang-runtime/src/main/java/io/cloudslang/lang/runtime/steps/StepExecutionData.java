/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.steps;

import com.hp.oo.sdk.content.annotations.Param;
import io.cloudslang.lang.entities.ListLoopStatement;
import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.entities.MapLoopStatement;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.bindings.ArgumentsBinding;
import io.cloudslang.lang.runtime.bindings.LoopsBinding;
import io.cloudslang.lang.runtime.bindings.OutputsBinding;
import io.cloudslang.lang.runtime.env.Context;
import io.cloudslang.lang.runtime.env.ForLoopCondition;
import io.cloudslang.lang.runtime.env.LoopCondition;
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.api.execution.ExecutionParametersConsts;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.cloudslang.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;

/**
 * User: stoneo
 * Date: 02/11/2014
 * Time: 10:23
 */
@Component
public class StepExecutionData extends AbstractExecutionData {

    @Autowired
    private ArgumentsBinding argumentsBinding;

    @Autowired
    private OutputsBinding outputsBinding;

    @Autowired
    private LoopsBinding loopsBinding;

    private static final Logger logger = Logger.getLogger(StepExecutionData.class);

    @SuppressWarnings("unused")
    public void beginStep(@Param(ScoreLangConstants.STEP_INPUTS_KEY) List<Argument> stepInputs,
                          @Param(ScoreLangConstants.LOOP_KEY) LoopStatement loop,
                          @Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                          @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                          @Param(ScoreLangConstants.NODE_NAME_KEY) String nodeName,

                          //CHECKSTYLE:OFF: checkstyle:parametername
                          @Param(ExecutionParametersConsts.RUNNING_EXECUTION_PLAN_ID) Long RUNNING_EXECUTION_PLAN_ID,
                          //CHECKSTYLE:ON

                          @Param(ScoreLangConstants.NEXT_STEP_ID_KEY) Long nextStepId,
                          @Param(ScoreLangConstants.REF_ID) String refId) {
        try {

            fireEvent(executionRuntimeServices, runEnv, ScoreLangConstants.EVENT_STEP_START,
                    "beginStep execution step started", LanguageEventData.StepType.STEP, nodeName);

            runEnv.removeCallArguments();
            runEnv.removeReturnValues();

            Context flowContext = runEnv.getStack().popContext();

            //loops
            if (loopStatementExist(loop)) {
                LoopCondition loopCondition = loopsBinding
                        .getOrCreateLoopCondition(loop, flowContext, runEnv.getSystemProperties(), nodeName);
                if (loopCondition == null || !loopCondition.hasMore()) {
                    runEnv.putNextStepPosition(nextStepId);
                    runEnv.getStack().pushContext(flowContext);
                    return;
                }

                if (loopCondition instanceof ForLoopCondition) {
                    ForLoopCondition forLoopCondition = (ForLoopCondition) loopCondition;

                    if (loop instanceof ListLoopStatement) {
                        // normal iteration
                        String varName = ((ListLoopStatement) loop).getVarName();
                        loopsBinding.incrementListForLoop(varName, flowContext, forLoopCondition);
                    } else {
                        // map iteration
                        MapLoopStatement mapLoopStatement = (MapLoopStatement) loop;
                        String keyName = mapLoopStatement.getKeyName();
                        String valueName = mapLoopStatement.getValueName();
                        loopsBinding.incrementMapForLoop(keyName, valueName, flowContext, forLoopCondition);
                    }
                }
            }

            Map<String, Value> flowVariables = flowContext.getImmutableViewOfVariables();

            sendStartBindingArgumentsEvent(
                    stepInputs,
                    runEnv,
                    executionRuntimeServices,
                    "Pre argument binding for step",
                    nodeName
            );

            Map<String, Value> boundInputs = argumentsBinding
                    .bindArguments(stepInputs, flowVariables, runEnv.getSystemProperties());
            saveStepInputsResultContext(flowContext, boundInputs);

            sendEndBindingArgumentsEvent(
                    stepInputs,
                    boundInputs,
                    runEnv,
                    executionRuntimeServices,
                    "Step inputs resolved",
                    nodeName
            );

            updateCallArgumentsAndPushContextToStack(runEnv, flowContext, boundInputs);

            // request the score engine to switch to the execution plan of the given ref
            //CHECKSTYLE:OFF
            requestSwitchToRefExecutableExecutionPlan(runEnv, executionRuntimeServices,
                    RUNNING_EXECUTION_PLAN_ID, refId, nextStepId);
            //CHECKSTYLE:ON

            // set the start step of the given ref as the next step to execute
            // (in the new running execution plan that will be set)
            runEnv.putNextStepPosition(executionRuntimeServices.getSubFlowBeginStep(refId));
        } catch (RuntimeException e) {
            logger.error("There was an error running the beginStep execution step of: \'" + nodeName +
                    "\'. Error is: " + e.getMessage());
            throw new RuntimeException("Error running: " + nodeName + ": " + e.getMessage(), e);
        }
    }

    private boolean loopStatementExist(LoopStatement forLoopStatement) {
        return forLoopStatement != null;
    }

    @SuppressWarnings("unused")
    public void endStep(@Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                        @Param(ScoreLangConstants.STEP_PUBLISH_KEY) List<Output> stepPublishValues,
                        @Param(ScoreLangConstants.STEP_NAVIGATION_KEY)
                                    Map<String, ResultNavigation> stepNavigationValues,
                        @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                        @Param(ScoreLangConstants.PREVIOUS_STEP_ID_KEY) Long previousStepId,
                        @Param(ScoreLangConstants.BREAK_LOOP_KEY) List<String> breakOn,
                        @Param(ScoreLangConstants.NODE_NAME_KEY) String nodeName,
                        @Param(ScoreLangConstants.PARALLEL_LOOP_KEY) boolean parallelLoop) {

        try {
            Context flowContext = runEnv.getStack().popContext();

            ReturnValues executableReturnValues = runEnv.removeReturnValues();
            fireEvent(executionRuntimeServices, runEnv, ScoreLangConstants.EVENT_OUTPUT_START, "Output binding started",
                    LanguageEventData.StepType.STEP, nodeName,
                    Pair.of(ScoreLangConstants.STEP_PUBLISH_KEY, (Serializable) stepPublishValues),
                    Pair.of(ScoreLangConstants.STEP_NAVIGATION_KEY, (Serializable) stepNavigationValues),
                    Pair.of("executableReturnValues", executableReturnValues),
                    Pair.of("parallelLoop", parallelLoop)
            );

            Map<String, Value> argumentsResultContext = removeStepInputsResultContext(flowContext);
            Map<String, Value> publishValues;
            if (parallelLoop) {
                publishValues = new HashMap<>(executableReturnValues.getOutputs());
            } else {
                publishValues =
                        outputsBinding.bindOutputs(
                                argumentsResultContext,
                                executableReturnValues.getOutputs(),
                                runEnv.getSystemProperties(),
                                stepPublishValues
                        );
            }
            flowContext.putVariables(publishValues);

            //loops
            Map<String, Value> langVariables = flowContext.getImmutableViewOfLanguageVariables();
            if (langVariables.containsKey(LoopCondition.LOOP_CONDITION_KEY)) {
                LoopCondition loopCondition = (LoopCondition) langVariables.get(LoopCondition.LOOP_CONDITION_KEY).get();
                if (!shouldBreakLoop(breakOn, executableReturnValues) && loopCondition.hasMore()) {
                    runEnv.putNextStepPosition(previousStepId);
                    runEnv.getStack().pushContext(flowContext);
                    throwEventOutputEnd(runEnv, executionRuntimeServices, nodeName,
                            publishValues, previousStepId,
                            new ReturnValues(publishValues, executableReturnValues.getResult()));
                    runEnv.getExecutionPath().forward();
                    return;
                } else {
                    flowContext.removeLanguageVariable(LoopCondition.LOOP_CONDITION_KEY);
                }
            }

            // if this is an endStep method from a branch then next execution step position should ne null
            // (end the flow) and result should be the one from the executable
            // (navigation is handled in join branches step)
            Long nextPosition = null;
            String executableResult = executableReturnValues.getResult();
            String presetResult = executableResult;

            if (!parallelLoop) {
                // set the position of the next step - for the use of the navigation
                // find in the navigation values the correct next step position, according to the operation result,
                // and set it
                ResultNavigation navigation = stepNavigationValues.get(executableResult);
                if (navigation == null) {
                    // should always have the executable response mapped to a navigation by the step,
                    // if not, it is an error
                    throw new RuntimeException("Step: " + nodeName +
                            " has no matching navigation for the executable result: " +
                            executableReturnValues.getResult());
                }

                nextPosition = navigation.getNextStepId();
                presetResult = navigation.getPresetResult();
            }

            runEnv.putNextStepPosition(nextPosition);

            Map<String, Value> flowVariables = flowContext.getImmutableViewOfVariables();
            HashMap<String, Value> outputs = new HashMap<>(flowVariables);

            ReturnValues returnValues = new ReturnValues(outputs,
                    presetResult != null ? presetResult : executableResult);
            runEnv.putReturnValues(returnValues);
            throwEventOutputEnd(runEnv, executionRuntimeServices, nodeName, publishValues, nextPosition, returnValues);

            runEnv.getStack().pushContext(flowContext);
            runEnv.getExecutionPath().forward();
        } catch (RuntimeException e) {
            logger.error("There was an error running the endStep execution step of: \'" + nodeName +
                    "\'. Error is: " + e.getMessage());
            throw new RuntimeException("Error running: \'" + nodeName + "\': " + e.getMessage(), e);
        }
    }

    private void throwEventOutputEnd(RunEnvironment runEnv,
                                     ExecutionRuntimeServices executionRuntimeServices,
                                     String nodeName,
                                     Map<String, Value> publishValues,
                                     Long nextPosition,
                                     ReturnValues returnValues) {
        fireEvent(executionRuntimeServices, runEnv, ScoreLangConstants.EVENT_OUTPUT_END, "Output binding finished",
                LanguageEventData.StepType.STEP, nodeName,
                Pair.of(LanguageEventData.OUTPUTS, (Serializable) publishValues),
                Pair.of(LanguageEventData.RESULT, returnValues.getResult()),
                Pair.of(LanguageEventData.NEXT_STEP_POSITION, nextPosition));
    }

    private boolean shouldBreakLoop(List<String> breakOn, ReturnValues executableReturnValues) {
        return breakOn.contains(executableReturnValues.getResult());
    }

    private void requestSwitchToRefExecutableExecutionPlan(RunEnvironment runEnv,
                                                           ExecutionRuntimeServices executionRuntimeServices,
                                                           Long runningExecutionPlanId,
                                                           String refId,
                                                           Long nextStepId) {
        pushParentFlowDataOnStack(runEnv, runningExecutionPlanId, nextStepId);

        // request the score engine to switch the execution plan to the one with the given refId once it can
        Long subFlowRunningExecutionPlanId = executionRuntimeServices.getSubFlowRunningExecutionPlan(refId);
        executionRuntimeServices.requestToChangeExecutionPlan(subFlowRunningExecutionPlanId);
    }

    private void saveStepInputsResultContext(Context context, Map<String, Value> stepInputsResultContext) {
        context.putLanguageVariable(ScoreLangConstants.STEP_INPUTS_RESULT_CONTEXT,
                ValueFactory.create((Serializable) stepInputsResultContext));
    }

    private Map<String, Value> removeStepInputsResultContext(Context context) {
        Value rawValue = context.removeLanguageVariable(ScoreLangConstants.STEP_INPUTS_RESULT_CONTEXT);
        @SuppressWarnings("unchecked")
        Map<String, Value> stepInputsResultContext = rawValue == null ? null : (Map<String, Value>) rawValue.get();
        return stepInputsResultContext;
    }

}
