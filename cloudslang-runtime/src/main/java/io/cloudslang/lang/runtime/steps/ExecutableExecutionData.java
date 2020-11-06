/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.steps;

import com.google.common.collect.Sets;
import com.hp.oo.sdk.content.annotations.Param;
import io.cloudslang.lang.entities.ExecutableType;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.entities.bindings.prompt.Prompt;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.runtime.bindings.InputsBinding;
import io.cloudslang.lang.runtime.bindings.OutputsBinding;
import io.cloudslang.lang.runtime.bindings.ResultsBinding;
import io.cloudslang.lang.runtime.bindings.strategies.DebuggerBreakpointsHandler;
import io.cloudslang.lang.runtime.bindings.strategies.MissingInputHandler;
import io.cloudslang.lang.runtime.env.Context;
import io.cloudslang.lang.runtime.env.ParentFlowData;
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.api.execution.precondition.ExecutionPreconditionService;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import io.cloudslang.score.lang.SystemContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static io.cloudslang.lang.entities.ScoreLangConstants.USE_EMPTY_VALUES_FOR_PROMPTS_KEY;
import static io.cloudslang.lang.entities.ScoreLangConstants.WORKER_GROUP;
import static io.cloudslang.lang.entities.bindings.values.Value.toStringSafe;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.SYSTEM_CONTEXT;
import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * User: stoneo
 * Date: 02/11/2014
 * Time: 10:24
 */
@Component
public class ExecutableExecutionData extends AbstractExecutionData {

    private static final Logger logger = Logger.getLogger(ExecutableExecutionData.class);
    public static final String ACTION_RETURN_VALUES_KEY = "actionReturnValues";
    private static final String PARENT_RUNNING_ID = "PARENT_RUNNING_ID";
    private static final String RUNNING_PLANS_MAP = "RUNNING_PLANS_MAP";

    private final ResultsBinding resultsBinding;
    private final InputsBinding inputsBinding;
    private final OutputsBinding outputsBinding;
    private final ExecutionPreconditionService executionPreconditionService;
    private final MissingInputHandler missingInputHandler;
    private final CsMagicVariableHelper magicVariableHelper;
    private final DebuggerBreakpointsHandler debuggerBreakpointsHandler;

    public ExecutableExecutionData(ResultsBinding resultsBinding, InputsBinding inputsBinding,
                                   OutputsBinding outputsBinding, ExecutionPreconditionService preconditionService,
                                   MissingInputHandler missingInputHandler,
                                   CsMagicVariableHelper magicVariableHelper,
                                   DebuggerBreakpointsHandler debuggerBreakpointsHandler) {
        this.resultsBinding = resultsBinding;
        this.inputsBinding = inputsBinding;
        this.outputsBinding = outputsBinding;
        this.executionPreconditionService = preconditionService;
        this.missingInputHandler = missingInputHandler;
        this.magicVariableHelper = magicVariableHelper;
        this.debuggerBreakpointsHandler = debuggerBreakpointsHandler;
    }

