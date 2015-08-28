/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.runtime.steps;

import com.hp.oo.sdk.content.annotations.Param;

import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.runtime.bindings.InputsBinding;
import io.cloudslang.lang.runtime.bindings.OutputsBinding;
import io.cloudslang.lang.runtime.bindings.ResultsBinding;
import io.cloudslang.lang.runtime.env.Context;
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import org.apache.log4j.Logger;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.runtime.env.ParentFlowData;
import org.apache.commons.lang3.tuple.Pair;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.cloudslang.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;

/**
 * User: stoneo
 * Date: 02/11/2014
 * Time: 10:24
 */
@Component
public class ExecutableSteps extends AbstractSteps {

    public static final String ACTION_RETURN_VALUES_KEY = "actionReturnValues";

    @Autowired
    private ResultsBinding resultsBinding;

    @Autowired
    private InputsBinding inputsBinding;

    @Autowired
    private OutputsBinding outputsBinding;

    private static final Logger logger = Logger.getLogger(ExecutableSteps.class);

    public void startExecutable(@Param(ScoreLangConstants.EXECUTABLE_INPUTS_KEY) List<Input> executableInputs,
                                @Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                                @Param(ScoreLangConstants.USER_INPUTS_KEY) Map<String, ? extends Serializable> userInputs,
                                @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                                @Param(ScoreLangConstants.NODE_NAME_KEY) String nodeName,
                                @Param(ScoreLangConstants.NEXT_STEP_ID_KEY) Long nextStepId) {
        try {
            Map<String, Serializable> callArguments = runEnv.removeCallArguments();

            if (userInputs != null) {
                callArguments.putAll(userInputs);
            }
            sendStartBindingInputsEvent(executableInputs, runEnv, executionRuntimeServices,
                    "Pre Input binding for operation/flow", LanguageEventData.StepType.EXECUTABLE, nodeName);

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

            sendEndBindingInputsEvent(executableInputs, executableContext, runEnv, executionRuntimeServices,
                    "Post Input binding for operation/flow", LanguageEventData.StepType.EXECUTABLE, nodeName);

            // put the next step position for the navigation
            runEnv.putNextStepPosition(nextStepId);
            runEnv.getExecutionPath().down();
        } catch (RuntimeException e){
            logger.error("There was an error running the start executable execution step of: \'" + nodeName + "\'.\n\tError is: " + e.getMessage());
            throw new RuntimeException("Error running: \'" + nodeName + "\'.\n\t " + e.getMessage(), e);
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
    public void finishExecutable(@Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                                 @Param(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY) List<Output> executableOutputs,
                                 @Param(ScoreLangConstants.EXECUTABLE_RESULTS_KEY) List<Result> executableResults,
                                 @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                                 @Param(ScoreLangConstants.NODE_NAME_KEY) String nodeName) {
		try {
            runEnv.getExecutionPath().up();
            Context operationContext = runEnv.getStack().popContext();
            Map<String, Serializable> operationVariables = operationContext == null ? null : operationContext.getImmutableViewOfVariables();
            ReturnValues actionReturnValues = runEnv.removeReturnValues();
            fireEvent(executionRuntimeServices, runEnv, ScoreLangConstants.EVENT_OUTPUT_START, "Output binding started",
                    LanguageEventData.StepType.EXECUTABLE, nodeName,
                    Pair.of(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY, (Serializable) executableOutputs),
                    Pair.of(ScoreLangConstants.EXECUTABLE_RESULTS_KEY, (Serializable) executableResults),
                    Pair.of(ACTION_RETURN_VALUES_KEY, actionReturnValues));

            // Resolving the result of the operation/flow
            String result = resultsBinding.resolveResult(operationVariables, actionReturnValues.getOutputs(), executableResults, actionReturnValues.getResult());

            Map<String, Serializable> operationReturnOutputs = outputsBinding.bindOutputs(operationVariables, actionReturnValues.getOutputs(), executableOutputs);

            //todo: hook

            ReturnValues returnValues = new ReturnValues(operationReturnOutputs, result);
            runEnv.putReturnValues(returnValues);
            fireEvent(executionRuntimeServices, runEnv, ScoreLangConstants.EVENT_OUTPUT_END, "Output binding finished",
                    LanguageEventData.StepType.EXECUTABLE, nodeName,
                    Pair.of(LanguageEventData.OUTPUTS, (Serializable) operationReturnOutputs),
                    Pair.of(LanguageEventData.RESULT, returnValues.getResult()));

            // If we have parent flow data on the stack, we pop it and request the score engine to switch to the parent
            // execution plan id once it can, and we set the next position that was stored there for the use of the navigation
            if (!runEnv.getParentFlowStack().isEmpty()) {
                handleNavigationToParent(runEnv, executionRuntimeServices);
            } else {
                fireEvent(executionRuntimeServices, runEnv, ScoreLangConstants.EVENT_EXECUTION_FINISHED,
                        "Execution finished running", LanguageEventData.StepType.EXECUTABLE, nodeName,
                        Pair.of(LanguageEventData.RESULT, returnValues.getResult()),
                        Pair.of(LanguageEventData.OUTPUTS, (Serializable) operationReturnOutputs));
            }
        } catch (RuntimeException e){
            logger.error("There was an error running the finish executable execution step of: \'" + nodeName + "\'.\n\tError is: " + e.getMessage());
            throw new RuntimeException("Error running: \'" + nodeName + "\'.\n\t" + e.getMessage(), e);
        }
    }

    private void handleNavigationToParent(RunEnvironment runEnv, ExecutionRuntimeServices executionRuntimeServices) {
        ParentFlowData parentFlowData = runEnv.getParentFlowStack().popParentFlowData();
        executionRuntimeServices.requestToChangeExecutionPlan(parentFlowData.getRunningExecutionPlanId());
        runEnv.putNextStepPosition(parentFlowData.getPosition());
    }

}
