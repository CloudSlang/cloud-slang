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

import org.openscore.lang.entities.bindings.Input;
import org.openscore.lang.runtime.env.Context;
import org.openscore.lang.runtime.env.ContextStack;
import org.openscore.lang.runtime.env.RunEnvironment;
import org.openscore.lang.runtime.events.LanguageEventData;
import org.apache.commons.lang3.tuple.Pair;
import org.openscore.lang.ExecutionRuntimeServices;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.openscore.lang.entities.ScoreLangConstants.EVENT_INPUT_END;

public abstract class AbstractSteps {

    public void sendBindingInputsEvent(List<Input> inputs, final Map<String, Serializable> context, RunEnvironment runEnv,
                                       ExecutionRuntimeServices executionRuntimeServices, String desc, String nodeName,
                                       LanguageEventData.levelName levelName) {
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
        fireEvent(executionRuntimeServices, runEnv, EVENT_INPUT_END, desc, Pair.of(LanguageEventData.BOUND_INPUTS,
                (Serializable) inputsForEvent), Pair.of(levelName.name(), nodeName));
    }

    protected void updateCallArgumentsAndPushContextToStack(RunEnvironment runEnvironment, Context currentContext, Map<String, Serializable> callArguments) {
        ContextStack contextStack = runEnvironment.getStack();
        contextStack.pushContext(currentContext);
        //TODO: put a deep clone of the new context
        runEnvironment.putCallArguments(callArguments);
    }

    @SafeVarargs
    public static void fireEvent(ExecutionRuntimeServices runtimeServices,
                                 RunEnvironment runEnvironment,
                                 String type,
                                 String description,
                                 Map.Entry<String, ? extends Serializable>... fields) {
        LanguageEventData eventData = new LanguageEventData();
        eventData.setEventType(type);
        eventData.setDescription(description);
        eventData.setTimeStamp(new Date());
        eventData.setExecutionId(runtimeServices.getExecutionId());
        eventData.setPath(runEnvironment.getExecutionPath().getCurrentPath());
        for (Entry<String, ? extends Serializable> field : fields) {
            eventData.put(field.getKey(), field.getValue());
        }
        runtimeServices.addEvent(type, eventData);
    }

}
