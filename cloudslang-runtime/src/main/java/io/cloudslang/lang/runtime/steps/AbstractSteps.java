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

import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.runtime.env.*;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import org.apache.commons.lang3.tuple.Pair;
import io.cloudslang.score.lang.ExecutionRuntimeServices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractSteps {

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
                                          final Map<String, Serializable> context,
                                          RunEnvironment runEnv,
                                          ExecutionRuntimeServices executionRuntimeServices,
                                          String desc,
                                          LanguageEventData.StepType stepType,
                                          String stepName) {
        Map<String, Serializable> inputsForEvent = new HashMap<>();
        for (Input input : inputs) {
            String inputName = input.getName();
            Serializable inputValue = context.get(inputName);
            if (input.isEncrypted()) {
                inputsForEvent.put(inputName, LanguageEventData.ENCRYPTED_VALUE);
            } else {
                inputsForEvent.put(inputName, inputValue);
            }
        }
        fireEvent(executionRuntimeServices, runEnv, ScoreLangConstants.EVENT_INPUT_END, desc, stepType, stepName,
                Pair.of(LanguageEventData.BOUND_INPUTS, (Serializable) inputsForEvent));
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
            eventData.put(field.getKey(), field.getValue());
        }
        runtimeServices.addEvent(type, eventData);
    }

    protected void updateCallArgumentsAndPushContextToStack(RunEnvironment runEnvironment, Context currentContext, Map<String, Serializable> callArguments) {
        ContextStack contextStack = runEnvironment.getStack();
        contextStack.pushContext(currentContext);
        //TODO: put a deep clone of the new context
        runEnvironment.putCallArguments(callArguments);
    }

    protected void pushParentFlowDataOnStack(RunEnvironment runEnv, Long RUNNING_EXECUTION_PLAN_ID, Long nextStepId) {
        // create ParentFlowData object containing the current running execution plan id and
        // the next step id to navigate to in the current execution plan,
        // and push it to the ParentFlowStack for future use (once we finish running the ref operation/flow)
        ParentFlowStack stack = runEnv.getParentFlowStack();
        stack.pushParentFlowData(new ParentFlowData(RUNNING_EXECUTION_PLAN_ID, nextStepId));
    }

}
