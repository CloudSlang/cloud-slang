package com.hp.score.lang.compiler.utils;

import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.ExecutionStep;
import com.hp.score.lang.compiler.domain.*;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
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

    private void mockFinishTask(Long stepId, CompiledTask compiledTask, Map<String, Long> navigationValues) {
        Map<String, Serializable> postTaskActionData = compiledTask.getPostTaskActionData();
        String taskName = compiledTask.getName();
        when(stepFactory.createFinishTaskStep(eq(stepId), same(postTaskActionData), eq(navigationValues), same(taskName))).thenReturn(new ExecutionStep(stepId));
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
        List<Input> inputs = new ArrayList<>();
        List<Output> outputs = new ArrayList<>();
        List<Result> results = new ArrayList<>();

        CompiledOperation compiledOperation =
                new CompiledOperation(preOpActionData, postOpActionData, compiledDoAction, operationName, inputs, outputs, results);

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
        List<CompiledTask> compiledTasks = new ArrayList<>();
        CompiledTask compiledTask = createSimpleCompiledTask("taskName");
        compiledTasks.add(compiledTask);
        CompiledWorkflow compiledWorkflow = new CompiledWorkflow(compiledTasks);
        String flowName = "flowName";
        List<Input> inputs = new ArrayList<>();
        List<Output> outputs = new ArrayList<>();
        List<Result> results = defaultFlowResults();

        Map<String, Long> navigationValues = new HashMap<>();
        navigationValues.put(ScoreLangConstants.SUCCESS_RESULT, 0L);
        navigationValues.put(ScoreLangConstants.FAILURE_RESULT, 0L);

        CompiledFlow compiledFlow =
                new CompiledFlow(preFlowActionData, postFlowActionData, compiledWorkflow, flowName, inputs, outputs, results);

        mockStartStep(compiledFlow);
        mockEndStep(0L, compiledFlow);

        mockBeginTask(2L, compiledTask);
        mockFinishTask(3L, compiledTask, navigationValues);
        ExecutionPlan executionPlan = executionPlanBuilder.createFlowExecutionPlan(compiledFlow);

        assertEquals("different number of execution steps than expected", 4, executionPlan.getSteps().size());
        assertEquals("flow name is different than expected", flowName, executionPlan.getName());
        assertEquals("language name is different than expected", "slang", executionPlan.getLanguage());
        assertEquals("begin step is different than expected", new Long(1), executionPlan.getBeginStep());
    }

    @Test
    public void createFlowWithTwoTasks() throws Exception {
        List<CompiledTask> compiledTasks = new ArrayList<>();
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
        List<Input> inputs = new ArrayList<>();
        List<Output> outputs = new ArrayList<>();
        List<Result> results = defaultFlowResults();


        CompiledFlow compiledFlow =
                new CompiledFlow(preFlowActionData, postFlowActionData, compiledWorkflow, flowName, inputs, outputs, results);

        mockStartStep(compiledFlow);
        mockEndStep(0L, compiledFlow);

        Map<String, Long> firstNavigationValues = new HashMap<>();
        firstNavigationValues.put(ScoreLangConstants.SUCCESS_RESULT, 4L);
        firstNavigationValues.put(ScoreLangConstants.FAILURE_RESULT, 0L);

        Map<String, Long> secondNavigationValues = new HashMap<>();
        secondNavigationValues.put(ScoreLangConstants.SUCCESS_RESULT, 0L);
        secondNavigationValues.put(ScoreLangConstants.FAILURE_RESULT, 0L);

        mockBeginTask(2L, firstCompiledTask);
        mockFinishTask(3L, firstCompiledTask, firstNavigationValues);
        mockBeginTask(4L, secondCompiledTask);
        mockFinishTask(5L, secondCompiledTask, secondNavigationValues);
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
        ArrayList<CompiledTask> compiledTasks = new ArrayList<>();
        CompiledWorkflow compiledWorkflow = new CompiledWorkflow(compiledTasks);
        String flowName = "flowName";
        List<Input> inputs = new ArrayList<>();
        List<Output> outputs = new ArrayList<>();
        List<Result> results = new ArrayList<>();

        CompiledFlow compiledFlow =
                new CompiledFlow(preFlowActionData, postFlowActionData, compiledWorkflow, flowName, inputs, outputs, results);

        mockStartStep(compiledFlow);
        mockEndStep(0L, compiledFlow);

        exception.expect(RuntimeException.class);
        exception.expectMessage(flowName);
        executionPlanBuilder.createFlowExecutionPlan(compiledFlow);
    }
}