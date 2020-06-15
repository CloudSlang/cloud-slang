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
import io.cloudslang.lang.entities.WorkerGroupStatement;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.entities.utils.ExpressionUtils;
import io.cloudslang.lang.entities.utils.MapUtils;
import io.cloudslang.lang.runtime.bindings.ArgumentsBinding;
import io.cloudslang.lang.runtime.bindings.LoopsBinding;
import io.cloudslang.lang.runtime.bindings.OutputsBinding;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import io.cloudslang.lang.runtime.env.Context;
import io.cloudslang.lang.runtime.env.ParentFlowData;
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.api.execution.ExecutionParametersConsts;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static io.cloudslang.lang.entities.ScoreLangConstants.STEP_NAVIGATION_OPTIONS_KEY;
import static io.cloudslang.lang.entities.ScoreLangConstants.WORKER_GROUP;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
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

            final String workerGroupVal = handleWorkerGroup(workerGroup, flowContext, runEnv, executionRuntimeServices);

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
                    RUNNING_EXECUTION_PLAN_ID, refId, nextStepId, workerGroupVal);
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
            Map<String, Value> outputsBindingContext = MapUtils.mergeMaps(argumentsResultContext, executableOutputs);

            String workerGroup = flowContext.getArgument(WORKER_GROUP);
            if (workerGroup != null) {
                executionRuntimeServices.setWorkerGroupName(workerGroup);
                //flowContext.removeArgument(WORKER_GROUP);
            }
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
                    outputsBindingContext
            );

            executionRuntimeServices.addRoiValue(roiValue);
            executionRuntimeServices.setShouldCheckGroup();

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

            if (workerGroup != null) {
                handleWorkerGroup(workerGroup, flowContext, runEnv, executionRuntimeServices);
                executionRuntimeServices.setShouldCheckGroup();
            }

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

    private String handleWorkerGroup(WorkerGroupStatement workerGroup,
                                     Context flowContext,
                                     RunEnvironment runEnv,
                                     ExecutionRuntimeServices execRuntimeServices) {
        String workerGroupVal = computeParentWorkerGroup(runEnv);

        if (workerGroupVal != null) {
            execRuntimeServices.setWorkerGroupName(workerGroupVal);
        } else {
            if (workerGroup != null) {
                workerGroupVal = computeWorkerGroup(workerGroup, flowContext, runEnv, workerGroup.getExpression());
                setWorkerGroupOnContexts(flowContext, execRuntimeServices, workerGroupVal);
            } else {
                setWorkerGroupOnContexts(flowContext, execRuntimeServices, DEFAULT_GROUP);
            }
        }
        return workerGroupVal;
    }

    private String computeParentWorkerGroup(RunEnvironment runEnv) {
        String workerGroupVal = null;
        Stack<ParentFlowData> parentFlowStack = runEnv.getParentFlowStack().cloneParentStackData();

        Iterator iterator = parentFlowStack.iterator();
        while (iterator.hasNext()) {
            ParentFlowData parentFlowData = (ParentFlowData) iterator.next();
            String workerGroupTemp = parentFlowData.getWorkerGroup();
            if (workerGroupTemp != null) {
                workerGroupVal = workerGroupTemp;
                break;
            }
        }
        return workerGroupVal;
    }

    private void setWorkerGroupOnContexts(Context flowContext,
                                          ExecutionRuntimeServices execRuntimeServices,
                                          String workerGroupVal) {
        execRuntimeServices.setWorkerGroupName(workerGroupVal);
        flowContext.putArgument(WORKER_GROUP, workerGroupVal);
    }

    private String computeWorkerGroup(WorkerGroupStatement workerGroup,
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
        return workerGroupValue.toString();
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
                    return roi != null ? Double.valueOf(roi.toString()) : ExecutionParametersConsts.DEFAULT_ROI_VALUE;
                }
            }
        }
        return ExecutionParametersConsts.DEFAULT_ROI_VALUE;
    }


}
