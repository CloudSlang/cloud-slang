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
import io.cloudslang.lang.entities.SystemProperty;
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
public class ParallelLoopFlowsTest extends SystemsTestsParent {

    private static final String BRANCH_MESSAGE = "branch ";

    @Test
    public void testFlowWithParallelLoop() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/parallel_loop/simple_parallel_loop.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/parallel_loop/print_branch.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));

        RuntimeInformation runtimeInformation = triggerWithData(SlangSource.fromFile(resource), path);

        List<StepData> branchesData = extractParallelLoopData(runtimeInformation);
        Assert.assertEquals("incorrect number of branches", 3, branchesData.size());
    }

    @Test
    public void testFlowWithParallelLoopPublish() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/parallel_loop/parallel_loop_publish.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/parallel_loop/print_branch.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));

        RuntimeInformation runtimeInformation = triggerWithData(
                SlangSource.fromFile(resource),
                path,
                getSystemProperties()
        );

        List<StepData> branchesData = extractParallelLoopData(runtimeInformation);
        Assert.assertEquals("incorrect number of branches", 3, branchesData.size());

        List<String> expectedNameOutputs = verifyBranchPublishValues(branchesData);

        verifyPublishValues(runtimeInformation, expectedNameOutputs);
    }

    @Test
    public void testFlowWithParallelLoopNavigate() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/parallel_loop/parallel_loop_navigate.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/parallel_loop/print_branch.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/loops/parallel_loop/print_list.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1), SlangSource.fromFile(operation2));

        RuntimeInformation runtimeInformation = triggerWithData(SlangSource.fromFile(resource), path);

        List<StepData> branchesData = extractParallelLoopData(runtimeInformation);
        Assert.assertEquals("incorrect number of branches", 3, branchesData.size());

        verifyNavigation(runtimeInformation);
    }

    @Test
    public void testFlowWithParallelLoopPublishNavigate() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/parallel_loop/parallel_loop_publish_navigate.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/parallel_loop/print_branch.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/loops/parallel_loop/print_list.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1), SlangSource.fromFile(operation2));

        RuntimeInformation runtimeInformation = triggerWithData(
                SlangSource.fromFile(resource),
                path,
                getSystemProperties()
        );

        List<StepData> branchesData = extractParallelLoopData(runtimeInformation);
        Assert.assertEquals("incorrect number of branches", 3, branchesData.size());

        List<String> expectedNameOutputs = verifyBranchPublishValues(branchesData);

        verifyPublishValues(runtimeInformation, expectedNameOutputs);

        verifyNavigation(runtimeInformation);
    }

    @Test
    public void testFlowContextInPublishSectionNotReachable() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/parallel_loop/parallel_loop_publish_flow_context.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/parallel_loop/print_branch.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));

        exception.expect(RuntimeException.class);
        exception.expectMessage("flow_var");
        exception.expectMessage("not defined");

        triggerWithData(SlangSource.fromFile(resource), path);
    }

    private Set<SystemProperty> getSystemProperties() {
        return Sets.newHashSet(
                new SystemProperty("loop", "parallel.prop1", "publish_value")
        );
    }

    private RuntimeInformation triggerWithData(
            SlangSource resource,
            Set<SlangSource> path,
            Set<SystemProperty> systemProperties) {
        CompilationArtifact compilationArtifact = slang.compile(resource, path);

        Map<String, Serializable> userInputs = new HashMap<>();
        return triggerWithData(compilationArtifact, userInputs, systemProperties);
    }

    private RuntimeInformation triggerWithData(SlangSource resource, Set<SlangSource> path) {
        return triggerWithData(resource, path, new HashSet<SystemProperty>());
    }

    private List<StepData> extractParallelLoopData(RuntimeInformation runtimeInformation) {
        Map<String, List<StepData>> branchesByPath = runtimeInformation.getBranchesByPath();
        Assert.assertTrue("parallel loop data not found", branchesByPath.containsKey(BRANCH_FIRST_STEP_PATH));
        List<StepData> stepDataList = new ArrayList<>();
        for (List<StepData> list : branchesByPath.values()){
            stepDataList.add(list.get(0));
        }

        return stepDataList;
    }

    private <T> boolean containsSameElementsWithoutOrdering(List<T> firstList, List<T> secondList) {
        return firstList.containsAll(secondList) && secondList.containsAll(firstList);
    }

    private List<String> verifyBranchPublishValues(List<StepData> branchesData) {
        // publish
        List<String> actualNameOutputsOfBranches = Lists.newArrayList();
        List<Integer> actualNumberOutputsOfBranches = Lists.newArrayList();
        for (StepData branchData : branchesData) {
            Map<String, Serializable> outputs = branchData.getOutputs();
            Assert.assertTrue(outputs.containsKey("name"));
            Assert.assertTrue(outputs.containsKey("int_output"));
            actualNameOutputsOfBranches.add((String) outputs.get("name"));
            actualNumberOutputsOfBranches.add((Integer) outputs.get("int_output"));
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

    private void verifyPublishValues(RuntimeInformation runtimeInformation, List<String> expectedNameOutputs) {
        // publish
        Map<String, StepData> parallelLoopSteps = runtimeInformation.getParallelSteps();
        StepData parallelLoopStep = parallelLoopSteps.get(FIRST_STEP_PATH);

        Map<String, Serializable> publishValues = parallelLoopStep.getOutputs();
        Assert.assertTrue("publish name not found in parallel loop outputs", publishValues.containsKey("name_list"));
        @SuppressWarnings("unchecked")
        List<String> actualPublishNameList = (List<String>) publishValues.get("name_list");

        Assert.assertTrue(
                "publish value does not have the expected value",
                containsSameElementsWithoutOrdering(Lists.newArrayList(actualPublishNameList), expectedNameOutputs)
        );

        Assert.assertEquals(
                "Publish value not bound correctly from system property",
                "publish_value",
                publishValues.get("from_sp")
        );
    }

    private void verifyNavigation(RuntimeInformation runtimeInformation) {
        Map<String, StepData> stepsData = runtimeInformation.getSteps();
        StepData stepAfterParallelLoop = stepsData.get(SECOND_STEP_KEY);
        Assert.assertEquals("navigation not as expected", "print_list", stepAfterParallelLoop.getName());
    }

}
