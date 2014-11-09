package com.hp.score.lang.runtime.steps;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.score.lang.ExecutionRuntimeServices;
import com.hp.score.lang.entities.bindings.Result;
import com.hp.score.lang.runtime.bindings.ResultsBinding;
import com.hp.score.lang.runtime.env.ReturnValues;
import com.hp.score.lang.runtime.env.RunEnvironment;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import static com.hp.score.lang.entities.ScoreLangConstants.*;
import static com.hp.score.lang.runtime.events.LanguageEventData.*;
import static com.hp.score.api.execution.ExecutionParametersConsts.*;

/**
 * User: stoneo
 * Date: 02/11/2014
 * Time: 10:24
 */
//todo: decide on a name that is suitable for both flow & operation
@Component
public class OperationSteps extends AbstractSteps {

    @Autowired
    private ResultsBinding resultsBinding;

    public void start(@Param(OPERATION_INPUTS_KEY) LinkedHashMap<String, Serializable> operationInputs,
                      @Param(RUN_ENV) RunEnvironment runEnv,
                      @Param(USER_INPUTS_KEY) HashMap<String, Serializable> userInputs,
                      @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices) {

        System.out.println("=======");
        System.out.println(" start ");
        System.out.println("=======");
        runEnv.getExecutionPath().down();
        Map<String, Serializable> operationContext = new HashMap<>();

        resolveGroups();

        Map<String, Serializable> callArguments = runEnv.removeCallArguments();
        callArguments.putAll(userInputs);

        Map<String, Serializable> actionArguments = createBindInputsMap(callArguments, operationInputs, executionRuntimeServices, runEnv);

        //todo: clone action context before updating
        operationContext.putAll(actionArguments);

        //done with the user inputs, don't want it to be available in next start steps..
        userInputs.clear();

        //todo: hook

        updateCallArgumentsAndPushContextToStack(runEnv, operationContext, actionArguments);

    }

    /**
     * This method is executed by the end execution step of an operation/flow
     *
     * @param runEnv the run environment object
     * @param operationOutputs the operation outputs data
     * @param operationResults the operation results data
     * @param executionRuntimeServices services supplied by score engine for handling the execution
     */
    public void end(@Param(RUN_ENV) RunEnvironment runEnv,
                    @Param(OPERATION_OUTPUTS_KEY) LinkedHashMap<String, Serializable> operationOutputs,
                    @Param(OPERATION_RESULTS_KEY) LinkedList<Result> operationResults,
                    @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices) {

        System.out.println("=====");
        System.out.println(" end ");
        System.out.println("=====");
        Map<String, Serializable> operationContext = runEnv.getStack().popContext();
        ReturnValues actionReturnValues = runEnv.removeReturnValues();
        fireEvent(executionRuntimeServices, runEnv, EVENT_OUTPUT_START, "Output binding started", Pair.of("operationOutputs", operationOutputs), Pair.of("operationResults", operationResults), Pair.of("actionReturnValues", actionReturnValues));

        // Resolving the result of the operation/flow
        String result = actionReturnValues.getResult();
        if(result == null) {
            result = resultsBinding.resolveResult(actionReturnValues.getOutputs(), operationResults);
        }

        Map<String, String> operationReturnOutputs = createOperationBindOutputsContext(operationContext, actionReturnValues.getOutputs(), operationOutputs);

        //todo: hook

        ReturnValues returnValues = new ReturnValues(operationReturnOutputs, result);
        runEnv.putReturnValues(returnValues);
        fireEvent(executionRuntimeServices, runEnv, EVENT_OUTPUT_END, "Output binding finished", Pair.of(RETURN_VALUES, returnValues));
        runEnv.getExecutionPath().up();
        printReturnValues(returnValues);
    }

    private void resolveGroups() {
    }

    private Map<String, String> createOperationBindOutputsContext(Map<String, Serializable> context,
                                                                  Map<String, String> retValue,
                                                                  Map<String, Serializable> outputs) {
        Map<String, String> tempContext = new LinkedHashMap<>();
        if (outputs != null) {
            for (Map.Entry<String, Serializable> output : outputs.entrySet()) {
                String outputKey = output.getKey();
                Serializable outputValue = output.getValue();
                String outputRetValue = null;
                if (outputValue != null) {
                    if (outputValue instanceof String) {
                        // assigning from another param
                        String paramName = (String) outputValue;
                        // TODO: missing - evaluate script
                        outputRetValue = retValue.get(getRetValueKey(paramName));
                        if (outputRetValue == null)
                            outputRetValue = paramName;
                    }
                } else {
                    outputRetValue = (String) context.get(outputKey);
                }
                tempContext.put(outputKey, outputRetValue);
            }
        }
        return tempContext;
    }

    private String getRetValueKey(String outputValue) {
        return outputValue;
        //todo: temp solution. currently removing the prefix of retVal[ and suffix of ]
//        return outputValue.substring(7, outputValue.length() - 1);
    }

}
