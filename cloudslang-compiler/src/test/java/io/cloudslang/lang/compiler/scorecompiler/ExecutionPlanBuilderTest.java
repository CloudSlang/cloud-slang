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
import io.cloudslang.lang.entities.ExecutableType;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Workflow;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.compiler.modeller.model.Action;
import io.cloudslang.lang.compiler.modeller.model.Operation;
import io.cloudslang.lang.compiler.modeller.model.Step;
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
import java.util.*;

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

    private Set<String> systemPropertyDependencies = Collections.emptySet();

    private Step createSimpleCompiledAsyncTask(String taskName) {
        return createSimpleCompiledTask(taskName, true);
    }

    private Step createSimpleCompiledTask(String taskName) {
        return createSimpleCompiledTask(taskName, false);
    }

    private Step createSimpleCompiledTask(String taskName, List<Map<String, String>> navigationStrings) {
        return createSimpleCompiledTask(taskName, false, navigationStrings);
    }

    private Step createSimpleCompiledTask(String taskName, boolean isAsync) {
        List<Map<String, String>> navigationStrings = new ArrayList<>();
        Map<String, String> successMap = new HashMap<>();
        successMap.put(ScoreLangConstants.SUCCESS_RESULT, ScoreLangConstants.SUCCESS_RESULT);
        Map<String, String> failureMap = new HashMap<>();
        failureMap.put(ScoreLangConstants.FAILURE_RESULT, ScoreLangConstants.FAILURE_RESULT);
        navigationStrings.add(successMap);
        navigationStrings.add(failureMap);

        return createSimpleCompiledTask(taskName, isAsync, navigationStrings);
    }

    private Step createSimpleCompiledTask(String taskName, boolean isAsync, List<Map<String, String>> navigationStrings) {
        Map<String, Serializable> preTaskActionData = new HashMap<>();

        if (isAsync) {
            preTaskActionData.put(ScoreLangConstants.ASYNC_LOOP_KEY, "value in values");
        }

        Map<String, Serializable> postTaskActionData = new HashMap<>();
        String refId = "refId";
        return new Step(
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

    private void mockEndStep(Long stepId, Executable executable, ExecutableType executableType) {
        Map<String, Serializable> postExecActionData = executable.getPostExecActionData();
        List<Output> outputs = executable.getOutputs();
        List<Result> results = executable.getResults();
        String execName = executable.getName();
        when(stepFactory.createEndStep(eq(stepId), same(postExecActionData), same(outputs), same(results), same(execName), same(executableType))).thenReturn(new ExecutionStep(stepId));
    }

    private void mockFinishTask(Long stepId, Step step) {
        mockFinishTask(stepId, step, false);
    }

    private void mockFinishAsyncTask(Long stepId, Step step) {
        mockFinishTask(stepId, step, true);
    }

    private void mockFinishTask(Long stepId, Step step, boolean isAsync) {
        Map<String, Serializable> postTaskActionData = step.getPostStepActionData();
        String taskName = step.getName();
        when(stepFactory.createFinishTaskStep(eq(stepId), eq(postTaskActionData), anyMapOf(String.class, ResultNavigation.class), eq(taskName), eq(isAsync))).thenReturn(new ExecutionStep(stepId));
    }

    private void mockBeginTask(Long stepId, Step step) {
        Map<String, Serializable> preTaskActionData = step.getPreTaskActionData();
        String refId = step.getRefId();
        String name = step.getName();
        when(stepFactory.createBeginTaskStep(eq(stepId), anyListOf(Argument.class), eq(preTaskActionData), eq(refId), eq(name))).thenReturn(new ExecutionStep(stepId));
    }

    private void mockAddBranchesStep(Long stepId, Long nextStepID, Long branchBeginStepID, Step step, Flow flow) {
        Map<String, Serializable> preTaskActionData = step.getPreTaskActionData();
        String refId = flow.getId();
        String name = step.getName();
        when(stepFactory.createAddBranchesStep(eq(stepId), eq(nextStepID), eq(branchBeginStepID), eq(preTaskActionData), eq(refId), eq(name))).thenReturn(new ExecutionStep(stepId));
    }

    private void mockJoinBranchesStep(Long stepId, Step step) {
        Map<String, Serializable> postTaskActionData = step.getPostStepActionData();
        String taskName = step.getName();
        when(stepFactory.createJoinBranchesStep(eq(stepId), eq(postTaskActionData), anyMapOf(String.class, ResultNavigation.class), eq(taskName))).thenReturn(new ExecutionStep(stepId));
    }

    @Test
    public void testCreateOperationExecutionPlan() throws Exception {
        Map<String, Serializable> preOpActionData = new HashMap<>();
        Map<String, Serializable> postOpActionData = new HashMap<>();
        Map<String, Serializable> actionData = new HashMap<>();
        Action action = new Action(actionData);
        String operationName = "operationName";
        String opNamespace = "user.flows";
        List<Input> inputs = new ArrayList<>();
        List<Output> outputs = new ArrayList<>();
        List<Result> results = new ArrayList<>();

        Operation compiledOperation =
                new Operation(preOpActionData, postOpActionData, action, opNamespace, operationName, inputs, outputs, results, null, systemPropertyDependencies);

        mockStartStep(compiledOperation);
        when(stepFactory.createActionStep(eq(2L), same(actionData))).thenReturn(new ExecutionStep(2L));
        mockEndStep(3L, compiledOperation, ExecutableType.OPERATION);

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
        Deque<Step> steps = new LinkedList<>();
        Step step = createSimpleCompiledTask("taskName");
        steps.add(step);
        Workflow workflow = new Workflow(steps);
        String flowName = "flowName";
        String flowNamespace = "user.flows";
        List<Input> inputs = new ArrayList<>();
        List<Output> outputs = new ArrayList<>();
        List<Result> results = defaultFlowResults();

        Flow compiledFlow =
                new Flow(preFlowActionData, postFlowActionData, workflow, flowNamespace, flowName, inputs, outputs, results, null, systemPropertyDependencies);

        mockStartStep(compiledFlow);
        mockEndStep(0L, compiledFlow, ExecutableType.FLOW);
        mockBeginTask(2L, step);
        mockFinishTask(3L, step);
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
        Deque<Step> steps = new LinkedList<>();
        Step step = createSimpleCompiledAsyncTask("taskName");
        steps.add(step);
        Workflow workflow = new Workflow(steps);
        String flowName = "flowName";
        String flowNamespace = "user.flows";
        List<Input> inputs = new ArrayList<>();
        List<Output> outputs = new ArrayList<>();
        List<Result> results = defaultFlowResults();

        Flow compiledFlow =
                new Flow(preFlowActionData, postFlowActionData, workflow, flowNamespace, flowName, inputs, outputs, results, null, systemPropertyDependencies);

        mockStartStep(compiledFlow);
        mockEndStep(0L, compiledFlow, ExecutableType.FLOW);
        mockAddBranchesStep(2L, 5L, 3L, step, compiledFlow);
        mockBeginTask(3L, step);
        mockFinishAsyncTask(4L, step);
        mockJoinBranchesStep(5L, step);
        ExecutionPlan executionPlan = executionPlanBuilder.createFlowExecutionPlan(compiledFlow);

        verify(stepFactory).createAddBranchesStep(
                eq(2L),
                eq(5L),
                eq(3L),
                eq(step.getPreTaskActionData()),
                eq(compiledFlow.getId()),
                eq(step.getName()));
        verify(stepFactory).createBeginTaskStep(eq(3L), anyListOf(Argument.class), eq(step.getPreTaskActionData()), eq(step.getRefId()), eq(step.getName()));
        verify(stepFactory).createFinishTaskStep(eq(4L), eq(step.getPostStepActionData()), anyMapOf(String.class, ResultNavigation.class), eq(step.getName()), eq(step.isAsync()));
        verify(stepFactory).createJoinBranchesStep(eq(5L), eq(step.getPostStepActionData()), anyMapOf(String.class, ResultNavigation.class), eq(step.getName()));

        assertEquals("different number of execution steps than expected", 6, executionPlan.getSteps().size());
        assertEquals("flow name is different than expected", flowName, executionPlan.getName());
        assertEquals("language name is different than expected", "CloudSlang", executionPlan.getLanguage());
        assertEquals("begin step is different than expected", new Long(1), executionPlan.getBeginStep());
    }

    @Test
    public void createFlowWithTwoTasks() throws Exception {
        Deque<Step> steps = new LinkedList<>();
        String secondTaskName = "2ndTask";
        List<Map<String, String>> navigationStrings = new ArrayList<>();
        Map<String, String> successMap = new HashMap<>();
        successMap.put(ScoreLangConstants.SUCCESS_RESULT, secondTaskName);
        Map<String, String> failureMap = new HashMap<>();
        failureMap.put(ScoreLangConstants.FAILURE_RESULT, ScoreLangConstants.FAILURE_RESULT);
        navigationStrings.add(successMap);
        navigationStrings.add(failureMap);
        Step firstStep = createSimpleCompiledTask("firstTaskName", navigationStrings);
        Step secondStep = createSimpleCompiledTask(secondTaskName);
        steps.add(firstStep);
        steps.add(secondStep);
        Map<String, Serializable> preFlowActionData = new HashMap<>();
        Map<String, Serializable> postFlowActionData = new HashMap<>();

        Workflow workflow = new Workflow(steps);
        String flowName = "flowName";
        String flowNamespace = "user.flows";
        List<Input> inputs = new ArrayList<>();
        List<Output> outputs = new ArrayList<>();
        List<Result> results = defaultFlowResults();


        Flow compiledFlow =
                new Flow(preFlowActionData, postFlowActionData, workflow, flowNamespace, flowName, inputs, outputs, results, null, systemPropertyDependencies);

        mockStartStep(compiledFlow);
        mockEndStep(0L, compiledFlow, ExecutableType.FLOW);

        mockBeginTask(2L, firstStep);
        mockFinishTask(3L, firstStep);
        mockBeginTask(4L, secondStep);
        mockFinishTask(5L, secondStep);
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
        Deque<Step> steps = new LinkedList<>();
        Workflow workflow = new Workflow(steps);
        String flowName = "flowName";
        String flowNamespace = "user.flows";
        List<Input> inputs = new ArrayList<>();
        List<Output> outputs = new ArrayList<>();
        List<Result> results = new ArrayList<>();

        Flow compiledFlow =
                new Flow(preFlowActionData, postFlowActionData, workflow, flowNamespace, flowName, inputs, outputs, results, null, systemPropertyDependencies);

        mockStartStep(compiledFlow);
        mockEndStep(0L, compiledFlow, ExecutableType.FLOW);

        exception.expect(RuntimeException.class);
        exception.expectMessage(flowName);
        executionPlanBuilder.createFlowExecutionPlan(compiledFlow);
    }
}