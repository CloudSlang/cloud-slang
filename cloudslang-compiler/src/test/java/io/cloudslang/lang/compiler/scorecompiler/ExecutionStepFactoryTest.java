/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.scorecompiler;

import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.entities.ExecutableType;
import io.cloudslang.lang.entities.ListLoopStatement;
import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.entities.bindings.ScriptFunction;
import io.cloudslang.score.api.ExecutionStep;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

public class ExecutionStepFactoryTest {

    private ExecutionStepFactory factory;

    @Before
    public void init() {
        factory = new ExecutionStepFactory();
    }

    @Test
    public void testCreateStartStep() throws Exception {
        ExecutionStep startStep = factory.createStartStep(1L, new HashMap<String, Serializable>(),
                new ArrayList<Input>(), "coolStep", ExecutableType.FLOW);
        Assert.assertNotNull("step should not be null", startStep);
        Assert.assertEquals("coolStep", startStep.getActionData().get(ScoreLangConstants.NODE_NAME_KEY));
    }

    @Test
    public void testCreateStartStepPutInputsUnderTheRightKey() throws Exception {
        ArrayList<Input> execInputs = new ArrayList<>();
        ExecutionStep startStep = factory.createStartStep(1L, new HashMap<String, Serializable>(),
                execInputs, "", ExecutableType.FLOW);
        Assert.assertNotNull("inputs key is null", startStep.getActionData()
                .get(ScoreLangConstants.EXECUTABLE_INPUTS_KEY));
        Assert.assertSame("inputs are not set under their key", execInputs,
                startStep.getActionData().get(ScoreLangConstants.EXECUTABLE_INPUTS_KEY));
    }

    @Test
    public void testCreateStartStepPutForUnderTheRightKey() throws Exception {
        LoopStatement statement = new ListLoopStatement("1", "2", new HashSet<ScriptFunction>(),
                new HashSet<String>(), false);
        HashMap<String, Serializable> preStepData = new HashMap<>();
        preStepData.put(SlangTextualKeys.FOR_KEY, statement);
        ExecutionStep startStep = factory.createBeginStepStep(1L, new ArrayList<Argument>(), preStepData, "", "");
        LoopStatement actualStatement = (LoopStatement) startStep.getActionData()
                .get(ScoreLangConstants.LOOP_KEY);
        Assert.assertNotNull("for key is null", actualStatement);
        Assert.assertSame("inputs are not set under their key", statement, actualStatement);
    }

