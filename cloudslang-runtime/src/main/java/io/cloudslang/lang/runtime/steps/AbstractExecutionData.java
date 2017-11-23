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

import io.cloudslang.lang.entities.ListLoopStatement;
import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.entities.MapLoopStatement;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.entities.properties.EventVerbosityLevel;
import io.cloudslang.lang.entities.utils.ValueUtils;
import io.cloudslang.lang.runtime.bindings.LoopsBinding;
import io.cloudslang.lang.runtime.bindings.OutputsBinding;
import io.cloudslang.lang.runtime.env.Context;
import io.cloudslang.lang.runtime.env.ContextStack;
import io.cloudslang.lang.runtime.env.ForLoopCondition;
import io.cloudslang.lang.runtime.env.LoopCondition;
import io.cloudslang.lang.runtime.env.ParentFlowData;
import io.cloudslang.lang.runtime.env.ParentFlowStack;
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static io.cloudslang.lang.entities.properties.SlangSystemPropertyConstant.CSLANG_RUNTIME_EVENTS_VERBOSITY;

public abstract class AbstractExecutionData {

    @SafeVarargs
    public static void fireEvent(ExecutionRuntimeServices runtimeServices,
                                 RunEnvironment runEnvironment,
                                 String type,
                                 String description,
                                 LanguageEventData.StepType stepType,
                                 String stepName,
                                 Map<String, Value> context,
                                 Map.Entry<String, ? extends Serializable>... fields) {
        fireEvent(runtimeServices, type, description,
                runEnvironment.getExecutionPath().getCurrentPath(), stepType, stepName, context, fields);
    }

    @SafeVarargs
    public static void fireEvent(ExecutionRuntimeServices runtimeServices,
                                 String type,
                                 String description,
                                 String path,
                                 LanguageEventData.StepType stepType,
                                 String stepName,
                                 Map<String, Value> context,
                                 Map.Entry<String, ? extends Serializable>... fields) {
        LanguageEventData eventData = new LanguageEventData();
        eventData.setStepType(stepType);
        eventData.setStepName(stepName);
        eventData.setEventType(type);
        eventData.setDescription(description);
        eventData.setTimeStamp(new Date());
        eventData.setExecutionId(runtimeServices.getExecutionId());
        eventData.setPath(path);

        setContext(eventData, context);

        for (Entry<String, ? extends Serializable> field : fields) {
            //noinspection unchecked
            eventData.put(field.getKey(), LanguageEventData.maskSensitiveValues(field.getValue()));
        }
        runtimeServices.addEvent(type, eventData);
    }

    private static void pushParentFlowDataOnStack(RunEnvironment runEnv, Long runningExecutionPlanId,
                                                  Long nextStepId) {
        // create ParentFlowData object containing the current running execution plan id and
        // the next step id to navigate to in the current execution plan,
        // and push it to the ParentFlowStack for future use (once we finish running the ref operation/flow)
        ParentFlowStack stack = runEnv.getParentFlowStack();
        stack.pushParentFlowData(new ParentFlowData(runningExecutionPlanId, nextStepId));
    }

    private static void setContext(LanguageEventData eventData, Map<String, Value> context) {
        String verbosityLevel = System.getProperty(
                CSLANG_RUNTIME_EVENTS_VERBOSITY.getValue(),
                EventVerbosityLevel.DEFAULT.getValue()
        );
        if (EventVerbosityLevel.ALL.getValue().equals(verbosityLevel) && context != null) {
            eventData.setContext(ValueUtils.flatten(context));
        }
    }

