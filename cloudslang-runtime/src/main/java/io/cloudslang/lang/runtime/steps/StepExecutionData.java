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
import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.utils.MapUtils;
import io.cloudslang.lang.runtime.bindings.ArgumentsBinding;
import io.cloudslang.lang.runtime.bindings.LoopsBinding;
import io.cloudslang.lang.runtime.bindings.OutputsBinding;
import io.cloudslang.lang.runtime.env.Context;
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.api.execution.ExecutionParametersConsts;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
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
public class StepExecutionData extends AbstractExecutionData {

    private static final Logger logger = Logger.getLogger(StepExecutionData.class);
    @Autowired
    private ArgumentsBinding argumentsBinding;
    @Autowired
    private OutputsBinding outputsBinding;
    @Autowired
    private LoopsBinding loopsBinding;

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
            runEnv.removeCallArguments();
            runEnv.removeReturnValues();

            Context flowContext = runEnv.getStack().popContext();
            Map<String, Value> flowVariables = flowContext.getImmutableViewOfVariables();

            fireEvent(
                    executionRuntimeServices,
                    runEnv,
                    ScoreLangConstants.EVENT_STEP_START,
                    "beginStep execution step started",
                    LanguageEventData.StepType.STEP,
                    nodeName,
                    flowVariables
            );

            //loops
            if (handleLoopStatement(loop, runEnv, nodeName, nextStepId, flowContext, loopsBinding)) {
                return;
            }

            sendStartBindingArgumentsEvent(
                    stepInputs,
                    runEnv,
                    executionRuntimeServices,
                    "Pre argument binding for step",
                    nodeName,
                    flowVariables
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
                    nodeName,
                    flowVariables
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
            Map<String, Value> argumentsResultContext = removeStepInputsResultContext(flowContext);
            Map<String, Value> executableOutputs = executableReturnValues.getOutputs();
            Map<String, Value> outputsBindingContext = MapUtils.mergeMaps(argumentsResultContext, executableOutputs);

            fireEvent(executionRuntimeServices, runEnv, ScoreLangConstants.EVENT_OUTPUT_START, "Output binding started",
                    LanguageEventData.StepType.STEP, nodeName,
                    outputsBindingContext,
                    Pair.of(ScoreLangConstants.STEP_PUBLISH_KEY, (Serializable) stepPublishValues),
                    Pair.of(ScoreLangConstants.STEP_NAVIGATION_KEY, (Serializable) stepNavigationValues),
                    Pair.of("executableReturnValues", executableReturnValues),
                    Pair.of("parallelLoop", parallelLoop)
            );

            final Map<String, Value> publishValues = publishValuesMap(runEnv.getSystemProperties(), stepPublishValues,
                    parallelLoop, executableOutputs, outputsBindingContext, outputsBinding);
            flowContext.putVariables(publishValues);

            //loops
            Map<String, Value> langVariables = flowContext.getImmutableViewOfLanguageVariables();

            if (handleEndLoopCondition(runEnv, executionRuntimeServices, previousStepId, breakOn, nodeName, flowContext,
                    executableReturnValues, outputsBindingContext, publishValues, langVariables)) {
                return;
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
                final ResultNavigation navigation = getResultNavigation(stepNavigationValues, nodeName,
                        executableReturnValues, executableResult);

                nextPosition = navigation.getNextStepId();
                presetResult = navigation.getPresetResult();
            }

            runEnv.putNextStepPosition(nextPosition);

            Map<String, Value> flowVariables = flowContext.getImmutableViewOfVariables();
            HashMap<String, Value> outputs = new HashMap<>(flowVariables);

            final ReturnValues returnValues = getReturnValues(executableResult, presetResult, outputs);

            runEnv.putReturnValues(returnValues);
            throwEventOutputEnd(
                    runEnv,
                    executionRuntimeServices,
                    nodeName,
                    publishValues,
                    nextPosition,
                    returnValues,
                    outputsBindingContext
            );

            runEnv.getStack().pushContext(flowContext);
            runEnv.getExecutionPath().forward();
        } catch (RuntimeException e) {
            logger.error("There was an error running the endStep execution step of: \'" + nodeName +
                    "\'. Error is: " + e.getMessage());
            throw new RuntimeException("Error running: \'" + nodeName + "\': " + e.getMessage(), e);
        }
    }


}
