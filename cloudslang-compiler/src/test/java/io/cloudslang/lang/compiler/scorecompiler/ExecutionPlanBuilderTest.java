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

import io.cloudslang.lang.compiler.modeller.model.*;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Workflow;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.compiler.modeller.model.Action;
import io.cloudslang.lang.compiler.modeller.model.Operation;
import io.cloudslang.lang.compiler.modeller.model.Task;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExecutionPlanBuilderTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private ExecutionPlanBuilder executionPlanBuilder;

    @Mock
    private ExecutionStepFactory stepFactory;

    private Task createSimpleCompiledAsyncTask(String taskName) {
        return createSimpleCompiledTask(taskName, true);
    }

    private Task createSimpleCompiledTask(String taskName) {
        return createSimpleCompiledTask(taskName, false);
    }

    private Task createSimpleCompiledTask(String taskName, Map<String, String> navigationStrings) {
        return createSimpleCompiledTask(taskName, false, navigationStrings);
    }

    private Task createSimpleCompiledTask(String taskName, boolean isAsync) {
        Map<String, String> navigationStrings = new HashMap<>();
        navigationStrings.put(ScoreLangConstants.SUCCESS_RESULT, ScoreLangConstants.SUCCESS_RESULT);
        navigationStrings.put(ScoreLangConstants.FAILURE_RESULT, ScoreLangConstants.FAILURE_RESULT);

        return createSimpleCompiledTask(taskName, isAsync, navigationStrings);
    }

    private Task createSimpleCompiledTask(String taskName, boolean isAsync, Map<String, String> navigationStrings) {
        Map<String, Serializable> preTaskActionData = new HashMap<>();

        if (isAsync) {
            preTaskActionData.put(ScoreLangConstants.ASYNC_LOOP_KEY, "value in values");
        }

        Map<String, Serializable> postTaskActionData = new HashMap<>();
        String refId = "refId";
        return new Task(
                taskName,
                preTaskActionData,
                postTaskActionData,
                null,
                navigationStrings,
                refId,
                isAsync);
    }

    private List<Result> defaultFlowResults() {
        List<Result> results = new ArrayList<>();
        results.add(new Result(ScoreLangConstants.SUCCESS_RESULT, null));
        results.add(new Result(ScoreLangConstants.FAILURE_RESULT, null));
        return results;
    }

    private void mockStartStep(Executable executable) {
        Map<String, Serializable> preExecActionData = executable.getPreExecActionData();
        String execName = executable.getName();
        List<Input> inputs = executable.getInputs();
        when(stepFactory.createStartStep(eq(1L), same(preExecActionData), same(inputs), same(execName))).thenReturn(new ExecutionStep(1L));
    }

    private void mockEndStep(Long stepId, Executable executable) {
        Map<String, Serializable> postExecActionData = executable.getPostExecActionData();
        List<Output> outputs = executable.getOutputs();
        List<Result> results = executable.getResults();
        String execName = executable.getName();
        when(stepFactory.createEndStep(eq(stepId), same(postExecActionData), same(outputs), same(results), same(execName))).thenReturn(new ExecutionStep(stepId));
    }

    private void mockFinishTask(Long stepId, Task task) {
        mockFinishTask(stepId, task, false);
    }

    private void mockFinishAsyncTask(Long stepId, Task task) {
        mockFinishTask(stepId, task, true);
    }

    private void mockFinishTask(Long stepId, Task task, boolean isAsync) {
        Map<String, Serializable> postTaskActionData = task.getPostTaskActionData();
        String taskName = task.getName();
        when(stepFactory.createFinishTaskStep(eq(stepId), eq(postTaskActionData), anyMapOf(String.class, ResultNavigation.class), eq(taskName), eq(isAsync))).thenReturn(new ExecutionStep(stepId));
    }

    private void mockBeginTask(Long stepId, Task task) {
        Map<String, Serializable> preTaskActionData = task.getPreTaskActionData();
        String refId = task.getRefId();
        String name = task.getName();
        when(stepFactory.createBeginTaskStep(eq(stepId), anyListOf(Input.class), eq(preTaskActionData), eq(refId), eq(name))).thenReturn(new ExecutionStep(stepId));
    }

    private void mockAddBranchesStep(Long stepId, Long nextStepID, Long branchBeginStepID, Task task, Flow flow) {
        Map<String, Serializable> preTaskActionData = task.getPreTaskActionData();
        String refId = flow.getId();
        String name = task.getName();
        when(stepFactory.createAddBranchesStep(eq(stepId), eq(nextStepID), eq(branchBeginStepID), eq(preTaskActionData), eq(refId), eq(name))).thenReturn(new ExecutionStep(stepId));
    }

    private void mockJoinBranchesStep(Long stepId, Task task) {
        Map<String, Serializable> postTaskActionData = task.getPostTaskActionData();
        String taskName = task.getName();
        when(stepFactory.createJoinBranchesStep(eq(stepId), eq(postTaskActionData), anyMapOf(String.class, ResultNavigation.class), eq(taskName))).thenReturn(new ExecutionStep(stepId));
    }

    @Test
    public void testCreateOperationExecutionPlan() throws Exception {
        Map<String, Serializable> preOpActionData = new HashMap<>();
        Map<String, Serializable> postOpActionData = new HashMap<>();
        Map<String, Serializable> actionData = new HashMap<>();
        Action action = new Action(actionData);
        String operationName = "opName";
        String opNamespace = "user.flows";
        List<Input> inputs = new ArrayList<>();
        List<Output> outputs = new ArrayList<>();
        List<Result> results = new ArrayList<>();

        Operation compiledOperation =
                new Operation(preOpActionData, postOpActionData, action, opNamespace, operationName, inputs, outputs, results, null);

        mockStartStep(compiledOperation);
        when(stepFactory.createActionStep(eq(2L), same(actionData))).thenReturn(new ExecutionStep(2L));
        mockEndStep(3L, compiledOperation);

        ExecutionPlan executionPlan = executionPlanBuilder.createOperationExecutionPlan(compiledOperation);

        assertEquals("different number of execution steps than expected", 3, executionPlan.getSteps().size());
        assertEquals("operation name is different than expected", operationName, executionPlan.getName());
        assertEquals("language name is different than expected", "CloudSlang", executionPlan.getLanguage());
        assertEquals("begin step is different than expected", new Long(1), executionPlan.getBeginStep());
    }

    @Test
    public void createSimpleFlow() throws Exception {
        Map<String, Serializable> preFlowActionData = new HashMap<>();
        Map<String, Serializable> postFlowActionData = new HashMap<>();
        Deque<Task> tasks = new LinkedList<>();
        Task task = createSimpleCompiledTask("taskName");
        tasks.add(task);
        Workflow workflow = new Workflow(tasks);
        String flowName = "flowName";
        String flowNamespace = "user.flows";
        List<Input> inputs = new ArrayList<>();
        List<Output> outputs = new ArrayList<>();
        List<Result> results = defaultFlowResults();

        Flow compiledFlow =
                new Flow(preFlowActionData, postFlowActionData, workflow, flowNamespace, flowName, inputs, outputs, results, null);

        mockStartStep(compiledFlow);
        mockEndStep(0L, compiledFlow);
        mockBeginTask(2L, task);
        mockFinishTask(3L, task);
        ExecutionPlan executionPlan = executionPlanBuilder.createFlowExecutionPlan(compiledFlow);

        assertEquals("different number of execution steps than expected", 4, executionPlan.getSteps().size());
        assertEquals("flow name is different than expected", flowName, executionPlan.getName());
        assertEquals("language name is different than expected", "CloudSlang", executionPlan.getLanguage());
        assertEquals("begin step is different than expected", new Long(1), executionPlan.getBeginStep());
    }

    @Test
    public void createSimpleFlowWithAsyncLoop() throws Exception {
        Map<String, Serializable> preFlowActionData = new HashMap<>();
        Map<String, Serializable> postFlowActionData = new HashMap<>();
        Deque<Task> tasks = new LinkedList<>();
        Task task = createSimpleCompiledAsyncTask("taskName");
        tasks.add(task);
        Workflow workflow = new Workflow(tasks);
        String flowName = "flowName";
        String flowNamespace = "user.flows";
        List<Input> inputs = new ArrayList<>();
        List<Output> outputs = new ArrayList<>();
        List<Result> results = defaultFlowResults();

        Flow compiledFlow =
                new Flow(preFlowActionData, postFlowActionData, workflow, flowNamespace, flowName, inputs, outputs, results, null);

        mockStartStep(compiledFlow);
        mockEndStep(0L, compiledFlow);
        mockAddBranchesStep(2L, 5L, 3L, task, compiledFlow);
        mockBeginTask(3L, task);
        mockFinishAsyncTask(4L, task);
        mockJoinBranchesStep(5L, task);
        ExecutionPlan executionPlan = executionPlanBuilder.createFlowExecutionPlan(compiledFlow);

        verify(stepFactory).createAddBranchesStep(
                eq(2L),
                eq(5L),
                eq(3L),
                eq(task.getPreTaskActionData()),
                eq(compiledFlow.getId()),
                eq(task.getName()));
        verify(stepFactory).createBeginTaskStep(eq(3L), anyListOf(Input.class), eq(task.getPreTaskActionData()), eq(task.getRefId()), eq(task.getName()));
        verify(stepFactory).createFinishTaskStep(eq(4L), eq(task.getPostTaskActionData()), anyMapOf(String.class, ResultNavigation.class), eq(task.getName()), eq(task.isAsync()));
        verify(stepFactory).createJoinBranchesStep(eq(5L), eq(task.getPostTaskActionData()), anyMapOf(String.class, ResultNavigation.class), eq(task.getName()));

        assertEquals("different number of execution steps than expected", 6, executionPlan.getSteps().size());
        assertEquals("flow name is different than expected", flowName, executionPlan.getName());
        assertEquals("language name is different than expected", "CloudSlang", executionPlan.getLanguage());
        assertEquals("begin step is different than expected", new Long(1), executionPlan.getBeginStep());
    }

    @Test
    public void createFlowWithTwoTasks() throws Exception {
        Deque<Task> tasks = new LinkedList<>();
        String secondTaskName = "2ndTask";
        HashMap<String, String> navigationStrings = new HashMap<>();
        navigationStrings.put(ScoreLangConstants.SUCCESS_RESULT, secondTaskName);
        navigationStrings.put(ScoreLangConstants.FAILURE_RESULT, ScoreLangConstants.FAILURE_RESULT);
        Task firstTask = createSimpleCompiledTask("firstTaskName", navigationStrings);
        Task secondTask = createSimpleCompiledTask(secondTaskName);
        tasks.add(firstTask);
        tasks.add(secondTask);
        Map<String, Serializable> preFlowActionData = new HashMap<>();
        Map<String, Serializable> postFlowActionData = new HashMap<>();

        Workflow workflow = new Workflow(tasks);
        String flowName = "flowName";
        String flowNamespace = "user.flows";
        List<Input> inputs = new ArrayList<>();
        List<Output> outputs = new ArrayList<>();
        List<Result> results = defaultFlowResults();


        Flow compiledFlow =
                new Flow(preFlowActionData, postFlowActionData, workflow, flowNamespace, flowName, inputs, outputs, results, null);

        mockStartStep(compiledFlow);
        mockEndStep(0L, compiledFlow);

        mockBeginTask(2L, firstTask);
        mockFinishTask(3L, firstTask);
        mockBeginTask(4L, secondTask);
        mockFinishTask(5L, secondTask);
        ExecutionPlan executionPlan = executionPlanBuilder.createFlowExecutionPlan(compiledFlow);

        assertEquals("different number of execution steps than expected", 6, executionPlan.getSteps().size());
        assertEquals("flow name is different than expected", flowName, executionPlan.getName());
        assertEquals("language name is different than expected", "CloudSlang", executionPlan.getLanguage());
        assertEquals("begin step is different than expected", new Long(1), executionPlan.getBeginStep());
    }

    @Test
    public void createFlowWithNoTasksShouldThrowException() throws Exception {
        Map<String, Serializable> preFlowActionData = new HashMap<>();
        Map<String, Serializable> postFlowActionData = new HashMap<>();
        Deque<Task> tasks = new LinkedList<>();
        Workflow workflow = new Workflow(tasks);
        String flowName = "flowName";
        String flowNamespace = "user.flows";
        List<Input> inputs = new ArrayList<>();
        List<Output> outputs = new ArrayList<>();
        List<Result> results = new ArrayList<>();

        Flow compiledFlow =
                new Flow(preFlowActionData, postFlowActionData, workflow, flowNamespace, flowName, inputs, outputs, results, null);

        mockStartStep(compiledFlow);
        mockEndStep(0L, compiledFlow);

        exception.expect(RuntimeException.class);
        exception.expectMessage(flowName);
        executionPlanBuilder.createFlowExecutionPlan(compiledFlow);
    }
}