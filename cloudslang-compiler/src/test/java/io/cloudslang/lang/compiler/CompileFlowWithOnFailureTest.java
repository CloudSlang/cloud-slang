/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.modeller.ExecutableBuilder;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.NavigationOptions;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;

import java.io.Serializable;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/*
 * Created by orius123 on 05/11/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CompileFlowWithOnFailureTest {

    @Autowired
    private SlangCompiler compiler;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testCompileOnFailureBasic() throws Exception {
        URI flow = getClass().getResource("/flow_with_on_failure.sl").toURI();
        URI operation = getClass().getResource("/test_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);
        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        assertNotNull("execution plan is null", executionPlan);
        assertEquals("there is a different number of steps than expected", 12, executionPlan.getSteps().size());
        assertEquals("execution plan name is different than expected", "flow_with_on_failure", executionPlan.getName());
        assertEquals("the dependencies size is not as expected", 1, compilationArtifact.getDependencies().size());
        assertEquals("the inputs size is not as expected", 1, compilationArtifact.getInputs().size());

        long firstOnFailureStep = 9L;
        long endFlowStep = 0L;

        ExecutionStep firstStep = executionPlan.getStep(5L);
        assertEquals("first step didn't navigate to on failure",
                firstOnFailureStep, getFailureNavigationStepId(firstStep));
        ExecutionStep secondStep = executionPlan.getStep(8L);
        assertEquals(endFlowStep, getFailureNavigationStepId(secondStep));
        ExecutionStep firstOnFailStep = executionPlan.getStep(11L);
        assertEquals(endFlowStep, getFailureNavigationStepId(firstOnFailStep));
    }

    @Test
    public void testCompileMultipleOnFailure() throws Exception {
        URI operation = getClass().getResource("/test_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        expectedException.expectMessage("Multiple 'on_failure' steps found");
        expectedException.expect(RuntimeException.class);

        URI flow = getClass().getResource("/corrupted/multiple_on_failure.sl").toURI();
        compiler.compile(SlangSource.fromFile(flow), path);
    }

    @Test
    public void testCompileSameNameInFlowAndOnFailure() throws Exception {
        URI operation = getClass().getResource("/fail_on_input_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        expectedException.expectMessage(ExecutableBuilder.UNIQUE_STEP_NAME_MESSAGE_SUFFIX);
        expectedException.expectMessage("step_same_name");
        expectedException.expect(RuntimeException.class);

        URI flow = getClass().getResource("/corrupted/same_step_name_in_flow_and_on_failure.sl").toURI();
        compiler.compile(SlangSource.fromFile(flow), path);
    }

    @Test
    public void testCompileOnFailureSecondStep() throws Exception {
        URI operation = getClass().getResource("/test_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("'on_failure' should be last step in the workflow");

        URI flow = getClass().getResource("/corrupted/flow_with_on_failure_second_step.sl").toURI();
        compiler.compile(SlangSource.fromFile(flow), path);
    }

    @Test
    public void testCompileNavigationWithRoi() throws Exception {
        URI flow = getClass().getResource("/flow_with_roi.sl").toURI();
        URI operation = getClass().getResource("/test_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);
        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        assertNotNull("execution plan is null", executionPlan);
        assertEquals("there is a different number of steps than expected", 9, executionPlan.getSteps().size());
        assertEquals("execution plan name is different than expected", "flow_with_roi", executionPlan.getName());
        assertEquals("the dependencies size is not as expected", 1, compilationArtifact.getDependencies().size());

        final long step1 = 1;
        final long step4 = 4;

        ExecutionStep firstStep = executionPlan.getStep(step1);
        assertTrue("navigation data is not empty", firstStep.getNavigationData() == null ||
                firstStep.getNavigationData().isEmpty());

        ExecutionStep thirdStep = executionPlan.getStep(step4);
        assertTrue("navigation data is empty", thirdStep.getNavigationData() != null &&
                !thirdStep.getNavigationData().isEmpty());
        List<NavigationOptions> optionsList = (List<NavigationOptions>) thirdStep.getNavigationData()
                .get(ScoreLangConstants.STEP_NAVIGATION_OPTIONS_KEY);
        assertTrue("navigation data options is empty", !optionsList.isEmpty());
        assertEquals(optionsList.get(0).getName(), "SUCCESS");
        assertNotNull("navigation SUCCESS options is empty", optionsList.get(0).getOptions());
        Map<String, Serializable> successOptions = optionsList.get(0).getOptions();
        assertEquals(2, successOptions.size());
        assertEquals("navigation SUCCESS next_step options is invalid", "SUCCESS",
                successOptions.get("next_step"));
        assertEquals("navigation SUCCESS ROI options is invalid", 11, successOptions.get("ROI"));
        assertEquals(optionsList.get(1).getName(), "FAILURE");
        assertNotNull("navigation FAILURE options is empty", optionsList.get(1).getOptions());
        Map<String, Serializable> failureOptions = optionsList.get(1).getOptions();
        assertEquals(1, failureOptions.size());
        assertEquals("navigation FAILURE next_step options is invalid", "reset_step_on_failure",
                failureOptions.get("next_step"));
    }

    @Test
    public void testDefaultNavigationMissingResultOnFailure() throws Exception {
        URI operation = getClass().getResource("/test_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage(
                "Failed to compile step: jedi_training_3." +
                        " The step/result name: FAILURE of navigation: SUCCESS -> FAILURE is missing"
        );

        URI flow = getClass().getResource("/corrupted/default_navigation_missing_result_on_failure.sl").toURI();
        compiler.compile(SlangSource.fromFile(flow), path);
    }

    @Test
    public void testFlowOnFailureStepFailureResultIsReachable() throws Exception {
        URI resource = getClass().getResource("/on_failure_reachable_result.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() == 0);
    }

    private long getFailureNavigationStepId(ExecutionStep firstStep) {
        Map<String, ResultNavigation> navigationData = getNavigationMap(firstStep);
        return navigationData.get(ScoreLangConstants.FAILURE_RESULT).getNextStepId();
    }

    private Map<String, ResultNavigation> getNavigationMap(ExecutionStep firstStep) {
        @SuppressWarnings("unchecked") Map<String, ResultNavigation> stringResultNavigationMap =
                (Map<String, ResultNavigation>) firstStep.getActionData().get(ScoreLangConstants.STEP_NAVIGATION_KEY);
        return stringResultNavigationMap;
    }

}
