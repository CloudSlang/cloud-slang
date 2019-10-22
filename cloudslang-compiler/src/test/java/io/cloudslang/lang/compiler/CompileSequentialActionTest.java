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
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.score.api.ExecutionPlan;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/*
 * Created by orius123 on 05/11/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CompileSequentialActionTest {

    @Autowired
    private SlangCompiler compiler;

    @Test
    public void testCompileSeq() throws Exception {
        URI flow = getClass().getResource("/seq-operation/outputs_robot.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);
        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        Assert.assertNotNull("execution plan is null", executionPlan);

        ArrayList<Output> outputs =
                (ArrayList<Output>) executionPlan.getStep(3L).getActionData().get("executableOutputs");

        Output output1 = outputs.get(0);

        Assert.assertEquals("output1", output1.getName());
        Assert.assertEquals(ValueFactory.create("abc"), output1.getValue());
        Assert.assertTrue(output1.hasRobotProperty());
        Assert.assertTrue(output1.isRobot());

        Output output2 = outputs.get(1);

        Assert.assertEquals("output2", output2.getName());
        Assert.assertEquals(ValueFactory.create("123"), output2.getValue());
        Assert.assertTrue(output2.hasRobotProperty());
        Assert.assertFalse(output2.isRobot());

        Output output3 = outputs.get(2);

        Assert.assertEquals("output3", output3.getName());
        Assert.assertEquals(ValueFactory.create("${output3}"), output3.getValue());
        Assert.assertFalse(output3.hasRobotProperty());
    }
}
