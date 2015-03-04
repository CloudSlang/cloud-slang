/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/
package org.openscore.lang.compiler.scorecompiler;

import org.openscore.lang.compiler.scorecompiler.ExecutionPlanBuilder;
import org.openscore.lang.compiler.scorecompiler.ExecutionStepFactory;
import org.openscore.lang.compiler.modeller.model.Action;
import org.openscore.lang.compiler.modeller.model.Executable;
import org.openscore.lang.compiler.modeller.model.Flow;
import org.openscore.lang.compiler.modeller.model.Operation;
import org.openscore.lang.compiler.modeller.model.Task;
import org.openscore.lang.compiler.modeller.model.Workflow;
import org.openscore.lang.entities.ResultNavigation;
import org.openscore.lang.entities.ScoreLangConstants;
import org.openscore.lang.entities.bindings.Input;
import org.openscore.lang.entities.bindings.Output;
import org.openscore.lang.entities.bindings.Result;
import org.openscore.api.ExecutionPlan;
import org.openscore.api.ExecutionStep;
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
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExecutionPlanBuilderTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private ExecutionPlanBuilder executionPlanBuilder;

    @Mock
    private ExecutionStepFactory stepFactory;

    private Task createSimpleCompiledTask(String taskName) {
        Map<String, String> navigationStrings = new HashMap<>();
        navigationStrings.put(ScoreLangConstants.SUCCESS_RESULT, ScoreLangConstants.SUCCESS_RESULT);
        navigationStrings.put(ScoreLangConstants.FAILURE_RESULT, ScoreLangConstants.FAILURE_RESULT);
        return createSimpleCompiledTask(taskName, navigationStrings);
    }

	private Task createSimpleCompiledTask(String taskName, Map<String, String> navigationStrings) {
		Map<String, Serializable> preTaskActionData = new HashMap<>();
		Map<String, Serializable> postTaskActionData = new HashMap<>();
		String refId = "refId";
		return new Task(taskName, preTaskActionData, postTaskActionData, null, navigationStrings, refId);
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
        Map<String, Serializable> postTaskActionData = task.getPostTaskActionData();
        String taskName = task.getName();
        when(stepFactory.createFinishTaskStep(eq(stepId), same(postTaskActionData), anyMapOf(String.class, ResultNavigation.class), same(taskName))).thenReturn(new ExecutionStep(stepId));
    }

    private void mockBeginTask(Long stepId, Task task) {
        Map<String, Serializable> preTaskActionData = task.getPreTaskActionData();
        String refId = task.getRefId();
        String name = task.getName();
        when(stepFactory.createBeginTaskStep(eq(stepId), anyListOf(Input.class), same(preTaskActionData), same(refId), same(name))).thenReturn(new ExecutionStep(stepId));
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
        assertEquals("language name is different than expected", "slang", executionPlan.getLanguage());
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
        assertEquals("language name is different than expected", "slang", executionPlan.getLanguage());
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
        assertEquals("language name is different than expected", "slang", executionPlan.getLanguage());
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