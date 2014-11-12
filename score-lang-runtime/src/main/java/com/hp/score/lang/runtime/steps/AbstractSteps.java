package com.hp.score.lang.runtime.steps;

import com.hp.score.lang.ExecutionRuntimeServices;
import com.hp.score.lang.entities.bindings.Input;
import com.hp.score.lang.runtime.env.ContextStack;
import com.hp.score.lang.runtime.env.ReturnValues;
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

    public void sendBindingInputsEvent(List<Input> inputs, final Map<String, Serializable> context,
                                            RunEnvironment runEnv, ExecutionRuntimeServices executionRuntimeServices,String desc) {
        Map<String,Serializable> inputsForEvent = new HashMap<>();
        for(Input input: inputs){
            String inputName = input.getName();
            Serializable inputValue = context.get(inputName);
            if(input.isEncrypted()){
                inputsForEvent.put(inputName, ENCRYPTED_VALUE);
            }
            else{
                inputsForEvent.put(inputName, inputValue);
            }
        }
        fireEvent(executionRuntimeServices, runEnv, EVENT_INPUT_END, desc, Pair.of(BOUND_INPUTS, (Serializable)inputsForEvent));
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
        for(Entry<String, ? extends Serializable> field : fields) {
            eventData.put(field.getKey(), field.getValue());
        }
        runtimeServices.addEvent(type, eventData);
    }

}
