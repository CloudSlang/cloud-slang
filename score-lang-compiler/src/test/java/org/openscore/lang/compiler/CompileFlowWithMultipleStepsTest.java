/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.lang.compiler;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openscore.api.ExecutionPlan;
import org.openscore.lang.compiler.configuration.SlangCompilerSpringConfig;
import org.openscore.lang.compiler.model.Executable;
import org.openscore.lang.compiler.model.SlangFileType;
import org.openscore.lang.entities.CompilationArtifact;
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
        URI operation = getClass().getResource("/operation.yaml").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        CompilationArtifact compilationArtifact = compiler.compileFlow(SlangSource.fromFile(flow), path);
        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        Assert.assertNotNull("execution plan is null", executionPlan);
        Assert.assertEquals("there is a different number of steps than expected", 10, executionPlan.getSteps().size());
        Assert.assertEquals("execution plan name is different than expected", "basic_flow", executionPlan.getName());
        Assert.assertEquals("the dependencies size is not as expected", 3, compilationArtifact.getDependencies().size());
    }

    @Test
    public void testPreCompileFlowBasic() throws Exception {
        URI flow = getClass().getResource("/flow_with_multiple_steps.yaml").toURI();
        Executable preCompiledMetaData = compiler.preCompileFlow(SlangSource.fromFile(flow));

        Assert.assertNotNull("Pre-Compiled meta-data is null", preCompiledMetaData);
        Assert.assertEquals("Flow name is wrong", "basic_flow", preCompiledMetaData.getName());
        Assert.assertEquals("Flow namespace is wrong", "user.ops", preCompiledMetaData.getNamespace());
        Assert.assertEquals("There is a different number of flow inputs than expected", 0, preCompiledMetaData.getInputs().size());
        Assert.assertEquals("There is a different number of flow outputs than expected", 0, preCompiledMetaData.getOutputs().size());
        Assert.assertEquals("There is a different number of flow results than expected", 2, preCompiledMetaData.getResults().size());
        Map<String, SlangFileType> dependencies = preCompiledMetaData.getDependencies();
        Assert.assertEquals("There is a different number of flow dependencies than expected", 3, dependencies.size());
    }


}