    public void startExecutable(@Param(ScoreLangConstants.EXECUTABLE_INPUTS_KEY) List<Input> executableInputs,
                                @Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                                @Param(ScoreLangConstants.USER_INPUTS_KEY) Map<String, ? extends Value> userInputs,
                                @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                                @Param(ScoreLangConstants.NODE_NAME_KEY) String nodeName,
                                @Param(ScoreLangConstants.NEXT_STEP_ID_KEY) Long nextStepId,
                                @Param(ScoreLangConstants.EXECUTABLE_TYPE) ExecutableType executableType,
                                @Param(SYSTEM_CONTEXT) SystemContext systemContext,
                                @Param(USE_EMPTY_VALUES_FOR_PROMPTS_KEY) Boolean useEmptyValuesForPrompts) {
        try {
            Map<String, Value> callArguments = runEnv.removeCallArguments();
            List<Input> mutableInputList = new ArrayList<>(executableInputs);

            if (userInputs != null) {
                callArguments.putAll(userInputs);
                // merge is done only for flows that have extra inputs besides those defined as "flow inputs"
                if (executionRuntimeServices.getMergeUserInputs()) {
                    Map<String, Input> executableInputsMap = mutableInputList.stream()
                            .collect(toMap(Input::getName, Function.identity()));
                    for (String inputName : userInputs.keySet()) {
                        Value inputValue = userInputs.get(inputName);
                        Input inputToUpdate = executableInputsMap.get(inputName);
                        if (inputToUpdate != null) {
                            Input updatedInput = new Input.InputBuilder(inputToUpdate, inputValue)
                                    .build();
                            mutableInputList.set(mutableInputList.indexOf(inputToUpdate), updatedInput);
                        } else {
                            Input toAddInput = new Input.InputBuilder(inputName, inputValue).build();
                            mutableInputList.add(toAddInput);
                        }
                    }
                }
            }
            if (systemContext.containsKey(ScoreLangConstants.DEBUGGER_FLOW_INPUTS)) {
                Map<String, Value> variables = debuggerBreakpointsHandler.resolveInputs(systemContext);
                callArguments.putAll(variables);
                Context flowContext = new Context(variables, magicVariableHelper.getGlobalContext(executionRuntimeServices));
                flowContext.putVariables(variables);
                runEnv.getStack().pushContext(flowContext);

            }

            executableInputs = Collections.unmodifiableList(mutableInputList);

            //restore what was already prompted and add newly prompted values
            Map<String, Value> promptedValues = runEnv.removePromptedValues();
            promptedValues.putAll(missingInputHandler.applyPromptInputValues(systemContext, executableInputs));

            Map<String, Prompt> promptArguments = runEnv.removePromptArguments();

            if (systemContext.containsKey(ScoreLangConstants.DEBUGGER_EXECUTABLE_INPUTS)) {
                callArguments.putAll(debuggerBreakpointsHandler.applyValues(systemContext, executableInputs));
            }
            List<Input> newExecutableInputs =
                    addUserDefinedStepInputs(executableInputs, callArguments, promptArguments);

            LanguageEventData.StepType stepType = LanguageEventData.convertExecutableType(executableType);
            sendStartBindingInputsEvent(
                    newExecutableInputs,
                    runEnv,
                    executionRuntimeServices,
                    "Pre Input binding for " + stepType,
                    stepType,
                    nodeName,
                    callArguments
            );
            Map<String, Value> magicVariables = magicVariableHelper.getGlobalContext(executionRuntimeServices);
            List<Input> missingInputs = new ArrayList<>();
            ReadOnlyContextAccessor context = new ReadOnlyContextAccessor(callArguments, magicVariables);
            Map<String, Value> boundInputValues = inputsBinding.bindInputs(
                    newExecutableInputs,
                    context.getMergedContexts(),
                    promptedValues,
                    runEnv.getSystemProperties(),
                    missingInputs,
                    isTrue(useEmptyValuesForPrompts),
                    promptArguments);

            //if there are any missing required input after binding
            // try to resolve it using provided missing input handler
            if (CollectionUtils.isNotEmpty(missingInputs)) {
                boolean canContinue = missingInputHandler.resolveMissingInputs(
                        missingInputs,
                        systemContext,
                        runEnv,
                        executionRuntimeServices,
                        stepType,
                        nodeName,
                        isTrue(useEmptyValuesForPrompts));

                if (!canContinue) {
                    //we must keep the state unchanged}
                    runEnv.putCallArguments(callArguments);
                    runEnv.putPromptArguments(promptArguments);
                    //keep what was already prompted
                    runEnv.keepPromptedValues(promptedValues);
                    return;
                }
            }

            boolean continueToNext = true;
            if (systemContext.containsKey(ScoreLangConstants.USER_INTERRUPT)) {
                Long parentRid = null;
                if (systemContext.containsKey(PARENT_RUNNING_ID)) {
                    parentRid = (Long) systemContext.get(PARENT_RUNNING_ID);
                }
                String newNodeName = executionRuntimeServices.getNodeName();
                continueToNext = !debuggerBreakpointsHandler.handleBreakpoints(
                        systemContext,
                        runEnv,
                        executionRuntimeServices,
                        stepType,
                        nodeName,
                        executionRuntimeServices.extractParentNameFromRunId(parentRid) + "." +
                                //need new node name for debugger, node name is still set to the last value
                                newNodeName);
            }


            Map<String, Value> actionArguments = new HashMap<>(boundInputValues);

            //updated stored input arguments to be later used for output binding
            saveStepInputsResultContext(runEnv, callArguments, promptedValues);

            //done with the user inputs, don't want it to be available in next startExecutable steps..
            if (userInputs != null) {
                userInputs.clear();
            }

            updateCallArgumentsAndPushContextToStack(runEnv,
                    new Context(boundInputValues, magicVariables), actionArguments, new HashMap<>());

            sendEndBindingInputsEvent(
                    newExecutableInputs,
                    boundInputValues,
                    runEnv,
                    executionRuntimeServices,
                    "Post Input binding for " + stepType,
                    stepType,
                    nodeName,
                    callArguments);

            executionRuntimeServices.setShouldCheckGroup();
            if (continueToNext) {
                // put the next step position for the navigation
                runEnv.putNextStepPosition(nextStepId);
                runEnv.getExecutionPath().down();
            }
        } catch (RuntimeException e) {
            logger.error("There was an error running the start executable execution step of: \'" + nodeName +
                    "\'.\n\tError is: " + e.getMessage());
            throw new RuntimeException("Error running: \'" + nodeName + "\'.\n\t " + e.getMessage(), e);
        }

    }

