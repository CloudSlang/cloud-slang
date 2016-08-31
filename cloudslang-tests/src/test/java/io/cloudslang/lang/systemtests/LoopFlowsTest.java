/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
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
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LoopFlowsTest extends SystemsTestsParent{
    
    @SuppressWarnings("unchecked")
    private static final Set<SystemProperty> EMPTY_SET = Collections.EMPTY_SET;

    @Test
    public void testFlowWithLoops() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/simple_loop.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/print.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Value> userInputs = new HashMap<>();
        Set<SystemProperty> systemProperties = new HashSet<>();
        systemProperties.add(
                new SystemProperty("loop", "for.prop1", "for_value")
        );

        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, systemProperties).getSteps();

        StepData firstStep = stepsData.get(FIRST_STEP_PATH);
        StepData secondStep = stepsData.get(SECOND_STEP_KEY);
        StepData thirdStep = stepsData.get(THIRD_STEP_KEY);

        Map<String, Serializable> expectedInputs = new HashMap<>();
        expectedInputs.put("text", "1");
        expectedInputs.put("sp_arg", "for_value");
        Assert.assertEquals(expectedInputs, firstStep.getInputs());
        expectedInputs.put("text", "2");
        Assert.assertEquals(expectedInputs, secondStep.getInputs());
        expectedInputs.put("text", "3");
        Assert.assertEquals(expectedInputs, thirdStep.getInputs());
    }

    @Test
    public void testFlowWithLoopsWithCustomNavigation() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/loop_with_custom_navigation.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/print.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Value> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, EMPTY_SET).getSteps();
        StepData thirdStep = stepsData.get(THIRD_STEP_KEY);
        Assert.assertEquals("print_other_values", thirdStep.getName());
    }

    @Test
    public void testFlowWithLoopsFromPropertyFile() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/loop_from_property_with_custom_navigation.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/print.sl").toURI();
        URI propertiesURI = getClass().getResource("/yaml/loops/loop_property.prop.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);
        Assert.assertEquals(1, compilationArtifact.getSystemProperties().size());
        Assert.assertEquals("loops.list", compilationArtifact.getSystemProperties().iterator().next());

        Set<SystemProperty> systemProperties = loadSystemProperties(SlangSource.fromFile(propertiesURI));
        Map<String, Value> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, systemProperties).getSteps();
        StepData secondStep = stepsData.get(SECOND_STEP_KEY);

        Assert.assertEquals("print_values", secondStep.getName());
        Assert.assertEquals("SUCCESS", secondStep.getResult());
        StepData thirdStep = stepsData.get(THIRD_STEP_KEY);
        Assert.assertEquals("print_other_values", thirdStep.getName());
    }

    @Test
    public void testFlowWithLoopsWithDefaultBreak() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/loop_with_default_break.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/operation_that_fails_when_value_is_2.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Value> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, EMPTY_SET).getSteps();
        List<String> actualSteps = getStepsOnly(stepsData);
        Assert.assertEquals(2, actualSteps.size());
    }

    @Test
    public void testFlowWithLoopsWithEmptyBreak() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/loop_with_empty_break.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/operation_that_fails_when_value_is_2.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Value> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, EMPTY_SET).getSteps();
        List<String> actualSteps = getStepsOnly(stepsData);
        Assert.assertEquals(3, actualSteps.size());
    }

    @Test
    public void testFlowWithLoopsWithBreak() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/loop_with_break.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/operation_that_goes_to_custom_when_value_is_2.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/loops/print.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1), SlangSource.fromFile(operation2));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Value> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, EMPTY_SET).getSteps();
        List<String> actualSteps = getStepsOnly(stepsData);
        Assert.assertEquals(3, actualSteps.size());
        StepData thirdStep = stepsData.get(THIRD_STEP_KEY);
        Assert.assertEquals("print_other_values", thirdStep.getName());
    }

    @Ignore("Remove when support for maps in loops is added")
    @Test
    public void testFlowWithMapLoops() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/simple_loop_with_map.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/print.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Value> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, EMPTY_SET).getSteps();
        verifyPersonMap(stepsData);
    }

    @Ignore("Remove when support for maps in loops is added")
    @Test
    public void testFlowWithHashMap() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/simple_loop_with_hashmap.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/print.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Value> personMap = new HashMap<>();
        personMap.put("john", ValueFactory.create(1));
        personMap.put("jane", ValueFactory.create(2));
        personMap.put("peter", ValueFactory.create("three"));
        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("person_map", ValueFactory.create((Serializable)personMap));
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, EMPTY_SET).getSteps();
        verifyPersonMap(stepsData);
    }

    @Ignore("Remove when support for maps in loops is added")
    @Test
    public void testFlowWithMapLoopsWithCustomNavigation() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/loop_with_custom_navigation_with_map.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/print.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Value> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, EMPTY_SET).getSteps();
        StepData fourthStep = stepsData.get(FOURTH_STEP_KEY);
        Assert.assertEquals("print_other_values", fourthStep.getName());
    }

    @Ignore("Remove when support for maps in loops is added")
    @Test
    public void testFlowWithMapLoopsWithDefaultBreak() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/loop_with_default_break_with_map.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/operation_that_fails_when_value_is_2.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Value> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, EMPTY_SET).getSteps();
        List<String> actualSteps = getStepsOnly(stepsData);
        Assert.assertEquals(1, actualSteps.size());
    }

    @Ignore("Remove when support for maps in loops is added")
    @Test
    public void testFlowWithMapLoopsWithEmptyBreak() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/loop_with_empty_break_with_map.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/operation_that_fails_when_value_is_2.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Value> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, EMPTY_SET).getSteps();
        List<String> actualSteps = getStepsOnly(stepsData);
        Assert.assertEquals(3, actualSteps.size());
    }

    @Ignore("Remove when support for maps in loops is added")
    @Test
    public void testFlowWithMapLoopsWithBreak() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/loop_with_break_with_map.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/operation_that_goes_to_custom_when_value_is_2.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/loops/print.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1), SlangSource.fromFile(operation2));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Value> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, EMPTY_SET).getSteps();
        List<String> actualSteps = getStepsOnly(stepsData);
        Assert.assertEquals(2, actualSteps.size());
        StepData secondStep = stepsData.get(SECOND_STEP_KEY);
        Assert.assertEquals("print_other_values", secondStep.getName());
    }

    private void verifyPersonMap(Map<String, StepData> stepsData) {
        StepData firstStep = stepsData.get(FIRST_STEP_PATH);
        StepData secondStep = stepsData.get(SECOND_STEP_KEY);
        StepData thirdStep = stepsData.get(THIRD_STEP_KEY);
        @SuppressWarnings("unchecked")
        Set<Map<String, Serializable>> actualContextSet = Sets.newHashSet(
                firstStep.getInputs(),
                secondStep.getInputs(),
                thirdStep.getInputs()
        );

        Map<String, Serializable> context1 = new HashMap<>();
        context1.put("text", "john");
        context1.put("text2", 1);
        Map<String, Serializable> context2 = new HashMap<>();
        context2.put("text", "jane");
        context2.put("text2", 2);
        Map<String, Serializable> context3 = new HashMap<>();
        context3.put("text", "peter");
        context3.put("text2", "three");
        @SuppressWarnings("unchecked")
        Set<Map<String, Serializable>> expectedContextSet = Sets.newHashSet(
                context1,
                context2,
                context3
        );

        Assert.assertEquals("loop step inputs not as expected", expectedContextSet, actualContextSet);
    }

}
