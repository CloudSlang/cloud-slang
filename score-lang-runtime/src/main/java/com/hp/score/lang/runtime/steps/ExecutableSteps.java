package com.hp.score.lang.runtime.steps;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.score.lang.entities.ScoreLangConstants;
import com.hp.score.lang.entities.bindings.Input;
import com.hp.score.lang.entities.bindings.Output;
import com.hp.score.lang.entities.bindings.Result;
import com.hp.score.lang.runtime.bindings.InputsBinding;
import com.hp.score.lang.runtime.bindings.OutputsBinding;
import com.hp.score.lang.runtime.bindings.ResultsBinding;
import com.hp.score.lang.runtime.env.ParentFlowData;
import com.hp.score.lang.runtime.env.ReturnValues;
import com.hp.score.lang.runtime.env.RunEnvironment;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.score.lang.ExecutionRuntimeServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_EXECUTION_FINISHED;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_OUTPUT_END;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_OUTPUT_START;
import static com.hp.score.lang.entities.ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY;
import static com.hp.score.lang.entities.ScoreLangConstants.EXECUTABLE_RESULTS_KEY;
import static com.hp.score.lang.entities.ScoreLangConstants.NEXT_STEP_ID_KEY;
import static com.hp.score.lang.entities.ScoreLangConstants.NODE_NAME_KEY;
import static com.hp.score.lang.entities.ScoreLangConstants.OPERATION_INPUTS_KEY;
import static com.hp.score.lang.entities.ScoreLangConstants.RUN_ENV;
import static com.hp.score.lang.entities.ScoreLangConstants.USER_INPUTS_KEY;
import static com.hp.score.lang.runtime.events.LanguageEventData.OUTPUTS;
import static com.hp.score.lang.runtime.events.LanguageEventData.RESULT;
import static com.hp.score.lang.runtime.events.LanguageEventData.levelName;
import static org.eclipse.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;

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
                                @Param(USER_INPUTS_KEY) Map<String, ? extends Serializable> userInputs,
                                @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                                @Param(NODE_NAME_KEY) String nodeName,
                                @Param(NEXT_STEP_ID_KEY) Long nextStepId) {

        runEnv.getExecutionPath().down();

        Map<String, Serializable> callArguments = runEnv.removeCallArguments();

        if(userInputs != null) {
            callArguments.putAll(userInputs);
        }
        Map<String, Serializable>  operationContext = inputsBinding.bindInputs(callArguments,operationInputs);

        Map<String, Serializable> actionArguments = new HashMap<>();

        //todo: clone action context before updating
        actionArguments.putAll(operationContext);

        //done with the user inputs, don't want it to be available in next startExecutable steps..
        if(userInputs != null) {
            userInputs.clear();
        }

        //todo: hook

        updateCallArgumentsAndPushContextToStack(runEnv, operationContext, actionArguments);

        sendBindingInputsEvent(operationInputs, operationContext, runEnv, executionRuntimeServices,
                "Post Input binding for operation/flow",nodeName, levelName.EXECUTABLE_NAME);

        // put the next step position for the navigation
        runEnv.putNextStepPosition(nextStepId);
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
                                 @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                                 @Param(NODE_NAME_KEY) String nodeName) {

        Map<String, Serializable> operationContext = runEnv.getStack().popContext();
        ReturnValues actionReturnValues = runEnv.removeReturnValues();
        fireEvent(executionRuntimeServices, runEnv, EVENT_OUTPUT_START, "Output binding started",
                Pair.of(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY, (Serializable) executableOutputs),
                Pair.of(ScoreLangConstants.EXECUTABLE_RESULTS_KEY, (Serializable)executableResults),
                Pair.of("actionReturnValues", actionReturnValues),Pair.of(levelName.EXECUTABLE_NAME.toString(),nodeName));

        // Resolving the result of the operation/flow
        String result = resultsBinding.resolveResult(operationContext, actionReturnValues.getOutputs(), executableResults, actionReturnValues.getResult());

        Map<String, String> operationReturnOutputs = outputsBinding.bindOutputs(operationContext, actionReturnValues.getOutputs(), executableOutputs);

        //todo: hook

        ReturnValues returnValues = new ReturnValues(operationReturnOutputs, result);
        runEnv.putReturnValues(returnValues);
        fireEvent(executionRuntimeServices, runEnv, EVENT_OUTPUT_END, "Output binding finished",
                Pair.of(OUTPUTS, (Serializable) operationReturnOutputs),
                Pair.of(RESULT, returnValues.getResult()),
                Pair.of(levelName.EXECUTABLE_NAME.toString(),nodeName));

        // If we have parent flow data on the stack, we pop it and request the score engine to switch to the parent
        // execution plan id once it can, and we set the next position that was stored there for the use of the navigation
        if(!runEnv.getParentFlowStack().isEmpty()) {
            handleNavigationToParent(runEnv, executionRuntimeServices);
        } else {
            fireEvent(executionRuntimeServices, runEnv, EVENT_EXECUTION_FINISHED, "Execution finished running",
                    Pair.of(RESULT, returnValues.getResult()),
                    Pair.of(OUTPUTS, (Serializable) operationReturnOutputs),
                    Pair.of(levelName.EXECUTABLE_NAME.toString(),nodeName));
        }

        runEnv.getExecutionPath().up();
    }

    private void handleNavigationToParent(RunEnvironment runEnv, ExecutionRuntimeServices executionRuntimeServices) {
        ParentFlowData parentFlowData = runEnv.getParentFlowStack().popParentFlowData();
        executionRuntimeServices.requestToChangeExecutionPlan(parentFlowData.getRunningExecutionPlanId());
        runEnv.putNextStepPosition(parentFlowData.getPosition());
    }

}