    @Test
    public void testCreateFinishTakStep() {
        ExecutionStep finishStepStep = factory.createFinishStepStep(
                1L,
                new HashMap<String, Serializable>(),
                new HashMap<String, ResultNavigation>(),
                "stepName",
                false);
        Assert.assertTrue(finishStepStep.getActionData().containsKey(ScoreLangConstants.PREVIOUS_STEP_ID_KEY));
        Assert.assertTrue(finishStepStep.getActionData().containsKey(ScoreLangConstants.BREAK_LOOP_KEY));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateStartStepWithNullData() throws Exception {
        factory.createStartStep(1L, null, new ArrayList<Input>(), "", ExecutableType.FLOW);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateStartStepWithNullInputs() throws Exception {
        factory.createStartStep(1L, new HashMap<String, Serializable>(), null, "", ExecutableType.FLOW);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateActionStepWithEmptyData() throws Exception {
        factory.createActionStep(1L, new HashMap<String, Serializable>());
    }

    @Test
    public void testCreateJavaActionStep() throws Exception {
        HashMap<String, Serializable> actionRawData = new HashMap<>();
        HashMap<String, String> javaActionData = new HashMap<>();
        javaActionData.put("key", "value");
        actionRawData.put(SlangTextualKeys.JAVA_ACTION_KEY, javaActionData);
        ExecutionStep actionStep = factory.createActionStep(1L, actionRawData);
        Assert.assertNotNull("step should not be null", actionStep);
        Assert.assertEquals(actionStep.getActionData().get("key"), "value");
    }

    @Test
    public void testCreatePythonActionStep() throws Exception {
        HashMap<String, Serializable> pythonActionData = new HashMap<>();
        pythonActionData.put(ScoreLangConstants.PYTHON_ACTION_SCRIPT_KEY, "print 'Hi there'");
        HashMap<String, Serializable> actionRawData = new HashMap<>();
        actionRawData.put(SlangTextualKeys.PYTHON_ACTION_KEY, pythonActionData);
        ExecutionStep actionStep = factory.createActionStep(1L, actionRawData);
        Assert.assertNotNull("step should not be null", actionStep);
        Assert.assertEquals(actionStep.getActionData()
                .get(ScoreLangConstants.PYTHON_ACTION_SCRIPT_KEY), "print 'Hi there'");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateActionStepWithNullData() throws Exception {
        factory.createActionStep(1L, null);
    }

    @Test
    public void testCreateEndStep() throws Exception {
        ExecutionStep endStep = factory.createEndStep(1L, new HashMap<String, Serializable>(),
                new ArrayList<Output>(), new ArrayList<Result>(), "", ExecutableType.FLOW);
        Assert.assertNotNull("step should not be null", endStep);
    }

    @Test
    public void testCreateEndStepPutOutputsUnderTheRightKey() throws Exception {
        ArrayList<Output> outputs = new ArrayList<>();
        ExecutionStep endStep = factory.createEndStep(1L, new HashMap<String, Serializable>(),
                outputs, new ArrayList<Result>(), "", ExecutableType.FLOW);
        Assert.assertNotNull("outputs key is null", endStep.getActionData()
                .get(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY));
        Assert.assertSame("outputs are not set under their key", outputs,
                endStep.getActionData().get(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY));
    }

    @Test
    public void testCreateEndStepPutResultsUnderTheRightKey() throws Exception {
        ArrayList<Result> results = new ArrayList<>();
        ExecutionStep endStep = factory.createEndStep(1L, new HashMap<String, Serializable>(),
                new ArrayList<Output>(), results, "", ExecutableType.FLOW);
        Assert.assertNotNull("results key is null", endStep.getActionData()
                .get(ScoreLangConstants.EXECUTABLE_RESULTS_KEY));
        Assert.assertSame("results are not set under their key", results,
                endStep.getActionData().get(ScoreLangConstants.EXECUTABLE_RESULTS_KEY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEndStepWithNullData() throws Exception {
        factory.createEndStep(1L, null, new ArrayList<Output>(), new ArrayList<Result>(), "", ExecutableType.FLOW);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEndStepWithNullOutputs() throws Exception {
        factory.createEndStep(1L, new HashMap<String, Serializable>(), null,
                new ArrayList<Result>(), "", ExecutableType.FLOW);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEndStepWithNullResults() throws Exception {
        factory.createEndStep(1L, new HashMap<String, Serializable>(),
                new ArrayList<Output>(), null, "", ExecutableType.FLOW);
    }

    @Test
    public void testStepName() throws Exception {
        ArrayList<Result> results = new ArrayList<>();
        ExecutionStep endStep = factory.createEndStep(1L, new HashMap<String, Serializable>(),
                new ArrayList<Output>(), results, "stepX", ExecutableType.FLOW);
        Assert.assertNotNull("results key is null", endStep.getActionData().get(ScoreLangConstants.NODE_NAME_KEY));
        Assert.assertEquals("stepX", endStep.getActionData().get(ScoreLangConstants.NODE_NAME_KEY));
    }

    @Test
    public void testExecutableType() throws Exception {
        ArrayList<Result> results = new ArrayList<>();
        ExecutionStep endStep = factory.createEndStep(1L, new HashMap<String, Serializable>(),
                new ArrayList<Output>(), results, "stepX", ExecutableType.FLOW);
        Assert.assertNotNull("key is null", endStep.getActionData().get(ScoreLangConstants.EXECUTABLE_TYPE));
        Assert.assertEquals(ExecutableType.FLOW, endStep.getActionData().get(ScoreLangConstants.EXECUTABLE_TYPE));
    }

    @Test
    public void testCreateAddBranchesStep() throws Exception {
        ExecutionStep startStep = factory.createAddBranchesStep(2L, 5L, 3L,
                new HashMap<String, Serializable>(), "refID", "evenCoolerStep");
        Assert.assertNotNull("step should not be null", startStep);
        Assert.assertEquals("evenCoolerStep", startStep.getActionData().get(ScoreLangConstants.NODE_NAME_KEY));
    }

    @Test
    public void testSplitStep() throws Exception {
        ExecutionStep startStep = factory.createAddBranchesStep(2L, 5L, 3L,
                new HashMap<String, Serializable>(), "refID", "evenCoolerStep");
        Assert.assertNotNull("step should not be null", startStep);
        Assert.assertEquals("not marked as split step", true, startStep.isSplitStep());
    }

    @Test
    public void testCreateAddBranchesStepPutParallelLoopUnderTheRightKey() throws Exception {
        ListLoopStatement statement = new ListLoopStatement("value", "values",
                new HashSet<ScriptFunction>(), new HashSet<String>(), true);
        HashMap<String, Serializable> preStepData = new HashMap<>();
        preStepData.put(SlangTextualKeys.PARALLEL_LOOP_KEY, statement);
        ExecutionStep startStep = factory.createAddBranchesStep(2L, 5L, 3L, preStepData, "refID", "evenCoolerStep");
        ListLoopStatement actualStatement = (ListLoopStatement) startStep.getActionData()
                .get(ScoreLangConstants.PARALLEL_LOOP_STATEMENT_KEY);
        Assert.assertNotNull("parallel loop statement not found in action data", actualStatement);
        Assert.assertSame("parallel loop statement in not correctly set under the key", statement, actualStatement);
    }

    @Test
    public void testCreateJoinBranchesStep() throws Exception {
        Map<String, Serializable> postStepData = new HashMap<>();
        postStepData.put(SlangTextualKeys.PUBLISH_KEY, new ArrayList<>());

        ExecutionStep executionStep = factory.createJoinBranchesStep(
                0L,
                postStepData,
                new HashMap<String, ResultNavigation>(),
                "joinStep");

        @SuppressWarnings("unchecked")
        Map<String, Serializable> actionData = (Map<String, Serializable>) executionStep.getActionData();
        Assert.assertTrue(actionData.containsKey(ScoreLangConstants.STEP_PUBLISH_KEY));
        Assert.assertTrue(actionData.containsKey(ScoreLangConstants.STEP_NAVIGATION_KEY));
        Assert.assertTrue(actionData.containsKey(ScoreLangConstants.NODE_NAME_KEY));
    }

}
