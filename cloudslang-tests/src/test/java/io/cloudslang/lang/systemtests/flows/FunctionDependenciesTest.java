/**
 * ****************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 * <p/>
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * *****************************************************************************
 */
package io.cloudslang.lang.systemtests.flows;

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.systemtests.RuntimeInformation;
import io.cloudslang.lang.systemtests.StepData;
import io.cloudslang.lang.systemtests.ValueSyntaxParent;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.*;

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

        Assert.assertEquals(
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

        Map<String, Serializable> userInputs = prepareUserInputs();
        Set<SystemProperty> systemProperties = prepareSystemProperties();

        // trigger ExecutionPlan
        RuntimeInformation runtimeInformation = triggerWithData(compilationArtifact, userInputs, systemProperties);

        Map<String, StepData> executionData = runtimeInformation.getTasks();

        StepData flowData = executionData.get(EXEC_START_PATH);
        StepData taskData = executionData.get(FIRST_STEP_PATH);
        Assert.assertNotNull("flow data is null", flowData);
        Assert.assertNotNull("task data is null", taskData);

        verifyFlowInputs(flowData);
        verifyTaskArguments(taskData);
        verifyTaskPublishValues(taskData);
        verifyFlowOutputs(flowData);

        // verify 'get' function worked in result expressions
        Assert.assertEquals("Function evaluation problem in result expression", "FUNCTIONS_KEY_EXISTS", flowData.getResult());
    }

    @Test
    public void testGetFunctionDefaultResult() throws Exception {
        URL resource = getClass().getResource("/yaml/functions/get_function_test_flow_default_result.sl");
        URI operation = getClass().getResource("/yaml/functions/get_function_test_default_result.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()), path);

        // trigger ExecutionPlan
        Map<String, Serializable> userInputs = new HashMap<>();
        RuntimeInformation runtimeInformation = triggerWithData(compilationArtifact, userInputs, EMPTY_SET);
        Map<String, StepData> executionData = runtimeInformation.getTasks();

        StepData flowData = executionData.get(EXEC_START_PATH);

        // verify 'get' function worked in result expressions
        Assert.assertEquals("Get function problem in result expression", "GET_FUNCTION_DEFAULT_VALUE", flowData.getResult());
    }

    private void verifyTaskArguments(StepData taskData) {
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
        expectedArguments.put("value_propagate", "flowInput_taskArg_");
        Map<String, Serializable> actualArguments = taskData.getInputs();
        Assert.assertEquals("task arguments not as expected", expectedArguments, actualArguments);
    }

    private void verifyFlowInputs(StepData flowData) {
        // verify `get`, `get_sp()`, `locals().get()` and mixed mode works
        Map<String, Serializable> expectedFlowInputs = new LinkedHashMap<>();
        expectedFlowInputs.put("input1", null);
        expectedFlowInputs.put("input1_safe", "input1_default");
        expectedFlowInputs.put("input2", 22);
        expectedFlowInputs.put("input2_safe", 22);
        expectedFlowInputs.put("input_locals_found", 22);
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
        expectedFlowInputs.put("value_propagate", "flowInput_");
        Map<String, Serializable> actualFlowInputs = flowData.getInputs();
        Assert.assertEquals("flow input values not as expected", expectedFlowInputs, actualFlowInputs);
    }

    private void verifyFlowOutputs(StepData flowData) {
        Map<String, Serializable> expectedFlowOutputs = new LinkedHashMap<>();
        expectedFlowOutputs.put("value_propagate", "flowInput_taskArg_opInput_opOutput_taskPublish_flowOutput_");
        Map<String, Serializable> actualFlowOutputs = flowData.getOutputs();
        Assert.assertEquals("flow output values not as expected", expectedFlowOutputs, actualFlowOutputs);
    }

    private void verifyTaskPublishValues(StepData taskData) {
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
        expectedPublishValues.put("value_propagate", "flowInput_taskArg_opInput_opOutput_taskPublish_");
        Map<String, Serializable> actualPublishValues = taskData.getOutputs();
        Assert.assertEquals("operation publish values not as expected", expectedPublishValues, actualPublishValues);
    }

    private Set<SystemProperty> prepareSystemProperties() {
        return Sets.newHashSet(
                SystemProperty.createSystemProperty("a.b", "c.host", "localhost"),
                SystemProperty.createSystemProperty("cloudslang", "lang.key", "language"),
                SystemProperty.createSystemProperty("", "a.b.c.null_value", null),
                SystemProperty.createSystemProperty("propagate", "flow.input", "flowInput_"),
                SystemProperty.createSystemProperty("propagate", "task.argument", "taskArg_"),
                SystemProperty.createSystemProperty("propagate", "op.input", "opInput_"),
                SystemProperty.createSystemProperty("propagate", "op.output", "opOutput_"),
                SystemProperty.createSystemProperty("propagate", "task.publish", "taskPublish_"),
                SystemProperty.createSystemProperty("propagate", "flow.output", "flowOutput_")
        );
    }

    private Map<String, Serializable> prepareUserInputs() {
        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("exist", "exist_value");
        return userInputs;
    }

    private Set<String> prepareSystemPropertiesForDependencyTest() {
        return Sets.newHashSet(
                "flow.input.prop1",
                "flow.input.prop2",
                "flow.input.prop3",
                "flow.output.prop1",
                "task.input.prop1",
                "task.input.prop2",
                "task.publish.prop1",
                "task.publish.prop2",
                "op.input.prop1",
                "op.input.prop2",
                "op.input.prop3",
                "op.output.prop1",
                "op.result.prop1",
                "async.aggregate.prop1",
                "async.aggregate.prop2",
                "for.input.prop1",
                "for.input.prop2",
                "for.publish.prop1",
                "for.publish.prop2"
                );
    }
    
}
