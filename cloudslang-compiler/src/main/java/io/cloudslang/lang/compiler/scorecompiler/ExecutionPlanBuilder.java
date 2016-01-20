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

import ch.lambdaj.Lambda;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Operation;
import io.cloudslang.lang.compiler.modeller.model.Task;
import io.cloudslang.lang.entities.ExecutableType;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

/*
 * Created by orius123 on 11/11/14.
 */
@Component
public class ExecutionPlanBuilder {

    @Autowired
    private ExecutionStepFactory stepFactory;

    private static final String CLOUDSLANG_NAME = "CloudSlang";
    private static final int NUMBER_OF_TASK_EXECUTION_STEPS = 2;
    private static final int NUMBER_OF_ASYNC_LOOP_EXECUTION_STEPS = 2;
    private static final long FLOW_END_STEP_ID = 0L;
    private static final long FLOW_START_STEP_ID = 1L;

    public ExecutionPlan createOperationExecutionPlan(Operation compiledOp) {
        ExecutionPlan executionPlan = new ExecutionPlan();
        executionPlan.setName(compiledOp.getName());
        executionPlan.setLanguage(CLOUDSLANG_NAME);
        executionPlan.setFlowUuid(compiledOp.getId());

        executionPlan.setBeginStep(1L);

        executionPlan.addStep(stepFactory.createStartStep(1L, compiledOp.getPreExecActionData(), compiledOp.getInputs(),
                compiledOp.getName()));
        executionPlan.addStep(stepFactory.createActionStep(2L, compiledOp.getAction().getActionData()));
        executionPlan.addStep(stepFactory.createEndStep(3L, compiledOp.getPostExecActionData(), compiledOp.getOutputs(),
                compiledOp.getResults(), compiledOp.getName(), ExecutableType.OPERATION));
        return executionPlan;
    }

    public ExecutionPlan createFlowExecutionPlan(Flow compiledFlow) {
        ExecutionPlan executionPlan = new ExecutionPlan();
        executionPlan.setName(compiledFlow.getName());
        executionPlan.setLanguage(CLOUDSLANG_NAME);
        executionPlan.setFlowUuid(compiledFlow.getId());

        executionPlan.setBeginStep(FLOW_START_STEP_ID);
        //flow start step
        executionPlan.addStep(stepFactory.createStartStep(FLOW_START_STEP_ID, compiledFlow.getPreExecActionData(),
                compiledFlow.getInputs(), compiledFlow.getName()));
        //flow end step
        executionPlan.addStep(stepFactory.createEndStep(FLOW_END_STEP_ID, compiledFlow.getPostExecActionData(),
                compiledFlow.getOutputs(), compiledFlow.getResults(), compiledFlow.getName(), ExecutableType.FLOW));

        Map<String, Long> taskReferences = new HashMap<>();
        for (Result result : compiledFlow.getResults()) {
            taskReferences.put(result.getName(), FLOW_END_STEP_ID);
        }

        Deque<Task> tasks = compiledFlow.getWorkflow().getTasks();

        if (CollectionUtils.isEmpty(tasks)) {
            throw new RuntimeException("Flow: " + compiledFlow.getName() + " has no tasks");
        }

        List<ExecutionStep> taskExecutionSteps = buildTaskExecutionSteps(tasks.getFirst(), taskReferences, tasks, compiledFlow);
        executionPlan.addSteps(taskExecutionSteps);

        return executionPlan;
    }

    private List<ExecutionStep> buildTaskExecutionSteps(
            Task task,
            Map<String, Long> taskReferences, Deque<Task> tasks,
            Flow compiledFlow) {

        List<ExecutionStep> taskExecutionSteps = new ArrayList<>();

        String taskName = task.getName();
        Long currentId = getCurrentId(taskReferences, tasks);
        boolean isAsync = task.isAsync();

        //Begin Task
        taskReferences.put(taskName, currentId);
        if (isAsync) {
            Long joinStepID = currentId + NUMBER_OF_ASYNC_LOOP_EXECUTION_STEPS + 1;
            taskExecutionSteps.add(
                    stepFactory.createAddBranchesStep(currentId++, joinStepID, currentId,
                            task.getPreTaskActionData(), compiledFlow.getId(), taskName
                    )
            );
        }
        taskExecutionSteps.add(
                stepFactory.createBeginTaskStep(currentId++, task.getArguments(),
                        task.getPreTaskActionData(), task.getRefId(), taskName)
        );

        //End Task
        Map<String, ResultNavigation> navigationValues = new HashMap<>();
        for (Map.Entry<String, String> entry : task.getNavigationStrings().entrySet()) {
            String nextStepName = entry.getValue();
            if (taskReferences.get(nextStepName) == null) {
                Task nextTaskToCompile = Lambda.selectFirst(tasks, having(on(Task.class).getName(), equalTo(nextStepName)));
                if (nextTaskToCompile == null) {
                    throw new RuntimeException("Failed to compile task: " + taskName + ". The task/result name: " + entry.getValue() + " of navigation: " + entry.getKey() + " -> " + entry.getValue() + " is missing");
                }
                taskExecutionSteps.addAll(buildTaskExecutionSteps(nextTaskToCompile, taskReferences, tasks, compiledFlow));
            }
            long nextStepId = taskReferences.get(nextStepName);
            String presetResult = (FLOW_END_STEP_ID == nextStepId) ? nextStepName : null;
            navigationValues.put(entry.getKey(), new ResultNavigation(nextStepId, presetResult));
        }
        if (isAsync) {
            taskExecutionSteps.add(
                    stepFactory.createFinishTaskStep(currentId++, task.getPostTaskActionData(),
                            new HashMap<String, ResultNavigation>(), taskName, true)
            );
            taskExecutionSteps.add(
                    stepFactory.createJoinBranchesStep(currentId, task.getPostTaskActionData(),
                            navigationValues, taskName)
            );
        } else {
            taskExecutionSteps.add(
                    stepFactory.createFinishTaskStep(currentId, task.getPostTaskActionData(),
                            navigationValues, taskName, false)
            );
        }
        return taskExecutionSteps;
    }

    private Long getCurrentId(Map<String, Long> taskReferences, Deque<Task> tasks) {
        Long currentID;

        Long max = Lambda.max(taskReferences);
        Map.Entry maxEntry = Lambda.selectFirst(taskReferences.entrySet(), having(on(Map.Entry.class).getValue(), equalTo(max)));
        String referenceKey = (String) (maxEntry).getKey();
        Task task = null;
        for (Task taskItem : tasks) {
            if (taskItem.getName().equals(referenceKey)) {
                task = taskItem;
                break;
            }
        }

        if (task == null || !task.isAsync()) {
            // the reference is not a task or is not an async task
            currentID = max + NUMBER_OF_TASK_EXECUTION_STEPS;
        } else {
            //async task
            currentID = max + NUMBER_OF_TASK_EXECUTION_STEPS + NUMBER_OF_ASYNC_LOOP_EXECUTION_STEPS;
        }

        return currentID;
    }

}
