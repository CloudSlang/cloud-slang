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
import org.junit.Ignore;
import org.junit.Test;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Date: 3/25/2015
 *
 * @author Bonczidai Levente
 */
public class AsyncLoopFlowsTest extends SystemsTestsParent {

    @Test
    @Ignore // TODO - async loop
    public void testFlowWithAsyncLoop() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/async_loop/simple_async_loop.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/async_loop/print_branch.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, null);
        StepData firstTask = stepsData.get(FIRST_STEP_PATH);
        // TODO - async loop - update test
//        StepData secondTask = stepsData.get(SECOND_STEP_KEY);
//        StepData thirdTask = stepsData.get(THIRD_STEP_KEY);
//        Assert.assertTrue(firstTask.getInputs().containsValue(1));
//        Assert.assertTrue(secondTask.getInputs().containsValue(2));
//        Assert.assertTrue(thirdTask.getInputs().containsValue(3));
    }


    @Test
    @Ignore
    public void testFlowWithAsyncLoopAggregate() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/async_loop/async_loop_aggregate.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/async_loop/print_branch.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, null);
        StepData firstTask = stepsData.get(FIRST_STEP_PATH);
        // TODO - async loop - update test
//        StepData secondTask = stepsData.get(SECOND_STEP_KEY);
//        StepData thirdTask = stepsData.get(THIRD_STEP_KEY);
//        Assert.assertTrue(firstTask.getInputs().containsValue(1));
//        Assert.assertTrue(secondTask.getInputs().containsValue(2));
//        Assert.assertTrue(thirdTask.getInputs().containsValue(3));
    }

    @Test
    @Ignore
    public void testFlowWithAsyncLoopAggregateNavigate() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/async_loop/async_loop_aggregate_navigate.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/async_loop/print_branch.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/loops/async_loop/print_list.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1), SlangSource.fromFile(operation2));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, null);
        StepData firstTask = stepsData.get(FIRST_STEP_PATH);
        // TODO - async loop - update test
//        StepData secondTask = stepsData.get(SECOND_STEP_KEY);
//        StepData thirdTask = stepsData.get(THIRD_STEP_KEY);
//        Assert.assertTrue(firstTask.getInputs().containsValue(1));
//        Assert.assertTrue(secondTask.getInputs().containsValue(2));
//        Assert.assertTrue(thirdTask.getInputs().containsValue(3));
    }

}
