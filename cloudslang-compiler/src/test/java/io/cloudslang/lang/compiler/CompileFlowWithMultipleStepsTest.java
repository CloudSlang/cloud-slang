/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.entities.CompilationArtifact;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.cloudslang.score.api.ExecutionPlan;
import org.mockito.internal.util.collections.Sets;
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
public class CompileFlowWithMultipleStepsTest {

    @Autowired
    private SlangCompiler compiler;

    @Test
    public void testCompileFlowBasic() throws Exception {
        URI flow = getClass().getResource("/flow_with_multiple_steps.yaml").toURI();
        URI operation1 = getClass().getResource("/test_op.sl").toURI();
        URI operation2 = getClass().getResource("/java_op.sl").toURI();
        URI operation3 = getClass().getResource("/check_Weather.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation1));
        path.add(SlangSource.fromFile(operation2));
        path.add(SlangSource.fromFile(operation3));

        CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);
        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        Assert.assertNotNull("execution plan is null", executionPlan);
        Assert.assertEquals("there is a different number of steps than expected", 10, executionPlan.getSteps().size());
        Assert.assertEquals("execution plan name is different than expected", "basic_flow", executionPlan.getName());
        Assert.assertEquals("the dependencies size is not as expected", 3, compilationArtifact.getDependencies().size());
    }

    @Test
    public void testImplicitAliasForCurrentNamespace() throws Exception {
        URI flow = getClass().getResource("/flow_implicit_alias_for_current_namespace.sl").toURI();
        URI operation1 = getClass().getResource("/test_op.sl").toURI();
        URI operation2 = getClass().getResource("/check_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation1));
        path.add(SlangSource.fromFile(operation2));

        CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);

        Map<String, ExecutionPlan> dependencies= compilationArtifact.getDependencies();
        Assert.assertNotNull("dependencies reference is null", dependencies);
        Set<String> actualDependencies = dependencies.keySet();
        Set<String> expectedDependencies = Sets.newSet("user.ops.test_op", "io.cloudslang.check_op");
        junit.framework.Assert.assertEquals("dependencies are not resolved as expected", expectedDependencies, actualDependencies);
    }

    @Test
    public void testPreCompileFlowBasic() throws Exception {
        URI flowUri = getClass().getResource("/flow_with_multiple_steps.yaml").toURI();
        Executable flow = compiler.preCompile(SlangSource.fromFile(flowUri));

        Assert.assertNotNull("Pre-Compiled meta-data is null", flow);
        Assert.assertEquals("Flow name is wrong", "basic_flow", flow.getName());
        Assert.assertEquals("Flow namespace is wrong", "user.ops", flow.getNamespace());
        Assert.assertEquals("There is a different number of flow inputs than expected", 0, flow.getInputs().size());
        Assert.assertEquals("There is a different number of flow outputs than expected", 0, flow.getOutputs().size());
        Assert.assertEquals("There is a different number of flow results than expected", 2, flow.getResults().size());
        Assert.assertEquals("There is a different number of flow dependencies than expected", 3, flow.getDependencies().size());
    }


}