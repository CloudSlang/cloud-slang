package com.hp.score.lang.compiler;

import com.hp.score.api.ExecutionStep;
import com.hp.score.lang.compiler.utils.ExecutionStepFactory;
import com.hp.score.lang.entities.ScoreLangConstants;
import junit.framework.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.HashMap;

public class ExecutionStepFactoryTest {

    @Test
    public void testCreateStartStep() throws Exception {
        ExecutionStepFactory factory = new ExecutionStepFactory();
        ExecutionStep startStep = factory.createStartStep(1L, new HashMap<String, Serializable>());
        Assert.assertNotNull("step should not be null", startStep);
    }

    @Test
    public void testCreateStartStepPutInputsUnderTheRightKey() throws Exception {
        String placeHolder = "place_holder";
        ExecutionStepFactory factory = new ExecutionStepFactory();
        HashMap<String, Serializable> preOpData = new HashMap<>();
        preOpData.put(SlangTextualKeys.INPUTS_KEY, placeHolder);
        ExecutionStep startStep = factory.createStartStep(1L, preOpData);
        Assert.assertNotNull("inputs key is null", startStep.getActionData().get(ScoreLangConstants.OPERATION_INPUTS_KEY));
        Assert.assertEquals("inputs are not set under their key", placeHolder, startStep.getActionData().get(ScoreLangConstants.OPERATION_INPUTS_KEY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateStartStepWithNullData() throws Exception {
        ExecutionStepFactory factory = new ExecutionStepFactory();
        factory.createStartStep(1L, null);
    }

    @Test
    public void testCreateActionStep() throws Exception {
        ExecutionStepFactory factory = new ExecutionStepFactory();
        ExecutionStep actionStep = factory.createActionStep(1L, new HashMap<String, Serializable>());
        Assert.assertNotNull("step should not be null", actionStep);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateActionStepWithNullData() throws Exception {
        ExecutionStepFactory factory = new ExecutionStepFactory();
        factory.createActionStep(1L, null);
    }

    @Test
    public void testCreateEndStep() throws Exception {
        ExecutionStepFactory factory = new ExecutionStepFactory();
        ExecutionStep endStep = factory.createEndStep(1L, new HashMap<String, Serializable>());
        Assert.assertNotNull("step should not be null", endStep);
    }

    @Test
    public void testCreateEndStepPutOutputsUnderTheRightKey() throws Exception {
        String placeHolder = "place_holder";
        ExecutionStepFactory factory = new ExecutionStepFactory();
        HashMap<String, Serializable> postOpData = new HashMap<>();
        postOpData.put(SlangTextualKeys.OUTPUTS_KEY, placeHolder);
        ExecutionStep endStep = factory.createEndStep(1L, postOpData);
        Assert.assertNotNull("outputs key is null", endStep.getActionData().get(ScoreLangConstants.OPERATION_OUTPUTS_KEY));
        Assert.assertEquals("outputs are not set under their key", placeHolder, endStep.getActionData().get(ScoreLangConstants.OPERATION_OUTPUTS_KEY));
    }

    @Test
    public void testCreateEndStepPutResultsUnderTheRightKey() throws Exception {
        String placeHolder = "place_holder";
        ExecutionStepFactory factory = new ExecutionStepFactory();
        HashMap<String, Serializable> postOpData = new HashMap<>();
        postOpData.put(SlangTextualKeys.RESULT_KEY, placeHolder);
        ExecutionStep endStep = factory.createEndStep(1L, postOpData);
        Assert.assertNotNull("results key is null", endStep.getActionData().get(ScoreLangConstants.OPERATION_RESULTS_KEY));
        Assert.assertEquals("results are not set under their key", placeHolder, endStep.getActionData().get(ScoreLangConstants.OPERATION_RESULTS_KEY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEndStepWithNullData() throws Exception {
        ExecutionStepFactory factory = new ExecutionStepFactory();
        factory.createEndStep(1L, null);
    }
}