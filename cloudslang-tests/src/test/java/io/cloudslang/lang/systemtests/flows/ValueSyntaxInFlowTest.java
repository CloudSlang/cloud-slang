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
import io.cloudslang.lang.systemtests.StepData;
import io.cloudslang.lang.systemtests.ValueSyntaxParent;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
        StepData stepData = steps.get(FIRST_STEP_PATH);

        verifyExecutableInputs(flowData);
        verifyExecutableOutputs(flowData);
        verifyStepInputs(stepData);
        verifyStepPublishValues(stepData);
        verifySuccessResult(flowData);
    }

    @Test
    public void testValuesStepsWithModifiers() throws Exception {
        // compile
        URI resource = getClass().getResource("/yaml/formats/values_steps_modifiers.sl").toURI();
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
        StepData stepData = steps.get(FIRST_STEP_PATH);

        verifyExecutableInputs(flowData);
        verifyExecutableOutputs(flowData);
        verifyStepInputs(stepData);
        verifyStepPublishValues(stepData);
        verifySuccessResult(flowData);
    }

    private void verifyStepInputs(StepData stepData) {
        Map<String, Serializable> expectedStepArguments = new HashMap<>();

        // properties
        expectedStepArguments.put("input_no_expression", "input_no_expression_value");

        // loaded by Yaml
        expectedStepArguments.put("input_int", "22");
        expectedStepArguments.put("input_str_no_quotes", "Hi");
        expectedStepArguments.put("input_str_single", "Hi");
        expectedStepArguments.put("input_str_double", "Hi");
        expectedStepArguments.put("input_yaml_list", "[1, 2, 3]");
        expectedStepArguments.put("input_yaml_map_folded", "{key1: medium, key2: false}");

        // evaluated via Python
        expectedStepArguments.put("input_python_null", null);
        // uncomment when types will be supported
        // expectedStepArguments.put("input_python_list", Lists.newArrayList(1, 2, 3));
        // HashMap<String, Serializable> expectedInputPythonMap = new HashMap<>();
        // expectedInputPythonMap.put("key1", "value1");
        // expectedInputPythonMap.put("key2", "value2");
        // expectedInputPythonMap.put("key3", "value3");
        // expectedStepArguments.put("input_python_map", expectedInputPythonMap);
        expectedStepArguments.put("b", "b");
        expectedStepArguments.put("b_copy", "b");
        expectedStepArguments.put("input_concat_1", "ab");
        expectedStepArguments.put("input_concat_2_folded", "prefix_ab_suffix");
        expectedStepArguments.put("step_argument_null", null);
        expectedStepArguments.put("input_no_value_tag", "input_no_value_tag_value");

        assertEquals("Step arguments not bound correctly", expectedStepArguments, stepData.getInputs());
    }

    private void verifyStepPublishValues(StepData stepData) {
        Map<String, Serializable> expectedStepPublishValues = new LinkedHashMap<>();

        expectedStepPublishValues.put("output_no_expression", "output_no_expression_value");
        expectedStepPublishValues.put("publish_int", "22");
        expectedStepPublishValues.put("publish_str", "publish_str_value");
        expectedStepPublishValues.put("publish_expression", "publish_str_value_suffix");
        expectedStepPublishValues.put("output_step_argument_null", "step_argument_null_value");

        assertEquals("Step publish values not bound correctly", expectedStepPublishValues, stepData.getOutputs());
    }
}
