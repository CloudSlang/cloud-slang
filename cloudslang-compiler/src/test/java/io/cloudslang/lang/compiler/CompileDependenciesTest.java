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
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/*
 * Created by orius123 on 05/11/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class CompileDependenciesTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private SlangCompiler compiler;

    @Test(expected = RuntimeException.class)
    public void emptyPathButThereAreImports() throws Exception {
        URI flow = getClass().getResource("/basic_flow.yaml").toURI();
        Set<SlangSource> path = new HashSet<>();
        compiler.compile(SlangSource.fromFile(flow), path);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullPathButThereAreImports() throws Exception {
        URI flow = getClass().getResource("/basic_flow.yaml").toURI();
        compiler.compile(SlangSource.fromFile(flow), null);
    }

    @Test
    public void referenceDoesNoExistInPath() throws Exception {
        URI flow = getClass().getResource("/basic_flow.yaml").toURI();
        URI operation = getClass().getResource("/operation_with_data.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        exception.expect(RuntimeException.class);
        exception.expectMessage(containsString("ops.test_op"));

        compiler.compile(SlangSource.fromFile(flow), path);
    }

    @Test
    public void importHasAKeyThatDoesNotExistInPath() throws Exception {
        URI flow = getClass().getResource("/basic_flow.yaml").toURI();
        URI operation = getClass().getResource("/flow_with_data.yaml").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        exception.expect(RuntimeException.class);
        exception.expectMessage(containsString("ops"));

        compiler.compile(SlangSource.fromFile(flow), path);
    }

    @Test
    public void filesThatAreNotImportedShouldNotBeCompiled() throws Exception {
        URI flow = getClass().getResource("/basic_flow.yaml").toURI();
        URI notImportedOperation = getClass().getResource("/flow_with_data.yaml").toURI();
        URI importedOperation = getClass().getResource("/test_op.sl").toURI();
        URI importedOperation2 = getClass().getResource("/check_Weather.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(notImportedOperation));
        path.add(SlangSource.fromFile(importedOperation));
        path.add(SlangSource.fromFile(importedOperation2));

        CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);
        Assert.assertThat(compilationArtifact.getDependencies(), Matchers.<String, ExecutionPlan>hasKey("user.ops.test_op"));
        Assert.assertThat(compilationArtifact.getDependencies(), not(Matchers.<String, ExecutionPlan>hasKey("slang.sample.flows.SimpleFlow")));
    }

    @Test
    public void sourceFileIsADirectory() throws Exception {
        URI dir = getClass().getResource("/").toURI();

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("directories"));

        compiler.compile(SlangSource.fromFile(dir), null);
    }

    @Test
    public void subFlowRefId() throws Exception {
        URI flow = getClass().getResource("/circular-dependencies/parent_flow.yaml").toURI();
        URI child_flow = getClass().getResource("/circular-dependencies/child_flow.yaml").toURI();
        URI operation = getClass().getResource("/test_op.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(child_flow));
        path.add(SlangSource.fromFile(operation));

        CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);
        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        Assert.assertNotNull(executionPlan);
        Assert.assertEquals("different number of dependencies than expected", 2, compilationArtifact.getDependencies().size());
        ExecutionStep secondStepStartExecutionStep = executionPlan.getStep(4L);
        String refId = (String) secondStepStartExecutionStep.getActionData().get(ScoreLangConstants.REF_ID);
        Assert.assertEquals("refId is not as expected", "user.flows.circular.child_flow", refId);
    }

    @Test
    public void bothFileAreDependentOnTheSameFile() throws Exception {
        URI flow = getClass().getResource("/circular-dependencies/parent_flow.yaml").toURI();
        URI child_flow = getClass().getResource("/circular-dependencies/child_flow.yaml").toURI();
        URI operation = getClass().getResource("/test_op.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(child_flow));
        path.add(SlangSource.fromFile(operation));
        CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);
        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        Assert.assertNotNull(executionPlan);
        Assert.assertEquals("different number of dependencies than expected", 2, compilationArtifact.getDependencies().size());
    }

    @Test
    public void circularDependencies() throws Exception {
        URI flow = getClass().getResource("/circular-dependencies/circular_parent_flow.yaml").toURI();
        URI child_flow = getClass().getResource("/circular-dependencies/circular_child_flow.yaml").toURI();
        URI operation = getClass().getResource("/test_op.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(child_flow));
        path.add(SlangSource.fromFile(operation));
        CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);
        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        Assert.assertNotNull(executionPlan);
        Assert.assertEquals(3, compilationArtifact.getDependencies().size());
    }

    @Test
    public void sameSourceAsDependencyWorks() throws Exception {
        URI flow = getClass().getResource("/basic_flow.yaml").toURI();
        URI importedOperation = getClass().getResource("/test_op.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(importedOperation));
        SlangSource flowSource = SlangSource.fromFile(flow);
        path.add(flowSource);

        Assert.assertTrue(path.contains(flowSource));
        CompilationArtifact compilationArtifact = compiler.compile(flowSource, path);
        Assert.assertNotNull(compilationArtifact);
    }

}