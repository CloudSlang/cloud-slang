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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.net.URI;
import java.util.*;

/**
 * Date: 3/25/2015
 *
 * @author Bonczidai Levente
 */
public class AsyncLoopFlowsTest extends SystemsTestsParent {

    private static final String BRANCH_MESSAGE = "branch ";

    @Test
    public void testFlowWithAsyncLoop() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/async_loop/simple_async_loop.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/async_loop/print_branch.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));

        RuntimeInformation runtimeInformation = triggerWithData(SlangSource.fromFile(resource), path);

        List<StepData> branchesData = extractAsyncLoopData(runtimeInformation);
        Assert.assertEquals("incorrect number of branches", 3, branchesData.size());
    }

    @Test
    public void testFlowWithAsyncLoopAggregate() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/async_loop/async_loop_aggregate.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/async_loop/print_branch.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));

        RuntimeInformation runtimeInformation = triggerWithData(SlangSource.fromFile(resource), path);

        List<StepData> branchesData = extractAsyncLoopData(runtimeInformation);
        Assert.assertEquals("incorrect number of branches", 3, branchesData.size());

        List<String> expectedNameOutputs = verifyPublishValues(branchesData);

        verifyAggregateValues(runtimeInformation, expectedNameOutputs);
    }

    @Test
    public void testFlowWithAsyncLoopNavigate() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/async_loop/async_loop_navigate.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/async_loop/print_branch.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/loops/async_loop/print_list.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1), SlangSource.fromFile(operation2));

        RuntimeInformation runtimeInformation = triggerWithData(SlangSource.fromFile(resource), path);

        List<StepData> branchesData = extractAsyncLoopData(runtimeInformation);
        Assert.assertEquals("incorrect number of branches", 3, branchesData.size());

        verifyNavigation(runtimeInformation);
    }

    @Test
    public void testFlowWithAsyncLoopAggregateNavigate() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/async_loop/async_loop_aggregate_navigate.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/async_loop/print_branch.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/loops/async_loop/print_list.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1), SlangSource.fromFile(operation2));

        RuntimeInformation runtimeInformation = triggerWithData(SlangSource.fromFile(resource), path);

        List<StepData> branchesData = extractAsyncLoopData(runtimeInformation);
        Assert.assertEquals("incorrect number of branches", 3, branchesData.size());

        List<String> expectedNameOutputs = verifyPublishValues(branchesData);

        verifyAggregateValues(runtimeInformation, expectedNameOutputs);

        verifyNavigation(runtimeInformation);
    }

    private RuntimeInformation triggerWithData(SlangSource resource, Set<SlangSource> path) {
        CompilationArtifact compilationArtifact = slang.compile(resource, path);

        Map<String, Serializable> userInputs = new HashMap<>();
        return triggerWithData(compilationArtifact, userInputs, null);
    }

    private List<StepData> extractAsyncLoopData(RuntimeInformation runtimeInformation) {
        Map<String, List<StepData>> branchesByPath = runtimeInformation.getBranchesByPath();
        Assert.assertTrue("async loop data not found", branchesByPath.containsKey(BRANCH_FIRST_STEP_PATH));
        List<StepData> stepDataList = new ArrayList<>();
        for (List<StepData> list : branchesByPath.values()){
            stepDataList.add(list.get(0));
        }

        return stepDataList;
    }

    private <T> boolean containsSameElementsWithoutOrdering(List<T> firstList, List<T> secondList) {
        return firstList.containsAll(secondList) && secondList.containsAll(firstList);
    }

    private List<String> verifyPublishValues(List<StepData> branchesData) {
        // publish
        List<String> actualNameOutputsOfBranches = Lists.newArrayList();
        List<Integer> actualNumberOutputsOfBranches = Lists.newArrayList();
        for (StepData branchData : branchesData) {
            Map<String, Serializable> outputs = branchData.getOutputs();
            Assert.assertTrue(outputs.containsKey("name"));
            Assert.assertTrue(outputs.containsKey("number"));
            actualNameOutputsOfBranches.add((String) outputs.get("name"));
            actualNumberOutputsOfBranches.add((Integer) outputs.get("number"));
        }

        List<String> expectedNameOutputs = Lists.newArrayList();
        List<Integer> expectedNumberOutputs = Lists.newArrayList();
        for (int i = 1; i < 4; i++) {
            expectedNameOutputs.add(BRANCH_MESSAGE + i);
            expectedNumberOutputs.add(i);
        }

        Assert.assertTrue(
                "branch publish values not as expected",
                containsSameElementsWithoutOrdering(expectedNameOutputs, actualNameOutputsOfBranches)
        );
        Assert.assertTrue(
                "branch publish values not as expected",
                containsSameElementsWithoutOrdering(expectedNumberOutputs, actualNumberOutputsOfBranches)
        );
        return expectedNameOutputs;
    }

    private void verifyAggregateValues(RuntimeInformation runtimeInformation, List<String> expectedNameOutputs) {
        // aggregate
        Map<String, StepData> asyncTasks = runtimeInformation.getAsyncTasks();
        StepData asyncTask = asyncTasks.get(FIRST_STEP_PATH);

        Map<String, Serializable> aggregateValues = asyncTask.getOutputs();
        Assert.assertTrue("aggregate name not found in async loop outputs", aggregateValues.containsKey("name_list"));
        @SuppressWarnings("unchecked")
        List<String> actualAggregateNameList = (List<String>) aggregateValues.get("name_list");

        Assert.assertTrue(
                "aggregate output does not have the expected value",
                containsSameElementsWithoutOrdering(Lists.newArrayList(actualAggregateNameList), expectedNameOutputs)
        );
    }

    private void verifyNavigation(RuntimeInformation runtimeInformation) {
        Map<String, StepData> tasksData = runtimeInformation.getTasks();
        StepData taskAfterAsyncLoop = tasksData.get(SECOND_STEP_KEY);
        Assert.assertEquals("navigation not as expected", "print_list", taskAfterAsyncLoop.getName());
    }

}
