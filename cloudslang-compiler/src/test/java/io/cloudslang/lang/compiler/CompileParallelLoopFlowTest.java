/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
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
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ListLoopStatement;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CompileParallelLoopFlowTest {

    @Autowired
    private SlangCompiler compiler;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testPreCompileParallelLoopFlow() throws Exception {
        Step step = getStepsAfterPrecompileFlow("/loops/parallel_loop/simple_parallel_loop.sl").getFirst();

        verifyParallelLoopStatement(step);

        List<Output> publishValues = getPublishOutputs(step);
        assertEquals("publish list is not empty", 0, publishValues.size());

        List<Map<String, String>> expectedNavigationStrings = new ArrayList<>();
        Map<String, String> successMap = new HashMap<>();
        successMap.put(ScoreLangConstants.SUCCESS_RESULT, "SUCCESS");
        Map<String, String> failureMap = new HashMap<>();
        failureMap.put(ScoreLangConstants.FAILURE_RESULT, "FAILURE");
        expectedNavigationStrings.add(successMap);
        expectedNavigationStrings.add(failureMap);
        verifyNavigationStrings(expectedNavigationStrings, step);

        assertTrue(step.isParallelLoop());
    }

    @Test
    public void testPreCompileParallelLoopFlowPublish() throws Exception {
        Step step = getStepsAfterPrecompileFlow("/loops/parallel_loop/parallel_loop_publish.sl").getFirst();

        verifyParallelLoopStatement(step);

        List<Output> publishValues = getPublishOutputs(step);
        assertEquals(2, publishValues.size());
        assertEquals("${ map(lambda x:str(x['name']), branches_context) }", publishValues.get(0).getValue().get());

        List<Map<String, String>> expectedNavigationStrings = new ArrayList<>();
        Map<String, String> successMap = new HashMap<>();
        successMap.put(ScoreLangConstants.SUCCESS_RESULT, "SUCCESS");
        Map<String, String> failureMap = new HashMap<>();
        failureMap.put(ScoreLangConstants.FAILURE_RESULT, "FAILURE");
        expectedNavigationStrings.add(successMap);
        expectedNavigationStrings.add(failureMap);
        verifyNavigationStrings(expectedNavigationStrings, step);

        assertTrue(step.isParallelLoop());
    }

    @Test
    public void testPreCompileParallelLoopFlowNavigate() throws Exception {
        Deque<Step> steps = getStepsAfterPrecompileFlow("/loops/parallel_loop/parallel_loop_navigate.sl");
        assertEquals(2, steps.size());

        Step parallelStep = steps.getFirst();

        verifyParallelLoopStatement(parallelStep);

        List<Output> publishValues = getPublishOutputs(parallelStep);
        assertEquals(0, publishValues.size());

        List<Map<String, String>> expectedNavigationStrings = new ArrayList<>();
        Map<String, String> successMap = new HashMap<>();
        successMap.put(ScoreLangConstants.SUCCESS_RESULT, "print_list");
        Map<String, String> failureMap = new HashMap<>();
        failureMap.put(ScoreLangConstants.FAILURE_RESULT, "FAILURE");
        expectedNavigationStrings.add(successMap);
        expectedNavigationStrings.add(failureMap);
        verifyNavigationStrings(expectedNavigationStrings, parallelStep);

        assertTrue(parallelStep.isParallelLoop());
    }

    @Test
    public void testPreCompileParallelLoopFlowPublishNavigate() throws Exception {
        Deque<Step> steps = getStepsAfterPrecompileFlow("/loops/parallel_loop/parallel_loop_publish_navigate.sl");
        assertEquals(2, steps.size());

        Step parallelStep = steps.getFirst();

        verifyParallelLoopStatement(parallelStep);

        List<Output> publishValues = getPublishOutputs(parallelStep);
        assertEquals(2, publishValues.size());
        assertEquals("${ map(lambda x:str(x['name']), branches_context) }", publishValues.get(0).getValue().get());

        List<Map<String, String>> expectedNavigationStrings = new ArrayList<>();
        Map<String, String> successMap = new HashMap<>();
        successMap.put(ScoreLangConstants.SUCCESS_RESULT, "print_list");
        Map<String, String> failureMap = new HashMap<>();
        failureMap.put(ScoreLangConstants.FAILURE_RESULT, "FAILURE");
        expectedNavigationStrings.add(successMap);
        expectedNavigationStrings.add(failureMap);
        verifyNavigationStrings(expectedNavigationStrings, parallelStep);

        assertTrue(parallelStep.isParallelLoop());
    }

    @Test
    public void testCompileParallelLoopFlow() throws Exception {
        final URI flow = getClass().getResource("/loops/parallel_loop/simple_parallel_loop.sl").toURI();
        final URI operation = getClass().getResource("/loops/parallel_loop/print_branch.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        CompilationArtifact artifact = compiler.compile(SlangSource.fromFile(flow), path);
        assertNotNull("artifact is null", artifact);

        ExecutionPlan executionPlan = artifact.getExecutionPlan();
        assertNotNull("executionPlan is null", executionPlan);

        ExecutionStep addBranchesStep = executionPlan.getStep(2L);
        assertTrue("add branches step is not marked as split step", addBranchesStep.isSplitStep());
        Map<String, ?> addBranchesActionData = addBranchesStep.getActionData();
        verifyParallelLoopStatement(addBranchesActionData);

        assertNotNull("branch begin step method not found", executionPlan.getStep(3L));
        assertNotNull("branch end step method not found", executionPlan.getStep(4L));
        assertNotNull("join branches method not found", executionPlan.getStep(5L));
    }

    @Test
    public void testCompileParallelLoopFlowPublish() throws Exception {
        final URI flow = getClass().getResource("/loops/parallel_loop/parallel_loop_publish.sl").toURI();
        final URI operation = getClass().getResource("/loops/parallel_loop/print_branch.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        CompilationArtifact artifact = compiler.compile(SlangSource.fromFile(flow), path);
        assertNotNull("artifact is null", artifact);

        ExecutionPlan executionPlan = artifact.getExecutionPlan();
        assertNotNull("executionPlan is null", executionPlan);

        ExecutionStep addBranchesStep = executionPlan.getStep(2L);
        assertTrue("add branches step is not marked as split step", addBranchesStep.isSplitStep());
        Map<String, ?> addBranchesActionData = addBranchesStep.getActionData();

        verifyParallelLoopStatement(addBranchesActionData);

        ExecutionStep joinBranchesStep = executionPlan.getStep(5L);
        Map<String, ?> joinBranchesActionData = joinBranchesStep.getActionData();

        verifyPublishValues(joinBranchesActionData);

        assertNotNull("branch begin step method not found", executionPlan.getStep(3L));
        ExecutionStep branchEndStepExecutionStep = executionPlan.getStep(4L);
        assertNotNull("branch end step method not found", branchEndStepExecutionStep);

        verifyBranchPublishValuesIsEmpty(branchEndStepExecutionStep.getActionData());
    }

    @Test
    public void testCompileParallelLoopFlowNavigate() throws Exception {
        final URI flow = getClass().getResource("/loops/parallel_loop/parallel_loop_navigate.sl").toURI();
        final URI operation1 = getClass().getResource("/loops/parallel_loop/print_branch.sl").toURI();
        final URI operation2 = getClass().getResource("/loops/parallel_loop/print_list.sl").toURI();
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

        verifyParallelLoopStatement(addBranchesActionData);

        ExecutionStep joinBranchesStep = executionPlan.getStep(5L);
        Map<String, ?> joinBranchesActionData = joinBranchesStep.getActionData();

        verifyNavigationValuesSuccessFailure(joinBranchesActionData);

        assertNotNull("branch begin step method not found", executionPlan.getStep(3L));
        assertNotNull("branch end step method not found", executionPlan.getStep(4L));
    }

    @Test
    public void testCompileParallelLoopFlowNavigateDefault() throws Exception {
        final URI flow = getClass().getResource("/loops/parallel_loop/parallel_loop_navigate_default.sl").toURI();
        final URI operation1 = getClass().getResource("/loops/parallel_loop/print_branch.sl").toURI();
        final URI operation2 = getClass().getResource("/loops/parallel_loop/print_list.sl").toURI();
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

        verifyParallelLoopStatement(addBranchesActionData);

        ExecutionStep joinBranchesStep = executionPlan.getStep(5L);
        Map<String, ?> joinBranchesActionData = joinBranchesStep.getActionData();

        verifyNavigationValuesSuccessFailure(joinBranchesActionData);

        assertNotNull("branch begin step method not found", executionPlan.getStep(3L));
        assertNotNull("branch end step method not found", executionPlan.getStep(4L));
    }

    @Test
    public void testCompileParallelLoopFlowNavigateDefaultCustom() throws Exception {
        final URI flow = getClass()
                .getResource("/loops/parallel_loop/parallel_loop_navigate_default_custom.sl").toURI();
        final URI operation1 = getClass().getResource("/loops/parallel_loop/print_branch_custom_only.sl").toURI();
        final URI operation2 = getClass().getResource("/loops/parallel_loop/print_list.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation1));
        path.add(SlangSource.fromFile(operation2));

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage(
                "Cannot compile flow 'parallel_loop_navigate_default_custom' since for step 'print_values' " +
                        "the navigation keys [FAILURE] have no matching results. The parallel loop depending on " +
                        "'loops.parallel_loop.print_branch_custom_only' can have the following results: [SUCCESS]."
        );

        compiler.compile(SlangSource.fromFile(flow), path);
    }

    @Test
    public void testCompileParallelLoopFlowNavigateCustom() throws Exception {
        final URI flow = getClass().getResource("/loops/parallel_loop/parallel_loop_navigate_custom.sl").toURI();
        final URI operation1 = getClass().getResource("/loops/parallel_loop/print_branch_custom_only.sl").toURI();
        final URI operation2 = getClass().getResource("/loops/parallel_loop/print_list.sl").toURI();
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

        verifyParallelLoopStatement(addBranchesActionData);

        ExecutionStep joinBranchesStep = executionPlan.getStep(5L);
        Map<String, ?> joinBranchesActionData = joinBranchesStep.getActionData();

        verifyNavigationValuesSuccess(joinBranchesActionData);

        assertNotNull("branch begin step method not found", executionPlan.getStep(3L));
        assertNotNull("branch end step method not found", executionPlan.getStep(4L));
    }

    @Test
    public void testCompileParallelLoopFlowNavigateDefaultSuccessOnly() throws Exception {
        final URI flow = getClass()
                .getResource("/loops/parallel_loop/parallel_loop_navigate_default_success_only.sl").toURI();
        final URI operation1 = getClass().getResource("/loops/parallel_loop/print_branch_success_only.sl").toURI();
        final URI operation2 = getClass().getResource("/loops/parallel_loop/print_list.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation1));
        path.add(SlangSource.fromFile(operation2));

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage(
                "Cannot compile flow 'parallel_loop_navigate_default_success_only' since for step 'print_values' " +
                        "the navigation keys [FAILURE] have no matching results. The parallel loop depending on " +
                        "'loops.parallel_loop.print_branch_success_only' can have the following results: [SUCCESS]."
        );

        compiler.compile(SlangSource.fromFile(flow), path);
    }

    @Test
    public void testCompileParallelLoopFlowNavigateSuccessOnly() throws Exception {
        final URI flow = getClass().getResource("/loops/parallel_loop/parallel_loop_navigate_success_only.sl").toURI();
        final URI operation1 = getClass().getResource("/loops/parallel_loop/print_branch_success_only.sl").toURI();
        final URI operation2 = getClass().getResource("/loops/parallel_loop/print_list.sl").toURI();
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

        verifyParallelLoopStatement(addBranchesActionData);

        ExecutionStep joinBranchesStep = executionPlan.getStep(5L);
        Map<String, ?> joinBranchesActionData = joinBranchesStep.getActionData();

        verifyNavigationValuesSuccess(joinBranchesActionData);

        assertNotNull("branch begin step method not found", executionPlan.getStep(3L));
        assertNotNull("branch end step method not found", executionPlan.getStep(4L));
    }

    @Test
    public void testCompileParallelLoopFlowNavigateNotWired() throws Exception {
        final URI flow = getClass().getResource("/loops/parallel_loop/parallel_loop_navigate_not_wired.sl").toURI();
        final URI operation1 = getClass().getResource("/loops/parallel_loop/print_branch.sl").toURI();
        final URI operation2 = getClass().getResource("/loops/parallel_loop/print_list.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation1));
        path.add(SlangSource.fromFile(operation2));

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage(
                "Cannot compile flow 'parallel_loop_navigate_not_wired' since for step " +
                        "'print_values' the parallel loop results [FAILURE] have no matching navigation."
        );

        compiler.compile(SlangSource.fromFile(flow), path);
    }

    @Test
    public void testCompileParallelLoopFlowPublishNavigate() throws Exception {
        final URI flow = getClass().getResource("/loops/parallel_loop/parallel_loop_publish_navigate.sl").toURI();
        final URI operation1 = getClass().getResource("/loops/parallel_loop/print_branch.sl").toURI();
        final URI operation2 = getClass().getResource("/loops/parallel_loop/print_list.sl").toURI();
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

        verifyParallelLoopStatement(addBranchesActionData);

        ExecutionStep joinBranchesStep = executionPlan.getStep(5L);
        Map<String, ?> joinBranchesActionData = joinBranchesStep.getActionData();

        verifyPublishValues(joinBranchesActionData);

        verifyNavigationValuesSuccessFailure(joinBranchesActionData);

        assertNotNull("branch begin step method not found", executionPlan.getStep(3L));
        ExecutionStep branchEndStepExecutionStep = executionPlan.getStep(4L);
        assertNotNull("branch end step method not found", branchEndStepExecutionStep);

        verifyBranchPublishValuesIsEmpty(branchEndStepExecutionStep.getActionData());
    }

    @Test
    public void testPublishOnBranchThrowsException() throws Exception {
        final URI flow = getClass()
                .getResource("/corrupted/loops/parallel_loop/parallel_loop_publish_on_branch.sl").toURI();
        final URI operation1 = getClass().getResource("/loops/parallel_loop/print_branch.sl").toURI();
        final URI operation2 = getClass().getResource("/loops/parallel_loop/print_list.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation1));
        path.add(SlangSource.fromFile(operation2));

        expectedException.expect(RuntimeException.class);
        expectedException
                .expectMessage("Artifact {print_values} has unrecognized tag {publish} under 'parallel_loop'. " +
                "Please take a look at the supported features per versions link");

        compiler.compile(SlangSource.fromFile(flow), path);
    }

    @Test
    public void testAggregateKeyThrowsException() throws Exception {
        final URI flow = getClass()
                .getResource("/corrupted/loops/parallel_loop/parallel_loop_aggregate_key.sl").toURI();
        final URI operation1 = getClass().getResource("/loops/parallel_loop/print_branch.sl").toURI();
        final URI operation2 = getClass().getResource("/loops/parallel_loop/print_list.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation1));
        path.add(SlangSource.fromFile(operation2));

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Artifact {print_values} has unrecognized tag {aggregate}. " +
                "Please take a look at the supported features per versions link");

        compiler.compile(SlangSource.fromFile(flow), path);
    }

    private void verifyPublishValues(Map<String, ?> joinBranchesActionData) {
        assertTrue(joinBranchesActionData.containsKey(ScoreLangConstants.STEP_PUBLISH_KEY));
        @SuppressWarnings("unchecked") List<Output> actualPublishOutputs =
                (List<Output>) joinBranchesActionData.get(ScoreLangConstants.STEP_PUBLISH_KEY);
        List<Output> expectedPublishOutputs = new ArrayList<>();
        expectedPublishOutputs
                .add(new Output("name_list",
                        ValueFactory.create("${ map(lambda x:str(x['name']), branches_context) }")));
        expectedPublishOutputs
                .add(new Output("number_from_last_branch",
                        ValueFactory.create("${ branches_context[-1]['number'] }")));
        assertEquals("publish values not as expected", expectedPublishOutputs, actualPublishOutputs);
    }

    private void verifyBranchPublishValuesIsEmpty(Map<String, ?> actionData) {
        @SuppressWarnings("unchecked") List<Output> publishValues =
                (List<Output>) actionData.get(ScoreLangConstants.STEP_PUBLISH_KEY);
        assertTrue(CollectionUtils.isEmpty(publishValues));
    }

    private void verifyNavigationValuesSuccessFailure(Map<String, ?> joinBranchesActionData) {
        assertTrue(joinBranchesActionData.containsKey(ScoreLangConstants.STEP_NAVIGATION_KEY));
        @SuppressWarnings("unchecked") Map<String, ResultNavigation> actualNavigateValues =
                (Map<String, ResultNavigation>) joinBranchesActionData.get(ScoreLangConstants.STEP_NAVIGATION_KEY);
        Map<String, ResultNavigation> expectedNavigationValues = new HashMap<>();
        expectedNavigationValues.put("SUCCESS", new ResultNavigation(6L, null));
        expectedNavigationValues.put("FAILURE", new ResultNavigation(0L, "FAILURE"));
        assertEquals("navigation values not as expected", expectedNavigationValues, actualNavigateValues);
    }

    private void verifyNavigationValuesSuccess(Map<String, ?> joinBranchesActionData) {
        assertTrue(joinBranchesActionData.containsKey(ScoreLangConstants.STEP_NAVIGATION_KEY));
        @SuppressWarnings("unchecked") Map<String, ResultNavigation> actualNavigateValues =
                (Map<String, ResultNavigation>) joinBranchesActionData.get(ScoreLangConstants.STEP_NAVIGATION_KEY);
        Map<String, ResultNavigation> expectedNavigationValues = new HashMap<>();
        expectedNavigationValues.put("SUCCESS", new ResultNavigation(6L, null));
        assertEquals("navigation values not as expected", expectedNavigationValues, actualNavigateValues);
    }

    private void verifyParallelLoopStatement(Map<String, ?> addBranchesActionData) {
        assertTrue(addBranchesActionData.containsKey(ScoreLangConstants.PARALLEL_LOOP_STATEMENT_KEY));
        ListLoopStatement parallelLoopStatement =
                (ListLoopStatement) addBranchesActionData.get(ScoreLangConstants.PARALLEL_LOOP_STATEMENT_KEY);
        assertEquals("parallel loop statement value not as expected",
                "value", parallelLoopStatement.getVarName());
        assertEquals("parallel loop statement expression not as expected",
                "values", parallelLoopStatement.getExpression());
    }

    private void verifyParallelLoopStatement(Step step) {
        assertTrue(step.getPreStepActionData().containsKey(SlangTextualKeys.PARALLEL_LOOP_KEY));
        ListLoopStatement parallelLoopStatement = (ListLoopStatement) step.getPreStepActionData()
                .get(SlangTextualKeys.PARALLEL_LOOP_KEY);
        assertEquals("values", parallelLoopStatement.getExpression());
        assertEquals("value", parallelLoopStatement.getVarName());
    }

    private Deque<Step> getStepsAfterPrecompileFlow(String flowPath) throws URISyntaxException {
        URI flow = getClass().getResource(flowPath).toURI();
        Executable executable = compiler.preCompile(SlangSource.fromFile(flow));
        assertNotNull("executable is null", executable);

        return ((Flow) executable).getWorkflow().getSteps();
    }

    private List<Output> getPublishOutputs(Step step) {
        assertTrue(step.getPostStepActionData().containsKey(SlangTextualKeys.PUBLISH_KEY));
        @SuppressWarnings("unchecked") List<Output> publishValues =
                (List<Output>) step.getPostStepActionData().get(SlangTextualKeys.PUBLISH_KEY);
        assertNotNull("publish list is null", publishValues);
        return publishValues;
    }

    private void verifyNavigationStrings(List<Map<String, String>> expectedNavigationStrings, Step step) {
        List<Map<String, String>> actualNavigationStrings = step.getNavigationStrings();
        assertEquals(expectedNavigationStrings, actualNavigationStrings);
    }

}
