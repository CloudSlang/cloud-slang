/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
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
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.ScoreLangConstants;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Created by orius123 on 05/11/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class CompileFlowWithOnFailureTest {

    @Autowired
    private SlangCompiler compiler;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testCompileOnFailureBasic() throws Exception {
        URI flow = getClass().getResource("/flow_with_on_failure.yaml").toURI();
        URI operation = getClass().getResource("/test_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);
        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        Assert.assertNotNull("execution plan is null", executionPlan);
        Assert.assertEquals("there is a different number of steps than expected", 10, executionPlan.getSteps().size());
        Assert.assertEquals("execution plan name is different than expected", "basic_flow", executionPlan.getName());
        Assert.assertEquals("the dependencies size is not as expected", 1, compilationArtifact.getDependencies().size());
        Assert.assertEquals("the inputs size is not as expected", 1, compilationArtifact.getInputs().size());

        long firstOnFailureStep = 6L;
        long endFlowStep = 0L;

        ExecutionStep firstStep = executionPlan.getStep(3L);
        Assert.assertEquals("first step didn't navigate to on failure", firstOnFailureStep, getFailureNavigationStepId(firstStep));
        ExecutionStep secondStep = executionPlan.getStep(5L);
        Assert.assertEquals(endFlowStep, getFailureNavigationStepId(secondStep));
        ExecutionStep firstOnFailStep = executionPlan.getStep(7L);
        Assert.assertEquals(endFlowStep, getFailureNavigationStepId(firstOnFailStep));
        ExecutionStep secondOnFailStep = executionPlan.getStep(9L);
        Assert.assertEquals(endFlowStep, getFailureNavigationStepId(secondOnFailStep));
        Map<String, ResultNavigation> secondOnFailStepNavigationMap = getNavigationMap(secondOnFailStep);
        ResultNavigation secondOnFailStepResultSuccessNavigation =
                secondOnFailStepNavigationMap.get(ScoreLangConstants.SUCCESS_RESULT);
        Assert.assertEquals("on failure success should navigate to failure",
                ScoreLangConstants.FAILURE_RESULT, secondOnFailStepResultSuccessNavigation.getPresetResult());
    }

    @Test
    public void testCompileMultipleOnFailure() throws Exception {
        URI flow = getClass().getResource("/corrupted/multiple_on_failure.sl").toURI();
        URI operation = getClass().getResource("/test_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        expectedException.expectMessage(ExecutableBuilder.MULTIPLE_ON_FAILURE_MESSAGE_SUFFIX);
        expectedException.expect(RuntimeException.class);

        compiler.compile(SlangSource.fromFile(flow), path);
    }

	private long getFailureNavigationStepId(ExecutionStep firstStep) {
        Map<String, ResultNavigation> navigationData = getNavigationMap(firstStep);
        return navigationData.get(ScoreLangConstants.FAILURE_RESULT).getNextStepId();
	}

    private Map<String, ResultNavigation> getNavigationMap(ExecutionStep firstStep) {
        @SuppressWarnings("unchecked") Map<String, ResultNavigation> stringResultNavigationMap =
                (Map<String, ResultNavigation>) firstStep.getActionData().get(ScoreLangConstants.TASK_NAVIGATION_KEY);
        return stringResultNavigationMap;
    }

}