    private List<Input> addUserDefinedStepInputs(List<Input> executableInputs,
                                                 Map<String, Value> callArguments,
                                                 Map<String, Prompt> promptArguments) {
        final List<Input> inputs = extractUserDefinedStepInputs(executableInputs, promptArguments, callArguments);

        if (inputs.size() == 0) {
            return executableInputs;
        }

        List<Input> newExecutableInputs = new ArrayList<>(executableInputs.size() + inputs.size());
        newExecutableInputs.addAll(executableInputs);
        newExecutableInputs.addAll(inputs);

        return newExecutableInputs;
    }

    /**
     * This method is executed by the finishExecutable execution step of an operation or flow
     *
     * @param runEnv                   the run environment object
     * @param executableOutputs        the operation outputs data
     * @param executableResults        the operation results data
     * @param executionRuntimeServices services supplied by score engine for handling the execution
     */
    public void finishExecutable(@Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                                 @Param(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY) List<Output> executableOutputs,
                                 @Param(ScoreLangConstants.EXECUTABLE_RESULTS_KEY) List<Result> executableResults,
                                 @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                                 @Param(ScoreLangConstants.NODE_NAME_KEY) String nodeName,
                                 @Param(ScoreLangConstants.EXECUTABLE_TYPE) ExecutableType executableType) {
        try {
            runEnv.getExecutionPath().up();
            Context operationContext = runEnv.getStack().popContext();
            Map<String, Value> operationVariables = operationContext == null ?
                    null : operationContext.getImmutableViewOfVariables();

            Context flowContext = runEnv.getStack().peekContext();
            if (executableType == ExecutableType.FLOW && flowContext != null) {
                String workerGroup = toStringSafe(flowContext.removeLanguageVariable(WORKER_GROUP));
                if (workerGroup != null) {
                    executionRuntimeServices.setWorkerGroupName(workerGroup);
                    executionRuntimeServices.setShouldCheckGroup();
                }
            }

            ReturnValues actionReturnValues = buildReturnValues(runEnv, executableType);
            LanguageEventData.StepType stepType = LanguageEventData.convertExecutableType(executableType);
            fireEvent(
                    executionRuntimeServices,
                    runEnv,
                    ScoreLangConstants.EVENT_OUTPUT_START,
                    "Output binding started",
                    stepType,
                    nodeName,
                    operationVariables,
                    Pair.of(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY, (Serializable) executableOutputs),
                    Pair.of(ScoreLangConstants.EXECUTABLE_RESULTS_KEY, (Serializable) executableResults),
                    Pair.of(ACTION_RETURN_VALUES_KEY,
                            executableType == ExecutableType.OPERATION ?
                                    new ReturnValues(new HashMap<String, Value>(), actionReturnValues.getResult()) :
                                    actionReturnValues)
            );

            // Resolving the result of the operation/flow
            String result = resultsBinding.resolveResult(
                    operationVariables,
                    actionReturnValues.getOutputs(),
                    runEnv.getSystemProperties(),
                    executableResults,
                    actionReturnValues.getResult()
            );

            ReadOnlyContextAccessor outputsBindingAccessor = new ReadOnlyContextAccessor(operationVariables,
                    actionReturnValues.getOutputs(), magicVariableHelper.getGlobalContext(executionRuntimeServices));
            Map<String, Value> operationReturnOutputs =
                    outputsBinding.bindOutputs(
                            outputsBindingAccessor,
                            runEnv.getSystemProperties(),
                            executableOutputs
                    );

            ReturnValues returnValues = new ReturnValues(operationReturnOutputs, result);
            runEnv.putReturnValues(returnValues);
            fireEvent(
                    executionRuntimeServices,
                    runEnv,
                    ScoreLangConstants.EVENT_OUTPUT_END,
                    "Output binding finished",
                    stepType,
                    nodeName,
                    operationVariables,
                    Pair.of(LanguageEventData.OUTPUTS, (Serializable) operationReturnOutputs),
                    Pair.of(LanguageEventData.RESULT, returnValues.getResult())
            );

            // If we have parent flow data on the stack, we pop it and request the score engine to switch
            // to the parent execution plan id once it can, and we set the next position that was stored there
            // for the use of the navigation
            if (!runEnv.getParentFlowStack().isEmpty()) {
                handleNavigationToParent(runEnv, executionRuntimeServices);
            } else {
                fireEvent(
                        executionRuntimeServices,
                        runEnv,
                        ScoreLangConstants.EVENT_EXECUTION_FINISHED,
                        "Execution finished running",
                        stepType,
                        nodeName,
                        operationVariables,
                        Pair.of(LanguageEventData.RESULT, returnValues.getResult()),
                        Pair.of(LanguageEventData.OUTPUTS, (Serializable) operationReturnOutputs)
                );
            }
        } catch (RuntimeException e) {
            logger.error("There was an error running the finish executable execution step of: \'" + nodeName +
                    "\'.\n\tError is: " + e.getMessage());
            throw new RuntimeException("Error running: \'" + nodeName + "\'.\n\t" + e.getMessage(), e);
        }
    }

