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

import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Step;
import io.cloudslang.lang.entities.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.entities.bindings.Output;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class CompileForLoopsFlowTest {

    @Autowired
    private SlangCompiler compiler;

    public static ListForLoopStatement validateListForLoopStatement(LoopStatement statement) {
        Assert.assertEquals(true, statement instanceof ListForLoopStatement);
        return (ListForLoopStatement) statement;
    }

    public static MapForLoopStatement validateMapForLoopStatement(LoopStatement statement) {
        Assert.assertEquals(true, statement instanceof MapForLoopStatement);
        return (MapForLoopStatement) statement;
    }

    @Test
    public void testPreCompileLoopFlow() throws Exception {
        URI flow = getClass().getResource("/loops/simple_loop.sl").toURI();
        Executable executable = compiler.preCompile(SlangSource.fromFile(flow));
        assertNotNull("executable is null", executable);
        Step step = ((Flow) executable).getWorkflow()
                                        .getSteps()
                                        .getFirst();
        assertTrue(step.getPreStepActionData().containsKey(SlangTextualKeys.FOR_KEY));
        LoopStatement forStatement = (LoopStatement) step.getPreStepActionData()
                                .get(SlangTextualKeys.FOR_KEY);
        ListForLoopStatement listForLoopStatement  = validateListForLoopStatement(forStatement);
        assertEquals("values", listForLoopStatement.getExpression());
        assertEquals("value", listForLoopStatement.getVarName());
        @SuppressWarnings("unchecked") List<Output> outputs = (List<Output>) step.getPostStepActionData()
                                  .get(SlangTextualKeys.PUBLISH_KEY);
        assertEquals("a", outputs.get(0).getValue());
        assertEquals(Arrays.asList(ScoreLangConstants.FAILURE_RESULT),
                step.getPostStepActionData().get(SlangTextualKeys.BREAK_KEY));
    }


    @Test
    public void testPreCompileLoopFlowWithBreak() throws Exception {
        URI flow = getClass().getResource("/loops/loop_with_break.sl").toURI();
        Executable executable = compiler.preCompile(SlangSource.fromFile(flow));
        assertNotNull("executable is null", executable);
        Step step = ((Flow) executable).getWorkflow()
                                       .getSteps()
                                       .getFirst();
        assertEquals(Arrays.asList(ScoreLangConstants.SUCCESS_RESULT, ScoreLangConstants.FAILURE_RESULT),
                step.getPostStepActionData().get(SlangTextualKeys.BREAK_KEY));
    }

    @Test
    public void testPreCompileLoopWithCustomNavigationFlow() throws Exception {
        URI flow = getClass().getResource("/loops/loop_with_custom_navigation.sl").toURI();
        Executable executable = compiler.preCompile(SlangSource.fromFile(flow));
        assertNotNull("executable is null", executable);
        Step step = ((Flow) executable).getWorkflow()
                                       .getSteps()
                                       .getFirst();
        assertTrue(step.getPreStepActionData().containsKey(SlangTextualKeys.FOR_KEY));
        LoopStatement forStatement = (LoopStatement) step.getPreStepActionData()
                                                         .get(SlangTextualKeys.FOR_KEY);
        ListForLoopStatement listForLoopStatement  = validateListForLoopStatement(forStatement);
        assertEquals("values", listForLoopStatement.getExpression());
        assertEquals("value", listForLoopStatement.getVarName());
        @SuppressWarnings("unchecked") List<Map<String, String>> actual = (List<Map<String, String>>) step.getPostStepActionData()
                                                                                .get(SlangTextualKeys.NAVIGATION_KEY);
        assertEquals("print_other_values", actual.get(0).get(ScoreLangConstants.SUCCESS_RESULT));
    }

    @Test
    public void testCompileLoopFlow() throws Exception {
        URI flow = getClass().getResource("/loops/simple_loop.sl").toURI();
        URI operation = getClass().getResource("/loops/print.sl").toURI();
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
        ListForLoopStatement listForLoopStatement  = validateListForLoopStatement(forStatement);
        assertEquals("values", listForLoopStatement.getExpression());
        assertEquals("value", listForLoopStatement.getVarName());

        Map<String, ?> endStepActionData = executionPlan.getStep(3L)
                                                          .getActionData();
        assertEquals(Arrays.asList(ScoreLangConstants.FAILURE_RESULT),
                endStepActionData.get(ScoreLangConstants.BREAK_LOOP_KEY));
    }

    @Test
    public void testCompileLoopFlowWithBreak() throws Exception {
        URI flow = getClass().getResource("/loops/loop_with_break.sl").toURI();
        URI operation = getClass().getResource("/loops/print.sl").toURI();
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
        URI flow = getClass().getResource("/loops/simple_loop_with_map.sl").toURI();
        Executable executable = compiler.preCompile(SlangSource.fromFile(flow));
        assertNotNull("executable is null", executable);
        Step step = ((Flow) executable).getWorkflow()
                .getSteps()
                .getFirst();
        assertTrue(step.getPreStepActionData().containsKey(SlangTextualKeys.FOR_KEY));
        LoopStatement forStatement = (LoopStatement) step.getPreStepActionData()
                .get(SlangTextualKeys.FOR_KEY);
        MapForLoopStatement mapForLoopStatement  = validateMapForLoopStatement(forStatement);
        assertEquals("person_map", mapForLoopStatement.getExpression());
        assertEquals("k", mapForLoopStatement.getKeyName());
        assertEquals("v", mapForLoopStatement.getValueName());
        @SuppressWarnings("unchecked") List<Output> outputs = (List<Output>) step.getPostStepActionData()
                .get(SlangTextualKeys.PUBLISH_KEY);
        assertEquals("a", outputs.get(0).getValue());
        assertEquals(Arrays.asList(ScoreLangConstants.FAILURE_RESULT),
                step.getPostStepActionData().get(SlangTextualKeys.BREAK_KEY));
    }

    @Test
    public void testPreCompileLoopFlowWithMapWithBreak() throws Exception {
        URI flow = getClass().getResource("/loops/loop_with_break_with_map.sl").toURI();
        Executable executable = compiler.preCompile(SlangSource.fromFile(flow));
        assertNotNull("executable is null", executable);
        Step step = ((Flow) executable).getWorkflow()
                .getSteps()
                .getFirst();
        assertEquals(Arrays.asList(ScoreLangConstants.SUCCESS_RESULT, ScoreLangConstants.FAILURE_RESULT),
                step.getPostStepActionData().get(SlangTextualKeys.BREAK_KEY));
    }

    @Test
    public void testPreCompileLoopWithMapWithCustomNavigationFlow() throws Exception {
        URI flow = getClass().getResource("/loops/loop_with_custom_navigation_with_map.sl").toURI();
        Executable executable = compiler.preCompile(SlangSource.fromFile(flow));
        assertNotNull("executable is null", executable);
        Step step = ((Flow) executable).getWorkflow()
                .getSteps()
                .getFirst();
        assertTrue(step.getPreStepActionData().containsKey(SlangTextualKeys.FOR_KEY));
        LoopStatement forStatement = (LoopStatement) step.getPreStepActionData()
                .get(SlangTextualKeys.FOR_KEY);
        MapForLoopStatement mapForLoopStatement  = validateMapForLoopStatement(forStatement);
        assertEquals("person_map", mapForLoopStatement.getExpression());
        assertEquals("k", mapForLoopStatement.getKeyName());
        assertEquals("v", mapForLoopStatement.getValueName());
        @SuppressWarnings("unchecked") List<Map<String, String>> actual = (List<Map<String, String>>) step.getPostStepActionData()
                .get(SlangTextualKeys.NAVIGATION_KEY);
        assertEquals("print_other_values", actual.get(0).get(ScoreLangConstants.SUCCESS_RESULT));
    }

    @Test
    public void testCompileLoopWithMapFlow() throws Exception {
        URI flow = getClass().getResource("/loops/simple_loop_with_map.sl").toURI();
        URI operation = getClass().getResource("/loops/print.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        CompilationArtifact artifact = compiler.compile(SlangSource.fromFile(flow), path);
        assertNotNull("artifact is null", artifact);
        ExecutionPlan executionPlan = artifact.getExecutionPlan();
        assertNotNull("executionPlan is null", executionPlan);
        Map<String, ?> startStartActionData = executionPlan.getStep(2L)
                .getActionData();
        assertTrue(startStartActionData.containsKey(ScoreLangConstants.LOOP_KEY));
        LoopStatement forStatement = (LoopStatement) startStartActionData.get(ScoreLangConstants.LOOP_KEY);
        MapForLoopStatement mapForLoopStatement  = validateMapForLoopStatement(forStatement);
        assertEquals("person_map", mapForLoopStatement.getExpression());
        assertEquals("k", mapForLoopStatement.getKeyName());
        assertEquals("v", mapForLoopStatement.getValueName());

        Map<String, ?> endStepActionData = executionPlan.getStep(3L)
                .getActionData();
        assertEquals(Arrays.asList(ScoreLangConstants.FAILURE_RESULT),
                endStepActionData.get(ScoreLangConstants.BREAK_LOOP_KEY));
    }

    @Test
    public void testCompileLoopFlowWithMapWithBreak() throws Exception {
        URI flow = getClass().getResource("/loops/loop_with_break_with_map.sl").toURI();
        URI operation = getClass().getResource("/loops/print.sl").toURI();
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
}
