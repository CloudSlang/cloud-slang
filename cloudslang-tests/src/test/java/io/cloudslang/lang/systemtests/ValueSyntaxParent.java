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
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;

import static org.junit.Assert.assertEquals;

/**
 * @author Bonczidai Levente
 * @since 11/13/2015
 */
public abstract class ValueSyntaxParent extends SystemsTestsParent {

    protected Map<String, StepData> prepareAndRun(CompilationArtifact compilationArtifact) {
        // trigger
        return triggerWithData(compilationArtifact, getUserInputs(), getSystemProperties()).getSteps();
    }

    protected Map<String, StepData> prepareAndRunDefault(CompilationArtifact compilationArtifact) {
        Map<String, Value> userInputs = getUserInputs();
        userInputs.put("enable_option_for_action", null);

        return triggerWithData(compilationArtifact, userInputs, getSystemProperties()).getSteps();
    }

    private Set<SystemProperty> getSystemProperties() {
        return Sets.newHashSet(
                new SystemProperty("user.sys", "props.host", "localhost")
        );
    }

    private Map<String, Value> getUserInputs() {
        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("input_no_expression", ValueFactory.create("input_no_expression_value"));
        userInputs.put("input_private", ValueFactory.create("i_should_not_be_assigned"));
        userInputs.put("enable_option_for_action", ValueFactory.create("enable_option_for_action_value"));
        userInputs.put("input_no_default_sensitive", ValueFactory.create("input_no_default_sensitive_value", false));
        return userInputs;
    }

    protected void verifyExecutableInputsDefault(StepData flowData) {
        Map<String, Serializable> expectedInputs = new HashMap<>();

        // snake-case to camel-case
        expectedInputs.put("enable_option_for_action", null);
        expectedInputs.put("enableOptionForAction", "default_value");

        // properties
        expectedInputs.put("input_no_expression", "input_no_expression_value");
        expectedInputs.put("input_no_expression_not_required", null);
        expectedInputs.put("input_system_property", "localhost");
        expectedInputs.put("input_private", "25");

        // loaded by Yaml
        expectedInputs.put("input_int", "22");
        expectedInputs.put("input_str_no_quotes", "Hi");
        expectedInputs.put("input_str_single", "Hi");
        expectedInputs.put("input_str_double", "Hi");
        expectedInputs.put("input_yaml_list", "[1, 2, 3]");
        expectedInputs.put("input_properties_yaml_map_folded", "medium");
        expectedInputs.put("input_yaml_map", "{'key1': 'value1', 'key2': 'value2', 'key3': 'value3'}");

        // evaluated via Python
        expectedInputs.put("input_python_null", null);
        // uncomment when types will be supported
        // expectedInputs.put("input_python_list", Lists.newArrayList(1, 2 ,3));
        // HashMap<String, Serializable> expectedInputMap = new LinkedHashMap<>();
        // expectedInputMap.put("key1", "value1");
        // expectedInputMap.put("key2", "value2");
        // expectedInputMap.put("key3", "value3");
        // expectedInputs.put("input_python_map", expectedInputMap);
        expectedInputs.put("b", "b");
        expectedInputs.put("b_copy", "b");
        expectedInputs.put("input_concat_1", "ab");
        expectedInputs.put("input_concat_2_folded", "prefix_ab_suffix");
        expectedInputs.put(
                "input_expression_characters",
                "docker run -d -e AUTHORIZED_KEYS=${base64 -w0 ./auth} -p 8888:22 --name test1 -v /data:"
        );
        expectedInputs.put("step_argument_null", "step_argument_null_value");
        expectedInputs.put("output_no_expression_input", "output_no_expression_value");
        expectedInputs.put("authorized_keys_path", "./auth");
        expectedInputs.put("scp_host_port", "8888");

        assertEquals("Executable inputs not bound correctly", expectedInputs, flowData.getInputs());
    }

