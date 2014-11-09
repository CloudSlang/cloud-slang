package com.hp.score.lang.runtime.steps;

import com.hp.score.lang.ExecutionRuntimeServices;
import com.hp.score.lang.runtime.env.ContextStack;
import com.hp.score.lang.runtime.env.ReturnValues;
import com.hp.score.lang.runtime.env.RunEnvironment;
import com.hp.score.lang.runtime.events.LanguageEventData;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static com.hp.score.lang.entities.ScoreLangConstants.*;
import static com.hp.score.lang.runtime.events.LanguageEventData.*;

public abstract class AbstractSteps {

    protected Map<String, Serializable> createBindInputsMap(Map<String, Serializable> callArguments, Map<String, Serializable> inputs, ExecutionRuntimeServices executionRuntimeServices, RunEnvironment runEnv) {
        fireEvent(executionRuntimeServices, runEnv, EVENT_INPUT_START, "Start binding inputs", Pair.of(CALL_ARGUMENTS, (Serializable)callArguments), Pair.of(INPUTS, (Serializable)inputs));
        Map<String, Serializable> tempContext = new LinkedHashMap<>();
        if (MapUtils.isEmpty(inputs)) return tempContext;
        for (Map.Entry<String, Serializable> input : inputs.entrySet()) {
            String inputKey = input.getKey();
            Serializable value = input.getValue();
            if (value != null) {
                if (value instanceof String) {
                    // assigning from another param
                    String paramName = (String) value;
                    Serializable callArgument = callArguments.get(paramName);
                    tempContext.put(inputKey, callArgument == null ? value : callArgument);
                } else {
                    tempContext.put(inputKey, value);
                }
            } else {
                tempContext.put(inputKey, callArguments.get(inputKey));
            }
        }
        fireEvent(executionRuntimeServices, runEnv, EVENT_INPUT_END, "Input binding finished", Pair.of(BOUND_INPUTS, (Serializable)tempContext));
        return tempContext;
    }

    protected void updateCallArgumentsAndPushContextToStack(RunEnvironment runEnvironment, Map<String, Serializable> currentContext, Map<String, Serializable> callArguments) {
        printMap(currentContext, "currentContext");
        printMap(callArguments, "callArguments");
        ContextStack contextStack = runEnvironment.getStack();
        contextStack.pushContext(currentContext);
        updateCallArguments(runEnvironment, callArguments);
    }

    protected void printMap(Map<String, Serializable> map, String label) {
//        if (MapUtils.isEmpty(map)) return;
//        MapUtils.debugPrint(System.out, label, map);
//        System.out.println("---------------------");
    }

    private void updateCallArguments(RunEnvironment runEnvironment, Map<String, Serializable> newContext) {
        //TODO: put a deep clone of the new context
        runEnvironment.putCallArguments(newContext);
    }

    protected void printReturnValues(ReturnValues returnValues) {
//        if (returnValues == null) return;
//        MapUtils.debugPrint(System.out, "Return Values", returnValues.getOutputs());
//        System.out.println("Result: " + returnValues.getResult());
    }

    @SafeVarargs
    protected static void fireEvent(ExecutionRuntimeServices runtimeServices, RunEnvironment runEnvironment, String type, String description, Map.Entry<String, ? extends Serializable>... fields) {
        LanguageEventData eventData = new LanguageEventData();
        eventData.setEventType(type);
        eventData.setDescription(description);
        eventData.setTimeStamp(new Date());
        eventData.setExecutionId(runtimeServices.getExecutionId());
        eventData.put(LanguageEventData.PATH, runEnvironment.getExecutionPath().getCurrentPath());
        for(Entry<String, ? extends Serializable> field : fields) {
            eventData.put(field.getKey(), field.getValue());
        }
        runtimeServices.addEvent(type, eventData);
    }

}
