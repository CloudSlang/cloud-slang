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
package io.cloudslang.lang.systemtests;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import org.junit.Assert;
import org.python.core.PyDictionary;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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
        userInputs.put("input_not_overridable", ValueFactory.create("i_should_not_be_assigned"));
        userInputs.put("enable_option_for_action", ValueFactory.create("enable_option_for_action_value"));
        return userInputs;
    }

    protected void verifyExecutableInputsDefault(StepData flowData) {
        Map<String, Value> expectedInputs = new HashMap<>();

        // snake-case to camel-case
        expectedInputs.put("enable_option_for_action", ValueFactory.create(null));
        expectedInputs.put("enableOptionForAction", ValueFactory.create("default_value"));

        Assert.assertTrue("Executable inputs not bound correctly", includeAllPairs(flowData.getInputs(), expectedInputs));
    }

    protected void verifyExecutableInputs(StepData flowData) {
        Map<String, Value> expectedInputs = new HashMap<>();

        // snake-case to camel-case
        expectedInputs.put("enable_option_for_action", ValueFactory.create("enable_option_for_action_value"));
        expectedInputs.put("enableOptionForAction", ValueFactory.create("enable_option_for_action_value"));

        // properties
        expectedInputs.put("input_no_expression", ValueFactory.create("input_no_expression_value"));
        expectedInputs.put("input_no_expression_not_required", ValueFactory.create(null));
        expectedInputs.put("input_system_property", ValueFactory.create("localhost"));
        expectedInputs.put("input_not_overridable", ValueFactory.create(25));

        // loaded by Yaml
        expectedInputs.put("input_int", ValueFactory.create(22));
        expectedInputs.put("input_str_no_quotes", ValueFactory.create("Hi"));
        expectedInputs.put("input_str_single", ValueFactory.create("Hi"));
        expectedInputs.put("input_str_double", ValueFactory.create("Hi"));
        expectedInputs.put("input_yaml_list", ValueFactory.create(Lists.newArrayList(1, 2, 3)));
        expectedInputs.put("input_properties_yaml_map_folded", ValueFactory.create("medium"));
        HashMap<String, Serializable> expectedInputMap = new LinkedHashMap<>();
        expectedInputMap.put("key1", "value1");
        expectedInputMap.put("key2", "value2");
        expectedInputMap.put("key3", "value3");
        expectedInputs.put("input_yaml_map", ValueFactory.create(expectedInputMap));

        // evaluated via Python
        expectedInputs.put("input_python_null", ValueFactory.create(null));
        expectedInputs.put("input_python_list",  ValueFactory.create(Lists.newArrayList(1, 2, 3)));
        PyDictionary expectedInputPyDictionary = new PyDictionary();
        expectedInputPyDictionary.putAll(expectedInputMap);
        expectedInputs.put("input_python_map", ValueFactory.create(expectedInputPyDictionary));
        expectedInputs.put("b", ValueFactory.create("b"));
        expectedInputs.put("b_copy", ValueFactory.create("b"));
        expectedInputs.put("input_concat_1", ValueFactory.create("ab"));
        expectedInputs.put("input_concat_2_folded", ValueFactory.create("prefix_ab_suffix"));
        expectedInputs.put(
                "input_expression_characters",
                ValueFactory.create("docker run -d -e AUTHORIZED_KEYS=${base64 -w0 ./auth} -p 8888:22 --name test1 -v /data:"
        ));
        expectedInputs.put("step_argument_null", ValueFactory.create("step_argument_null_value"));

        Assert.assertTrue("Executable inputs not bound correctly", includeAllPairs(flowData.getInputs(), expectedInputs));
    }

    protected void verifyExecutableOutputs(StepData flowData) {
        Map<String, Value> expectedOutputs = new HashMap<>();

        expectedOutputs.put("output_no_expression", ValueFactory.create("output_no_expression_value"));
        expectedOutputs.put("output_int", ValueFactory.create(22));
        expectedOutputs.put("output_str", ValueFactory.create("output_str_value"));
        expectedOutputs.put("output_expression", ValueFactory.create("output_str_value_suffix"));
        expectedOutputs.put("output_step_argument_null", ValueFactory.create("step_argument_null_value"));

        Assert.assertEquals("Executable outputs not bound correctly", expectedOutputs, flowData.getOutputs());
    }

    protected void verifySuccessResult(StepData stepData) {
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, stepData.getResult());
    }

    protected boolean includeAllPairs(Map<String, Value> map1, Map<String, Value> map2) {
        Map<String, Value> accumulator = new HashMap<>(map1);
        accumulator.putAll(map2);
        return accumulator.equals(map1);
    }

}
