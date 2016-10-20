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
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;

import java.net.URI;
import java.util.HashSet;
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

/**
 * @author Bonczidai Levente
 * @since 9/10/2015
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CompileFlowReferenceIdTest {

    @Autowired
    private SlangCompiler compiler;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testBasicAlias() throws Exception {
        final URI flow = getClass().getResource("/alias/basic_alias.yaml").toURI();
        final URI operation = getClass().getResource("/alias/simple_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);

        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        Assert.assertNotNull("execution plan is null", executionPlan);
        Assert.assertEquals("there is a different number of steps than expected", 4, executionPlan.getSteps().size());

        ExecutionStep beginStepExecutionStep = executionPlan.getStep(2L);
        @SuppressWarnings("unchecked")
        String referenceId = (String) beginStepExecutionStep.getActionData().get(ScoreLangConstants.REF_ID);

        Assert.assertEquals("Wrong reference ID for step", "cloudslang.sample.simple_op", referenceId);
    }

    @Test
    public void testDefaultNamespace() throws Exception {
        final URI flow = getClass().getResource("/alias/default_namespace.yaml").toURI();
        final URI operation = getClass().getResource("/alias/simple_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);

        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        Assert.assertNotNull("execution plan is null", executionPlan);
        Assert.assertEquals("there is a different number of steps than expected", 4, executionPlan.getSteps().size());

        ExecutionStep beginStepExecutionStep = executionPlan.getStep(2L);
        @SuppressWarnings("unchecked")
        String referenceId = (String) beginStepExecutionStep.getActionData().get(ScoreLangConstants.REF_ID);

        Assert.assertEquals("Wrong reference ID for step", "cloudslang.sample.simple_op", referenceId);
    }

    @Test
    public void testShortFullPathNoExpanding() throws Exception {
        final URI flow = getClass().getResource("/alias/short_full_path_no_expanding.yaml").toURI();
        final URI operation = getClass().getResource("/alias/cloud_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);

        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        Assert.assertNotNull("execution plan is null", executionPlan);
        Assert.assertEquals("there is a different number of steps than expected", 4, executionPlan.getSteps().size());

        ExecutionStep beginStepExecutionStep = executionPlan.getStep(2L);
        @SuppressWarnings("unchecked")
        String referenceId = (String) beginStepExecutionStep.getActionData().get(ScoreLangConstants.REF_ID);

        Assert.assertEquals("Wrong reference ID for step", "cloud.cloud_op", referenceId);
    }

    @Test
    public void testLongFullPathNoExpanding() throws Exception {
        final URI flow = getClass().getResource("/alias/long_full_path_no_expanding.yaml").toURI();
        final URI operation = getClass().getResource("/alias/print.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);

        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        Assert.assertNotNull("execution plan is null", executionPlan);
        Assert.assertEquals("there is a different number of steps than expected", 4, executionPlan.getSteps().size());

        ExecutionStep beginStepExecutionStep = executionPlan.getStep(2L);
        @SuppressWarnings("unchecked")
        String referenceId = (String) beginStepExecutionStep.getActionData().get(ScoreLangConstants.REF_ID);

        Assert.assertEquals("Wrong reference ID for step", "a.b.c.d.print", referenceId);
    }

    @Test
    public void testLongFullPathWithExpanding() throws Exception {
        final URI flow = getClass().getResource("/alias/long_full_path_with_expanding.yaml").toURI();
        final URI operation = getClass().getResource("/alias/print.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);

        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        Assert.assertNotNull("execution plan is null", executionPlan);
        Assert.assertEquals("there is a different number of steps than expected", 4, executionPlan.getSteps().size());

        ExecutionStep beginStepExecutionStep = executionPlan.getStep(2L);
        @SuppressWarnings("unchecked")
        String referenceId = (String) beginStepExecutionStep.getActionData().get(ScoreLangConstants.REF_ID);

        Assert.assertEquals("Wrong reference ID for step", "a.b.c.d.print", referenceId);
    }

    @Test
    public void testReferenceNotFound() throws Exception {
        final URI flow = getClass().getResource("/alias/reference_not_found_flow.yaml").toURI();
        final URI operation = getClass().getResource("/alias/simple_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        exception.expectMessage("sample_typo.simple_op");
        exception.expect(RuntimeException.class);

        compiler.compile(SlangSource.fromFile(flow), path);
    }

}
