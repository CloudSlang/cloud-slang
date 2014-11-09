package com.hp.score.lang.runtime.steps;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.score.lang.ExecutionRuntimeServices;
import com.hp.score.lang.entities.ScoreLangConstants;
import com.hp.score.lang.runtime.env.ReturnValues;
import com.hp.score.lang.runtime.env.RunEnvironment;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.hp.score.lang.entities.ScoreLangConstants.*;
import static com.hp.score.lang.runtime.events.LanguageEventData.RETURN_VALUES;
import static com.hp.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;

/**
 * User: stoneo
 * Date: 02/11/2014
 * Time: 10:23
 */
@Component
public class TaskSteps extends AbstractSteps {

    public void beginTask(@Param(ScoreLangConstants.TASK_INPUTS_KEY) LinkedHashMap<String, Serializable> taskInputs,
                          @Param(RUN_ENV) RunEnvironment runEnv,
                          @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices) {

        System.out.println("===========");
        System.out.println(" beginTask ");
        System.out.println("===========");
        runEnv.getExecutionPath().forward();
        runEnv.removeCallArguments();
        runEnv.removeReturnValues();

        Map<String, Serializable> flowContext = runEnv.getStack().popContext();

        Map<String, Serializable> operationArguments = createBindInputsMap(flowContext, taskInputs, executionRuntimeServices, runEnv);

        //todo: hook

        updateCallArgumentsAndPushContextToStack(runEnv, flowContext, operationArguments);
    }

    public void finishTask(@Param(RUN_ENV) RunEnvironment runEnv,
                           @Param(ScoreLangConstants.TASK_PUBLISH_KEY) LinkedHashMap<String, Serializable> taskPublishValues,
                           @Param(ScoreLangConstants.TASK_NAVIGATION_KEY) LinkedHashMap<String, Long> taskNavigationValues,
                           @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices) {

        System.out.println("============");
        System.out.println(" finishTask ");
        System.out.println("============");

        Map<String, Serializable> flowContext = runEnv.getStack().popContext();

        ReturnValues operationReturnValues = runEnv.removeReturnValues();
        List<String> eventFields = Arrays.asList("taskPublishValues", "taskNavigationValues", "operationReturnValues");
        List<Serializable> eventValues = Arrays.asList(taskPublishValues, taskNavigationValues, operationReturnValues);
        fireEvent(executionRuntimeServices, runEnv, EVENT_OUTPUT_START, "Output binding started", Pair.of("taskPublishValues", taskPublishValues), Pair.of("taskNavigationValues", taskNavigationValues), Pair.of("operationReturnValues", operationReturnValues));
        Map<String, Serializable> publishValues = createBindOutputsContext(operationReturnValues.getOutputs(), taskPublishValues);
        flowContext.putAll(publishValues);
        printMap(flowContext, "flowContext");

        //todo: hook

        Long nextPosition = calculateNextPosition(operationReturnValues.getResult(), taskNavigationValues);
        runEnv.putNextStepPosition(nextPosition);
        ReturnValues returnValues = new ReturnValues(new HashMap<String, String>(), operationReturnValues.getResult());
        runEnv.putReturnValues(returnValues);
        fireEvent(executionRuntimeServices, runEnv, EVENT_OUTPUT_END, "Output binding finished", Pair.of(RETURN_VALUES, returnValues), Pair.of("nextPosition", nextPosition));
        printReturnValues(returnValues);
        System.out.println("next position: " + nextPosition);

        runEnv.getStack().pushContext(flowContext);
    }

    private Map<String, Serializable> createBindOutputsContext(Map<String, String> operationResultContext, LinkedHashMap<String, Serializable> taskOutputs) {
        Map<String, Serializable> tempContext = new LinkedHashMap<>();
        if (taskOutputs != null) {
            for (Map.Entry<String, Serializable> output : taskOutputs.entrySet()) {
                String outputKey = output.getKey();
                Serializable outputValue = output.getValue();
                String outputRetValue = null;
                if (outputValue != null) {
                    if (outputValue instanceof String) {
                        // assigning from another param
                        String paramName = (String) outputValue;
                        // TODO: missing - evaluate script
                        outputRetValue = operationResultContext.get(paramName);
                        if (outputRetValue == null)
                            outputRetValue = paramName;
                    } else {
                        tempContext.put(outputKey, outputValue);
                    }
                } else {
                    outputRetValue = operationResultContext.get(outputKey);
                }
                tempContext.put(outputKey, outputRetValue);
            }
        }
        return tempContext;
    }

    private Long calculateNextPosition(String result, LinkedHashMap<String, Long> taskNavigationValues) {
        //todo: implement
        return taskNavigationValues.get(result);
    }

}
