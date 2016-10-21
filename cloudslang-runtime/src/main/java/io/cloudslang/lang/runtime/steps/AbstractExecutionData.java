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

import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.runtime.env.Context;
import io.cloudslang.lang.runtime.env.ContextStack;
import io.cloudslang.lang.runtime.env.ParentFlowData;
import io.cloudslang.lang.runtime.env.ParentFlowStack;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.tuple.Pair;

public abstract class AbstractExecutionData {

    public void sendStartBindingInputsEvent(List<Input> inputs,
                                            RunEnvironment runEnv,
                                            ExecutionRuntimeServices executionRuntimeServices,
                                            String desc,
                                            LanguageEventData.StepType stepType,
                                            String stepName) {
        ArrayList<String> inputNames = new ArrayList<>();
        for (Input input : inputs) {
            inputNames.add(input.getName());
        }
        fireEvent(executionRuntimeServices, runEnv, ScoreLangConstants.EVENT_INPUT_START, desc, stepType, stepName,
                Pair.of(LanguageEventData.INPUTS, inputNames));
    }

    public void sendEndBindingInputsEvent(List<Input> inputs,
                                          final Map<String, Value> context,
                                          RunEnvironment runEnv,
                                          ExecutionRuntimeServices executionRuntimeServices,
                                          String desc,
                                          LanguageEventData.StepType stepType,
                                          String stepName) {
        Map<String, Value> inputsForEvent = new LinkedHashMap<>();
        for (Input input : inputs) {
            String inputName = input.getName();
            Value inputValue = context.get(inputName);
            inputsForEvent.put(inputName, inputValue);
        }
        fireEvent(executionRuntimeServices, runEnv, ScoreLangConstants.EVENT_INPUT_END, desc, stepType, stepName,
                Pair.of(LanguageEventData.BOUND_INPUTS, (Serializable) inputsForEvent));
    }

    public void sendStartBindingArgumentsEvent(
            List<Argument> arguments,
            RunEnvironment runEnv,
            ExecutionRuntimeServices executionRuntimeServices,
            String description,
            String stepName) {
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
                Pair.of(LanguageEventData.ARGUMENTS, argumentNames)
        );
    }

    public void sendEndBindingArgumentsEvent(
            List<Argument> arguments,
            final Map<String, Value> context,
            RunEnvironment runEnv,
            ExecutionRuntimeServices executionRuntimeServices,
            String description,
            String stepName) {
        Map<String, Value> argumentsForEvent = new LinkedHashMap<>();
        for (Argument argument : arguments) {
            String argumentName = argument.getName();
            Value argumentValue = context.get(argumentName);
            argumentsForEvent.put(argumentName, argumentValue);
        }
        fireEvent(
                executionRuntimeServices,
                runEnv, ScoreLangConstants.EVENT_ARGUMENT_END,
                description,
                LanguageEventData.StepType.STEP,
                stepName,
                Pair.of(LanguageEventData.BOUND_ARGUMENTS, (Serializable) argumentsForEvent)
        );
    }

    @SafeVarargs
    public static void fireEvent(ExecutionRuntimeServices runtimeServices,
                                 RunEnvironment runEnvironment,
                                 String type,
                                 String description,
                                 LanguageEventData.StepType stepType,
                                 String stepName,
                                 Map.Entry<String, ? extends Serializable>... fields) {
        fireEvent(runtimeServices, type, description,
                runEnvironment.getExecutionPath().getCurrentPath(), stepType, stepName, fields);
    }

    @SafeVarargs
    public static void fireEvent(ExecutionRuntimeServices runtimeServices,
                                 String type,
                                 String description,
                                 String path,
                                 LanguageEventData.StepType stepType,
                                 String stepName,
                                 Map.Entry<String, ? extends Serializable>... fields) {
        LanguageEventData eventData = new LanguageEventData();
        eventData.setStepType(stepType);
        eventData.setStepName(stepName);
        eventData.setEventType(type);
        eventData.setDescription(description);
        eventData.setTimeStamp(new Date());
        eventData.setExecutionId(runtimeServices.getExecutionId());
        eventData.setPath(path);
        for (Entry<String, ? extends Serializable> field : fields) {
            //noinspection unchecked
            eventData.put(field.getKey(), LanguageEventData.maskSensitiveValues(field.getValue()));
        }
        runtimeServices.addEvent(type, eventData);
    }

    protected void updateCallArgumentsAndPushContextToStack(RunEnvironment runEnvironment, Context currentContext,
                                                            Map<String, Value> callArguments) {
        ContextStack contextStack = runEnvironment.getStack();
        contextStack.pushContext(currentContext);
        runEnvironment.putCallArguments(callArguments);
    }

    protected void pushParentFlowDataOnStack(RunEnvironment runEnv, Long runningExecutionPlanId, Long nextStepId) {
        // create ParentFlowData object containing the current running execution plan id and
        // the next step id to navigate to in the current execution plan,
        // and push it to the ParentFlowStack for future use (once we finish running the ref operation/flow)
        ParentFlowStack stack = runEnv.getParentFlowStack();
        stack.pushParentFlowData(new ParentFlowData(runningExecutionPlanId, nextStepId));
    }

}
