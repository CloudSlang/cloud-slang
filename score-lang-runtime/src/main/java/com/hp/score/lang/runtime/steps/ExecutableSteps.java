package com.hp.score.lang.runtime.steps;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.score.lang.ExecutionRuntimeServices;
import com.hp.score.lang.entities.ScoreLangConstants;
import com.hp.score.lang.entities.bindings.Input;
import com.hp.score.lang.entities.bindings.Output;
import com.hp.score.lang.entities.bindings.Result;
import com.hp.score.lang.runtime.bindings.InputsBinding;
import com.hp.score.lang.runtime.bindings.OutputsBinding;
import com.hp.score.lang.runtime.bindings.ResultsBinding;
import com.hp.score.lang.runtime.env.ReturnValues;
import com.hp.score.lang.runtime.env.RunEnvironment;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;

import static com.hp.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;
import static com.hp.score.lang.entities.ScoreLangConstants.*;
import static com.hp.score.lang.runtime.events.LanguageEventData.*;

/**
 * User: stoneo
 * Date: 02/11/2014
 * Time: 10:24
 */
@Component
public class ExecutableSteps extends AbstractSteps {

    @Autowired
    private ResultsBinding resultsBinding;

    @Autowired
    private InputsBinding inputsBinding;

    @Autowired
    private OutputsBinding outputsBinding;

    public void startExecutable(@Param(OPERATION_INPUTS_KEY) List<Input> operationInputs,
                                @Param(RUN_ENV) RunEnvironment runEnv,
                                @Param(USER_INPUTS_KEY) HashMap<String, Serializable> userInputs,
                                @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices) {

        System.out.println("=================");
        System.out.println(" startExecutable ");
        System.out.println("=================");
        runEnv.getExecutionPath().down();

        resolveGroups();

        Map<String, Serializable> callArguments = runEnv.removeCallArguments();

        if(userInputs != null) {
            callArguments.putAll(userInputs);
        }
        Map<String, Serializable>  operationContext = inputsBinding.bindInputs(callArguments,operationInputs);

        Map<String, Serializable> actionArguments = new HashMap<>();

        //todo: clone action context before updating
        actionArguments.putAll(operationContext);

        //done with the user inputs, don't want it to be available in next startExecutable steps..
        userInputs.clear();

        //todo: hook

        updateCallArgumentsAndPushContextToStack(runEnv, operationContext, actionArguments);

        sendBindingInputsEvent(operationInputs, operationContext, runEnv, executionRuntimeServices,"Post Input binding for operation/flow");
    }

    /**
     * This method is executed by the finishExecutable execution step of an operation/flow
     *
     * @param runEnv the run environment object
     * @param executableOutputs the operation outputs data
     * @param executableResults the operation results data
     * @param executionRuntimeServices services supplied by score engine for handling the execution
     */
    public void finishExecutable(@Param(RUN_ENV) RunEnvironment runEnv,
                                 @Param(EXECUTABLE_OUTPUTS_KEY) List<Output> executableOutputs,
                                 @Param(EXECUTABLE_RESULTS_KEY) List<Result> executableResults,
                                 @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices) {

        System.out.println("===============");
        System.out.println(" finishExecutable ");
        System.out.println("===============");
        Map<String, Serializable> operationContext = runEnv.getStack().popContext();
        ReturnValues actionReturnValues = runEnv.removeReturnValues();
        fireEvent(executionRuntimeServices, runEnv, EVENT_OUTPUT_START, "Output binding started",
                Pair.of(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY, (Serializable) executableOutputs),
                Pair.of(ScoreLangConstants.EXECUTABLE_RESULTS_KEY, (Serializable)executableResults),
                Pair.of("actionReturnValues", actionReturnValues));

        // Resolving the result of the operation/flow
        String result = resultsBinding.resolveResult(actionReturnValues.getOutputs(), executableResults, actionReturnValues.getResult());

        Map<String, String> operationReturnOutputs = outputsBinding.bindOutputs(operationContext, actionReturnValues.getOutputs(), executableOutputs);

        //todo: hook

        ReturnValues returnValues = new ReturnValues(operationReturnOutputs, result);
        runEnv.putReturnValues(returnValues);
        fireEvent(executionRuntimeServices, runEnv, EVENT_OUTPUT_END, "Output binding finished", Pair.of(RETURN_VALUES, returnValues));
        runEnv.getExecutionPath().up();
        printReturnValues(returnValues);
    }

    private void resolveGroups() {
    }

}
