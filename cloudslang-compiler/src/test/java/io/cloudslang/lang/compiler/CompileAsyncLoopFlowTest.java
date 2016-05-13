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
import io.cloudslang.lang.compiler.modeller.model.Step;
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
        Step step = getStepsAfterPrecompileFlow("/loops/async_loop/simple_async_loop.sl").getFirst();

        verifyAsyncLoopStatement(step);

        List<Output> aggregateValues = getAggregateOutputs(step);
        assertEquals("aggregate list is not empty", 0, aggregateValues.size());

        List<Output> publishValues = getPublishOutputs(step);
        assertEquals("aggregate list is not empty", 0, publishValues.size());

        List<Map<String, String>> expectedNavigationStrings = new ArrayList<>();
        Map<String, String> successMap = new HashMap<>();
        successMap.put(ScoreLangConstants.SUCCESS_RESULT, "SUCCESS");
        Map<String, String> failureMap = new HashMap<>();
        failureMap.put(ScoreLangConstants.FAILURE_RESULT, "FAILURE");
        expectedNavigationStrings.add(successMap);
        expectedNavigationStrings.add(failureMap);
        verifyNavigationStrings(expectedNavigationStrings, step);

        assertTrue(step.isAsync());
    }

    @Test
    public void testPreCompileAsyncLoopFlowAggregate() throws Exception {
        Step step = getStepsAfterPrecompileFlow("/loops/async_loop/async_loop_aggregate.sl").getFirst();

        verifyAsyncLoopStatement(step);

        List<Output> aggregateValues = getAggregateOutputs(step);
        assertEquals(2, aggregateValues.size());
        assertEquals("${ map(lambda x:str(x['name']), branches_context) }", aggregateValues.get(0).getValue());

        List<Output> publishValues = getPublishOutputs(step);
        assertEquals("aggregate list is not empty", 2, publishValues.size());
        assertEquals("${name}", publishValues.get(0).getValue());

        List<Map<String, String>> expectedNavigationStrings = new ArrayList<>();
        Map<String, String> successMap = new HashMap<>();
        successMap.put(ScoreLangConstants.SUCCESS_RESULT, "SUCCESS");
        Map<String, String> failureMap = new HashMap<>();
        failureMap.put(ScoreLangConstants.FAILURE_RESULT, "FAILURE");
        expectedNavigationStrings.add(successMap);
        expectedNavigationStrings.add(failureMap);
        verifyNavigationStrings(expectedNavigationStrings, step);

        assertTrue(step.isAsync());
    }

    @Test
    public void testPreCompileAsyncLoopFlowNavigate() throws Exception {
        Deque<Step> steps = getStepsAfterPrecompileFlow("/loops/async_loop/async_loop_navigate.sl");
        assertEquals(2, steps.size());

        Step asyncStep = steps.getFirst();

        verifyAsyncLoopStatement(asyncStep);

        List<Output> aggregateValues = getAggregateOutputs(asyncStep);
        assertEquals(0, aggregateValues.size());

        List<Output> publishValues = getPublishOutputs(asyncStep);
        assertEquals("aggregate list is not empty", 0, publishValues.size());

        List<Map<String, String>> expectedNavigationStrings = new ArrayList<>();
        Map<String, String> successMap = new HashMap<>();
        successMap.put(ScoreLangConstants.SUCCESS_RESULT, "print_list");
        Map<String, String> failureMap = new HashMap<>();
        failureMap.put(ScoreLangConstants.FAILURE_RESULT, "FAILURE");
        expectedNavigationStrings.add(successMap);
        expectedNavigationStrings.add(failureMap);
        verifyNavigationStrings(expectedNavigationStrings, asyncStep);

        assertTrue(asyncStep.isAsync());
    }

    @Test
    public void testPreCompileAsyncLoopFlowAggregateNavigate() throws Exception {
        Deque<Step> steps = getStepsAfterPrecompileFlow("/loops/async_loop/async_loop_aggregate_navigate.sl");
        assertEquals(2, steps.size());

        Step asyncStep = steps.getFirst();

        verifyAsyncLoopStatement(asyncStep);

        List<Output> aggregateValues = getAggregateOutputs(asyncStep);
        assertEquals(2, aggregateValues.size());
        assertEquals("${ map(lambda x:str(x['name']), branches_context) }", aggregateValues.get(0).getValue());

        List<Output> publishValues = getPublishOutputs(asyncStep);
        assertEquals("aggregate list is not empty", 2, publishValues.size());
        assertEquals("${name}", publishValues.get(0).getValue());

        List<Map<String, String>> expectedNavigationStrings = new ArrayList<>();
        Map<String, String> successMap = new HashMap<>();
        successMap.put(ScoreLangConstants.SUCCESS_RESULT, "print_list");
        Map<String, String> failureMap = new HashMap<>();
        failureMap.put(ScoreLangConstants.FAILURE_RESULT, "FAILURE");
        expectedNavigationStrings.add(successMap);
        expectedNavigationStrings.add(failureMap);
        verifyNavigationStrings(expectedNavigationStrings, asyncStep);

        assertTrue(asyncStep.isAsync());
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

        assertNotNull("branch begin step method not found", executionPlan.getStep(3L));
        assertNotNull("branch end step method not found", executionPlan.getStep(4L));
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

        assertNotNull("branch begin step method not found", executionPlan.getStep(3L));
        ExecutionStep branchEndStepExecutionStep = executionPlan.getStep(4L);
        assertNotNull("branch end step method not found", branchEndStepExecutionStep);

        verifyPublishValues(branchEndStepExecutionStep.getActionData());
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

        assertNotNull("branch begin step method not found", executionPlan.getStep(3L));
        assertNotNull("branch end step method not found", executionPlan.getStep(4L));
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

        assertNotNull("branch begin step method not found", executionPlan.getStep(3L));
        ExecutionStep branchEndStepExecutionStep = executionPlan.getStep(4L);
        assertNotNull("branch end step method not found", branchEndStepExecutionStep);

        verifyPublishValues(branchEndStepExecutionStep.getActionData());
    }

    private void verifyPublishValues(Map<String, ?> branchEndStepActionData) {
        @SuppressWarnings("unchecked") List<Output> actualPublishOutputs =
                (List<Output>) branchEndStepActionData.get(ScoreLangConstants.STEP_PUBLISH_KEY);
        List<Output> expectedPublishOutputs = new ArrayList<>();
        expectedPublishOutputs.add(new Output("name", "${name}"));
        expectedPublishOutputs.add(new Output("number", "${ int_output }"));
        assertEquals("publish outputs not as expected", expectedPublishOutputs, actualPublishOutputs);
    }

    private void verifyNavigationValues(Map<String, ?> joinBranchesActionData) {
        assertTrue(joinBranchesActionData.containsKey(ScoreLangConstants.STEP_NAVIGATION_KEY));
        @SuppressWarnings("unchecked") Map<String, ResultNavigation> actualNavigateValues =
                (Map<String, ResultNavigation>) joinBranchesActionData.get(ScoreLangConstants.STEP_NAVIGATION_KEY);
        Map<String, ResultNavigation> expectedNavigationValues = new HashMap<>();
        expectedNavigationValues.put("SUCCESS", new ResultNavigation(6L, null));
        expectedNavigationValues.put("FAILURE", new ResultNavigation(0L, "FAILURE"));
        assertEquals("navigation values not as expected", expectedNavigationValues, actualNavigateValues);
    }

    private void verifyAggregateValues(Map<String, ?> joinBranchesActionData) {
        assertTrue(joinBranchesActionData.containsKey(ScoreLangConstants.STEP_AGGREGATE_KEY));
        @SuppressWarnings("unchecked") List<Output> actualAggregateOutputs =
                (List<Output>) joinBranchesActionData.get(ScoreLangConstants.STEP_AGGREGATE_KEY);
        List<Output> expectedAggregateOutputs = new ArrayList<>();
        expectedAggregateOutputs.add(new Output("name_list", "${ map(lambda x:str(x['name']), branches_context) }"));
        expectedAggregateOutputs.add(new Output("number_from_last_branch", "${ branches_context[-1]['number'] }"));
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

    private Deque<Step> getStepsAfterPrecompileFlow(String flowPath) throws URISyntaxException {
        URI flow = getClass().getResource(flowPath).toURI();
        Executable executable = compiler.preCompile(SlangSource.fromFile(flow));
        assertNotNull("executable is null", executable);

        return ((Flow) executable).getWorkflow().getSteps();
    }

    private void verifyAsyncLoopStatement(Step step) {
        assertTrue(step.getPreStepActionData().containsKey(ScoreLangConstants.ASYNC_LOOP_KEY));
        AsyncLoopStatement asyncLoopStatement = (AsyncLoopStatement) step.getPreStepActionData()
                .get(ScoreLangConstants.ASYNC_LOOP_KEY);
        assertEquals("values", asyncLoopStatement.getExpression());
        assertEquals("value", asyncLoopStatement.getVarName());
    }

    private List<Output> getAggregateOutputs(Step step) {
        assertTrue(step.getPostStepActionData().containsKey(SlangTextualKeys.AGGREGATE_KEY));
        @SuppressWarnings("unchecked") List<Output> aggregateValues = (List<Output>) step.getPostStepActionData().get(SlangTextualKeys.AGGREGATE_KEY);
        assertNotNull("aggregate list is null", aggregateValues);
        return aggregateValues;
    }

    private List<Output> getPublishOutputs(Step step) {
        assertTrue(step.getPostStepActionData().containsKey(SlangTextualKeys.PUBLISH_KEY));
        @SuppressWarnings("unchecked") List<Output> publishValues = (List<Output>) step.getPostStepActionData().get(SlangTextualKeys.PUBLISH_KEY);
        assertNotNull("publish list is null", publishValues);
        return publishValues;
    }

    private void verifyNavigationStrings(List<Map<String, String>> expectedNavigationStrings, Step step) {
        List<Map<String, String>> actualNavigationStrings = step.getNavigationStrings();
        assertEquals(expectedNavigationStrings, actualNavigationStrings);
    }

}
