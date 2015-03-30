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
import io.cloudslang.lang.compiler.modeller.model.Task;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Operation;
import org.apache.commons.collections4.CollectionUtils;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

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
    private static final long FLOW_END_STEP_ID = 0L;
    private static final long FLOW_START_STEP_ID = 1L;
    private static final long BRANCH_START_STEP_ID = 0L;
    private static final long BRANCH_END_STEP_ID = 1L;
    private static final String BRANCH_KEY = "branch";

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
                compiledOp.getResults(), compiledOp.getName()));
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
                compiledFlow.getOutputs(), compiledFlow.getResults(), compiledFlow.getName()));

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

    public Map<String, ExecutionPlan> createBranchExecutionPlans(Flow compiledFlow) {
        Deque<Task> tasks = compiledFlow.getWorkflow().getTasks();

        if (CollectionUtils.isEmpty(tasks)) {
            throw new RuntimeException("Flow: " + compiledFlow.getName() + " has no tasks");
        }

        Map<String, ExecutionPlan> branchExecutionPlans = new HashMap<>();
        for (Task task : tasks) {
            if (task.isAsync()) {
                String refID = generateAsyncTaskRefID(compiledFlow, task);

                ExecutionPlan executionPlan = new ExecutionPlan();
                executionPlan.setName(BRANCH_KEY + "-" + refID);
                executionPlan.setLanguage(CLOUDSLANG_NAME);
                executionPlan.setFlowUuid(refID);

                executionPlan.setBeginStep(BRANCH_START_STEP_ID);
                //branch start step
                executionPlan.addStep(stepFactory.createBeginTaskStep(BRANCH_START_STEP_ID, task.getInputs(), task.getPreTaskActionData(),
                        task.getRefId(), task.getName()));
                //branch end step
                Map<String, ResultNavigation> navigationValues = new HashMap<>();
                for (Map.Entry<String, String> entry : task.getNavigationStrings().entrySet()) {
                    navigationValues.put(entry.getKey(), new ResultNavigation(0, entry.getKey()));
                }
                executionPlan.addStep(stepFactory.createFinishTaskStep(BRANCH_END_STEP_ID, task.getPostTaskActionData(),
                        navigationValues, task.getName(), true));

                branchExecutionPlans.put(refID, executionPlan);
            }
        }

        return branchExecutionPlans;
    }

    private List<ExecutionStep> buildTaskExecutionSteps(Task task,
                                                        Map<String, Long> taskReferences, Deque<Task> tasks, Flow compiledFlow) {

        List<ExecutionStep> taskExecutionSteps = new ArrayList<>();

        String taskName = task.getName();
        Long currentId = getCurrentId(taskReferences);
        boolean isAsync = task.isAsync();

        //Begin Task
        taskReferences.put(taskName, currentId);
        if (isAsync) {
            taskExecutionSteps.add(stepFactory.createAddBranchesStep(currentId++, task.getPreTaskActionData(), generateAsyncTaskRefID(compiledFlow, task), taskName));
        } else {
            taskExecutionSteps.add(stepFactory.createBeginTaskStep(currentId++, task.getInputs(), task.getPreTaskActionData(),
                    task.getRefId(), taskName));
        }

        //End Task
        Map<String, ResultNavigation> navigationValues = new HashMap<>();
        for (Map.Entry<String, String> entry : task.getNavigationStrings().entrySet()) {
            String nextStepName = entry.getValue();
            if (taskReferences.get(nextStepName) == null) {
                Task nextTaskToCompile = Lambda.selectFirst(tasks, having(on(Task.class).getName(), equalTo(nextStepName)));
                if(nextTaskToCompile == null){
                    throw new RuntimeException("Failed to compile task: " + taskName + ". The task/result name: " + entry.getValue() + " of navigation: " + entry.getKey() + " -> " + entry.getValue() + " is missing");
                }
                taskExecutionSteps.addAll(buildTaskExecutionSteps(nextTaskToCompile, taskReferences, tasks, compiledFlow));
            }
			long nextStepId = taskReferences.get(nextStepName);
			String presetResult = (FLOW_END_STEP_ID == nextStepId) ? nextStepName : null;
			navigationValues.put(entry.getKey(), new ResultNavigation(nextStepId, presetResult));
        }
        if (isAsync) {
            taskExecutionSteps.add(stepFactory.createJoinBranchesStep(currentId, task.getPostTaskActionData(), navigationValues, taskName));
        } else {
            taskExecutionSteps.add(stepFactory.createFinishTaskStep(currentId, task.getPostTaskActionData(),
                    navigationValues, taskName, false));
        }
        return taskExecutionSteps;
    }

    private Long getCurrentId(Map<String, Long> taskReferences) {
        Long max = Lambda.max(taskReferences);
        return max + NUMBER_OF_TASK_EXECUTION_STEPS;
    }

    private String generateAsyncTaskRefID(Flow compiledFlow, Task task) {
        return compiledFlow.getNamespace() + "." + compiledFlow.getName() + "." + task.getName();
    }

}
