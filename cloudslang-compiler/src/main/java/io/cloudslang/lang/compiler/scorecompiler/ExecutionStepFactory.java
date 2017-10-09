/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.scorecompiler;

import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.entities.ActionType;
import io.cloudslang.lang.entities.ExecutableType;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.score.api.ControlActionMetadata;
import io.cloudslang.score.api.ExecutionStep;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.Validate;

public class ExecutionStepFactory {

    private static final String STEPS_PACKAGE = "io.cloudslang.lang.runtime.steps";
    private static final String STEP_EXECUTION_DATA_CLASS = STEPS_PACKAGE + ".StepExecutionData";
    private static final String OPERATION_STEPS_CLASS = STEPS_PACKAGE + ".ExecutableExecutionData";
    private static final String ACTION_STEPS_CLASS = STEPS_PACKAGE + ".ActionExecutionData";
    private static final String PARALLEL_LOOP_STEPS_CLASS = STEPS_PACKAGE + ".ParallelLoopExecutionData";
    private static final String NAVIGATION_ACTIONS_CLASS = "io.cloudslang.lang.runtime.navigations.Navigations";
    private static final String SIMPLE_NAVIGATION_METHOD = "navigate";


    public ExecutionStep createBeginStepStep(Long index, List<Argument> stepInputs,
                                             Map<String, Serializable> preStepData, String refId, String stepName) {
        Validate.notNull(preStepData, "preStepData is null");
        Map<String, Serializable> actionData = new HashMap<>();
        actionData.put(ScoreLangConstants.STEP_INPUTS_KEY, (Serializable) stepInputs);
        actionData.put(ScoreLangConstants.LOOP_KEY, preStepData.get(SlangTextualKeys.FOR_KEY));
        actionData.put(ScoreLangConstants.HOOKS, "TBD");
        actionData.put(ScoreLangConstants.NODE_NAME_KEY, stepName);
        actionData.put(ScoreLangConstants.REF_ID, refId);
        actionData.put(ScoreLangConstants.NEXT_STEP_ID_KEY, index + 1);
        return createGeneralStep(index, STEP_EXECUTION_DATA_CLASS, "beginStep", actionData);
    }

    public ExecutionStep createFinishStepStep(Long index, Map<String, Serializable> postStepData,
                                              Map<String, ResultNavigation> navigationValues,
                                              String stepName, boolean parallelLoop) {
        Validate.notNull(postStepData, "postStepData is null");
        Map<String, Serializable> actionData = new HashMap<>();

        if (!parallelLoop) {
            actionData.put(ScoreLangConstants.STEP_PUBLISH_KEY, postStepData.get(SlangTextualKeys.PUBLISH_KEY));
        }

        actionData.put(ScoreLangConstants.PREVIOUS_STEP_ID_KEY, index - 1);
        actionData.put(ScoreLangConstants.BREAK_LOOP_KEY, postStepData.get(SlangTextualKeys.BREAK_KEY));
        actionData.put(ScoreLangConstants.STEP_NAVIGATION_KEY, new HashMap<>(navigationValues));
        actionData.put(ScoreLangConstants.HOOKS, "TBD");
        actionData.put(ScoreLangConstants.NODE_NAME_KEY, stepName);
        actionData.put(ScoreLangConstants.PARALLEL_LOOP_KEY, parallelLoop);

        ExecutionStep finishStep = createGeneralStep(index, STEP_EXECUTION_DATA_CLASS, "endStep", actionData);
        finishStep.setNavigationData(null);
        return finishStep;
    }

    public ExecutionStep createStartStep(Long index, Map<String, Serializable> preExecutableData, List<Input>
            execInputs, String executableName, ExecutableType executableType) {
        Validate.notNull(preExecutableData, "preExecutableData is null");
        Validate.notNull(execInputs, "Executable inputs are null");
        Map<String, Serializable> actionData = new HashMap<>();
        actionData.put(ScoreLangConstants.EXECUTABLE_INPUTS_KEY, (Serializable) execInputs);
        actionData.put(ScoreLangConstants.HOOKS, (Serializable) preExecutableData);
        actionData.put(ScoreLangConstants.NODE_NAME_KEY, executableName);
        actionData.put(ScoreLangConstants.NEXT_STEP_ID_KEY, index + 1);
        actionData.put(ScoreLangConstants.EXECUTABLE_TYPE, executableType);
        return createGeneralStep(index, OPERATION_STEPS_CLASS, "startExecutable", actionData);
    }

