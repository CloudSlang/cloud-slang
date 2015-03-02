package org.openscore.lang.runtime.steps;

import com.hp.oo.sdk.content.annotations.Param;

import org.apache.log4j.Logger;
import org.openscore.lang.entities.ScoreLangConstants;
import org.openscore.lang.entities.bindings.Input;
import org.openscore.lang.entities.bindings.Output;
import org.openscore.lang.entities.bindings.Result;
import org.openscore.lang.runtime.bindings.InputsBinding;
import org.openscore.lang.runtime.bindings.OutputsBinding;
import org.openscore.lang.runtime.bindings.ResultsBinding;
import org.openscore.lang.runtime.env.Context;
import org.openscore.lang.runtime.env.ExecutionPath;
import org.openscore.lang.runtime.env.ParentFlowData;
import org.openscore.lang.runtime.env.ReturnValues;
import org.openscore.lang.runtime.env.RunEnvironment;
import org.apache.commons.lang3.tuple.Pair;
import org.openscore.lang.ExecutionRuntimeServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openscore.lang.entities.ScoreLangConstants.*;
import static org.openscore.lang.runtime.events.LanguageEventData.OUTPUTS;
import static org.openscore.lang.runtime.events.LanguageEventData.RESULT;
import static org.openscore.lang.runtime.events.LanguageEventData.levelName;
import static org.openscore.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;

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

    private static final Logger logger = Logger.getLogger(ExecutableSteps.class);

    public void startExecutable(@Param(EXECUTABLE_INPUTS_KEY) List<Input> executableInputs,
                                @Param(RUN_ENV) RunEnvironment runEnv,
                                @Param(USER_INPUTS_KEY) Map<String, ? extends Serializable> userInputs,
                                @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                                @Param(NODE_NAME_KEY) String nodeName,
                                @Param(NEXT_STEP_ID_KEY) Long nextStepId) {
        try {
            runEnv.getExecutionPath()
                  .down();
            Map<String, Serializable> callArguments = runEnv.removeCallArguments();

            if (userInputs != null) {
                callArguments.putAll(userInputs);
            }
            Map<String, Serializable> executableContext = inputsBinding.bindInputs(executableInputs, callArguments, runEnv.getSystemProperties());

            Map<String, Serializable> actionArguments = new HashMap<>();

            //todo: clone action context before updating
            actionArguments.putAll(executableContext);

            //done with the user inputs, don't want it to be available in next startExecutable steps..
            if (userInputs != null) {
                userInputs.clear();
            }

            //todo: hook

            updateCallArgumentsAndPushContextToStack(runEnv, new Context(executableContext), actionArguments);

            sendBindingInputsEvent(executableInputs, executableContext, runEnv, executionRuntimeServices,
                    "Post Input binding for operation/flow", nodeName, levelName.EXECUTABLE_NAME);

            // put the next step position for the navigation
            runEnv.putNextStepPosition(nextStepId);
        } catch (RuntimeException e){
            logger.error("There was an error running the start executable execution step of: \'" + nodeName + "\'. Error is: " + e.getMessage());
            throw new RuntimeException("Error running: \'" + nodeName + "\': " + e.getMessage(), e);
        }
    }

    /**
     * This method is executed by the finishExecutable execution step of an operation or flow
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
		try {
            ExecutionPath executionPath = runEnv.getExecutionPath();
            executionPath.up();
            if (executionPath.getDepth() < 1) executionPath.down(); // In case we're at top level
            Context operationContext = runEnv.getStack().popContext();
            Map<String, Serializable> operationVariables = operationContext == null ? null : operationContext.getImmutableViewOfVariables();
            ReturnValues actionReturnValues = runEnv.removeReturnValues();
            fireEvent(executionRuntimeServices, runEnv, EVENT_OUTPUT_START, "Output binding started",
                    Pair.of(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY, (Serializable) executableOutputs),
                    Pair.of(ScoreLangConstants.EXECUTABLE_RESULTS_KEY, (Serializable) executableResults),
                    Pair.of("actionReturnValues", actionReturnValues), Pair.of(levelName.EXECUTABLE_NAME.toString(), nodeName));

            // Resolving the result of the operation/flow
            String result = resultsBinding.resolveResult(operationVariables, actionReturnValues.getOutputs(), executableResults, actionReturnValues.getResult());

            Map<String, Serializable> operationReturnOutputs = outputsBinding.bindOutputs(operationVariables, actionReturnValues.getOutputs(), executableOutputs);

            //todo: hook

            ReturnValues returnValues = new ReturnValues(operationReturnOutputs, result);
            runEnv.putReturnValues(returnValues);
            fireEvent(executionRuntimeServices, runEnv, EVENT_OUTPUT_END, "Output binding finished",
                    Pair.of(OUTPUTS, (Serializable) operationReturnOutputs),
                    Pair.of(RESULT, returnValues.getResult()),
                    Pair.of(levelName.EXECUTABLE_NAME.toString(), nodeName));

            // If we have parent flow data on the stack, we pop it and request the score engine to switch to the parent
            // execution plan id once it can, and we set the next position that was stored there for the use of the navigation
            if (!runEnv.getParentFlowStack().isEmpty()) {
                handleNavigationToParent(runEnv, executionRuntimeServices);
            } else {
                fireEvent(executionRuntimeServices, runEnv, EVENT_EXECUTION_FINISHED, "Execution finished running",
                        Pair.of(RESULT, returnValues.getResult()),
                        Pair.of(OUTPUTS, (Serializable) operationReturnOutputs),
                        Pair.of(levelName.EXECUTABLE_NAME.toString(), nodeName));
            }
        } catch (RuntimeException e){
            logger.error("There was an error running the finish executable execution step of: \'" + nodeName + "\'. Error is: " + e.getMessage());
            throw new RuntimeException("Error running: \'" + nodeName + "\': " + e.getMessage(), e);
        }
    }

    private void handleNavigationToParent(RunEnvironment runEnv, ExecutionRuntimeServices executionRuntimeServices) {
        ParentFlowData parentFlowData = runEnv.getParentFlowStack().popParentFlowData();
        executionRuntimeServices.requestToChangeExecutionPlan(parentFlowData.getRunningExecutionPlanId());
        runEnv.putNextStepPosition(parentFlowData.getPosition());
    }

}
