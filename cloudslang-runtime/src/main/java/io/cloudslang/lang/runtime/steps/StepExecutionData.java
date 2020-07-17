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
import com.hp.oo.sdk.content.plugin.StepSerializableSessionObject;
import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.entities.NavigationOptions;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.WorkerGroupMetadata;
import io.cloudslang.lang.entities.WorkerGroupStatement;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.InOutParam;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.prompt.Prompt;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.entities.utils.ExpressionUtils;
import io.cloudslang.lang.runtime.bindings.ArgumentsBinding;
import io.cloudslang.lang.runtime.bindings.LoopsBinding;
import io.cloudslang.lang.runtime.bindings.OutputsBinding;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import io.cloudslang.lang.runtime.env.Context;
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.api.execution.ExecutionParametersConsts;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.cloudslang.lang.entities.ScoreLangConstants.STEP_NAVIGATION_OPTIONS_KEY;
import static io.cloudslang.lang.entities.ScoreLangConstants.WORKER_GROUP;
import static io.cloudslang.lang.entities.ScoreLangConstants.WORKER_GROUP_OVERRIDE;
import static io.cloudslang.lang.entities.ScoreLangConstants.WORKER_GROUP_VALUE;
import static io.cloudslang.lang.entities.bindings.values.Value.toStringSafe;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;
import static java.lang.Double.parseDouble;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * User: stoneo
 * Date: 02/11/2014
 * Time: 10:23
 */
@Component
public class StepExecutionData extends AbstractExecutionData {

    private static final String DEFAULT_GROUP = "RAS_Operator_Path";
    private static final Logger logger = Logger.getLogger(StepExecutionData.class);
    @Autowired
    private ArgumentsBinding argumentsBinding;
    @Autowired
    private OutputsBinding outputsBinding;
    @Autowired
    private LoopsBinding loopsBinding;
    @Autowired
    private ScriptEvaluator scriptEvaluator;

