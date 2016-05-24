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
import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Date: 3/25/2015
 *
 * @author Bonczidai Levente
 */
public class ParallelLoopFlowsTest extends SystemsTestsParent {

    private static final String BRANCH_RESULTS_LIST_PUBLISH_VALUE = "branch_results_list";
    private static final String CUSTOM_RESULT = "CUSTOM";
    private static final String SUCCESS_RESULT = "SUCCESS";

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
    public void testFlowWithSensitiveInputParallelLoop() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/parallel_loop/sensitive_input_parallel_loop.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/parallel_loop/print_branch.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));

        RuntimeInformation runtimeInformation = triggerWithData(SlangSource.fromFile(resource), path);

        List<StepData> branchesData = extractParallelLoopData(runtimeInformation);
        Assert.assertEquals("incorrect number of branches", 3, branchesData.size());
    }

    @Test
    public void testFlowWithSensitiveOutputParallelLoop() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/parallel_loop/sensitive_output_parallel_loop.sl").toURI();
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
    }

    @Test
    public void testFlowBranchResults() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/parallel_loop/parallel_loop_branch_result.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/parallel_loop/print_branch.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));

        RuntimeInformation runtimeInformation = triggerWithData(
                SlangSource.fromFile(resource),
                path,
                getSystemProperties()
        );

        List<StepData> branchesData = extractParallelLoopData(runtimeInformation);
        Assert.assertEquals("incorrect number of branches", 3, branchesData.size());

        verifyPublishValuesBranchResultsCase(
                runtimeInformation,
                Lists.newArrayList(SUCCESS_RESULT, SUCCESS_RESULT, SUCCESS_RESULT)
        );
    }

    @Test
    public void testFlowBranchResultsOutputCollision() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/parallel_loop/parallel_loop_branch_result_output_collision.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/parallel_loop/print_branch_with_result_output_collision.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));

        RuntimeInformation runtimeInformation = triggerWithData(
                SlangSource.fromFile(resource),
                path,
                getSystemProperties()
        );

        List<StepData> branchesData = extractParallelLoopData(runtimeInformation);
        Assert.assertEquals("incorrect number of branches", 3, branchesData.size());

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

    private void verifyPublishValuesBranchResultsCase(
            RuntimeInformation runtimeInformation,
            List<String> publishValue) {
        Map<String, StepData> parallelLoopSteps = runtimeInformation.getParallelSteps();
        StepData parallelLoopStep = parallelLoopSteps.get(FIRST_STEP_PATH);

        Map<String, Value> publishValues = parallelLoopStep.getOutputs();
        Assert.assertTrue(publishValues.containsKey(BRANCH_RESULTS_LIST_PUBLISH_VALUE));
        @SuppressWarnings("unchecked")
        List<String> actualBranchResultsPublishValue = (List<String>) publishValues.get(BRANCH_RESULTS_LIST_PUBLISH_VALUE).get();

        Assert.assertEquals(
                publishValue,
                actualBranchResultsPublishValue
        );
    }

    private void verifyNavigation(RuntimeInformation runtimeInformation) {
        Map<String, StepData> stepsData = runtimeInformation.getSteps();
        StepData stepAfterParallelLoop = stepsData.get(SECOND_STEP_KEY);
        Assert.assertEquals("navigation not as expected", "print_list", stepAfterParallelLoop.getName());
    }

}
