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
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import io.cloudslang.score.events.ScoreEvent;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testSimpleFlowBasicMissingTaskInput() throws Exception {
        URI flow = getClass().getResource("/yaml/simple_flow.yaml").toURI();
        URI operations1 = getClass().getResource("/yaml/get_time_zone.sl").toURI();
        URI operations2 = getClass().getResource("/yaml/comopute_daylight_time_zone.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operations1), SlangSource.fromFile(operations2));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(flow), path);
        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("input1", "-2");
        userInputs.put("time_zone_as_string", "+2");
        userInputs.put("host", "hostName");
        for (Map.Entry<String, ? extends Serializable> input : new HashMap<String, Serializable>().entrySet()) {
            userInputs.put(input.getKey(), input.getValue());
        }
        exception.expect(RuntimeException.class);
        exception.expectMessage("port");
        exception.expectMessage("Required");
        ScoreEvent event = trigger(compilationArtifact, userInputs, new HashMap<String, Serializable>());
        Assert.assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
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
        Assert.assertEquals("the system properties size is not as expected", 3, compilationArtifact.getSystemProperties().size());
		ScoreEvent event = trigger(compilationArtifact, inputs, systemProperties);
		Assert.assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
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
}
