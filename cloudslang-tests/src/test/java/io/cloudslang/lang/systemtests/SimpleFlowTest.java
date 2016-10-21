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

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.events.ScoreEvent;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import static io.cloudslang.lang.compiler.SlangSource.fromFile;
import static org.apache.commons.io.FileUtils.readFileToString;

/**
 * Date: 11/14/2014
 *
 * @author Bonczidai Levente
 */
public class SimpleFlowTest extends SystemsTestsParent {

    private static final Set<SystemProperty> SYS_PROPS = new HashSet<>();
    @SuppressWarnings("unchecked")
    private static final Set<SystemProperty> EMPTY_SET = Collections.EMPTY_SET;

    static {
        SYS_PROPS.add(new SystemProperty("user.sys", "props.host", "localhost"));
        SYS_PROPS.add(new SystemProperty("user.sys", "props.port", "22"));
        SYS_PROPS.add(new SystemProperty("user.sys", "props.alla", "balla"));
    }

    private static final long DEFAULT_TIMEOUT = 20000;

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testSimpleFlowBasic() throws Exception {
        Map<String, Value> inputs = new HashMap<>();
        inputs.put("input1", ValueFactory.create("-2"));
        inputs.put("time_zone_as_string", ValueFactory.create("+2"));
        compileAndRunSimpleFlow(inputs, SYS_PROPS);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testOneLinerIsInvalid() throws Exception {
        Map<String, Value> inputs = new HashMap<>();
        inputs.put("input1", ValueFactory.create("-2"));
        inputs.put("time_zone_as_string", ValueFactory.create("+2"));
        compileAndRunSimpleFlowOneLinerSyntax(inputs, SYS_PROPS);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testSimpleFlowNavigation() throws Exception {
        Map<String, Value> inputs = new HashMap<>();
        inputs.put("input1", ValueFactory.create("-999"));
        compileAndRunSimpleFlow(inputs, SYS_PROPS);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testSimpleFlowBasicMissingFlowInput() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("input1");
        exception.expectMessage("Required");
        compileAndRunSimpleFlow(new HashMap<String, Value>(), EMPTY_SET);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testSimpleFlowBasicMissingSysProps() throws Exception {
        Map<String, Value> inputs = new HashMap<>();
        inputs.put("input1", ValueFactory.create("-2"));
        inputs.put("time_zone_as_string", ValueFactory.create("+2"));
        exception.expect(RuntimeException.class);
        exception.expectMessage("host");
        exception.expectMessage("Required");
        compileAndRunSimpleFlow(inputs, EMPTY_SET);
    }

    @Test
    public void testFlowWithGlobalSession() throws Exception {
        URI resource = getClass().getResource("/yaml/flow_using_global_session.yaml").toURI();
        URI operation1 = getClass().getResource("/yaml/set_global_session_object.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/get_global_session_object.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(fromFile(operation1), fromFile(operation2));
        CompilationArtifact compilationArtifact = slang.compile(fromFile(resource), path);

        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("object_value", ValueFactory.create("SessionValue"));
        ScoreEvent event = trigger(compilationArtifact, userInputs, EMPTY_SET);
        Assert.assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
    }

    private void compileAndRunSimpleFlow(Map<String, Value> inputs,
                                         Set<SystemProperty> systemProperties) throws Exception {
        URI flow = getClass().getResource("/yaml/simple_flow.yaml").toURI();
        URI operations1 = getClass().getResource("/yaml/get_time_zone.sl").toURI();
        URI operations2 = getClass().getResource("/yaml/compute_daylight_time_zone.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(fromFile(operations1), fromFile(operations2));
        CompilationArtifact compilationArtifact = slang.compile(fromFile(flow), path);
        Assert.assertEquals("the system properties size is not as expected",
                2, compilationArtifact.getSystemProperties().size());
        ScoreEvent event = trigger(compilationArtifact, inputs, systemProperties);
        Assert.assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
    }

    private void compileAndRunSimpleFlowOneLinerSyntax(Map<String, Value> inputs,
                                                       Set<SystemProperty> systemProperties) throws Exception {
        URI flow = getClass().getResource("/yaml/simple_flow_one_liner.yaml").toURI();
        URI operations1 = getClass().getResource("/yaml/get_time_zone.sl").toURI();
        URI operations2 = getClass().getResource("/yaml/compute_daylight_time_zone.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(fromFile(operations1), fromFile(operations2));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step arguments");
        CompilationArtifact compilationArtifact = slang.compile(fromFile(flow), path);
        trigger(compilationArtifact, inputs, systemProperties);
    }

    @Test
    public void testFlowWithMissingNavigationFromOperationResult() throws Exception {
        URI resource = getClass().getResource("/yaml/flow_with_missing_navigation_from_op_result.sl").toURI();
        URI operations = getClass().getResource("/yaml/print_custom_result_op.sl").toURI();

        SlangSource operationsSource = fromFile(operations);
        Set<SlangSource> path = Sets.newHashSet(operationsSource);
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step1");
        exception.expectMessage("CUSTOM");
        exception.expectMessage("navigation");
        CompilationArtifact compilationArtifact = slang.compile(fromFile(resource), path);
        trigger(compilationArtifact, new HashMap<String, Value>(), null);
    }

    @Test
    public void testFlowWithRequiredInputUtf8() throws Exception {
        URI resource = getClass().getResource("/yaml/flow_with_required_input.sl").toURI();
        URI operations = getClass().getResource("/yaml/print.sl").toURI();
        String inputValue = readFileToString(new File(getClass().getResource("/inputs/utf8_input.txt").getFile()),
                StandardCharsets.UTF_8);
        Map<String, Value> inputs = new HashMap<>();
        inputs.put("input", ValueFactory.create(inputValue));

        SlangSource operationsSource = fromFile(operations);
        Set<SlangSource> path = Sets.newHashSet(operationsSource);
        CompilationArtifact compilationArtifact = slang.compile(fromFile(resource), path);

        Serializable stepsData = trigger(compilationArtifact, inputs, SYS_PROPS).getData();
        Map<String, Serializable> outputs = ((LanguageEventData) stepsData).getOutputs();
        Assert.assertEquals(outputs.get("returnResult"), outputs.get("printed_text"));
    }

    @Test
    public void testFlowWithSameInputNameAsStep() throws Exception {
        URI resource = getClass().getResource("/yaml/flow_with_same_input_name_as_step.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/string_equals.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/test_op.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(fromFile(operation1), fromFile(operation2));
        CompilationArtifact compilationArtifact = slang.compile(fromFile(resource), path);

        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("first", ValueFactory.create("value"));
        userInputs.put("second_string", ValueFactory.create("value"));

        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, EMPTY_SET).getSteps();

        List<String> actualSteps = getStepsOnly(stepsData);
        Assert.assertEquals(2, actualSteps.size());
        StepData firstStep = stepsData.get(FIRST_STEP_PATH);
        StepData secondStep = stepsData.get(SECOND_STEP_KEY);
        Assert.assertEquals("CheckBinding", firstStep.getName());
        Assert.assertEquals("StepOnSuccess", secondStep.getName());
    }

    @Test
    public void testFlowWithExtensionsTagIgnored() throws Exception {
        URI resource = getClass().getResource("/yaml/flow_with_extensions_tag.sl").toURI();
        URI operation = getClass().getResource("/yaml/noop.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(fromFile(operation));
        CompilationArtifact compilationArtifact = slang.compile(fromFile(resource), path);

        Map<String, Value> userInputs = new HashMap<>();
        triggerWithData(compilationArtifact, userInputs, EMPTY_SET);
    }

    @Test
    public void testFlowGetValue() throws Exception {
        URI resource = getClass().getResource("/yaml/check_get_value.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/get_value.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/check_equal_types.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(fromFile(operation1), fromFile(operation2));
        CompilationArtifact compilationArtifact = slang.compile(fromFile(resource), path);

        Map<String, Value> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, EMPTY_SET).getSteps();

        List<String> actualSteps = getStepsOnly(stepsData);
        Assert.assertEquals(2, actualSteps.size());
        StepData firstStep = stepsData.get(FIRST_STEP_PATH);
        StepData secondStep = stepsData.get(SECOND_STEP_KEY);
        Assert.assertEquals("get_value", firstStep.getName());
        Assert.assertEquals("SUCCESS", firstStep.getResult());
        Assert.assertEquals("test_equality", secondStep.getName());
        Assert.assertEquals("SUCCESS", secondStep.getResult());
    }
}
