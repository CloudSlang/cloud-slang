package com.hp.score.lang.compiler.utils;

import com.hp.score.api.ExecutionStep;
import com.hp.score.lang.compiler.SlangTextualKeys;
import com.hp.score.lang.entities.ScoreLangConstants;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.HashMap;

public class ExecutionStepFactoryTest {

    private ExecutionStepFactory factory;

    @Before
    public void init() {
        factory = new ExecutionStepFactory();
    }

    @Test
    public void testCreateStartStep() throws Exception {
        ExecutionStep startStep = factory.createStartStep(1L, new HashMap<String, Serializable>());
        Assert.assertNotNull("step should not be null", startStep);
    }

    @Test
    public void testCreateStartStepPutInputsUnderTheRightKey() throws Exception {
        String placeHolder = "place_holder";
        HashMap<String, Serializable> preOpData = new HashMap<>();
        preOpData.put(SlangTextualKeys.INPUTS_KEY, placeHolder);
        ExecutionStep startStep = factory.createStartStep(1L, preOpData);
        Assert.assertNotNull("inputs key is null", startStep.getActionData().get(ScoreLangConstants.OPERATION_INPUTS_KEY));
        Assert.assertEquals("inputs are not set under their key", placeHolder, startStep.getActionData().get(ScoreLangConstants.OPERATION_INPUTS_KEY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateStartStepWithNullData() throws Exception {
        factory.createStartStep(1L, null);
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
        ExecutionStep endStep = factory.createEndStep(1L, new HashMap<String, Serializable>());
        Assert.assertNotNull("step should not be null", endStep);
    }

    @Test
    public void testCreateEndStepPutOutputsUnderTheRightKey() throws Exception {
        String placeHolder = "place_holder";
        HashMap<String, Serializable> postOpData = new HashMap<>();
        postOpData.put(SlangTextualKeys.OUTPUTS_KEY, placeHolder);
        ExecutionStep endStep = factory.createEndStep(1L, postOpData);
        Assert.assertNotNull("outputs key is null", endStep.getActionData().get(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY));
        Assert.assertEquals("outputs are not set under their key", placeHolder, endStep.getActionData().get(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY));
    }

    @Test
    public void testCreateEndStepPutResultsUnderTheRightKey() throws Exception {
        String placeHolder = "place_holder";
        HashMap<String, Serializable> postOpData = new HashMap<>();
        postOpData.put(SlangTextualKeys.RESULT_KEY, placeHolder);
        ExecutionStep endStep = factory.createEndStep(1L, postOpData);
        Assert.assertNotNull("results key is null", endStep.getActionData().get(ScoreLangConstants.EXECUTABLE_RESULTS_KEY));
        Assert.assertEquals("results are not set under their key", placeHolder, endStep.getActionData().get(ScoreLangConstants.EXECUTABLE_RESULTS_KEY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEndStepWithNullData() throws Exception {
        factory.createEndStep(1L, null);
    }
}