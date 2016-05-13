/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.lang.systemtests;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.score.events.ScoreEvent;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * User: stoneo
 * Date: 11/11/2014
 * Time: 11:55
 */

public class OperationSystemTest extends SystemsTestsParent {

    @Test
    public void testCompileAndRunOperationBasic() throws Exception {
        URL resource = getClass().getResource("/yaml/test_op.sl");
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()), null);
        //Trigger ExecutionPlan
        Map<String, Serializable> userInputs = new HashMap<>();
        ScoreEvent event = trigger(compilationArtifact, userInputs, new HashSet<SystemProperty>());
        Assert.assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
    }

    @Test
    public void testCompileAndRunOperationWithData() throws Exception {
        URL resource = getClass().getResource("/yaml/test_op_2.sl");
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()),null);
        //Trigger ExecutionPlan
        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("input1", "value1");
        userInputs.put("input2", "value2");
        userInputs.put("input4", "value4");
        userInputs.put("input5", "value5");
        ScoreEvent event = trigger(compilationArtifact, userInputs, new HashSet<SystemProperty>());
        Assert.assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
    }
    @Test
    public void testCompileAndRunOperationWithDataMissingInput() throws Exception {
        URL resource = getClass().getResource("/yaml/test_op_2.sl");
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()),null);
        //Trigger ExecutionPlan
        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("input2", "value2");
        userInputs.put("input4", "value4");
        userInputs.put("input5", "value5");
        exception.expect(RuntimeException.class);
        exception.expectMessage("input1");
        exception.expectMessage("Required");
        ScoreEvent event = trigger(compilationArtifact, userInputs, new HashSet<SystemProperty>());
        Assert.assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
    }

    @Test
    public void testOperationWithJavaAction() throws Exception {
        URI resource = getClass().getResource("/yaml/java_action_test.sl").toURI();

        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), null);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("host", "localhost");
        userInputs.put("port", "8080");
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, new HashSet<SystemProperty>()).getSteps();
        StepData execStepData = stepsData.get(EXEC_START_PATH);
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, execStepData.getResult());
        Assert.assertEquals("http://localhost:8080", execStepData.getOutputs().get("url"));
    }

    @Test
    public void testOperationWithJavaActionWithSerializableOutput() throws Exception {
        URI resource = getClass().getResource("/yaml/java_action_serializable_op.sl").toURI();

        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), null);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("string", "please print it");
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, new HashSet<SystemProperty>()).getSteps();
        StepData execStepData = stepsData.get(EXEC_START_PATH);
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, execStepData.getResult());
        Assert.assertEquals(120, execStepData.getOutputs().get("dur"));
    }

    @Test
    public void testOperationWithPythonWithBooleanOutput() throws Exception {
        URI resource = getClass().getResource("/yaml/python_op_with_boolean.sl").toURI();

        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), null);

        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, null, new HashSet<SystemProperty>()).getSteps();
        StepData execStepData = stepsData.get(EXEC_START_PATH);
        Assert.assertEquals(true, execStepData.getOutputs().get("condition_1"));
        Assert.assertEquals(false, execStepData.getOutputs().get("condition_2"));
        Assert.assertEquals(false, execStepData.getOutputs().get("condition_3"));
        Assert.assertEquals(true, execStepData.getOutputs().get("condition_4"));
        Assert.assertEquals(1, execStepData.getOutputs().get("an_int"));
    }

}