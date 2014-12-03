/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package com.hp.score.lang.compiler.utils;

import org.eclipse.score.api.ExecutionPlan;
import org.eclipse.score.api.ExecutionStep;
import com.hp.score.lang.compiler.domain.*;
import com.hp.score.lang.entities.ResultNavigation;
import com.hp.score.lang.entities.ScoreLangConstants;
import com.hp.score.lang.entities.bindings.Input;
import com.hp.score.lang.entities.bindings.Output;
import com.hp.score.lang.entities.bindings.Result;

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
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExecutionPlanBuilderTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private ExecutionPlanBuilder executionPlanBuilder;

    @Mock
    private ExecutionStepFactory stepFactory;

    private CompiledTask createSimpleCompiledTask(String taskName) {
        Map<String, String> navigationStrings = new HashMap<>();
        navigationStrings.put(ScoreLangConstants.SUCCESS_RESULT, ScoreLangConstants.SUCCESS_RESULT);
        navigationStrings.put(ScoreLangConstants.FAILURE_RESULT, ScoreLangConstants.FAILURE_RESULT);
        return createSimpleCompiledTask(taskName, navigationStrings);
    }

    private CompiledTask createSimpleCompiledTask(String taskName, Map<String, String> navigationStrings) {
        Map<String, Serializable> preTaskActionData = new HashMap<>();
        Map<String, Serializable> postTaskActionData = new HashMap<>();
        String refId = "refId";
        return new CompiledTask(
                taskName,
                preTaskActionData,
                postTaskActionData,
                navigationStrings,
                refId
        );
    }

    private List<Result> defaultFlowResults() {
        List<Result> results = new ArrayList<>();
        results.add(new Result(ScoreLangConstants.SUCCESS_RESULT, null));
        results.add(new Result(ScoreLangConstants.FAILURE_RESULT, null));
        return results;
    }

    private void mockStartStep(CompiledExecutable compiledExecutable) {
        Map<String, Serializable> preExecActionData = compiledExecutable.getPreExecActionData();
        String execName = compiledExecutable.getName();
        List<Input> inputs = compiledExecutable.getInputs();
        when(stepFactory.createStartStep(eq(1L), same(preExecActionData), same(inputs), same(execName))).thenReturn(new ExecutionStep(1L));
    }

    private void mockEndStep(Long stepId, CompiledExecutable compiledExecutable) {
        Map<String, Serializable> postExecActionData = compiledExecutable.getPostExecActionData();
        List<Output> outputs = compiledExecutable.getOutputs();
        List<Result> results = compiledExecutable.getResults();
        String execName = compiledExecutable.getName();
        when(stepFactory.createEndStep(eq(stepId), same(postExecActionData), same(outputs), same(results), same(execName))).thenReturn(new ExecutionStep(stepId));
    }

    private void mockFinishTask(Long stepId, CompiledTask compiledTask) {
        Map<String, Serializable> postTaskActionData = compiledTask.getPostTaskActionData();
        String taskName = compiledTask.getName();
        when(stepFactory.createFinishTaskStep(eq(stepId), same(postTaskActionData), anyMapOf(String.class, ResultNavigation.class), same(taskName))).thenReturn(new ExecutionStep(stepId));
    }

    private void mockBeginTask(Long stepId, CompiledTask compiledTask) {
        Map<String, Serializable> preTaskActionData = compiledTask.getPreTaskActionData();
        String refId = compiledTask.getRefId();
        String name = compiledTask.getName();
        when(stepFactory.createBeginTaskStep(eq(stepId), same(preTaskActionData), same(refId), same(name))).thenReturn(new ExecutionStep(stepId));
    }

    @Test
    public void testCreateOperationExecutionPlan() throws Exception {
        Map<String, Serializable> preOpActionData = new HashMap<>();
        Map<String, Serializable> postOpActionData = new HashMap<>();
        Map<String, Serializable> actionData = new HashMap<>();
        CompiledDoAction compiledDoAction = new CompiledDoAction(actionData);
        String operationName = "opName";
        String opNamespace = "user.flows";
        List<Input> inputs = new ArrayList<>();
        List<Output> outputs = new ArrayList<>();
        List<Result> results = new ArrayList<>();

        CompiledOperation compiledOperation =
                new CompiledOperation(preOpActionData, postOpActionData, compiledDoAction, opNamespace, operationName, inputs, outputs, results);

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
        Deque<CompiledTask> compiledTasks = new LinkedList<>();
        CompiledTask compiledTask = createSimpleCompiledTask("taskName");
        compiledTasks.add(compiledTask);
        CompiledWorkflow compiledWorkflow = new CompiledWorkflow(compiledTasks);
        String flowName = "flowName";
        String flowNamespace = "user.flows";
        List<Input> inputs = new ArrayList<>();
        List<Output> outputs = new ArrayList<>();
        List<Result> results = defaultFlowResults();

        CompiledFlow compiledFlow =
                new CompiledFlow(preFlowActionData, postFlowActionData, compiledWorkflow, flowNamespace, flowName, inputs, outputs, results);

        mockStartStep(compiledFlow);
        mockEndStep(0L, compiledFlow);
        mockBeginTask(2L, compiledTask);
        mockFinishTask(3L, compiledTask);
        ExecutionPlan executionPlan = executionPlanBuilder.createFlowExecutionPlan(compiledFlow);

        assertEquals("different number of execution steps than expected", 4, executionPlan.getSteps().size());
        assertEquals("flow name is different than expected", flowName, executionPlan.getName());
        assertEquals("language name is different than expected", "slang", executionPlan.getLanguage());
        assertEquals("begin step is different than expected", new Long(1), executionPlan.getBeginStep());
    }

    @Test
    public void createFlowWithTwoTasks() throws Exception {
        Deque<CompiledTask> compiledTasks = new LinkedList<>();
        String secondTaskName = "2ndTask";
        HashMap<String, String> navigationStrings = new HashMap<>();
        navigationStrings.put(ScoreLangConstants.SUCCESS_RESULT, secondTaskName);
        navigationStrings.put(ScoreLangConstants.FAILURE_RESULT, ScoreLangConstants.FAILURE_RESULT);
        CompiledTask firstCompiledTask = createSimpleCompiledTask("firstTaskName", navigationStrings);
        CompiledTask secondCompiledTask = createSimpleCompiledTask(secondTaskName);
        compiledTasks.add(firstCompiledTask);
        compiledTasks.add(secondCompiledTask);
        Map<String, Serializable> preFlowActionData = new HashMap<>();
        Map<String, Serializable> postFlowActionData = new HashMap<>();

        CompiledWorkflow compiledWorkflow = new CompiledWorkflow(compiledTasks);
        String flowName = "flowName";
        String flowNamespace = "user.flows";
        List<Input> inputs = new ArrayList<>();
        List<Output> outputs = new ArrayList<>();
        List<Result> results = defaultFlowResults();


        CompiledFlow compiledFlow =
                new CompiledFlow(preFlowActionData, postFlowActionData, compiledWorkflow, flowNamespace, flowName, inputs, outputs, results);

        mockStartStep(compiledFlow);
        mockEndStep(0L, compiledFlow);

        mockBeginTask(2L, firstCompiledTask);
        mockFinishTask(3L, firstCompiledTask);
        mockBeginTask(4L, secondCompiledTask);
        mockFinishTask(5L, secondCompiledTask);
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
        Deque<CompiledTask> compiledTasks = new LinkedList<>();
        CompiledWorkflow compiledWorkflow = new CompiledWorkflow(compiledTasks);
        String flowName = "flowName";
        String flowNamespace = "user.flows";
        List<Input> inputs = new ArrayList<>();
        List<Output> outputs = new ArrayList<>();
        List<Result> results = new ArrayList<>();

        CompiledFlow compiledFlow =
                new CompiledFlow(preFlowActionData, postFlowActionData, compiledWorkflow, flowNamespace, flowName, inputs, outputs, results);

        mockStartStep(compiledFlow);
        mockEndStep(0L, compiledFlow);

        exception.expect(RuntimeException.class);
        exception.expectMessage(flowName);
        executionPlanBuilder.createFlowExecutionPlan(compiledFlow);
    }
}