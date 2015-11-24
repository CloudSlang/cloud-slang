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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.systemtests.StepData;
import io.cloudslang.lang.systemtests.ValueSyntaxParent;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Bonczidai Levente
 * @since 11/6/2015
 */
public class ValueSyntaxInFlowTest extends ValueSyntaxParent {

    @Test
    public void testValues() throws Exception {
        // compile
        URI resource = getClass().getResource("/yaml/formats/values_flow.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/formats/values_op.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/noop.sl").toURI();

        SlangSource dep1 = SlangSource.fromFile(operation1);
        SlangSource dep2 = SlangSource.fromFile(operation2);
        Set<SlangSource> path = Sets.newHashSet(dep1, dep2);
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        // trigger
        Map<String, StepData> steps = prepareAndRun(compilationArtifact);

        // verify
        StepData flowData = steps.get(EXEC_START_PATH);
        StepData taskData = steps.get(FIRST_STEP_PATH);

        verifyExecutableInputs(flowData);
        verifyExecutableOutputs(flowData);
        verifyTaskInputs(taskData);
        verifyTaskPublishValues(taskData);
        verifySuccessResult(flowData);
    }

    private void verifyTaskInputs(StepData taskData) {
        Map<String, Serializable> expectedTaskArguments = new HashMap<>();

        // properties
        expectedTaskArguments.put("input_no_expression", "input_no_expression_value");

        // loaded by Yaml
        expectedTaskArguments.put("input_int", 22);
        expectedTaskArguments.put("input_str_no_quotes", "Hi");
        expectedTaskArguments.put("input_str_single", "Hi");
        expectedTaskArguments.put("input_str_double", "Hi");
        expectedTaskArguments.put("input_yaml_list", Lists.newArrayList(1, 2, 3));
        HashMap<String, Serializable> expectedYamlMapFolded = new HashMap<>();
        expectedYamlMapFolded.put("key1", "medium");
        expectedYamlMapFolded.put("key2", false);
        expectedTaskArguments.put("input_yaml_map_folded", expectedYamlMapFolded);

        // evaluated via Python
        expectedTaskArguments.put("input_python_null", null);
        expectedTaskArguments.put("input_python_list", Lists.newArrayList(1, 2, 3));
        HashMap<String, Serializable> expectedInputPythonMap = new HashMap<>();
        expectedInputPythonMap.put("key1", "value1");
        expectedInputPythonMap.put("key2", "value2");
        expectedInputPythonMap.put("key3", "value3");
        expectedTaskArguments.put("input_python_map", expectedInputPythonMap);
        expectedTaskArguments.put("b", "b");
        expectedTaskArguments.put("b_copy", "b");
        expectedTaskArguments.put("input_concat_1", "ab");
        expectedTaskArguments.put("input_concat_2_folded", "prefix_ab_suffix");

        Assert.assertTrue("Task arguments not bound correctly", includeAllPairs(taskData.getInputs(), expectedTaskArguments));
    }

    private void verifyTaskPublishValues(StepData taskData) {
        Map<String, Serializable> expectedTaskPublishValues = new HashMap<>();

        expectedTaskPublishValues.put("output_no_expression", "output_no_expression_value");
        expectedTaskPublishValues.put("publish_int", 22);
        expectedTaskPublishValues.put("publish_str", "publish_str_value");
        expectedTaskPublishValues.put("publish_expression", "publish_str_value_suffix");

        Assert.assertEquals("Task publish values not bound correctly", expectedTaskPublishValues, taskData.getOutputs());
    }
}
