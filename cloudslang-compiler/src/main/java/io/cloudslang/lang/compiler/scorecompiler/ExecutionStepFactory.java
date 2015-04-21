/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
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
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import org.apache.commons.lang3.StringUtils;
import io.cloudslang.lang.entities.ResultNavigation;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.Validate;
import io.cloudslang.score.api.ControlActionMetadata;
import io.cloudslang.score.api.ExecutionStep;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Created by orius123 on 05/11/14.
 */
@Component
public class ExecutionStepFactory {

    private static final String STEPS_PACKAGE = "io.cloudslang.lang.runtime.steps";
    private static final String TASK_STEPS_CLASS = STEPS_PACKAGE + ".TaskSteps";
    private static final String OPERATION_STEPS_CLASS = STEPS_PACKAGE + ".ExecutableSteps";
    private static final String ACTION_STEPS_CLASS = STEPS_PACKAGE + ".ActionSteps";
    private static final String ASYNC_LOOP_STEPS_CLASS = STEPS_PACKAGE + ".AsyncLoopSteps";
    private static final String NAVIGATION_ACTIONS_CLASS = "io.cloudslang.lang.runtime.navigations.Navigations";
    private static final String SIMPLE_NAVIGATION_METHOD = "navigate";


    public ExecutionStep createBeginTaskStep(Long index, List<Input> inputs, Map<String, Serializable> preTaskData, String refId, String taskName) {
        Validate.notNull(preTaskData, "preTaskData is null");
        Map<String, Serializable> actionData = new HashMap<>();
        actionData.put(ScoreLangConstants.TASK_INPUTS_KEY, (Serializable)inputs);
        actionData.put(ScoreLangConstants.LOOP_KEY, preTaskData.get(SlangTextualKeys.FOR_KEY));
        actionData.put(ScoreLangConstants.HOOKS, "TBD"); //todo add implementation for user custom hooks
        actionData.put(ScoreLangConstants.NODE_NAME_KEY, taskName);
        actionData.put(ScoreLangConstants.REF_ID, refId);
        actionData.put(ScoreLangConstants.NEXT_STEP_ID_KEY, index + 1);
        return createGeneralStep(index, TASK_STEPS_CLASS, "beginTask", actionData);
    }

    public ExecutionStep createFinishTaskStep(Long index, Map<String, Serializable> postTaskData,
                                              Map<String, ResultNavigation> navigationValues, String taskName, boolean isAsync) {
        Validate.notNull(postTaskData, "postTaskData is null");
        Map<String, Serializable> actionData = new HashMap<>();
        actionData.put(ScoreLangConstants.TASK_PUBLISH_KEY, postTaskData.get(SlangTextualKeys.PUBLISH_KEY));
        actionData.put(ScoreLangConstants.PREVIOUS_STEP_ID_KEY, index - 1);
        actionData.put(ScoreLangConstants.BREAK_LOOP_KEY, postTaskData.get(SlangTextualKeys.BREAK_KEY));
        actionData.put(ScoreLangConstants.TASK_NAVIGATION_KEY, new HashMap<>(navigationValues));
        actionData.put(ScoreLangConstants.HOOKS, "TBD"); //todo add implementation for user custom hooks
        actionData.put(ScoreLangConstants.NODE_NAME_KEY, taskName);
        actionData.put(ScoreLangConstants.ASYNC_LOOP_KEY, isAsync);
        ExecutionStep finishTask = createGeneralStep(index, TASK_STEPS_CLASS, "endTask", actionData);
        finishTask.setNavigationData(null);
        return finishTask;
    }

    public ExecutionStep createStartStep(Long index, Map<String, Serializable> preExecutableData, List<Input>
            execInputs, String executableName) {
        Validate.notNull(preExecutableData, "preExecutableData is null");
        Validate.notNull(execInputs, "Executable inputs are null");
        Map<String, Serializable> actionData = new HashMap<>();
        actionData.put(ScoreLangConstants.EXECUTABLE_INPUTS_KEY, (Serializable) execInputs);
        actionData.put(ScoreLangConstants.HOOKS, (Serializable) preExecutableData);
        actionData.put(ScoreLangConstants.NODE_NAME_KEY, executableName);
        actionData.put(ScoreLangConstants.NEXT_STEP_ID_KEY, index + 1);
        return createGeneralStep(index, OPERATION_STEPS_CLASS, "startExecutable", actionData);
    }

