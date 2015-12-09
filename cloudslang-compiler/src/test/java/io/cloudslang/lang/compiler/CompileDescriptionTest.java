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
import io.cloudslang.lang.compiler.modeller.model.Executable;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;

/**
 * @author Bonczidai Levente
 * @since 12/7/2015
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class CompileDescriptionTest {

    @Autowired
    private SlangCompiler compiler;

    @Test
    public void testFlowDescription() throws Exception {
        URI flowURI = getClass().getResource("/description/flow_description.sl").toURI();
        Executable flow = compiler.preCompile(SlangSource.fromFile(flowURI));

        String flowDescription = flow.getDescription();
        Assert.assertEquals("Flow description not as expected", "sample flow description", flowDescription);
    }

}
