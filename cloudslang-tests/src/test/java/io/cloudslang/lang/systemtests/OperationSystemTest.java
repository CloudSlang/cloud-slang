/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
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
import io.cloudslang.lang.entities.bindings.values.SensitiveValue;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
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
        Map<String, Value> userInputs = new HashMap<>();
        ScoreEvent event = trigger(compilationArtifact, userInputs, new HashSet<SystemProperty>());
        Assert.assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
    }

    @Test
    public void testCompileAndRunOperationWithData() throws Exception {
        URL resource = getClass().getResource("/yaml/test_op_2.sl");
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()), null);
        //Trigger ExecutionPlan
        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("input1", ValueFactory.create("value1"));
        userInputs.put("input2", ValueFactory.create("value2"));
        userInputs.put("input4", ValueFactory.create("value4"));
        userInputs.put("input5", ValueFactory.create("value5"));
        ScoreEvent event = trigger(compilationArtifact, userInputs, new HashSet<SystemProperty>());
        Assert.assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
    }

    @Test
    public void testCompileAndRunOperationWithWrongInputType() throws Exception {

        //Trigger ExecutionPlan
        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("input1", ValueFactory.create(new Integer(11)));
        userInputs.put("input2", ValueFactory.create("value2"));
        userInputs.put("input4", ValueFactory.create("value4"));
        userInputs.put("input5", ValueFactory.create("value5"));

        exception.expect(RuntimeException.class);
        exception.expectMessage("Error binding input: 'input1' should have a String value, but got value '11' " +
                "of type Integer.");

        URL resource = getClass().getResource("/yaml/test_op_2.sl");
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()), null);
        ScoreEvent event = trigger(compilationArtifact, userInputs, new HashSet<SystemProperty>());
        Assert.assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
    }

    @Test
    public void testCompileAndRunOperationWithDataMissingInput() throws Exception {
        URL resource = getClass().getResource("/yaml/test_op_2.sl");
        final CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()), null);
        //Trigger ExecutionPlan
        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("input2", ValueFactory.create("value2"));
        userInputs.put("input4", ValueFactory.create("value4"));
        userInputs.put("input5", ValueFactory.create("value5"));
        exception.expect(RuntimeException.class);
        exception.expectMessage("input1");
        exception.expectMessage("Required");
        ScoreEvent event = trigger(compilationArtifact, userInputs, new HashSet<SystemProperty>());
        Assert.assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
        System.out.println("testCompileAndRunOperationWithDataMissingInput finished successfully");
    }

    @Test
    public void testOperationWithJavaAction() throws Exception {
        URI resource = getClass().getResource("/yaml/java_action_test.sl").toURI();

        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), null);

        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("host", ValueFactory.create("localhost"));
        userInputs.put("port", ValueFactory.create("8080"));
        Map<String, StepData> stepsData =
                triggerWithData(compilationArtifact, userInputs, new HashSet<SystemProperty>()).getSteps();
        StepData execStepData = stepsData.get(EXEC_START_PATH);
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, execStepData.getResult());
        Assert.assertEquals("http://localhost:8080", execStepData.getOutputs().get("url"));
    }

    @Test
    public void testOperationWithJavaActionSensitive() throws Exception {
        URI resource = getClass().getResource("/yaml/java_action_sensitive_input_test.sl").toURI();

        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), null);

        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("host", ValueFactory.create("localhost"));
        userInputs.put("port", ValueFactory.create("8080"));
        Map<String, StepData> stepsData =
                triggerWithData(compilationArtifact, userInputs, new HashSet<SystemProperty>()).getSteps();
        StepData execStepData = stepsData.get(EXEC_START_PATH);
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, execStepData.getResult());
        Assert.assertEquals("http://localhost:8080", execStepData.getOutputs().get("url"));
        Assert.assertEquals("http://localhost:8080", execStepData.getOutputs().get("url1"));
        Assert.assertEquals("http://localhost:8080", execStepData.getOutputs().get("url2_not_sensitive"));
        Assert.assertEquals(SensitiveValue.SENSITIVE_VALUE_MASK, execStepData.getOutputs().get("url3_sensitive"));
    }

    @Test
    public void testOperationWithJavaActionWithSerializableOutput() throws Exception {
        URI resource = getClass().getResource("/yaml/java_action_serializable_op.sl").toURI();

        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), null);

        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("string", ValueFactory.create("please print it"));
        Map<String, StepData> stepsData =
                triggerWithData(compilationArtifact, userInputs, new HashSet<SystemProperty>()).getSteps();
        StepData execStepData = stepsData.get(EXEC_START_PATH);
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, execStepData.getResult());
        Assert.assertEquals("120", execStepData.getOutputs().get("dur"));
    }

    @Test
    public void testOperationWithPythonWithBooleanOutput() throws Exception {
        URI resource = getClass().getResource("/yaml/python_op_with_boolean.sl").toURI();

        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), null);

        Map<String, StepData> stepsData =
                triggerWithData(compilationArtifact, null, new HashSet<SystemProperty>()).getSteps();
        StepData execStepData = stepsData.get(EXEC_START_PATH);
        Assert.assertEquals("True", execStepData.getOutputs().get("condition_1"));
        Assert.assertEquals("False", execStepData.getOutputs().get("condition_2"));
        Assert.assertEquals("False", execStepData.getOutputs().get("condition_3"));
        Assert.assertEquals("True", execStepData.getOutputs().get("condition_4"));
        Assert.assertEquals("1", execStepData.getOutputs().get("an_int"));
    }

    @Test
    public void testOperationWithPythonWithSensitiveBooleanOutput() throws Exception {
        URI resource = getClass().getResource("/yaml/python_op_with_boolean_sensitive.sl").toURI();

        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), null);

        Map<String, StepData> stepsData =
                triggerWithData(compilationArtifact, null, new HashSet<SystemProperty>()).getSteps();
        StepData execStepData = stepsData.get(EXEC_START_PATH);
        Assert.assertEquals("True", execStepData.getOutputs().get("condition_1"));
        Assert.assertEquals("False", execStepData.getOutputs().get("condition_2"));
        Assert.assertEquals("False", execStepData.getOutputs().get("condition_3"));
        Assert.assertEquals(SensitiveValue.SENSITIVE_VALUE_MASK, execStepData.getOutputs().get("condition_4"));
        Assert.assertEquals("1", execStepData.getOutputs().get("an_int"));
    }

    @Test
    public void testOperationPyClassInScript() throws Exception {
        URI resource = getClass().getResource("/yaml/op_python_pyclass.sl").toURI();

        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), null);
        Map<String, StepData> stepsData =
                triggerWithData(compilationArtifact, null, new HashSet<SystemProperty>()).getSteps();

        StepData execStepData = stepsData.get(EXEC_START_PATH);
        Map<String, Serializable> expectedOutputs = new HashMap<>();
        expectedOutputs.put("x", "abc");
        Assert.assertEquals(expectedOutputs, execStepData.getOutputs());
    }

}