    public ExecutionStep createActionStep(Long index, Map<String, Serializable> actionRawData) {
        Validate.notNull(actionRawData, "actionData is null");
        Map<String, Serializable> actionData = new HashMap<>();
        ActionType actionType;

        @SuppressWarnings("unchecked") Map<String, String> javaActionData = (Map<String, String>) actionRawData.get(SlangTextualKeys.JAVA_ACTION);
        @SuppressWarnings("unchecked") String pythonScript = (String) actionRawData.get(ScoreLangConstants.PYTHON_SCRIPT_KEY);
        boolean javaActionFound = MapUtils.isNotEmpty(javaActionData);
        boolean pythonScriptFound = StringUtils.isNotEmpty(pythonScript);

        if (javaActionFound) {
            actionType = ActionType.JAVA;
            actionData.putAll(javaActionData);
        } else  if (pythonScriptFound) {
            actionType = ActionType.PYTHON;
            actionData.putAll(actionRawData);
        } else {
            // java action or python script data is missing
            throw new RuntimeException("Invalid action data");
        }

        actionData.put(ScoreLangConstants.ACTION_TYPE, actionType);
        actionData.put(ScoreLangConstants.NEXT_STEP_ID_KEY, index + 1);
        return createGeneralStep(index, ACTION_STEPS_CLASS, "doAction", actionData);
    }

    public ExecutionStep createEndStep(Long index, Map<String, Serializable> postExecutableData,
                                       List<Output> outputs, List<Result> results, String executableName) {
        Validate.notNull(postExecutableData, "postExecutableData is null");
        Validate.notNull(outputs, "Executable outputs are null");
        Validate.notNull(results, "Executable results are null");
        Map<String, Serializable> actionData = new HashMap<>();
        actionData.put(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY, (Serializable) outputs);
        actionData.put(ScoreLangConstants.EXECUTABLE_RESULTS_KEY, (Serializable) results);
        actionData.put(ScoreLangConstants.HOOKS, (Serializable) postExecutableData);
        actionData.put(ScoreLangConstants.NODE_NAME_KEY, executableName);
        return createGeneralStep(index, OPERATION_STEPS_CLASS, "finishExecutable", actionData);
    }

    public ExecutionStep createAddBranchesStep(Long currentStepID, Long nextStepID, Long branchBeginStepID, Map<String, Serializable> preTaskData, String refId, String taskName) {
        Validate.notNull(preTaskData, "preTaskData is null");
        Map<String, Serializable> actionData = new HashMap<>();
        actionData.put(ScoreLangConstants.NODE_NAME_KEY, taskName);
        actionData.put(ScoreLangConstants.REF_ID, refId);
        actionData.put(ScoreLangConstants.NEXT_STEP_ID_KEY, nextStepID);
        actionData.put(ScoreLangConstants.BRANCH_BEGIN_STEP_ID_KEY, branchBeginStepID);
        actionData.put(ScoreLangConstants.ASYNC_LOOP_STATEMENT_KEY, preTaskData.get(ScoreLangConstants.ASYNC_LOOP_KEY));
        ExecutionStep executionStep = createGeneralStep(currentStepID, ASYNC_LOOP_STEPS_CLASS, "addBranches", actionData);
        executionStep.setSplitStep(true);
        return executionStep;
    }

    public ExecutionStep createJoinBranchesStep(Long index, Map<String, Serializable> postTaskData,
                                                Map<String, ResultNavigation> navigationValues, String taskName) {
        Validate.notNull(postTaskData, "postTaskData is null");
        Validate.notNull(navigationValues, "navigationValues is null");
        Map<String, Serializable> actionData = new HashMap<>();
        actionData.put(ScoreLangConstants.TASK_AGGREGATE_KEY, postTaskData.get(SlangTextualKeys.AGGREGATE_KEY));
        actionData.put(ScoreLangConstants.TASK_NAVIGATION_KEY, new HashMap<>(navigationValues));
        actionData.put(ScoreLangConstants.NODE_NAME_KEY, taskName);

        return createGeneralStep(index, ASYNC_LOOP_STEPS_CLASS, "joinBranches", actionData);
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