    public ExecutionStep createActionStep(Long index, Map<String, Serializable> actionRawData) {
        Validate.notNull(actionRawData, "actionData is null");
        Map<String, Serializable> actionData = new HashMap<>();
        ActionType actionType;

        @SuppressWarnings("unchecked")
        Map<String, String> javaActionData =
                (Map<String, String>) actionRawData.get(SlangTextualKeys.JAVA_ACTION_KEY);
        @SuppressWarnings("unchecked")
        Map<String, Serializable> pythonActionData =
                (Map<String, Serializable>) actionRawData.get(SlangTextualKeys.PYTHON_ACTION_KEY);
        boolean javaActionFound = MapUtils.isNotEmpty(javaActionData);
        boolean pythonActionFound = MapUtils.isNotEmpty(pythonActionData);

        if (javaActionFound) {
            actionType = ActionType.JAVA;
            actionData.putAll(javaActionData);
        } else if (pythonActionFound) {
            actionType = ActionType.PYTHON;
            actionData.putAll(pythonActionData);
        } else {
            // java action or python script data is missing
            throw new RuntimeException("Invalid action data");
        }

        actionData.put(ScoreLangConstants.ACTION_TYPE, actionType);
        actionData.put(ScoreLangConstants.NEXT_STEP_ID_KEY, index + 1);
        return createGeneralStep(index, ACTION_STEPS_CLASS, "doAction", actionData);
    }

    public ExecutionStep createEndStep(Long index, Map<String, Serializable> postExecutableData,
                                       List<Output> outputs, List<Result> results,
                                       String executableName, ExecutableType executableType) {
        Validate.notNull(postExecutableData, "postExecutableData is null");
        Validate.notNull(outputs, "Executable outputs are null");
        Validate.notNull(results, "Executable results are null");
        Map<String, Serializable> actionData = new HashMap<>();
        actionData.put(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY, (Serializable) outputs);
        actionData.put(ScoreLangConstants.EXECUTABLE_RESULTS_KEY, (Serializable) results);
        actionData.put(ScoreLangConstants.HOOKS, (Serializable) postExecutableData);
        actionData.put(ScoreLangConstants.NODE_NAME_KEY, executableName);
        actionData.put(ScoreLangConstants.EXECUTABLE_TYPE, executableType);
        return createGeneralStep(index, OPERATION_STEPS_CLASS, "finishExecutable", actionData);
    }

    public ExecutionStep createAddBranchesStep(Long currentStepId, Long nextStepId,
                                               Long branchBeginStepId, Map<String, Serializable> preStepData,
                                               String refId, String stepName) {
        Validate.notNull(preStepData, "preStepData is null");
        Map<String, Serializable> actionData = new HashMap<>();
        actionData.put(ScoreLangConstants.NODE_NAME_KEY, stepName);
        actionData.put(ScoreLangConstants.REF_ID, refId);
        actionData.put(ScoreLangConstants.NEXT_STEP_ID_KEY, nextStepId);
        actionData.put(ScoreLangConstants.BRANCH_BEGIN_STEP_ID_KEY, branchBeginStepId);
        actionData.put(ScoreLangConstants.PARALLEL_LOOP_STATEMENT_KEY,
                preStepData.get(SlangTextualKeys.PARALLEL_LOOP_KEY));
        ExecutionStep executionStep =
                createGeneralStep(currentStepId, PARALLEL_LOOP_STEPS_CLASS, "addBranches", actionData);
        executionStep.setSplitStep(true);
        return executionStep;
    }

    public ExecutionStep createJoinBranchesStep(Long index, Map<String, Serializable> postStepData,
                                                Map<String, ResultNavigation> navigationValues, String stepName) {
        Validate.notNull(postStepData, "postStepData is null");
        Validate.notNull(navigationValues, "navigationValues is null");
        Map<String, Serializable> actionData = new HashMap<>();
        actionData.put(ScoreLangConstants.STEP_PUBLISH_KEY, postStepData.get(SlangTextualKeys.PUBLISH_KEY));
        actionData.put(ScoreLangConstants.STEP_NAVIGATION_KEY, new HashMap<>(navigationValues));
        actionData.put(ScoreLangConstants.NODE_NAME_KEY, stepName);

        return createGeneralStep(index, PARALLEL_LOOP_STEPS_CLASS, "joinBranches", actionData);
    }

    private ExecutionStep createGeneralStep(
            Long stepId,
            String actionClassName,
            String actionMethodName,
            Map<String, Serializable> actionData) {

        ExecutionStep step = new ExecutionStep(stepId);
        step.setAction(new ControlActionMetadata(actionClassName, actionMethodName));
        step.setActionData(actionData);

        step.setNavigation(new ControlActionMetadata(NAVIGATION_ACTIONS_CLASS, SIMPLE_NAVIGATION_METHOD));
        step.setNavigationData(new HashMap<String, Object>());

        return step;
    }

}
