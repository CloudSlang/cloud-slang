/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package com.hp.score.lang.runtime.steps;

import org.eclipse.score.lang.ExecutionRuntimeServices;
import com.hp.score.lang.entities.bindings.Input;
import com.hp.score.lang.runtime.env.ContextStack;
import com.hp.score.lang.runtime.env.RunEnvironment;
import com.hp.score.lang.runtime.events.LanguageEventData;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_INPUT_END;
import static com.hp.score.lang.runtime.events.LanguageEventData.BOUND_INPUTS;
import static com.hp.score.lang.runtime.events.LanguageEventData.ENCRYPTED_VALUE;

public abstract class AbstractSteps {

    public void sendBindingInputsEvent(List<Input> inputs, final Map<String, Serializable> context, RunEnvironment runEnv,
                                       ExecutionRuntimeServices executionRuntimeServices, String desc, String nodeName,
                                       LanguageEventData.levelName levelName) {
        Map<String, Serializable> inputsForEvent = new HashMap<>();
        for (Input input : inputs) {
            String inputName = input.getName();
            Serializable inputValue = context.get(inputName);
            if (input.isEncrypted()) {
                inputsForEvent.put(inputName, ENCRYPTED_VALUE);
            } else {
                inputsForEvent.put(inputName, inputValue);
            }
        }
        fireEvent(executionRuntimeServices, runEnv, EVENT_INPUT_END, desc, Pair.of(BOUND_INPUTS,
                (Serializable) inputsForEvent), Pair.of(levelName.name(), nodeName));
    }

    protected void updateCallArgumentsAndPushContextToStack(RunEnvironment runEnvironment, Map<String, Serializable> currentContext, Map<String, Serializable> callArguments) {
        ContextStack contextStack = runEnvironment.getStack();
        contextStack.pushContext(currentContext);
        updateCallArguments(runEnvironment, callArguments);
    }

    private void updateCallArguments(RunEnvironment runEnvironment, Map<String, Serializable> newContext) {
        //TODO: put a deep clone of the new context
        runEnvironment.putCallArguments(newContext);
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
