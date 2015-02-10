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

import org.openscore.lang.compiler.SlangTextualKeys;
import org.openscore.lang.compiler.scorecompiler.ExecutionStepFactory;
import org.openscore.lang.entities.ScoreLangConstants;
import org.openscore.lang.entities.bindings.Input;
import org.openscore.lang.entities.bindings.Output;
import org.openscore.lang.entities.bindings.Result;
import junit.framework.Assert;
import org.openscore.api.ExecutionStep;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class ExecutionStepFactoryTest {

    private ExecutionStepFactory factory;

    @Before
    public void init() {
        factory = new ExecutionStepFactory();
    }

    @Test
    public void testCreateStartStep() throws Exception {
        ExecutionStep startStep = factory.createStartStep(1L, new HashMap<String, Serializable>(), new ArrayList<Input>(),"coolStep");
        Assert.assertNotNull("step should not be null", startStep);
        Assert.assertEquals("coolStep",startStep.getActionData().get(ScoreLangConstants.NODE_NAME_KEY));
    }

    @Test
    public void testCreateStartStepPutInputsUnderTheRightKey() throws Exception {
        ArrayList<Input> execInputs = new ArrayList<>();
        ExecutionStep startStep = factory.createStartStep(1L, new HashMap<String, Serializable>(), execInputs,"");
        Assert.assertNotNull("inputs key is null", startStep.getActionData().get(ScoreLangConstants.EXECUTABLE_INPUTS_KEY));
        Assert.assertSame("inputs are not set under their key", execInputs, startStep.getActionData().get(ScoreLangConstants.EXECUTABLE_INPUTS_KEY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateStartStepWithNullData() throws Exception {
        factory.createStartStep(1L, null, new ArrayList<Input>(),"");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateStartStepWithNullInputs() throws Exception {
        factory.createStartStep(1L, new HashMap<String, Serializable>(), null,"");
    }

    @Test
    public void testCreateActionStep() throws Exception {
        ExecutionStep actionStep = factory.createActionStep(1L, new HashMap<String, Serializable>());
        Assert.assertNotNull("step should not be null", actionStep);
    }

    @Test
    public void testCreateJavaActionStep() throws Exception {
        HashMap<String, Serializable> actionRawData = new HashMap<>();
        HashMap<String, String> javaActionData = new HashMap<>();
        javaActionData.put("key", "value");
        actionRawData.put(SlangTextualKeys.JAVA_ACTION, javaActionData);
        ExecutionStep actionStep = factory.createActionStep(1L, actionRawData);
        Assert.assertNotNull("step should not be null", actionStep);
        Assert.assertEquals(actionStep.getActionData().get("key"), "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateActionStepWithNullData() throws Exception {
        factory.createActionStep(1L, null);
    }

    @Test
    public void testCreateEndStep() throws Exception {
        ExecutionStep endStep = factory.createEndStep(1L, new HashMap<String, Serializable>(), new ArrayList<Output>(), new ArrayList<Result>(),"");
        Assert.assertNotNull("step should not be null", endStep);
    }

    @Test
    public void testCreateEndStepPutOutputsUnderTheRightKey() throws Exception {
        ArrayList<Output> outputs = new ArrayList<>();
        ExecutionStep endStep = factory.createEndStep(1L, new HashMap<String, Serializable>(), outputs, new ArrayList<Result>(),"");
        Assert.assertNotNull("outputs key is null", endStep.getActionData().get(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY));
        Assert.assertSame("outputs are not set under their key", outputs, endStep.getActionData().get(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY));
    }

    @Test
    public void testCreateEndStepPutResultsUnderTheRightKey() throws Exception {
        ArrayList<Result> results = new ArrayList<>();
        ExecutionStep endStep = factory.createEndStep(1L, new HashMap<String, Serializable>(), new ArrayList<Output>(), results,"");
        Assert.assertNotNull("results key is null", endStep.getActionData().get(ScoreLangConstants.EXECUTABLE_RESULTS_KEY));
        Assert.assertSame("results are not set under their key", results, endStep.getActionData().get(ScoreLangConstants.EXECUTABLE_RESULTS_KEY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEndStepWithNullData() throws Exception {
        factory.createEndStep(1L, null, new ArrayList<Output>(), new ArrayList<Result>(),"");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEndStepWithNullOutputs() throws Exception {
        factory.createEndStep(1L, new HashMap<String, Serializable>(), null, new ArrayList<Result>(),"");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEndStepWithNullResults() throws Exception {
        factory.createEndStep(1L, new HashMap<String, Serializable>(), new ArrayList<Output>(), null,"");
    }

    @Test
    public void testStepName() throws Exception {
        ArrayList<Result> results = new ArrayList<>();
        ExecutionStep endStep = factory.createEndStep(1L, new HashMap<String, Serializable>(), new ArrayList<Output>(), results,"stepX");
        Assert.assertNotNull("results key is null", endStep.getActionData().get(ScoreLangConstants.NODE_NAME_KEY));
        Assert.assertEquals("stepX", endStep.getActionData().get(ScoreLangConstants.NODE_NAME_KEY));
    }
}