    protected static boolean handleLoopStatement(LoopStatement loop,
                                                 RunEnvironment runEnv,
                                                 String nodeName,
                                                 Long nextStepId,
                                                 Context flowContext,
                                                 LoopsBinding loopsBinding) {
        if (loop != null) {
            LoopCondition loopCondition = loopsBinding
                    .getOrCreateLoopCondition(loop, flowContext, runEnv.getSystemProperties(), nodeName);
            if (loopCondition == null || !loopCondition.hasMore()) {
                runEnv.putNextStepPosition(nextStepId);
                runEnv.getStack().pushContext(flowContext);
                return true;
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
        return false;
    }

    protected static boolean handleEndLoopCondition(RunEnvironment runEnv,
                                                    ExecutionRuntimeServices executionRuntimeServices,
                                                    Long previousStepId,
                                                    List<String> breakOn,
                                                    String nodeName,
                                                    Context flowContext, ReturnValues executableReturnValues,
                                                    Map<String, Value> outputsBindingContext,
                                                    Map<String, Value> publishValues,
                                                    Map<String, Value> langVariables) {
        if (langVariables.containsKey(LoopCondition.LOOP_CONDITION_KEY)) {
            LoopCondition loopCondition = (LoopCondition) langVariables.get(LoopCondition.LOOP_CONDITION_KEY).get();
            if (!shouldBreakLoop(breakOn, executableReturnValues) && loopCondition.hasMore()) {
                runEnv.putNextStepPosition(previousStepId);
                runEnv.getStack().pushContext(flowContext);
                throwEventOutputEnd(
                        runEnv,
                        executionRuntimeServices,
                        nodeName,
                        publishValues,
                        previousStepId,
                        new ReturnValues(publishValues, executableReturnValues.getResult()),
                        outputsBindingContext
                );
                runEnv.getExecutionPath().forward();
                return true;
            } else {
                flowContext.removeLanguageVariable(LoopCondition.LOOP_CONDITION_KEY);
            }
        }
        return false;
    }

    protected static void throwEventOutputEnd(RunEnvironment runEnv,
                                              ExecutionRuntimeServices executionRuntimeServices,
                                              String nodeName,
                                              Map<String, Value> publishValues,
                                              Long nextPosition,
                                              ReturnValues returnValues,
                                              Map<String, Value> context) {
        fireEvent(executionRuntimeServices, runEnv, ScoreLangConstants.EVENT_OUTPUT_END, "Output binding finished",
                LanguageEventData.StepType.STEP, nodeName,
                context,
                Pair.of(LanguageEventData.OUTPUTS, (Serializable) publishValues),
                Pair.of(LanguageEventData.RESULT, returnValues.getResult()),
                Pair.of(LanguageEventData.NEXT_STEP_POSITION, nextPosition));
    }

    private static boolean shouldBreakLoop(List<String> breakOn, ReturnValues executableReturnValues) {
        return breakOn.contains(executableReturnValues.getResult());
    }

    protected static void requestSwitchToRefExecutableExecutionPlan(RunEnvironment runEnv,
                                                                    ExecutionRuntimeServices executionRuntimeServices,
                                                                    Long runningExecutionPlanId,
                                                                    String refId,
                                                                    Long nextStepId) {
        pushParentFlowDataOnStack(runEnv, runningExecutionPlanId, nextStepId);

        // request the score engine to switch the execution plan to the one with the given refId once it can
        Long subFlowRunningExecutionPlanId = executionRuntimeServices.getSubFlowRunningExecutionPlan(refId);
        executionRuntimeServices.requestToChangeExecutionPlan(subFlowRunningExecutionPlanId);
    }

    protected static void saveStepInputsResultContext(Context context, Map<String, Value> stepInputsResultContext) {
        context.putLanguageVariable(ScoreLangConstants.STEP_INPUTS_RESULT_CONTEXT,
                ValueFactory.create((Serializable) stepInputsResultContext));
    }

    protected static Map<String, Value> removeStepInputsResultContext(Context context) {
        Value rawValue = context.removeLanguageVariable(ScoreLangConstants.STEP_INPUTS_RESULT_CONTEXT);
        @SuppressWarnings("unchecked")
        Map<String, Value> stepInputsResultContext = rawValue == null ?
                Collections.emptyMap() : (Map<String, Value>) rawValue.get();
        return stepInputsResultContext;
    }

    protected static ReturnValues getReturnValues(String executableResult, String presetResult,
                                                  HashMap<String, Value> outputs) {
        final String result = presetResult != null ? presetResult : executableResult;
        return new ReturnValues(outputs, result);
    }

    protected static Map<String, Value> publishValuesMap(Set<SystemProperty> systemProperties,
                                                         List<Output> stepPublishValues,
                                                         boolean parallelLoop, Map<String, Value> aflResultMap,
                                                         Map<String, Value> outputsBindingContext,
                                                         OutputsBinding outputsBinding) {
        if (parallelLoop) {
            return new HashMap<>(aflResultMap);
        }
        return outputsBinding.bindOutputs(outputsBindingContext, systemProperties, stepPublishValues);
    }

    public void sendStartBindingInputsEvent(List<Input> inputs,
                                            RunEnvironment runEnv,
                                            ExecutionRuntimeServices executionRuntimeServices,
                                            String desc,
                                            LanguageEventData.StepType stepType,
                                            String stepName,
                                            Map<String, Value> context) {
        ArrayList<String> inputNames = new ArrayList<>();
        for (Input input : inputs) {
            inputNames.add(input.getName());
        }
        fireEvent(
                executionRuntimeServices,
                runEnv,
                ScoreLangConstants.EVENT_INPUT_START,
                desc,
                stepType,
                stepName,
                context,
                Pair.of(LanguageEventData.INPUTS, inputNames));
    }

    public void sendEndBindingInputsEvent(List<Input> inputs,
                                          final Map<String, Value> boundInputValues,
                                          RunEnvironment runEnv,
                                          ExecutionRuntimeServices executionRuntimeServices,
                                          String desc,
                                          LanguageEventData.StepType stepType,
                                          String stepName,
                                          Map<String, Value> context) {
        Map<String, Value> inputsForEvent = new LinkedHashMap<>();
        for (Input input : inputs) {
            String inputName = input.getName();
            Value inputValue = boundInputValues.get(inputName);
            inputsForEvent.put(inputName, inputValue);
        }
        fireEvent(
                executionRuntimeServices,
                runEnv,
                ScoreLangConstants.EVENT_INPUT_END,
                desc,
                stepType,
                stepName,
                context,
                Pair.of(LanguageEventData.BOUND_INPUTS, (Serializable) inputsForEvent)
        );
    }

    public void sendStartBindingArgumentsEvent(
            List<Argument> arguments,
            RunEnvironment runEnv,
            ExecutionRuntimeServices executionRuntimeServices,
            String description,
            String stepName,
            Map<String, Value> context) {
        ArrayList<String> argumentNames = new ArrayList<>();
        for (Argument argument : arguments) {
            argumentNames.add(argument.getName());
        }
        fireEvent(
                executionRuntimeServices,
                runEnv,
                ScoreLangConstants.EVENT_ARGUMENT_START,
                description,
                LanguageEventData.StepType.STEP,
                stepName,
                context,
                Pair.of(LanguageEventData.ARGUMENTS, argumentNames)
        );
    }

    public void sendEndBindingArgumentsEvent(
            List<Argument> arguments,
            final Map<String, Value> boundInputs,
            RunEnvironment runEnv,
            ExecutionRuntimeServices executionRuntimeServices,
            String description,
            String stepName,
            Map<String, Value> context) {
        Map<String, Value> argumentsForEvent = new LinkedHashMap<>();
        for (Argument argument : arguments) {
            String argumentName = argument.getName();
            Value argumentValue = boundInputs.get(argumentName);
            argumentsForEvent.put(argumentName, argumentValue);
        }
        fireEvent(
                executionRuntimeServices,
                runEnv, ScoreLangConstants.EVENT_ARGUMENT_END,
                description,
                LanguageEventData.StepType.STEP,
                stepName,
                context,
                Pair.of(LanguageEventData.BOUND_ARGUMENTS, (Serializable) argumentsForEvent)
        );
    }

    protected void updateCallArgumentsAndPushContextToStack(RunEnvironment runEnvironment, Context currentContext,
                                                            Map<String, Value> callArguments) {
        ContextStack contextStack = runEnvironment.getStack();
        contextStack.pushContext(currentContext);
        runEnvironment.putCallArguments(callArguments);
    }

    protected ResultNavigation getResultNavigation(Map<String, ResultNavigation> stepNavigationValues,
                                                   String nodeName, ReturnValues executableReturnValues,
                                                   String executableResult) {
        final ResultNavigation navigation = stepNavigationValues.get(executableResult);
        if (navigation == null) {
            // should always have the executable response mapped to a navigation by the step,
            // if not, it is an error
            throw new RuntimeException("Step: " + nodeName +
                    " has no matching navigation for the executable result: " +
                    executableReturnValues.getResult());
        }
        return navigation;
    }


}