    protected void verifyExecutableInputs(StepData flowData) {
        Map<String, Serializable> expectedInputs = new HashMap<>();

        // snake-case to camel-case
        expectedInputs.put("enable_option_for_action", "enable_option_for_action_value");
        expectedInputs.put("enableOptionForAction", "enable_option_for_action_value");

        // properties
        expectedInputs.put("input_no_expression", "input_no_expression_value");
        expectedInputs.put("input_no_expression_not_required", null);
        expectedInputs.put("input_system_property", "localhost");
        expectedInputs.put("input_private", "25");

        // loaded by Yaml
        expectedInputs.put("input_int", "22");
        expectedInputs.put("input_str_no_quotes", "Hi");
        expectedInputs.put("input_str_single", "Hi");
        expectedInputs.put("input_str_double", "Hi");
        expectedInputs.put("input_yaml_list", "[1, 2, 3]");
        expectedInputs.put("input_properties_yaml_map_folded", "medium");
        expectedInputs.put("input_yaml_map", "{'key1': 'value1', 'key2': 'value2', 'key3': 'value3'}");

        // evaluated via Python
        expectedInputs.put("input_python_null", null);
        // uncomment when types will be supported
        // expectedInputs.put("input_python_list", Lists.newArrayList(1, 2 ,3));
        // HashMap<String, Serializable> expectedInputMap = new LinkedHashMap<>();
        // expectedInputMap.put("key1", "value1");
        // expectedInputMap.put("key2", "value2");
        // expectedInputMap.put("key3", "value3");
        // expectedInputs.put("input_python_map", expectedInputMap);
        expectedInputs.put("b", "b");
        expectedInputs.put("b_copy", "b");
        expectedInputs.put("input_concat_1", "ab");
        expectedInputs.put("input_concat_2_folded", "prefix_ab_suffix");
        expectedInputs.put(
                "input_expression_characters",
                "docker run -d -e AUTHORIZED_KEYS=${base64 -w0 ./auth} -p 8888:22 --name test1 -v /data:"
        );
        expectedInputs.put("step_argument_null", "step_argument_null_value");
        expectedInputs.put("output_no_expression_input", "output_no_expression_value");
        expectedInputs.put("authorized_keys_path", "./auth");
        expectedInputs.put("scp_host_port", "8888");

        assertEquals("Executable inputs not bound correctly", expectedInputs, flowData.getInputs());
    }

    protected void verifyExecutableInputsStepInputModifiers(StepData flowData) {
        Map<String, Serializable> expectedInputs = new HashMap<>();

        // snake-case to camel-case
        expectedInputs.put("enable_option_for_action", "enable_option_for_action_value");
        expectedInputs.put("enableOptionForAction", "enable_option_for_action_value");

        // properties
        expectedInputs.put("input_no_expression", "input_no_expression_value");
        expectedInputs.put("input_no_expression_not_required", null);
        expectedInputs.put("input_system_property", "localhost");
        expectedInputs.put("input_private", "25");

        // loaded by Yaml
        expectedInputs.put("input_int", "22");
        expectedInputs.put("input_str_no_quotes", "Hi");
        expectedInputs.put("input_str_single", "Hi");
        expectedInputs.put("input_str_double", "Hi");
        expectedInputs.put("input_yaml_list", "[1, 2, 3]");
        expectedInputs.put("input_properties_yaml_map_folded", "medium");
        expectedInputs.put("input_yaml_map", "{'key1': 'value1', 'key2': 'value2', 'key3': 'value3'}");

        // evaluated via Python
        expectedInputs.put("input_python_null", null);
        // uncomment when types will be supported
        // expectedInputs.put("input_python_list", Lists.newArrayList(1, 2 ,3));
        // HashMap<String, Serializable> expectedInputMap = new LinkedHashMap<>();
        // expectedInputMap.put("key1", "value1");
        // expectedInputMap.put("key2", "value2");
        // expectedInputMap.put("key3", "value3");
        // expectedInputs.put("input_python_map", expectedInputMap);
        expectedInputs.put("b", "b");
        expectedInputs.put("b_copy", "b");
        expectedInputs.put("input_concat_1", "ab");
        expectedInputs.put("input_concat_2_folded", "prefix_ab_suffix");
        expectedInputs.put(
                "input_expression_characters",
                "docker run -d -e AUTHORIZED_KEYS=${base64 -w0 ./auth} -p 8888:22 --name test1 -v /data:"
        );
        expectedInputs.put("step_argument_null", "step_argument_null_value");
        expectedInputs.put("output_no_expression_input", "output_no_expression_value");
        expectedInputs.put("authorized_keys_path", "./auth");
        expectedInputs.put("scp_host_port", "8888");
        expectedInputs.put("input_no_value_tag", "input_no_value_tag_value");

        assertEquals("Executable inputs not bound correctly", expectedInputs, flowData.getInputs());
    }

    protected void verifyExecutableOutputs(StepData flowData) {
        Map<String, Serializable> expectedOutputs = new HashMap<>();

        expectedOutputs.put("output_no_expression", "output_no_expression_value");
        expectedOutputs.put("output_int", "22");
        expectedOutputs.put("output_str", "output_str_value");
        expectedOutputs.put("output_expression", "output_str_value_suffix");
        expectedOutputs.put("output_step_argument_null", "step_argument_null_value");

        Assert.assertEquals("Executable outputs not bound correctly", expectedOutputs, flowData.getOutputs());
    }

    protected void verifySuccessResult(StepData stepData) {
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, stepData.getResult());
    }

}
