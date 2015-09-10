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
package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Bonczidai Levente
 * @since 9/10/2015
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class CompileFlowReferenceIDTest {

    @Autowired
    private SlangCompiler compiler;

    @Test
    public void testBasicAlias() throws Exception {
        URI flow = getClass().getResource("/alias/basic_alias.yaml").toURI();
        URI operation = getClass().getResource("/alias/simple_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);

        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        Assert.assertNotNull("execution plan is null", executionPlan);
        Assert.assertEquals("there is a different number of steps than expected", 4, executionPlan.getSteps().size());

        ExecutionStep beginTaskStep = executionPlan.getStep(2L);
        @SuppressWarnings("unchecked") String referenceID = (String) beginTaskStep.getActionData().get(ScoreLangConstants.REF_ID);

        Assert.assertEquals("Wrong reference ID for task", "slang.sample.operations.simple_op", referenceID);
    }

}
