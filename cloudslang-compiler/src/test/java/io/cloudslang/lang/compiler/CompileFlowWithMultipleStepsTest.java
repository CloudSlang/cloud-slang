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
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.score.api.ExecutionPlan;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/*
 * Created by orius123 on 05/11/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
        assertEquals("there is a different number of steps than expected", 10, executionPlan.getSteps().size());
        assertEquals("execution plan name is different than expected",
                "flow_with_multiple_steps", executionPlan.getName());
        assertEquals("the dependencies size is not as expected", 3, compilationArtifact.getDependencies().size());
    }

    @Test
    public void testPreCompileFlowBasic() throws Exception {
        URI flowUri = getClass().getResource("/flow_with_multiple_steps.yaml").toURI();
        Executable flow = compiler.preCompile(SlangSource.fromFile(flowUri));

        Assert.assertNotNull("Pre-Compiled meta-data is null", flow);
        assertEquals("Flow name is wrong", "flow_with_multiple_steps", flow.getName());
        assertEquals("Flow namespace is wrong", "user.ops", flow.getNamespace());
        assertEquals("There is a different number of flow inputs than expected", 0, flow.getInputs().size());
        assertEquals("There is a different number of flow outputs than expected", 0, flow.getOutputs().size());
        assertEquals("There is a different number of flow results than expected", 2, flow.getResults().size());
        assertEquals("There is a different number of flow dependencies than expected",
                3, flow.getExecutableDependencies().size());
    }

}
