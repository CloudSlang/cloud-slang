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
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.systemtests.StepData;
import io.cloudslang.lang.systemtests.SystemsTestsParent;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Bonczidai Levente
 * @since 11/6/2015
 */
public class ExpressionTest extends SystemsTestsParent {

    @Test
    public void testValuesInFlow() throws Exception {
        // compile
        URI resource = getClass().getResource("/yaml/formats/values_flow.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/formats/values_op.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/noop.sl").toURI();

        SlangSource dep1 = SlangSource.fromFile(operation1);
        SlangSource dep2 = SlangSource.fromFile(operation2);
        Set<SlangSource> path = Sets.newHashSet(dep1, dep2);
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        // trigger
        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("input_no_expression", "input_no_expression_value");
        userInputs.put("input_overridable", "i_should_not_be_assigned");
        Map<String, Serializable> systemProperties = new HashMap<>();
        systemProperties.put("user.sys.props.host", "localhost");

        Map<String, StepData> steps = triggerWithData(compilationArtifact, userInputs, systemProperties).getTasks();

        // verify
        StepData flowData = steps.get(EXEC_START_PATH);
        StepData taskData = steps.get(FIRST_STEP_PATH);
        StepData oneLinerTaskData = steps.get(SECOND_STEP_KEY);

        verifyFlowInputs(flowData);
        verifyFlowOutputs(flowData);
        verifyTaskInputs(taskData);
        verifyTaskPublishValues(taskData);
        verifyOneLinerInputs(oneLinerTaskData);
        verifyExecutableResult(flowData);
    }

    @Test
    public void testValuesInOperation() throws Exception {
        // compile
        URI resource = getClass().getResource("/yaml/formats/values_op.sl").toURI();

        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), new HashSet<SlangSource>());

        // trigger
        Map<String, Serializable> userInputs = new HashMap<>();
        Map<String, Serializable> systemProperties = new HashMap<>();

        Map<String, StepData> steps = triggerWithData(compilationArtifact, userInputs, systemProperties).getTasks();

        // verify
        StepData operationData = steps.get(EXEC_START_PATH);

        verifyExecutableResult(operationData);
    }

    private void verifyFlowInputs(StepData flowData) {
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

        // evaluated via Python
        expectedInputs.put("input_python_null", null);
        expectedInputs.put("input_python_list",  Lists.newArrayList(1, 2, 3));
        HashMap<String, Serializable> expectedInputPythonMap = new HashMap<>();
        expectedInputPythonMap.put("key1", "value1");
        expectedInputPythonMap.put("key2", "value2");
        expectedInputPythonMap.put("key3", "value3");
        expectedInputs.put("input_python_map", expectedInputPythonMap);
        HashMap<String, Serializable> expectedInputPythonMapQuotes = new HashMap<>();
        expectedInputPythonMapQuotes.put("value", 2);
        expectedInputs.put("input_python_map_quotes", expectedInputPythonMapQuotes);
        expectedInputs.put("b", "b");
        expectedInputs.put("b_copy", "b");
        expectedInputs.put("input_concat_1", "ab");
        expectedInputs.put("input_concat_2_one_liner", "prefix_ab_suffix");
        expectedInputs.put("input_concat_2_folded", "prefix_ab_suffix");

        Assert.assertTrue("Flow inputs not bound correctly", includeAllPairs(flowData.getInputs(), expectedInputs));
    }

    private void verifyFlowOutputs(StepData flowData) {
        Map<String, Serializable> expectedOutputs = new HashMap<>();

        expectedOutputs.put("output_no_expression", "output_no_expression_value");
        expectedOutputs.put("output_int", 22);
        expectedOutputs.put("output_str", "output_str_value");
        expectedOutputs.put("output_expression", "output_str_value_suffix");

        Assert.assertEquals("Flow outputs not bound correctly", expectedOutputs, flowData.getOutputs());
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
        HashMap<String, Serializable> expectedInputPythonMapQuotes = new HashMap<>();
        expectedInputPythonMapQuotes.put("value", 2);
        expectedTaskArguments.put("input_python_map_quotes", expectedInputPythonMapQuotes);
        expectedTaskArguments.put("b", "b");
        expectedTaskArguments.put("b_copy", "b");
        expectedTaskArguments.put("input_concat_1", "ab");
        expectedTaskArguments.put("input_concat_2_one_liner", "prefix_ab_suffix");
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

    private void verifyOneLinerInputs(StepData taskData) {
        Map<String, Serializable> expectedTaskArguments = new HashMap<>();

        expectedTaskArguments.put("input_no_expression", "input_no_expression_value");
        expectedTaskArguments.put("input_int", 22);
        expectedTaskArguments.put("input_expression", "input_no_expression_value_suffix");

        Assert.assertTrue("One liner task arguments not bound correctly", includeAllPairs(taskData.getInputs(), expectedTaskArguments));
    }

    private void verifyExecutableResult(StepData stepData) {
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, stepData.getResult());
    }

    private boolean includeAllPairs(Map<String, Serializable> map1, Map<String, Serializable> map2) {
        Map<String, Serializable> accumulator = new HashMap<>(map1);
        accumulator.putAll(map2);
        return accumulator.equals(map1);
    }

}
