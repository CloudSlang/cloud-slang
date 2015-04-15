/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Task;
import io.cloudslang.lang.entities.AsyncLoopStatement;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Date: 3/25/2015
 *
 * @author Bonczidai Levente
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class CompileAsyncLoopFlowTest {

    @Autowired
    private SlangCompiler compiler;

    @Test
    public void testPreCompileAsyncLoopFlow() throws Exception {
        Task task = getTasksAfterPrecompileFlow("/loops/async_loop/simple_async_loop.sl").getFirst();

        verifyAsyncLoopStatement(task);

        List<Output> aggregateValues = getAggregateOutputs(task);
        assertEquals("aggregate list is not empty", 0, aggregateValues.size());

        List<Output> publishValues = getPublishOutputs(task);
        assertEquals("aggregate list is not empty", 0, publishValues.size());

        Map<String, String> expectedNavigationStrings = new HashMap<>();
        expectedNavigationStrings.put("SUCCESS", "SUCCESS");
        expectedNavigationStrings.put("FAILURE", "FAILURE");
        verifyNavigationStrings(expectedNavigationStrings, task);

        assertTrue(task.isAsync());
    }

    @Test
    public void testPreCompileAsyncLoopFlowAggregate() throws Exception {
        Task task = getTasksAfterPrecompileFlow("/loops/async_loop/async_loop_aggregate.sl").getFirst();

        verifyAsyncLoopStatement(task);

        List<Output> aggregateValues = getAggregateOutputs(task);
        assertEquals(2, aggregateValues.size());
        assertEquals("map(lambda x:str(x['name']), branches_context)", aggregateValues.get(0).getExpression());

        List<Output> publishValues = getPublishOutputs(task);
        assertEquals("aggregate list is not empty", 2, publishValues.size());
        assertEquals("name", publishValues.get(0).getExpression());

        Map<String, String> expectedNavigationStrings = new HashMap<>();
        expectedNavigationStrings.put("SUCCESS", "SUCCESS");
        expectedNavigationStrings.put("FAILURE", "FAILURE");
        verifyNavigationStrings(expectedNavigationStrings, task);

        assertTrue(task.isAsync());
    }

    @Test
    public void testPreCompileAsyncLoopFlowNavigate() throws Exception {
        Deque<Task> tasks = getTasksAfterPrecompileFlow("/loops/async_loop/async_loop_navigate.sl");
        assertEquals(2, tasks.size());

        Task asyncTask = tasks.getFirst();

        verifyAsyncLoopStatement(asyncTask);

        List<Output> aggregateValues = getAggregateOutputs(asyncTask);
        assertEquals(0, aggregateValues.size());

        List<Output> publishValues = getPublishOutputs(asyncTask);
        assertEquals("aggregate list is not empty", 0, publishValues.size());

        Map<String, String> expectedNavigationStrings = new HashMap<>();
        expectedNavigationStrings.put("SUCCESS", "print_list");
        expectedNavigationStrings.put("FAILURE", "FAILURE");
        verifyNavigationStrings(expectedNavigationStrings, asyncTask);

        assertTrue(asyncTask.isAsync());
    }

    @Test
    public void testPreCompileAsyncLoopFlowAggregateNavigate() throws Exception {
        Deque<Task> tasks = getTasksAfterPrecompileFlow("/loops/async_loop/async_loop_aggregate_navigate.sl");
        assertEquals(2, tasks.size());

        Task asyncTask = tasks.getFirst();

        verifyAsyncLoopStatement(asyncTask);

        List<Output> aggregateValues = getAggregateOutputs(asyncTask);
        assertEquals(2, aggregateValues.size());
        assertEquals("map(lambda x:str(x['name']), branches_context)", aggregateValues.get(0).getExpression());

        List<Output> publishValues = getPublishOutputs(asyncTask);
        assertEquals("aggregate list is not empty", 2, publishValues.size());
        assertEquals("name", publishValues.get(0).getExpression());

        Map<String, String> expectedNavigationStrings = new HashMap<>();
        expectedNavigationStrings.put("SUCCESS", "print_list");
        expectedNavigationStrings.put("FAILURE", "FAILURE");
        verifyNavigationStrings(expectedNavigationStrings, asyncTask);

        assertTrue(asyncTask.isAsync());
    }

    @Test
    public void testCompileAsyncLoopFlow() throws Exception {
        URI flow = getClass().getResource("/loops/async_loop/simple_async_loop.sl").toURI();
        URI operation = getClass().getResource("/loops/async_loop/print_branch.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        CompilationArtifact artifact = compiler.compile(SlangSource.fromFile(flow), path);
        assertNotNull("artifact is null", artifact);

        ExecutionPlan executionPlan = artifact.getExecutionPlan();
        assertNotNull("executionPlan is null", executionPlan);

        ExecutionStep addBranchesStep = executionPlan.getStep(2L);
        assertTrue("add branches step is not marked as split step", addBranchesStep.isSplitStep());
        Map<String, ?> addBranchesActionData = addBranchesStep.getActionData();
        verifyAsyncLoopStatement(addBranchesActionData);

        assertNotNull("branch begin task method not found", executionPlan.getStep(3L));
        assertNotNull("branch end task method not found", executionPlan.getStep(4L));
        assertNotNull("join branches method not found", executionPlan.getStep(5L));
    }

    @Test
    public void testCompileAsyncLoopFlowAggregate() throws Exception {
        URI flow = getClass().getResource("/loops/async_loop/async_loop_aggregate.sl").toURI();
        URI operation = getClass().getResource("/loops/async_loop/print_branch.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        CompilationArtifact artifact = compiler.compile(SlangSource.fromFile(flow), path);
        assertNotNull("artifact is null", artifact);

        ExecutionPlan executionPlan = artifact.getExecutionPlan();
        assertNotNull("executionPlan is null", executionPlan);

        ExecutionStep addBranchesStep = executionPlan.getStep(2L);
        assertTrue("add branches step is not marked as split step", addBranchesStep.isSplitStep());
        Map<String, ?> addBranchesActionData = addBranchesStep.getActionData();

        verifyAsyncLoopStatement(addBranchesActionData);

        ExecutionStep joinBranchesStep = executionPlan.getStep(5L);
        Map<String, ?> joinBranchesActionData = joinBranchesStep.getActionData();

        verifyAggregateValues(joinBranchesActionData);

        assertNotNull("branch begin task method not found", executionPlan.getStep(3L));
        ExecutionStep branchEndTaskStep = executionPlan.getStep(4L);
        assertNotNull("branch end task method not found", branchEndTaskStep);

        verifyPublishValues(branchEndTaskStep.getActionData());
    }

    @Test
    public void testCompileAsyncLoopFlowNavigate() throws Exception {
        URI flow = getClass().getResource("/loops/async_loop/async_loop_navigate.sl").toURI();
        URI operation1 = getClass().getResource("/loops/async_loop/print_branch.sl").toURI();
        URI operation2 = getClass().getResource("/loops/async_loop/print_list.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation1));
        path.add(SlangSource.fromFile(operation2));
        CompilationArtifact artifact = compiler.compile(SlangSource.fromFile(flow), path);
        assertNotNull("artifact is null", artifact);

        ExecutionPlan executionPlan = artifact.getExecutionPlan();
        assertNotNull("executionPlan is null", executionPlan);

        ExecutionStep addBranchesStep = executionPlan.getStep(2L);
        assertTrue("add branches step is not marked as split step", addBranchesStep.isSplitStep());
        Map<String, ?> addBranchesActionData = addBranchesStep.getActionData();

        verifyAsyncLoopStatement(addBranchesActionData);

        ExecutionStep joinBranchesStep = executionPlan.getStep(5L);
        Map<String, ?> joinBranchesActionData = joinBranchesStep.getActionData();

        verifyNavigationValues(joinBranchesActionData);

        assertNotNull("branch begin task method not found", executionPlan.getStep(3L));
        assertNotNull("branch end task method not found", executionPlan.getStep(4L));
    }

    @Test
    public void testCompileAsyncLoopFlowAggregateNavigate() throws Exception {
        URI flow = getClass().getResource("/loops/async_loop/async_loop_aggregate_navigate.sl").toURI();
        URI operation1 = getClass().getResource("/loops/async_loop/print_branch.sl").toURI();
        URI operation2 = getClass().getResource("/loops/async_loop/print_list.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation1));
        path.add(SlangSource.fromFile(operation2));
        CompilationArtifact artifact = compiler.compile(SlangSource.fromFile(flow), path);
        assertNotNull("artifact is null", artifact);

        ExecutionPlan executionPlan = artifact.getExecutionPlan();
        assertNotNull("executionPlan is null", executionPlan);

        ExecutionStep addBranchesStep = executionPlan.getStep(2L);
        assertTrue("add branches step is not marked as split step", addBranchesStep.isSplitStep());
        Map<String, ?> addBranchesActionData = addBranchesStep.getActionData();

        verifyAsyncLoopStatement(addBranchesActionData);

        ExecutionStep joinBranchesStep = executionPlan.getStep(5L);
        Map<String, ?> joinBranchesActionData = joinBranchesStep.getActionData();

        verifyAggregateValues(joinBranchesActionData);

        verifyNavigationValues(joinBranchesActionData);

        assertNotNull("branch begin task method not found", executionPlan.getStep(3L));
        ExecutionStep branchEndTaskStep = executionPlan.getStep(4L);
        assertNotNull("branch end task method not found", branchEndTaskStep);

        verifyPublishValues(branchEndTaskStep.getActionData());
    }

    private void verifyPublishValues(Map<String, ?> branchEndTaskActionData) {
        @SuppressWarnings("unchecked") List<Output> actualPublishOutputs =
                (List<Output>) branchEndTaskActionData.get(ScoreLangConstants.TASK_PUBLISH_KEY);
        List<Output> expectedPublishOutputs = new ArrayList<>();
        expectedPublishOutputs.add(new Output("name", "name"));
        expectedPublishOutputs.add(new Output("number", "int_output"));
        assertEquals("publish outputs not as expected", expectedPublishOutputs, actualPublishOutputs);
    }

    private void verifyNavigationValues(Map<String, ?> joinBranchesActionData) {
        assertTrue(joinBranchesActionData.containsKey(ScoreLangConstants.TASK_NAVIGATION_KEY));
        @SuppressWarnings("unchecked") Map<String, ResultNavigation> actualNavigateValues =
                (Map<String, ResultNavigation>) joinBranchesActionData.get(ScoreLangConstants.TASK_NAVIGATION_KEY);
        Map<String, ResultNavigation> expectedNavigationValues = new HashMap<>();
        expectedNavigationValues.put("SUCCESS", new ResultNavigation(6L, null));
        expectedNavigationValues.put("FAILURE", new ResultNavigation(0L, "FAILURE"));
        assertEquals("navigation values not as expected", expectedNavigationValues, actualNavigateValues);
    }

    private void verifyAggregateValues(Map<String, ?> joinBranchesActionData) {
        assertTrue(joinBranchesActionData.containsKey(ScoreLangConstants.TASK_AGGREGATE_KEY));
        @SuppressWarnings("unchecked") List<Output> actualAggregateOutputs =
                (List<Output>) joinBranchesActionData.get(ScoreLangConstants.TASK_AGGREGATE_KEY);
        List<Output> expectedAggregateOutputs = new ArrayList<>();
        expectedAggregateOutputs.add(new Output("name_list", "map(lambda x:str(x['name']), branches_context)"));
        expectedAggregateOutputs.add(new Output("number_from_last_branch", "branches_context[-1]['number']"));
        assertEquals("aggregate outputs not as expected", expectedAggregateOutputs, actualAggregateOutputs);
    }

    private Map<Long, ExecutionStep> verifyBranchExecutionPlan(
            CompilationArtifact artifact,
            String branchExecutionPlanKey,
            int numberOfDependencies) {
        //verify branch execution plan is created as a dependency
        Map<String, ExecutionPlan> dependencies = artifact.getDependencies();
        assertEquals(numberOfDependencies, dependencies.size());
        assertTrue(
                "branch execution plan key not found in dependencies",
                dependencies.containsKey(branchExecutionPlanKey));
        ExecutionPlan branchExecutionPlan = dependencies.get(branchExecutionPlanKey);
        assertEquals(
                "number of steps in branch execution plan not as expected",
                2,
                branchExecutionPlan.getSteps().size());
        return branchExecutionPlan.getSteps();
    }

    private void verifyAsyncLoopStatement(Map<String, ?> addBranchesActionData) {
        assertTrue(addBranchesActionData.containsKey(ScoreLangConstants.ASYNC_LOOP_STATEMENT_KEY));
        AsyncLoopStatement asyncLoopStatement =
                (AsyncLoopStatement) addBranchesActionData.get(ScoreLangConstants.ASYNC_LOOP_STATEMENT_KEY);
        assertEquals("async loop statement value not as expected", "value", asyncLoopStatement.getVarName());
        assertEquals("async loop statement expression not as expected", "values", asyncLoopStatement.getExpression());
    }

    private Deque<Task> getTasksAfterPrecompileFlow(String flowPath) throws URISyntaxException {
        URI flow = getClass().getResource(flowPath).toURI();
        Executable executable = compiler.preCompile(SlangSource.fromFile(flow));
        assertNotNull("executable is null", executable);

        return ((Flow) executable).getWorkflow().getTasks();
    }

    private void verifyAsyncLoopStatement(Task task) {
        assertTrue(task.getPreTaskActionData().containsKey(ScoreLangConstants.ASYNC_LOOP_KEY));
        AsyncLoopStatement asyncLoopStatement = (AsyncLoopStatement) task.getPreTaskActionData()
                .get(ScoreLangConstants.ASYNC_LOOP_KEY);
        assertEquals("values", asyncLoopStatement.getExpression());
        assertEquals("value", asyncLoopStatement.getVarName());
    }

    private List<Output> getAggregateOutputs(Task task) {
        assertTrue(task.getPostTaskActionData().containsKey(SlangTextualKeys.AGGREGATE_KEY));
        @SuppressWarnings("unchecked") List<Output> aggregateValues = (List<Output>) task.getPostTaskActionData().get(SlangTextualKeys.AGGREGATE_KEY);
        assertNotNull("aggregate list is null", aggregateValues);
        return aggregateValues;
    }

    private List<Output> getPublishOutputs(Task task) {
        assertTrue(task.getPostTaskActionData().containsKey(SlangTextualKeys.PUBLISH_KEY));
        @SuppressWarnings("unchecked") List<Output> publishValues = (List<Output>) task.getPostTaskActionData().get(SlangTextualKeys.PUBLISH_KEY);
        assertNotNull("publish list is null", publishValues);
        return publishValues;
    }

    private void verifyNavigationStrings(Map<String, String> expectedNavigationStrings, Task task) {
        Map<String, String> actualNavigationStrings = task.getNavigationStrings();
        assertEquals(expectedNavigationStrings, actualNavigationStrings);
    }

}
