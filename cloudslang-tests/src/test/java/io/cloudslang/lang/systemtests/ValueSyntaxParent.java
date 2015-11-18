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
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import org.junit.Assert;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bonczidai Levente
 * @since 11/13/2015
 */
public abstract class ValueSyntaxParent extends SystemsTestsParent {

    protected Map<String, StepData> prepareAndRun(CompilationArtifact compilationArtifact) {
        // trigger
        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("input_no_expression", "input_no_expression_value");
        userInputs.put("input_not_overridable", "i_should_not_be_assigned");
        Map<String, Serializable> systemProperties = new HashMap<>();
        systemProperties.put("user.sys.props.host", "localhost");

        return triggerWithData(compilationArtifact, userInputs, systemProperties).getTasks();
    }

    protected void verifyExecutableInputs(StepData flowData) {
        Map<String, Serializable> expectedInputs = new HashMap<>();

        // properties
        expectedInputs.put("input_no_expression", "input_no_expression_value");
        expectedInputs.put("input_no_expression_not_required", null);
        expectedInputs.put("input_system_property", "localhost");
        expectedInputs.put("input_not_overridable", 25);

        // loaded by Yaml
        expectedInputs.put("input_int", 22);
        expectedInputs.put("input_str_no_quotes", "Hi");
        expectedInputs.put("input_str_single", "Hi");
        expectedInputs.put("input_str_double", "Hi");
        expectedInputs.put("input_yaml_list", Lists.newArrayList(1, 2, 3));
        expectedInputs.put("input_properties_yaml_map_folded", "medium");
        HashMap<String, Serializable> expectedInputMap = new HashMap<>();
        expectedInputMap.put("key1", "value1");
        expectedInputMap.put("key2", "value2");
        expectedInputMap.put("key3", "value3");
        expectedInputs.put("input_yaml_map", expectedInputMap);

        // evaluated via Python
        expectedInputs.put("input_python_null", null);
        expectedInputs.put("input_python_list",  Lists.newArrayList(1, 2, 3));
        expectedInputs.put("input_python_map", expectedInputMap);
        expectedInputs.put("b", "b");
        expectedInputs.put("b_copy", "b");
        expectedInputs.put("input_concat_1", "ab");
        expectedInputs.put("input_concat_2_one_liner", "prefix_ab_suffix");
        expectedInputs.put("input_concat_2_folded", "prefix_ab_suffix");
        expectedInputs.put(
                "input_expression_characters",
                "docker run -d -e AUTHORIZED_KEYS=${base64 -w0 ./auth} -p 8888:22 --name test1 -v /data:"
        );

        Assert.assertTrue("Executable inputs not bound correctly", includeAllPairs(flowData.getInputs(), expectedInputs));
    }

    protected void verifyExecutableOutputs(StepData flowData) {
        Map<String, Serializable> expectedOutputs = new HashMap<>();

        expectedOutputs.put("output_no_expression", "output_no_expression_value");
        expectedOutputs.put("output_int", 22);
        expectedOutputs.put("output_str", "output_str_value");
        expectedOutputs.put("output_expression", "output_str_value_suffix");

        Assert.assertEquals("Executable outputs not bound correctly", expectedOutputs, flowData.getOutputs());
    }

    protected void verifySuccessResult(StepData stepData) {
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, stepData.getResult());
    }

    protected boolean includeAllPairs(Map<String, Serializable> map1, Map<String, Serializable> map2) {
        Map<String, Serializable> accumulator = new HashMap<>(map1);
        accumulator.putAll(map2);
        return accumulator.equals(map1);
    }

}
