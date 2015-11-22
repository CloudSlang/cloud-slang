/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.systemtests;

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.score.events.ScoreEvent;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.*;

/**
 * Date: 11/14/2014
 * @author Bonczidai Levente
 */
public class SimpleFlowTest extends SystemsTestsParent {

	private static final Map<String, Serializable> SYS_PROPS = new HashMap<>();
	static {
		SYS_PROPS.put("user.sys.props.host", "localhost");
		SYS_PROPS.put("user.sys.props.port", 22);
		SYS_PROPS.put("user.sys.props.alla", "balla");
	}

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static final long DEFAULT_TIMEOUT = 20000;

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testSimpleFlowBasic() throws Exception {
        Map<String, Serializable> inputs = new HashMap<>();
        inputs.put("input1", "-2");
        inputs.put("time_zone_as_string", "+2");
		compileAndRunSimpleFlow(inputs, SYS_PROPS);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testSimpleFlowInvalidOneLinerSyntax() throws Exception {
        Map<String, Serializable> inputs = new HashMap<>();
        inputs.put("input1", "-2");
        inputs.put("time_zone_as_string", "+2");
        compileAndRunSimpleFlowOneLinerSyntax(inputs, SYS_PROPS);
    }

	@Test(timeout = DEFAULT_TIMEOUT)
	public void testSimpleFlowNavigation() throws Exception {
        Map<String, Serializable> inputs = new HashMap<>();
        inputs.put("input1", -999);
		compileAndRunSimpleFlow(inputs, SYS_PROPS);
	}

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testSimpleFlowBasicMissingFlowInput() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("input1");
        exception.expectMessage("Required");
        compileAndRunSimpleFlow(new HashMap<String, Serializable>(), null);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testSimpleFlowBasicMissingSysProps() throws Exception {
        Map<String, Serializable> inputs = new HashMap<>();
        inputs.put("input1", "-2");
        inputs.put("time_zone_as_string", "+2");
        exception.expect(RuntimeException.class);
        exception.expectMessage("host");
        exception.expectMessage("Required");
        compileAndRunSimpleFlow(inputs, null);
    }

    @Test
    public void testFlowWithGlobalSession() throws Exception {
        URI resource = getClass().getResource("/yaml/flow_using_global_session.yaml").toURI();
        URI operation1 = getClass().getResource("/yaml/set_global_session_object.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/get_global_session_object.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1), SlangSource.fromFile(operation2));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("object_value", "SessionValue");
        ScoreEvent event = trigger(compilationArtifact, userInputs, null);
        Assert.assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
    }

	private void compileAndRunSimpleFlow(Map<String, ? extends Serializable> inputs, Map<String, ? extends Serializable> systemProperties) throws Exception {
		URI flow = getClass().getResource("/yaml/simple_flow.yaml").toURI();
		URI operations1 = getClass().getResource("/yaml/get_time_zone.sl").toURI();
		URI operations2 = getClass().getResource("/yaml/comopute_daylight_time_zone.sl").toURI();
		Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operations1), SlangSource.fromFile(operations2));
		CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(flow), path);
        Assert.assertEquals("the system properties size is not as expected", 2, compilationArtifact.getSystemProperties().size());
		ScoreEvent event = trigger(compilationArtifact, inputs, systemProperties);
		Assert.assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
	}

    private void compileAndRunSimpleFlowOneLinerSyntax(Map<String, ? extends Serializable> inputs, Map<String, ? extends Serializable> systemProperties) throws Exception {
        URI flow = getClass().getResource("/yaml/simple_flow_one_liner.yaml").toURI();
        URI operations1 = getClass().getResource("/yaml/get_time_zone.sl").toURI();
        URI operations2 = getClass().getResource("/yaml/comopute_daylight_time_zone.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operations1), SlangSource.fromFile(operations2));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Task arguments");
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(flow), path);
        ScoreEvent event = trigger(compilationArtifact, inputs, systemProperties);
    }

    @Test
    public void testFlowWithMissingNavigationFromOperationResult() throws Exception {
        URI resource = getClass().getResource("/yaml/flow_with_missing_navigation_from_op_result.sl").toURI();
        URI operations = getClass().getResource("/yaml/print_custom_result_op.sl").toURI();

        SlangSource operationsSource = SlangSource.fromFile(operations);
        Set<SlangSource> path = Sets.newHashSet(operationsSource);
        exception.expect(RuntimeException.class);
        exception.expectMessage("Task1");
        exception.expectMessage("CUSTOM");
        exception.expectMessage("navigation");
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);
        trigger(compilationArtifact, new HashMap<String, Serializable>(), null);
    }

    @Test
    public void testFlowWithSameInputNameAsTask() throws Exception {
        URI resource = getClass().getResource("/yaml/flow_with_same_input_name_as_task.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/string_equals.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/test_op.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1), SlangSource.fromFile(operation2));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("first", "value");
        userInputs.put("second_string", "value");

        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, null).getTasks();

        List<String> actualTasks = getTasksOnly(stepsData);
        Assert.assertEquals(2, actualTasks.size());
        StepData firstTask = stepsData.get(FIRST_STEP_PATH);
        StepData secondTask = stepsData.get(SECOND_STEP_KEY);
        Assert.assertEquals("CheckBinding", firstTask.getName());
        Assert.assertEquals("TaskOnSuccess", secondTask.getName());
    }

    @Test
    public void testGetFunctionBasic() throws Exception {
        URL resource = getClass().getResource("/yaml/get_function_test_flow_basic.sl");
        URI operation = getClass().getResource("/yaml/get_function_test_basic.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()), path);

        // trigger ExecutionPlan
        Map<String, Serializable> userInputs = new HashMap<>();
        RuntimeInformation runtimeInformation = triggerWithData(compilationArtifact, userInputs, null);
        Map<String, StepData> executionData = runtimeInformation.getTasks();

        StepData flowData = executionData.get(EXEC_START_PATH);
        StepData taskData = executionData.get(FIRST_STEP_PATH);
        Assert.assertNotNull("flow data is null", flowData);
        Assert.assertNotNull("task data is null", taskData);

        // verify 'get' function worked in input binding and locals().get() works
        Map<String, Serializable> expectedFlowInputs = new LinkedHashMap<>();
        expectedFlowInputs.put("input1", null);
        expectedFlowInputs.put("input1_safe", "input1_default");
        expectedFlowInputs.put("input2", 22);
        expectedFlowInputs.put("input2_safe", 22);
        expectedFlowInputs.put("input_locals_found", 22);
        expectedFlowInputs.put("input_locals_not_found", "input_locals_not_found_default");
        Map<String, Serializable> actualFlowInputs = flowData.getInputs();
        Assert.assertEquals("flow input values not as expected", expectedFlowInputs, actualFlowInputs);

        // verify 'get' function worked in output binding
        Map<String, Serializable> expectedOperationPublishValues = new LinkedHashMap<>();
        expectedOperationPublishValues.put("output1_safe", "CloudSlang");
        expectedOperationPublishValues.put("output2_safe", "output2_default");
        expectedOperationPublishValues.put("output_same_name", "output_same_name_default");
        Map<String, Serializable> actualOperationPublishValues = taskData.getOutputs();
        Assert.assertEquals("operation publish values not as expected", expectedOperationPublishValues, actualOperationPublishValues);

        // verify 'get' function worked in result expressions
        Assert.assertEquals("Get function problem in result expression", "GET_FUNCTION_KEY_EXISTS", flowData.getResult());
    }

    @Test
    public void testGetFunctionDefaultResult() throws Exception {
        URL resource = getClass().getResource("/yaml/get_function_test_flow_default_result.sl");
        URI operation = getClass().getResource("/yaml/get_function_test_default_result.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()), path);

        // trigger ExecutionPlan
        Map<String, Serializable> userInputs = new HashMap<>();
        RuntimeInformation runtimeInformation = triggerWithData(compilationArtifact, userInputs, null);
        Map<String, StepData> executionData = runtimeInformation.getTasks();

        StepData flowData = executionData.get(EXEC_START_PATH);

        // verify 'get' function worked in result expressions
        Assert.assertEquals("Get function problem in result expression", "GET_FUNCTION_DEFAULT_VALUE", flowData.getResult());
    }

}
