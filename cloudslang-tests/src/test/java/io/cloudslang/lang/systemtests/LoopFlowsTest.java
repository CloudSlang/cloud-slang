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
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ch.lambdaj.Lambda.filter;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;

public class LoopFlowsTest extends SystemsTestsParent{

    @Test
    public void testFlowWithLoops() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/simple_loop.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/print.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, null);
        StepData firstTask = stepsData.get(FIRST_STEP_PATH);
        StepData secondTask = stepsData.get(SECOND_STEP_KEY);
        StepData thirdTask = stepsData.get(THIRD_STEP_KEY);
        Assert.assertTrue(firstTask.getInputs().containsValue(1));
        Assert.assertTrue(secondTask.getInputs().containsValue(2));
        Assert.assertTrue(thirdTask.getInputs().containsValue(3));
    }

    @Test
    public void testFlowWithLoopsWithCustomNavigation() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/loop_with_custom_navigation.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/print.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, null);
        StepData thirdTask = stepsData.get(THIRD_STEP_KEY);
        Assert.assertEquals("print_other_values", thirdTask.getName());
    }

    @Test
    public void testFlowWithLoopsWithDefaultBreak() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/loop_with_default_break.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/operation_that_fails_when_value_is_2.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, null);
        List<String> actualTasks = getTasksOnly(stepsData);
        Assert.assertEquals(2, actualTasks.size());
    }

    @Test
    public void testFlowWithLoopsWithEmptyBreak() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/loop_with_empty_break.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/operation_that_fails_when_value_is_2.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, null);
        List<String> actualTasks = getTasksOnly(stepsData);
        Assert.assertEquals(3, actualTasks.size());
    }

    @Test
    public void testFlowWithLoopsWithBreak() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/loop_with_break.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/operation_that_goes_to_custom_when_value_is_2.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/loops/print.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1), SlangSource.fromFile(operation2));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, null);
        List<String> actualTasks = getTasksOnly(stepsData);
        Assert.assertEquals(3, actualTasks.size());
        StepData thirdTask = stepsData.get(THIRD_STEP_KEY);
        Assert.assertEquals("print_other_values", thirdTask.getName());
    }

    @Test
    public void testFlowWithMapLoops() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/simple_loop_with_map.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/print.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, null);
        verifyPersonMap(stepsData);
    }

    @Test
    public void testFlowWithHashMap() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/simple_loop_with_hashmap.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/print.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> personMap = new HashMap<>();
        personMap.put("john", 1);
        personMap.put("jane", 2);
        personMap.put("peter", "three");
        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("person_map", (Serializable) personMap);
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, null);
        verifyPersonMap(stepsData);
    }

    @Test
    public void testFlowWithMapLoopsWithCustomNavigation() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/loop_with_custom_navigation_with_map.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/print.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, null);
        StepData thirdTask = stepsData.get(FOURTH_STEP_KEY);
        Assert.assertEquals("print_other_values", thirdTask.getName());
    }

    @Test
    public void testFlowWithMapLoopsWithDefaultBreak() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/loop_with_default_break_with_map.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/operation_that_fails_when_value_is_2.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, null);
        List<String> actualTasks = getTasksOnly(stepsData);
        Assert.assertEquals(2, actualTasks.size());
    }

    @Test
    public void testFlowWithMapLoopsWithEmptyBreak() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/loop_with_empty_break_with_map.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/operation_that_fails_when_value_is_2.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, null);
        List<String> actualTasks = getTasksOnly(stepsData);
        Assert.assertEquals(3, actualTasks.size());
    }

    @Test
    public void testFlowWithMapLoopsWithBreak() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/loop_with_break_with_map.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/operation_that_goes_to_custom_when_value_is_2.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/loops/print.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1), SlangSource.fromFile(operation2));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, null);
        List<String> actualTasks = getTasksOnly(stepsData);
        Assert.assertEquals(3, actualTasks.size());
        StepData thirdTask = stepsData.get(THIRD_STEP_KEY);
        Assert.assertEquals("print_other_values", thirdTask.getName());
    }

    private List<String> getTasksOnly(Map<String, StepData> stepsData) {
        return filter(not(endsWith("0")), stepsData.keySet());
    }

    private void verifyPersonMap(Map<String, StepData> stepsData) {
        StepData firstTask = stepsData.get(FIRST_STEP_PATH);
        StepData secondTask = stepsData.get(SECOND_STEP_KEY);
        StepData thirdTask = stepsData.get(THIRD_STEP_KEY);
        Assert.assertTrue(firstTask.getInputs().containsValue("john"));
        Assert.assertTrue(firstTask.getInputs().containsValue(1));
        Assert.assertTrue(secondTask.getInputs().containsValue("jane"));
        Assert.assertTrue(secondTask.getInputs().containsValue(2));
        Assert.assertTrue(thirdTask.getInputs().containsValue("peter"));
        Assert.assertTrue(thirdTask.getInputs().containsValue("three"));
    }
}
