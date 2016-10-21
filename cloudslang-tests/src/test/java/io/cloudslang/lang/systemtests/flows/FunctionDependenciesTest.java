/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.systemtests.flows;

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.systemtests.RuntimeInformation;
import io.cloudslang.lang.systemtests.StepData;
import io.cloudslang.lang.systemtests.ValueSyntaxParent;

import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Bonczidai Levente
 * @since 11/6/2015
 */
public class FunctionDependenciesTest extends ValueSyntaxParent {

    @SuppressWarnings("unchecked")
    private static final Set<SystemProperty> EMPTY_SET = Collections.EMPTY_SET;

    @Test
    public void testSystemPropertyDependencies() throws Exception {
        URL resource = getClass().getResource("/yaml/functions/system_property_dependencies_flow.sl");
        URI operation = getClass().getResource("/yaml/functions/system_property_dependencies_op.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()), path);

        assertEquals(
                "system property dependencies not as expected",
                prepareSystemPropertiesForDependencyTest(),
                compilationArtifact.getSystemProperties()
        );
    }

    @Test
    public void testFunctionsBasic() throws Exception {
        URL resource = getClass().getResource("/yaml/functions/functions_test_flow.sl");
        URI operation = getClass().getResource("/yaml/functions/functions_test_op.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()), path);

        Map<String, Value> userInputs = prepareUserInputs();
        Set<SystemProperty> systemProperties = prepareSystemProperties();

        // trigger ExecutionPlan
        RuntimeInformation runtimeInformation = triggerWithData(compilationArtifact, userInputs, systemProperties);

        Map<String, StepData> executionData = runtimeInformation.getSteps();

        StepData flowData = executionData.get(EXEC_START_PATH);
        StepData stepData = executionData.get(FIRST_STEP_PATH);
        Assert.assertNotNull("flow data is null", flowData);
        Assert.assertNotNull("step data is null", stepData);

        verifyFlowInputs(flowData);
        verifyStepArguments(stepData);
        verifyStepPublishValues(stepData);
        verifyFlowOutputs(flowData);

        // verify 'get' function worked in result expressions
        assertEquals("Function evaluation problem in result expression",
                "FUNCTIONS_KEY_EXISTS", flowData.getResult());
    }

    @Test
    public void testGetFunctionDefaultResult() throws Exception {
        URL resource = getClass().getResource("/yaml/functions/get_function_test_flow_default_result.sl");
        URI operation = getClass().getResource("/yaml/functions/get_function_test_default_result.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()), path);

        // trigger ExecutionPlan
        Map<String, Value> userInputs = new HashMap<>();
        RuntimeInformation runtimeInformation = triggerWithData(compilationArtifact, userInputs, EMPTY_SET);
        Map<String, StepData> executionData = runtimeInformation.getSteps();

        StepData flowData = executionData.get(EXEC_START_PATH);

        // verify 'get' function worked in result expressions
        assertEquals("Get function problem in result expression",
                "GET_FUNCTION_DEFAULT_VALUE", flowData.getResult());
    }

    private void verifyStepArguments(StepData stepData) {
        // verify `get`, `get_sp()`, `locals().get()` and mixed mode works
        Map<String, Serializable> expectedArguments = new LinkedHashMap<>();
        expectedArguments.put("exist", "exist_value");
        expectedArguments.put("input_3", null);
        expectedArguments.put("input_4", "default_str");
        expectedArguments.put("input_5", "localhost");
        expectedArguments.put("input_6", "localhost");
        expectedArguments.put("input_7", "localhost");
        expectedArguments.put("input_8", "exist_value");
        expectedArguments.put("input_9", "localhost");
        expectedArguments.put("input_10", "localhost");
        expectedArguments.put("input_11", "default_str");
        expectedArguments.put("value_propagate_input", "flowInput_stepArg_");
        expectedArguments.put("input_12", "hyphen_value");
        expectedArguments.put("input_13", "hyphen_value");
        expectedArguments.put("input_14", "localhost");
        expectedArguments.put("input_15", "localhost");
        expectedArguments.put("input_16", null);
        expectedArguments.put("input_17", "default_str");
        Map<String, Serializable> actualArguments = stepData.getInputs();
        assertEquals("step arguments not as expected", expectedArguments, actualArguments);
    }

    private void verifyFlowInputs(StepData flowData) {
        // verify `get`, `get_sp()`, `locals().get()` and mixed mode works
        Map<String, Serializable> expectedFlowInputs = new LinkedHashMap<>();
        expectedFlowInputs.put("input1", "value1");
        expectedFlowInputs.put("input1_safe", "input1_default");
        expectedFlowInputs.put("input2", "22");
        expectedFlowInputs.put("input2_safe", "22");
        expectedFlowInputs.put("input_locals_found", "22");
        expectedFlowInputs.put("input_locals_not_found", "input_locals_not_found_default");
        expectedFlowInputs.put("exist", "exist_value");
        expectedFlowInputs.put("input_3", null);
        expectedFlowInputs.put("input_4", "default_str");
        expectedFlowInputs.put("input_5", "localhost");
        expectedFlowInputs.put("input_6", "localhost");
        expectedFlowInputs.put("input_7", "localhost");
        expectedFlowInputs.put("input_8", "exist_value");
        expectedFlowInputs.put("input_9", "localhost");
        expectedFlowInputs.put("input_10", "localhost");
        expectedFlowInputs.put("input_11", "default_str");
        expectedFlowInputs.put("value_propagate_input", "flowInput_");
        expectedFlowInputs.put("input_12", "hyphen_value");
        expectedFlowInputs.put("input_13", "hyphen_value");
        expectedFlowInputs.put("input_14", "localhost");
        expectedFlowInputs.put("input_15", "localhost");
        expectedFlowInputs.put("input_15", "localhost");
        expectedFlowInputs.put("input_16", null);
        expectedFlowInputs.put("input_17", "default_str");
        Map<String, Serializable> actualFlowInputs = flowData.getInputs();
        assertEquals("flow input values not as expected", expectedFlowInputs, actualFlowInputs);
    }

    private void verifyFlowOutputs(StepData flowData) {
        Map<String, Serializable> expectedFlowOutputs = new LinkedHashMap<>();
        expectedFlowOutputs.put("value_propagate", "flowInput_stepArg_opInput_opOutput_stepPublish_flowOutput_");
        Map<String, Serializable> actualFlowOutputs = flowData.getOutputs();
        assertEquals("flow output values not as expected", expectedFlowOutputs, actualFlowOutputs);
    }

    private void verifyStepPublishValues(StepData stepData) {
        // verify `get`, `get_sp()` and mixed mode works
        Map<String, Serializable> expectedPublishValues = new LinkedHashMap<>();
        expectedPublishValues.put("output1_safe", "CloudSlang");
        expectedPublishValues.put("output2_safe", "output2_default");
        expectedPublishValues.put("output_same_name", "output_same_name_default");
        expectedPublishValues.put("output_1", null);
        expectedPublishValues.put("output_2", "default_str");
        expectedPublishValues.put("output_3", "localhost");
        expectedPublishValues.put("output_4", "localhost");
        expectedPublishValues.put("output_5", "localhost");
        expectedPublishValues.put("output_6", "exist_value");
        expectedPublishValues.put("output_7", "localhost");
        expectedPublishValues.put("output_8", "localhost");
        expectedPublishValues.put("output_9", "default_str");
        expectedPublishValues.put("output_10", "hyphen_value");
        expectedPublishValues.put("output_11", "hyphen_value");
        expectedPublishValues.put("output_12", "localhost");
        expectedPublishValues.put("output_13", "localhost");
        expectedPublishValues.put("output_14", null);
        expectedPublishValues.put("output_15", "default_str");
        expectedPublishValues.put("value_propagate", "flowInput_stepArg_opInput_opOutput_stepPublish_");
        Map<String, Serializable> actualPublishValues = stepData.getOutputs();
        assertEquals("operation publish values not as expected", expectedPublishValues, actualPublishValues);
    }

    private Set<SystemProperty> prepareSystemProperties() {
        return Sets.newHashSet(
                new SystemProperty("a.b", "c.host", "localhost"),
                new SystemProperty("cloudslang", "lang.key", "language"),
                new SystemProperty("", "a.b.c.null_value", (String) null),
                new SystemProperty("propagate", "flow.input", "flowInput_"),
                new SystemProperty("propagate", "step.argument", "stepArg_"),
                new SystemProperty("propagate", "op.input", "opInput_"),
                new SystemProperty("propagate", "op.output", "opOutput_"),
                new SystemProperty("propagate", "step.publish", "stepPublish_"),
                new SystemProperty("propagate", "flow.output", "flowOutput_"),
                new SystemProperty("chars-b", "c-hyphen", "hyphen_value")
        );
    }

    private Map<String, Value> prepareUserInputs() {
        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("exist", ValueFactory.create("exist_value"));
        return userInputs;
    }

    private Set<String> prepareSystemPropertiesForDependencyTest() {
        return Sets.newHashSet(
                "flow.input.prop1",
                "flow.input.prop2",
                "flow.input.prop3",
                "flow.input.prop4",
                "flow.input.prop5",
                "flow.output.prop1",
                "step.input.prop1",
                "step.input.prop2",
                "step.input.prop3",
                "step.input.prop4",
                "step.publish.prop1",
                "step.publish.prop2",
                "step.publish.prop3",
                "step.publish.prop4",
                "op.input.prop1",
                "op.input.prop2",
                "op.input.prop3",
                "op.input.prop4",
                "op.input.prop5",
                "op.output.prop1",
                "op.result.prop1",
                "parallel_loop.publish.prop1",
                "parallel_loop.publish.prop2",
                "for.input.prop1",
                "for.input.prop2",
                "for.publish.prop1",
                "for.publish.prop2"
        );
    }

}
