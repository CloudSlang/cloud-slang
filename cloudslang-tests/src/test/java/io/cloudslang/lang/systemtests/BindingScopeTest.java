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
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.entities.utils.ExpressionUtils;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author Bonczidai Levente
 * @since 3/18/2016
 */
public class BindingScopeTest extends SystemsTestsParent {

    @Test
    public void testStepPublishValues() throws Exception {
        URL resource = getClass().getResource("/yaml/binding_scope_flow.sl");
        URI operation = getClass().getResource("/yaml/binding_scope_op.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()), path);

        Map<String, Value> userInputs = Collections.emptyMap();
        Set<SystemProperty> systemProperties = Collections.emptySet();

        // trigger ExecutionPlan
        RuntimeInformation runtimeInformation = triggerWithData(compilationArtifact, userInputs, systemProperties);

        Map<String, StepData> executionData = runtimeInformation.getSteps();

        StepData stepData = executionData.get(FIRST_STEP_PATH);
        Assert.assertNotNull("step data is null", stepData);

        verifyStepPublishValues(stepData);
    }

    @Test
    public void testInputMissing() throws Exception {
        URL resource = getClass().getResource("/yaml/check_weather_missing_input.sl");
        CompilationArtifact compilationArtifact = slang
                .compile(SlangSource.fromFile(resource.toURI()), new HashSet<SlangSource>());

        Map<String, Value> userInputs = Collections.emptyMap();
        Set<SystemProperty> systemProperties = Collections.emptySet();

        exception.expect(RuntimeException.class);

        exception.expectMessage(new BaseMatcher<String>() {
            public void describeTo(Description description) {
            }

            public boolean matches(Object o) {
                String message = o.toString();
                return message.contains("Error running: 'check_weather_missing_input'") &&
                        message.contains("Error binding input: 'input_get_missing_input'") &&
                        message.contains("Error is: Error in running script expression: 'missing_input'") &&
                        message.contains("Exception is: name 'missing_input' is not defined");
            }
        });
        triggerWithData(compilationArtifact, userInputs, systemProperties);
    }

    @Test
    public void testInputWithDefaultValue() throws Exception {
        URL resource = getClass().getResource("/yaml/check_weather_required_input_with_default.sl");
        CompilationArtifact compilationArtifact = slang
                .compile(SlangSource.fromFile(resource.toURI()), new HashSet<SlangSource>());

        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("city", ValueFactory.create(""));
        userInputs.put("input_with_default_value", ValueFactory.create(""));
        Set<SystemProperty> systemProperties = Collections.emptySet();

        Map<String, StepData> steps = triggerWithData(compilationArtifact, userInputs, systemProperties).getSteps();

        assertEquals("default_value", steps.get(EXEC_START_PATH).getInputs().get("input_with_default_value"));
        assertEquals("", steps.get(EXEC_START_PATH).getInputs().get("city"));
    }

    @Test
    public void testInputNotRequiredNull() throws Exception {
        URL resource = getClass().getResource("/yaml/check_weather_required_input_with_default.sl");
        CompilationArtifact compilationArtifact = slang
                .compile(SlangSource.fromFile(resource.toURI()), new HashSet<SlangSource>());

        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("city", ValueFactory.create(null));
        userInputs.put("input_with_default_value", ValueFactory.create(""));
        Set<SystemProperty> systemProperties = Collections.emptySet();

        Map<String, StepData> steps = triggerWithData(compilationArtifact, userInputs, systemProperties).getSteps();

        assertEquals("default_value", steps.get(EXEC_START_PATH).getInputs().get("input_with_default_value"));
        assertEquals(null, steps.get(EXEC_START_PATH).getInputs().get("city"));
    }

    @Test
    public void testInputRequiredWithEmptyValue() throws Exception {
        URL resource = getClass().getResource("/yaml/check_weather_input_required.sl");
        final CompilationArtifact compilationArtifact = slang
                .compile(SlangSource.fromFile(resource.toURI()), new HashSet<SlangSource>());

        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("city", ValueFactory.create(""));
        Set<SystemProperty> systemProperties = Collections.emptySet();

        exception.expect(RuntimeException.class);

        exception.expectMessage(new BaseMatcher<String>() {
            public void describeTo(Description description) {
            }

            public boolean matches(Object o) {
                String message = o.toString();
                return message.contains("Error running: 'check_weather_input_required'.") &&
                        message.contains("Input with name: 'city' is Required, but value is empty");
            }
        });
        triggerWithData(compilationArtifact, userInputs, systemProperties);
    }

    @Test
    public void testInputRequiredWithNullValue() throws Exception {
        URL resource = getClass().getResource("/yaml/check_weather_input_required.sl");
        final CompilationArtifact compilationArtifact = slang
                .compile(SlangSource.fromFile(resource.toURI()), new HashSet<SlangSource>());

        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("city", ValueFactory.create(null));
        Set<SystemProperty> systemProperties = Collections.emptySet();

        exception.expect(RuntimeException.class);

        exception.expectMessage(new BaseMatcher<String>() {
            public void describeTo(Description description) {
            }

            public boolean matches(Object o) {
                String message = o.toString();
                return message.contains("Error running: 'check_weather_input_required'.") &&
                        message.contains("Input with name: 'city' is Required, but value is empty");
            }
        });
        triggerWithData(compilationArtifact, userInputs, systemProperties);
    }

    @Test
    public void testInputOptionalWithEmptyValue() throws Exception {
        URL resource = getClass().getResource("/yaml/check_weather_optional_input_with_default.sl");
        CompilationArtifact compilationArtifact = slang
                .compile(SlangSource.fromFile(resource.toURI()), new HashSet<SlangSource>());

        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("input_with_default_value", ValueFactory.create(""));
        Set<SystemProperty> systemProperties = Collections.emptySet();

        Map<String, StepData> steps = triggerWithData(compilationArtifact, userInputs, systemProperties).getSteps();

        assertEquals("default_value", steps.get(EXEC_START_PATH).getInputs().get("input_with_default_value"));
    }

    @Test
    public void testInputOptionalWithNullValue() throws Exception {
        URL resource = getClass().getResource("/yaml/check_weather_optional_input_with_default.sl");
        CompilationArtifact compilationArtifact = slang
                .compile(SlangSource.fromFile(resource.toURI()), new HashSet<SlangSource>());

        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("input_with_default_value", ValueFactory.create(null));
        Set<SystemProperty> systemProperties = Collections.emptySet();

        Map<String, StepData> steps = triggerWithData(compilationArtifact, userInputs, systemProperties).getSteps();

        assertEquals("default_value", steps.get(EXEC_START_PATH).getInputs().get("input_with_default_value"));
    }

    @Test
    public void testStepInputRequiredWithEmptyValue() throws Exception {
        URL resource = getClass().getResource("/yaml/check_weather_flow.sl");
        URI operation1 = getClass().getResource("/yaml/check_weather_required_input_with_default.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));

        CompilationArtifact compilationArtifact = slang
                .compile(SlangSource.fromFile(resource.toURI()), path);

        Map<String, Value> userInputs = new HashMap<>();
        Set<SystemProperty> systemProperties = Collections.emptySet();

        Map<String, StepData> steps = triggerWithData(compilationArtifact, userInputs, systemProperties).getSteps();

        assertEquals("weather thing default_value", steps.get(FIRST_STEP_PATH).getOutputs().get("kuku"));
    }

    @Test
    public void testSensitiveInputAndOutputsWithAndWithoutDefault() throws Exception {
        URL resource = getClass().getResource("/yaml/check_weather_flow_sensitive.sl");
        URI operation1 = getClass().getResource("/yaml/check_weather_required_input_sensitive.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));

        CompilationArtifact compilationArtifact = slang
                .compile(SlangSource.fromFile(resource.toURI()), path);

        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("flow_input_sensitive", ValueFactory.create("sensitiveValue2", true));
        Set<SystemProperty> systemProperties = Collections.emptySet();

        Map<String, StepData> steps = triggerWithData(compilationArtifact, userInputs, systemProperties).getSteps();

        assertEquals("********", steps.get(EXEC_START_PATH).getInputs().get("flow_input_sensitive"));
        assertEquals("********", steps.get(EXEC_START_PATH).getInputs().get("flow_input_0"));
        assertEquals("defaultValue", steps.get(EXEC_START_PATH).getInputs().get("flow_input_1"));
        assertEquals("********", steps.get(EXEC_START_PATH).getOutputs().get("flow_output_0"));
        assertEquals("weather thing default_value sensitiveValue",
                steps.get(EXEC_START_PATH).getOutputs().get("flow_output_1"));
        assertEquals("sensitiveValue", steps.get(FIRST_STEP_PATH).getInputs().get("input_with_sensitive_no_default"));
    }

    @Test
    public void testSensitiveStepOutputs() throws Exception {
        URL resource = getClass().getResource("/yaml/check_weather_flow_sensitive_outputs.sl");
        URI operation1 = getClass().getResource("/yaml/check_weather_required_input_sensitive.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));

        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()), path);

        Map<String, Value> userInputs = new HashMap<>();
        Set<SystemProperty> systemProperties = Collections.emptySet();

        Map<String, StepData> steps = triggerWithData(compilationArtifact, userInputs, systemProperties).getSteps();

        assertEquals("********", steps.get(EXEC_START_PATH).getOutputs().get("flow_output_0"));
        assertEquals("********", steps.get(EXEC_START_PATH).getOutputs().get("flow_output_1"));
        assertEquals("sensitive", steps.get(FIRST_STEP_PATH).getInputs().get("input_with_sensitive_no_default"));
    }

    @Test
    public void testStepInputOptionalWithEmptyValue() throws Exception {
        URL resource = getClass().getResource("/yaml/check_weather_flow_optional.sl");
        URI operation1 = getClass().getResource("/yaml/check_weather_optional_input_with_default.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));

        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()), path);

        Map<String, Value> userInputs = new HashMap<>();
        Set<SystemProperty> systemProperties = Collections.emptySet();

        Map<String, StepData> steps = triggerWithData(compilationArtifact, userInputs, systemProperties).getSteps();

        assertEquals("weather thing default_value", steps.get(FIRST_STEP_PATH).getOutputs().get("kuku"));
    }

    private void verifyStepPublishValues(StepData stepData) {
        Map<String, Serializable> expectedPublishValues = new LinkedHashMap<>();
        expectedPublishValues.put("step1_publish_1", "op_output_1_value op_input_1_step step_arg_1_value");
        expectedPublishValues.put("step1_publish_2_conflict", "op_output_2_value");
        Map<String, Serializable> actualPublishValues = stepData.getOutputs();
        assertEquals("step publish values not as expected", expectedPublishValues, actualPublishValues);
    }

    @Test
    public void testFlowContextInStepPublishSection() throws Exception {
        URL resource = getClass().getResource("/yaml/binding_scope_flow_context_in_step_publish.sl");
        URI operation = getClass().getResource("/yaml/binding_scope_op.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation));

        // pre-validation - step expression uses flow var name
        SlangSource flowSource = SlangSource.fromFile(resource.toURI());
        Executable flowExecutable = slangCompiler.preCompile(flowSource);
        String flowVarName = "flow_var";
        assertEquals(
                "Input name should be: " + flowVarName,
                flowVarName,
                flowExecutable.getInputs().get(0).getName()
        );
        @SuppressWarnings("unchecked")
        List<Output> stepPublishValues = (List<Output>) ((Flow) flowExecutable)
                .getWorkflow()
                .getSteps()
                .getFirst()
                .getPostStepActionData()
                .get(SlangTextualKeys.PUBLISH_KEY);
        assertEquals(
                "Step expression should contain: " + flowVarName,
                flowVarName,
                StringUtils.trim(ExpressionUtils.extractExpression(stepPublishValues.get(0).getValue().get()))
        );

        final CompilationArtifact compilationArtifact = slang.compile(flowSource, path);

        final Map<String, Value> userInputs = Collections.emptyMap();
        final Set<SystemProperty> systemProperties = Collections.emptySet();

        exception.expect(RuntimeException.class);
        exception.expectMessage("flow_var");
        exception.expectMessage("not defined");

        // trigger ExecutionPlan
        triggerWithData(compilationArtifact, userInputs, systemProperties);
    }

}