    @SuppressWarnings("unused")
    public void beginStep(@Param(ScoreLangConstants.STEP_INPUTS_KEY) List<Argument> stepInputs,
                          @Param(ScoreLangConstants.WORKER_GROUP) WorkerGroupStatement workerGroup,
                          @Param(ScoreLangConstants.LOOP_KEY) LoopStatement loop,
                          @Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                          @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                          @Param(ScoreLangConstants.NODE_NAME_KEY) String nodeName,

                          //CHECKSTYLE:OFF: checkstyle:parametername
                          @Param(ExecutionParametersConsts.RUNNING_EXECUTION_PLAN_ID) Long RUNNING_EXECUTION_PLAN_ID,
                          //CHECKSTYLE:ON

                          @Param(ScoreLangConstants.NEXT_STEP_ID_KEY) Long nextStepId,
                          @Param(ScoreLangConstants.REF_ID) String refId,
                          @Param(STEP_NAVIGATION_OPTIONS_KEY) List<NavigationOptions> stepNavigationOptions) {
        try {
            runEnv.removeCallArguments();
            runEnv.removeReturnValues();

            final int flowDepth = runEnv.getParentFlowStack().size();
            prepareNodeName(executionRuntimeServices, nodeName, flowDepth);

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
            ReadOnlyContextAccessor contextAccessor = new ReadOnlyContextAccessor(
                    flowVariables,
                    flowContext.getImmutableViewOfMagicVariables());
            Map<String, Value> boundInputs = argumentsBinding
                    .bindArguments(stepInputs, contextAccessor,
                            runEnv.getSystemProperties());
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

            updateCallArgumentsAndPushContextToStack(
                    runEnv,
                    flowContext,
                    boundInputs,
                    createPrompts(stepInputs));

            Value workerGroupValue = flowContext.removeLanguageVariable(WORKER_GROUP_VALUE);
            Value workerGroupOverride = flowContext.removeLanguageVariable(WORKER_GROUP_OVERRIDE);
            // request the score engine to switch to the execution plan of the given ref
            //CHECKSTYLE:OFF
            requestSwitchToRefExecutableExecutionPlan(runEnv, executionRuntimeServices,
                    RUNNING_EXECUTION_PLAN_ID, refId, nextStepId,
                    toStringSafe(workerGroupValue),
                    BooleanUtils.toBoolean(toStringSafe(workerGroupOverride)));
            //CHECKSTYLE:ON

            // set the start step of the given ref as the next step to execute
            // (in the new running execution plan that will be set)
            runEnv.putNextStepPosition(executionRuntimeServices.getSubFlowBeginStep(refId));

            putStepNavigationOptions(runEnv, stepNavigationOptions, nodeName);
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

            removeStepSerializableSessionObjects(runEnv);

            ReturnValues executableReturnValues = runEnv.removeReturnValues();
            Map<String, Value> argumentsResultContext = removeStepInputsResultContext(flowContext);
            Map<String, Value> executableOutputs = executableReturnValues.getOutputs();
            Map<String, Value> globalContext = flowContext.getImmutableViewOfMagicVariables();
            ReadOnlyContextAccessor outputsBindingAccessor = new ReadOnlyContextAccessor(
                    argumentsResultContext,
                    executableOutputs,
                    globalContext);

            fireEvent(executionRuntimeServices, runEnv, ScoreLangConstants.EVENT_OUTPUT_START, "Output binding started",
                    LanguageEventData.StepType.STEP, nodeName,
                    outputsBindingAccessor,
                    Pair.of(ScoreLangConstants.STEP_PUBLISH_KEY, (Serializable) stepPublishValues),
                    Pair.of(ScoreLangConstants.STEP_NAVIGATION_KEY, (Serializable) stepNavigationValues),
                    Pair.of("executableReturnValues", executableReturnValues),
                    Pair.of("parallelLoop", parallelLoop)
            );

            final Map<String, Value> publishValues = publishValuesMap(runEnv.getSystemProperties(), stepPublishValues,
                    parallelLoop, executableOutputs, outputsBindingAccessor, outputsBinding);
            flowContext.putVariables(publishValues);

            //loops
            Map<String, Value> langVariables = flowContext.getImmutableViewOfLanguageVariables();

            if (handleEndLoopCondition(runEnv, executionRuntimeServices, previousStepId, breakOn, nodeName, flowContext,
                    executableReturnValues, outputsBindingAccessor, publishValues, langVariables)) {
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

            List<NavigationOptions> stepNavigationOptions = runEnv
                    .removeStepNavigationOptions(nodeName + previousStepId);
            final Double roiValue = getRoiValue(executableResult, stepNavigationOptions, flowVariables);

            runEnv.putReturnValues(returnValues);
            throwEventOutputEnd(
                    runEnv,
                    executionRuntimeServices,
                    nodeName,
                    publishValues,
                    nextPosition,
                    returnValues,
                    roiValue,
                    outputsBindingAccessor
            );

            executionRuntimeServices.addRoiValue(roiValue);

            runEnv.getStack().pushContext(flowContext);
            runEnv.getExecutionPath().forward();
        } catch (RuntimeException e) {
            logger.error("There was an error running the endStep execution step of: \'" + nodeName +
                    "\'. Error is: " + e.getMessage());
            throw new RuntimeException("Error running: \'" + nodeName + "\': " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unused")
    public void setWorkerGroupStep(
            @Param(ScoreLangConstants.WORKER_GROUP) WorkerGroupStatement workerGroup,
            @Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
            @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
            @Param(ScoreLangConstants.NODE_NAME_KEY) String nodeName,
            @Param(ScoreLangConstants.NEXT_STEP_ID_KEY) Long nextStepId) {
        try {
            Context flowContext = runEnv.getStack().peekContext();

            handleWorkerGroup(workerGroup, flowContext, runEnv, executionRuntimeServices);
            runEnv.putNextStepPosition(nextStepId);
        } catch (RuntimeException e) {
            logger.error("There was an error running the setWorkerGroupStep execution step of: \'" + nodeName +
                    "\'. Error is: " + e.getMessage());
            throw new RuntimeException("Error running: " + nodeName + ": " + e.getMessage(), e);
        }
    }

    private void removeStepSerializableSessionObjects(final RunEnvironment runEnv) {
        final int flowDepth = runEnv.getParentFlowStack().size();
        runEnv.getSerializableDataMap().entrySet().removeIf(
            (Map.Entry<String, ?> entry) -> {
                try {
                    final String key = entry.getKey();
                    final String valueClassName = entry.getValue()
                                                       .getClass()
                                                       .getName();
                    final int entryDepth = Integer.parseInt(key.substring(key.lastIndexOf('_') + 1));
                    return (entryDepth > flowDepth) &&
                            valueClassName.equals(StepSerializableSessionObject.class.getName());
                } catch (Exception ignore) {
                    return false;
                }
            }
        );
    }

    private WorkerGroupMetadata handleWorkerGroup(WorkerGroupStatement workerGroup,
                                                  Context flowContext,
                                                  RunEnvironment runEnv,
                                                  ExecutionRuntimeServices execRuntimeServices) {
        WorkerGroupMetadata workerGroupVal = runEnv.getParentFlowStack().computeParentWorkerGroup();

        if (workerGroupVal.getValue() != null && workerGroupVal.isOverride()) {
            //use the parent worker group
            execRuntimeServices.setWorkerGroupName(workerGroupVal.getValue());
        } else {
            if (workerGroup != null) {
                //use the step worker group
                workerGroupVal = computeWorkerGroup(workerGroup, flowContext, runEnv, workerGroup.getExpression());
                execRuntimeServices.setWorkerGroupName(workerGroupVal.getValue());
                flowContext.putLanguageVariable(WORKER_GROUP, ValueFactory.create(workerGroupVal.getValue()));
            } else {
                /* It can get here in two situations:
                 * 1. if there is a parent worker group and override = false
                 * 2. there is no set worker, in which case the default worker is used
                 */
                String valueOrDefault = workerGroupVal.getValue() != null ? workerGroupVal.getValue() : DEFAULT_GROUP;
                execRuntimeServices.setWorkerGroupName(valueOrDefault);
                flowContext.putLanguageVariable(WORKER_GROUP, ValueFactory.create(valueOrDefault));
            }
        }
        execRuntimeServices.setShouldCheckGroup();
        flowContext.putLanguageVariable(WORKER_GROUP_VALUE, ValueFactory.create(workerGroupVal.getValue()));
        flowContext.putLanguageVariable(WORKER_GROUP_OVERRIDE, ValueFactory.create(workerGroupVal.isOverride()));

        return workerGroupVal;
    }

    private WorkerGroupMetadata computeWorkerGroup(WorkerGroupStatement workerGroup,
                                                   Context flowContext,
                                                   RunEnvironment runEnv,
                                                   String expression) {
        Value workerGroupValue;
        if (workerGroup.getFunctionDependencies() == null && workerGroup.getSystemPropertyDependencies() == null) {
            workerGroupValue = ValueFactory.create(expression);
        } else {
            workerGroupValue = scriptEvaluator.evalExpr(expression, flowContext.getImmutableViewOfVariables(),
                    runEnv.getSystemProperties(), workerGroup.getFunctionDependencies());
        }
        return new WorkerGroupMetadata(workerGroupValue.toString(), workerGroup.isOverride());
    }

    private void putStepNavigationOptions(RunEnvironment runEnv, List<NavigationOptions> stepNavigationOptions,
                                          String nodeName) {
        if (CollectionUtils.isNotEmpty(stepNavigationOptions)) {
            runEnv.putStepNavigationOptions(nodeName + stepNavigationOptions.get(0).getCurrStepId(),
                    stepNavigationOptions);
        }
    }

    private Double getRoiValue(String stepExecutableResult, List<NavigationOptions> stepNavigationOptions,
                               Map<String, Value> flowVariables) {
        if (isNotEmpty(stepExecutableResult) &&  stepNavigationOptions != null) {
            for (NavigationOptions navigationOptions: stepNavigationOptions) {
                if (navigationOptions.getName().equals(stepExecutableResult)) {
                    Serializable roi = navigationOptions.getOptions().get(LanguageEventData.ROI);
                    if (roi instanceof String) {
                        String expr = ExpressionUtils.extractExpression(roi);
                        if (isNotEmpty(expr) && flowVariables.containsKey(expr)) {
                            roi = flowVariables.get(expr).get();
                        }
                    }
                    return roi != null ? parseDouble(roi.toString()) : ExecutionParametersConsts.DEFAULT_ROI_VALUE;
                }
            }
        }
        return ExecutionParametersConsts.DEFAULT_ROI_VALUE;
    }

    private Map<String, Prompt> createPrompts(List<Argument> stepInputs) {
        return stepInputs
                .stream()
                .filter(Argument::hasPrompt)
                .collect(toMap(InOutParam::getName, Argument::getPrompt));
    }


}
