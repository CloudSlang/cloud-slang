/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
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
import io.cloudslang.lang.entities.bindings.values.Value;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import static com.google.common.collect.Sets.newHashSet;
import static io.cloudslang.lang.compiler.SlangSource.fromFile;

/**
 * Date: 3/25/2015
 *
 * @author Bonczidai Levente
 */
public class ParallelLoopFlowsTest extends SystemsTestsParent {

    private static final String BRANCH_MESSAGE = "branch ";
    private static final String BRANCH_RESULTS_LIST_PUBLISH_VALUE = "branch_results_list";
    private static final String CUSTOM_RESULT = "CUSTOM";
    private static final String SUCCESS_RESULT = "SUCCESS";
    private static final String BRANCH_RESULT_OUTPUT_VALUE = "should_be_overridden";

    @Test
    public void testFlowWithParallelLoop() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/parallel_loop/simple_parallel_loop.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/parallel_loop/print_branch.sl").toURI();
        Set<SlangSource> path = newHashSet(fromFile(operation1));

        RuntimeInformation runtimeInformation = triggerWithData(fromFile(resource), path);

        List<StepData> branchesData = extractParallelLoopData(runtimeInformation);
        Assert.assertEquals("incorrect number of branches", 3, branchesData.size());
    }

    @Test
    public void testFlowWithSensitiveInputParallelLoop() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/parallel_loop/sensitive_input_parallel_loop.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/parallel_loop/print_branch.sl").toURI();
        Set<SlangSource> path = newHashSet(fromFile(operation1));

        RuntimeInformation runtimeInformation =
                triggerWithData(fromFile(resource), path, getSystemProperties());

        List<StepData> branchesData = extractParallelLoopData(runtimeInformation);
        Assert.assertEquals("incorrect number of branches", 3, branchesData.size());

        List<String> expectedNameOutputs = verifyBranchPublishValues(branchesData);

        verifyPublishValues(runtimeInformation, expectedNameOutputs);
    }

    @Test
    public void testFlowWithSensitiveOutputParallelLoop() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/parallel_loop/sensitive_output_parallel_loop.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/parallel_loop/print_branch.sl").toURI();
        Set<SlangSource> path = newHashSet(fromFile(operation1));

        RuntimeInformation runtimeInformation =
                triggerWithData(fromFile(resource), path, getSystemProperties());

        List<StepData> branchesData = extractParallelLoopData(runtimeInformation);
        Assert.assertEquals("incorrect number of branches", 3, branchesData.size());

        List<String> expectedNameOutputs = verifyBranchPublishValues(branchesData);

        verifyPublishValues(runtimeInformation, expectedNameOutputs);
    }

    @Test
    public void testFlowWithParallelLoopPublish() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/parallel_loop/parallel_loop_publish.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/parallel_loop/print_branch.sl").toURI();
        Set<SlangSource> path = newHashSet(fromFile(operation1));

        RuntimeInformation runtimeInformation = triggerWithData(
                fromFile(resource),
                path,
                getSystemProperties()
        );

        List<StepData> branchesData = extractParallelLoopData(runtimeInformation);
        Assert.assertEquals("incorrect number of branches", 3, branchesData.size());

        List<String> expectedNameOutputs = verifyBranchPublishValues(branchesData);

        verifyPublishValues(runtimeInformation, expectedNameOutputs);
    }

    @Test
    public void testFlowBranchResults() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/parallel_loop/parallel_loop_branch_result.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/parallel_loop/print_branch.sl").toURI();
        Set<SlangSource> path = newHashSet(fromFile(operation1));

        RuntimeInformation runtimeInformation = triggerWithData(
                fromFile(resource),
                path,
                getSystemProperties()
        );

        List<StepData> branchesData = extractParallelLoopData(runtimeInformation);
        Assert.assertEquals("incorrect number of branches", 3, branchesData.size());

        verifyBranchPublishValues(branchesData);

        verifyPublishValuesBranchResultsCase(
                runtimeInformation,
                Lists.newArrayList(SUCCESS_RESULT, SUCCESS_RESULT, SUCCESS_RESULT)
        );
    }

    @Test
    public void testFlowBranchResultsOutputCollision() throws Exception {
        URI resource = getClass()
                .getResource("/yaml/loops/parallel_loop/parallel_loop_branch_result_output_collision.sl").toURI();
        URI operation1 = getClass()
                .getResource("/yaml/loops/parallel_loop/print_branch_with_result_output_collision.sl").toURI();
        Set<SlangSource> path = newHashSet(fromFile(operation1));

        RuntimeInformation runtimeInformation = triggerWithData(
                fromFile(resource),
                path,
                getSystemProperties()
        );

        List<StepData> branchesData = extractParallelLoopData(runtimeInformation);
        Assert.assertEquals("incorrect number of branches", 3, branchesData.size());

        verifyBranchPublishValuesBranchResultsCase(branchesData);

        verifyPublishValuesBranchResultsCase(
                runtimeInformation,
                Lists.newArrayList(CUSTOM_RESULT, CUSTOM_RESULT, CUSTOM_RESULT)
        );
    }

    @Test
    public void testFlowWithParallelLoopNavigate() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/parallel_loop/parallel_loop_navigate.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/parallel_loop/print_branch.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/loops/parallel_loop/print_list.sl").toURI();

        Set<SlangSource> path = newHashSet(fromFile(operation1), fromFile(operation2));

        RuntimeInformation runtimeInformation = triggerWithData(fromFile(resource), path);

        List<StepData> branchesData = extractParallelLoopData(runtimeInformation);
        Assert.assertEquals("incorrect number of branches", 3, branchesData.size());

        verifyNavigation(runtimeInformation);
    }

    @Test
    public void testFlowWithParallelLoopPublishNavigate() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/parallel_loop/parallel_loop_publish_navigate.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/parallel_loop/print_branch.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/loops/parallel_loop/print_list.sl").toURI();

        Set<SlangSource> path = newHashSet(fromFile(operation1), fromFile(operation2));

        RuntimeInformation runtimeInformation = triggerWithData(
                fromFile(resource),
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
        final URI resource = getClass()
                .getResource("/yaml/loops/parallel_loop/parallel_loop_publish_flow_context.sl").toURI();
        final URI operation1 = getClass()
                .getResource("/yaml/loops/parallel_loop/print_branch.sl").toURI();
        final Set<SlangSource> path = newHashSet(fromFile(operation1));

        exception.expect(RuntimeException.class);
        exception.expectMessage("flow_var");
        exception.expectMessage("not defined");

        triggerWithData(fromFile(resource), path);
    }

    @Test
    public void testFlowWithInlineMapLoops() throws Exception {
        final URI resource = getClass()
                .getResource("/yaml/loops/parallel_loop/parallel_loop_with_inline_map.sl").toURI();
        final URI operation1 = getClass().getResource("/yaml/loops/parallel_loop/print_branch_map.sl").toURI();

        Set<SlangSource> path = newHashSet(fromFile(operation1));
        final CompilationArtifact compilationArtifact = slang.compile(fromFile(resource), path);

        Map<String, Value> userInputs = new HashMap<>();
        RuntimeInformation runtimeInformation = triggerWithData(compilationArtifact, userInputs, getSystemProperties());
        List<StepData> branchesData = extractParallelLoopData(runtimeInformation);
        Assert.assertEquals("incorrect number of branches", 3, branchesData.size());
        List<String> expectedNameOutputs = verifyBranchPublishValues(branchesData);
        verifyPublishValues(runtimeInformation, expectedNameOutputs);
    }

    private Set<SystemProperty> getSystemProperties() {
        return newHashSet(new SystemProperty("loop", "parallel.prop1", "publish_value"));
    }

    private RuntimeInformation triggerWithData(
            SlangSource resource,
            Set<SlangSource> path,
            Set<SystemProperty> systemProperties) {
        CompilationArtifact compilationArtifact = slang.compile(resource, path);

        Map<String, Value> userInputs = new HashMap<>();
        return triggerWithData(compilationArtifact, userInputs, systemProperties);
    }

    private RuntimeInformation triggerWithData(SlangSource resource, Set<SlangSource> path) {
        return triggerWithData(resource, path, new HashSet<SystemProperty>());
    }

    private List<StepData> extractParallelLoopData(RuntimeInformation runtimeInformation) {
        Map<String, List<StepData>> branchesByPath = runtimeInformation.getBranchesByPath();
        Assert.assertTrue("parallel loop data not found", branchesByPath.containsKey(BRANCH_FIRST_STEP_PATH));
        List<StepData> stepDataList = new ArrayList<>();
        for (List<StepData> list : branchesByPath.values()) {
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
        List<String> actualNumberOutputsOfBranches = Lists.newArrayList();
        for (StepData branchData : branchesData) {
            Map<String, Serializable> outputs = branchData.getOutputs();
            Assert.assertTrue(outputs.containsKey("name"));
            Assert.assertTrue(outputs.containsKey("int_output"));
            actualNameOutputsOfBranches.add((String) outputs.get("name"));
            actualNumberOutputsOfBranches.add((String) outputs.get("int_output"));
        }

        List<String> expectedNameOutputs = Lists.newArrayList();
        List<String> expectedNumberOutputs = Lists.newArrayList();
        for (int i = 1; i < 4; i++) {
            expectedNameOutputs.add(BRANCH_MESSAGE + i);
            expectedNumberOutputs.add(Integer.toString(i));
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

    private void verifyBranchPublishValuesBranchResultsCase(List<StepData> branchesData) {
        // publish
        List<String> actualBranchResultOutputs = Lists.newArrayList();
        for (StepData branchData : branchesData) {
            Map<String, Serializable> outputs = branchData.getOutputs();
            Assert.assertTrue(outputs.containsKey("branch_result"));
            actualBranchResultOutputs.add((String) outputs.get("branch_result"));
        }

        List<String> expectedBranchResultOutputs = Lists.newArrayList(
                BRANCH_RESULT_OUTPUT_VALUE,
                BRANCH_RESULT_OUTPUT_VALUE,
                BRANCH_RESULT_OUTPUT_VALUE
        );

        Assert.assertEquals(expectedBranchResultOutputs, actualBranchResultOutputs);
    }

    private void verifyPublishValues(RuntimeInformation runtimeInformation, List<String> expectedNameOutputs) {
        // publish
        Map<String, StepData> parallelLoopSteps = runtimeInformation.getParallelSteps();
        StepData parallelLoopStep = parallelLoopSteps.get(FIRST_STEP_PATH);

        Map<String, Serializable> publishValues = parallelLoopStep.getOutputs();
        Assert.assertTrue("publish name not found in parallel loop outputs", publishValues.containsKey("name_list"));
        @SuppressWarnings("unchecked")
        String actualPublishNames = (String) publishValues.get("name_list");

        ArrayList<String> actualPublishNameList = getArrayListFromString(actualPublishNames);
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

    private ArrayList<String> getArrayListFromString(String actualPublishNames) {
        String[] actualArray = actualPublishNames.replaceAll("'", "")
                .replaceAll("\\[", "").replaceAll("]", "").split(",");
        ArrayList<String> actualPublishNameList = new ArrayList<>();
        for (String s : actualArray) {
            actualPublishNameList.add(s.trim());
        }
        return actualPublishNameList;
    }

    private void verifyPublishValuesBranchResultsCase(
            RuntimeInformation runtimeInformation,
            List<String> publishValue) {
        Map<String, StepData> parallelLoopSteps = runtimeInformation.getParallelSteps();
        StepData parallelLoopStep = parallelLoopSteps.get(FIRST_STEP_PATH);

        Map<String, Serializable> publishValues = parallelLoopStep.getOutputs();
        Assert.assertTrue(publishValues.containsKey(BRANCH_RESULTS_LIST_PUBLISH_VALUE));
        @SuppressWarnings("unchecked")
        String actualBranchResultsPublishValue = (String) publishValues.get(BRANCH_RESULTS_LIST_PUBLISH_VALUE);

        Assert.assertEquals(
                publishValue,
                getArrayListFromString(actualBranchResultsPublishValue)
        );
    }

    private void verifyNavigation(RuntimeInformation runtimeInformation) {
        Map<String, StepData> stepsData = runtimeInformation.getSteps();
        StepData stepAfterParallelLoop = stepsData.get(SECOND_STEP_KEY);
        Assert.assertEquals("navigation not as expected", "print_list", stepAfterParallelLoop.getName());
    }

}
