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
import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.entities.MapLoopStatement;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.score.api.ExecutionPlan;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Ignore;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CompileForLoopsFlowTest {

    @Autowired
    private SlangCompiler compiler;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    public static ListLoopStatement validateListForLoopStatement(LoopStatement statement) {
        Assert.assertEquals(true, statement instanceof ListLoopStatement);
        return (ListLoopStatement) statement;
    }

    public static MapLoopStatement validateMapForLoopStatement(LoopStatement statement) {
        Assert.assertEquals(true, statement instanceof MapLoopStatement);
        return (MapLoopStatement) statement;
    }

    @Test
    public void testPreCompileLoopFlow() throws Exception {
        final URI flow = getClass().getResource("/loops/simple_loop.sl").toURI();
        final Executable executable = compiler.preCompile(SlangSource.fromFile(flow));
        assertNotNull("executable is null", executable);
        Step step = ((Flow) executable).getWorkflow()
                .getSteps()
                .getFirst();
        assertTrue(step.getPreStepActionData().containsKey(SlangTextualKeys.FOR_KEY));
        LoopStatement forStatement = (LoopStatement) step.getPreStepActionData()
                .get(SlangTextualKeys.FOR_KEY);
        ListLoopStatement listLoopStatement = validateListForLoopStatement(forStatement);
        assertEquals("values.split(\",\")", listLoopStatement.getExpression());
        assertEquals("value", listLoopStatement.getVarName());
        @SuppressWarnings("unchecked") List<Output> outputs = (List<Output>) step.getPostStepActionData()
                .get(SlangTextualKeys.PUBLISH_KEY);
        assertEquals("a", outputs.get(0).getValue().get());
        assertEquals(Collections.singletonList(ScoreLangConstants.FAILURE_RESULT),
                step.getPostStepActionData().get(SlangTextualKeys.BREAK_KEY));
    }


    @Test
    public void testPreCompileLoopFlowWithBreak() throws Exception {
        final URI flow = getClass().getResource("/loops/loop_with_break.sl").toURI();
        final Executable executable = compiler.preCompile(SlangSource.fromFile(flow));
        assertNotNull("executable is null", executable);
        Step step = ((Flow) executable).getWorkflow()
                .getSteps()
                .getFirst();
        assertEquals(Arrays.asList(ScoreLangConstants.SUCCESS_RESULT, ScoreLangConstants.FAILURE_RESULT),
                step.getPostStepActionData().get(SlangTextualKeys.BREAK_KEY));
    }

    @Test
    public void testPreCompileLoopWithCustomNavigationFlow() throws Exception {
        final URI flow = getClass().getResource("/loops/loop_with_custom_navigation.sl").toURI();
        final Executable executable = compiler.preCompile(SlangSource.fromFile(flow));
        assertNotNull("executable is null", executable);
        Step step = ((Flow) executable).getWorkflow()
                .getSteps()
                .getFirst();
        assertTrue(step.getPreStepActionData().containsKey(SlangTextualKeys.FOR_KEY));
        LoopStatement forStatement = (LoopStatement) step.getPreStepActionData()
                .get(SlangTextualKeys.FOR_KEY);
        ListLoopStatement listLoopStatement = validateListForLoopStatement(forStatement);
        assertEquals("values.split(\",\")", listLoopStatement.getExpression());
        assertEquals("value", listLoopStatement.getVarName());
        @SuppressWarnings("unchecked")
        List<Map<String, String>> actual = (List<Map<String, String>>) step.getPostStepActionData()
                .get(SlangTextualKeys.NAVIGATION_KEY);
        assertEquals("print_other_values", actual.get(0).get(ScoreLangConstants.SUCCESS_RESULT));
    }

    @Test
    public void testCompileLoopFlow() throws Exception {
        final URI flow = getClass().getResource("/loops/simple_loop.sl").toURI();
        final URI operation = getClass().getResource("/loops/print.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        CompilationArtifact artifact = compiler.compile(SlangSource.fromFile(flow), path);
        assertNotNull("artifact is null", artifact);
        ExecutionPlan executionPlan = artifact.getExecutionPlan();
        assertNotNull("executionPlan is null", executionPlan);
        Map<String, ?> startStepActionData = executionPlan.getStep(2L)
                .getActionData();
        assertTrue(startStepActionData.containsKey(ScoreLangConstants.LOOP_KEY));
        LoopStatement forStatement = (LoopStatement) startStepActionData.get(ScoreLangConstants.LOOP_KEY);
        ListLoopStatement listLoopStatement = validateListForLoopStatement(forStatement);
        assertEquals("values.split(\",\")", listLoopStatement.getExpression());
        assertEquals("value", listLoopStatement.getVarName());

        Map<String, ?> endStepActionData = executionPlan.getStep(3L)
                .getActionData();
        assertEquals(Collections.singletonList(ScoreLangConstants.FAILURE_RESULT),
                endStepActionData.get(ScoreLangConstants.BREAK_LOOP_KEY));
    }

    @Test
    public void testCompileEmptyLoopFlow() throws Exception {
        final URI flow = getClass().getResource("/loops/simple_loop_empty.sl").toURI();
        final URI operation = getClass().getResource("/loops/print.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        exception.expect(RuntimeException.class);
        exception.expectMessage("For step 'print_values' syntax is illegal.\n" +
                "For statement is empty.");
        compiler.compile(SlangSource.fromFile(flow), path);
    }

    @Test
    public void testCompileEmptyStringLoopFlow() throws Exception {
        final URI flow = getClass().getResource("/loops/simple_loop_empty_string.sl").toURI();
        final URI operation = getClass().getResource("/loops/print.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        exception.expect(RuntimeException.class);
        exception.expectMessage("For step 'print_values' syntax is illegal.\n" +
                "For statement is empty.");
        compiler.compile(SlangSource.fromFile(flow), path);
    }

    @Test
    public void testCompileLoopFlowWithBreak() throws Exception {
        final URI flow = getClass().getResource("/loops/loop_with_break.sl").toURI();
        final URI operation = getClass().getResource("/loops/print.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        CompilationArtifact artifact = compiler.compile(SlangSource.fromFile(flow), path);
        assertNotNull("artifact is null", artifact);
        ExecutionPlan executionPlan = artifact.getExecutionPlan();

        Map<String, ?> endStepActionData = executionPlan.getStep(3L)
                .getActionData();
        assertEquals(Arrays.asList(ScoreLangConstants.SUCCESS_RESULT, ScoreLangConstants.FAILURE_RESULT),
                endStepActionData.get(ScoreLangConstants.BREAK_LOOP_KEY));
    }

    @Test
    public void testPreCompileLoopFlowWithMap() throws Exception {
        final URI flow = getClass().getResource("/loops/simple_loop_with_map.sl").toURI();
        Executable executable = compiler.preCompile(SlangSource.fromFile(flow));
        assertNotNull("executable is null", executable);
        Step step = ((Flow) executable).getWorkflow()
                .getSteps()
                .getFirst();
        assertTrue(step.getPreStepActionData().containsKey(SlangTextualKeys.FOR_KEY));
        LoopStatement forStatement = (LoopStatement) step.getPreStepActionData()
                .get(SlangTextualKeys.FOR_KEY);
        MapLoopStatement mapLoopStatement = validateMapForLoopStatement(forStatement);
        assertEquals("person_map", mapLoopStatement.getExpression());
        assertEquals("k", mapLoopStatement.getKeyName());
        assertEquals("v", mapLoopStatement.getValueName());
        @SuppressWarnings("unchecked") List<Output> outputs = (List<Output>) step.getPostStepActionData()
                .get(SlangTextualKeys.PUBLISH_KEY);
        assertEquals("a", outputs.get(0).getValue().get());
        assertEquals(Collections.singletonList(ScoreLangConstants.FAILURE_RESULT),
                step.getPostStepActionData().get(SlangTextualKeys.BREAK_KEY));
    }

    @Test
    public void testPreCompileLoopFlowWithMapWithBreak() throws Exception {
        final URI flow = getClass().getResource("/loops/loop_with_break_with_map.sl").toURI();
        final Executable executable = compiler.preCompile(SlangSource.fromFile(flow));
        assertNotNull("executable is null", executable);
        Step step = ((Flow) executable).getWorkflow()
                .getSteps()
                .getFirst();
        assertEquals(Arrays.asList(ScoreLangConstants.SUCCESS_RESULT, ScoreLangConstants.FAILURE_RESULT),
                step.getPostStepActionData().get(SlangTextualKeys.BREAK_KEY));
    }

    @Ignore("Remove when support for maps in loops is added")
    @Test
    public void testPreCompileLoopWithMapWithCustomNavigationFlow() throws Exception {
        final URI flow = getClass().getResource("/loops/loop_with_custom_navigation_with_map.sl").toURI();
        final Executable executable = compiler.preCompile(SlangSource.fromFile(flow));
        assertNotNull("executable is null", executable);
        Step step = ((Flow) executable).getWorkflow()
                .getSteps()
                .getFirst();
        assertTrue(step.getPreStepActionData().containsKey(SlangTextualKeys.FOR_KEY));
        LoopStatement forStatement = (LoopStatement) step.getPreStepActionData()
                .get(SlangTextualKeys.FOR_KEY);
        MapLoopStatement mapLoopStatement = validateMapForLoopStatement(forStatement);
        assertEquals("person_map", mapLoopStatement.getExpression());
        assertEquals("k", mapLoopStatement.getKeyName());
        assertEquals("v", mapLoopStatement.getValueName());
        @SuppressWarnings("unchecked")
        List<Map<String, String>> actual = (List<Map<String, String>>) step.getPostStepActionData()
                .get(SlangTextualKeys.NAVIGATION_KEY);
        assertEquals("print_other_values", actual.get(0).get(ScoreLangConstants.SUCCESS_RESULT));
    }

    @Test
    public void testCompileLoopWithMapFlow() throws Exception {
        final URI flow = getClass().getResource("/loops/simple_loop_with_map.sl").toURI();
        final URI operation = getClass().getResource("/loops/print.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        CompilationArtifact artifact = compiler.compile(SlangSource.fromFile(flow), path);
        assertNotNull("artifact is null", artifact);
        ExecutionPlan executionPlan = artifact.getExecutionPlan();
        assertNotNull("executionPlan is null", executionPlan);
        Map<String, ?> startStartActionData = executionPlan.getStep(2L)
                .getActionData();
        assertTrue(startStartActionData.containsKey(ScoreLangConstants.LOOP_KEY));
        LoopStatement forStatement = (LoopStatement) startStartActionData.get(ScoreLangConstants.LOOP_KEY);
        MapLoopStatement mapLoopStatement = validateMapForLoopStatement(forStatement);
        assertEquals("person_map", mapLoopStatement.getExpression());
        assertEquals("k", mapLoopStatement.getKeyName());
        assertEquals("v", mapLoopStatement.getValueName());

        Map<String, ?> endStepActionData = executionPlan.getStep(3L)
                .getActionData();
        assertEquals(Collections.singletonList(ScoreLangConstants.FAILURE_RESULT),
                endStepActionData.get(ScoreLangConstants.BREAK_LOOP_KEY));
    }

    @Test
    public void testCompileLoopFlowWithMapWithBreak() throws Exception {
        final URI flow = getClass().getResource("/loops/loop_with_break_with_map.sl").toURI();
        final URI operation = getClass().getResource("/loops/print.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        CompilationArtifact artifact = compiler.compile(SlangSource.fromFile(flow), path);
        assertNotNull("artifact is null", artifact);
        ExecutionPlan executionPlan = artifact.getExecutionPlan();

        Map<String, ?> endStepActionData = executionPlan.getStep(3L)
                .getActionData();
        assertEquals(Arrays.asList(ScoreLangConstants.SUCCESS_RESULT, ScoreLangConstants.FAILURE_RESULT),
                endStepActionData.get(ScoreLangConstants.BREAK_LOOP_KEY));
    }

    @Test
    public void testCompileLoopFlowWithSystemProperty() throws Exception {
        final URI flow = getClass().getResource("/loops/loop_from_property_with_custom_navigation.sl").toURI();
        final URI operation = getClass().getResource("/loops/print.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        CompilationArtifact artifact = compiler.compile(SlangSource.fromFile(flow), path);
        assertNotNull("artifact is null", artifact);
        Assert.assertEquals(1, artifact.getSystemProperties().size());
        Assert.assertEquals("loops.list", artifact.getSystemProperties().iterator().next());
    }

    @Test
    public void testCompileLoopFlowWithBreakOnNonExistingResult() throws Exception {
        final URI flow = getClass().getResource("/loops/loop_with_break_on_non_existing_result.sl").toURI();
        final URI operation = getClass().getResource("/loops/print.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(
                "Cannot compile flow 'loops.loop_with_break_on_non_existing_result' since in step" +
                        " 'print_values' the results [CUSTOM_1, CUSTOM_2] declared in 'break' section " +
                        "are not declared in the dependency 'loops.print' result section."
        );
        compiler.compile(SlangSource.fromFile(flow), path);
    }

}