    public void canExecute(@Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                           @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                           @Param(ScoreLangConstants.NODE_NAME_KEY) String nodeName,
                           @Param(ScoreLangConstants.NEXT_STEP_ID_KEY) Long nextStepId) {
        try {
            if (!isEmpty(runEnv.getExecutionPath().getParentPath())) {
                // If it is start of a sub flow then the check should not happen
                runEnv.putNextStepPosition(nextStepId);
                return;
            }

            if (!executionPreconditionService.canExecute(valueOf(executionRuntimeServices.getExecutionId()),
                    executionRuntimeServices.isEnterpriseMode())) {
                logger.warn("Execution precondition not fulfilled. Waiting for it to be true.");

                if (!executionRuntimeServices.getPreconditionNotFulfilled()) {
                    executionRuntimeServices.setPreconditionNotFulfilled();
                }
            } else if (executionRuntimeServices.getPreconditionNotFulfilled()) {
                executionRuntimeServices.removePreconditionNotFulfilled();
            }

            runEnv.putNextStepPosition(nextStepId);
        } catch (RuntimeException e) {
            logger.error("There was an error running the finish executable execution step of: \'" + nodeName +
                    "\'.\n\tError is: " + e.getMessage());
            throw new RuntimeException("Error running: \'" + nodeName + "\'.\n\t" + e.getMessage(), e);
        }
    }

    private void handleNavigationToParent(RunEnvironment runEnv, ExecutionRuntimeServices executionRuntimeServices) {
        ParentFlowData parentFlowData = runEnv.getParentFlowStack().popParentFlowData();
        executionRuntimeServices.requestToChangeExecutionPlan(parentFlowData.getRunningExecutionPlanId());
        runEnv.putNextStepPosition(parentFlowData.getPosition());
    }

    private ReturnValues buildReturnValues(RunEnvironment runEnvironment, ExecutableType executableType) {
        ReturnValues returnValues = runEnvironment.removeReturnValues();
        switch (executableType) {
            case DECISION:
                returnValues = new ReturnValues(Collections.<String, Value>emptyMap(), null);
                break;
            case FLOW:
                break;
            case OPERATION:
                break;
            default:
                throw new RuntimeException("Unrecognized type: " + executableType);
        }
        return returnValues;
    }

    private List<Input> extractUserDefinedStepInputs(List<Input> inputs,
                                                     Map<String, Prompt> prompts,
                                                     Map<String, Value> callArguments) {
        Set<String> inputNames = inputs.stream().map(Input::getName).collect(toCollection(LinkedHashSet::new));
        Set<String> promptInputNames = prompts.keySet();

        return Sets
                .difference(promptInputNames, inputNames)
                .stream()
                .map(additionalInputName ->
                        new Input
                                .InputBuilder(additionalInputName, callArguments.get(additionalInputName))
                                .withPrompt(prompts.get(additionalInputName))
                                .build()
                )
                .collect(toList());

    }

    public void saveStepInputsResultContext(RunEnvironment runEnv,
                                            Map<String, Value> originalCallArguments,
                                            Map<String, Value> promptedValues) {
        Context flowContext = runEnv.getStack().peekContext();
        if (flowContext != null) {
            Map<String, Value> finalActionArguments = new HashMap<>(originalCallArguments);
            finalActionArguments.putAll(promptedValues);
            saveStepInputsResultContext(flowContext, finalActionArguments);
        }
    }
}
