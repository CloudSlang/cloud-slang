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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openscore.lang.compiler.configuration.SlangCompilerSpringConfig;
import org.openscore.lang.compiler.modeller.model.Executable;
import org.openscore.lang.compiler.modeller.model.Flow;
import org.openscore.lang.compiler.modeller.model.LoopStatement;
import org.openscore.lang.compiler.modeller.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.net.URI;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class CompileLoopsFlowTest {

    @Autowired
    private SlangCompiler compiler;

    @Test
    public void testCompileFlowBasic() throws Exception {
        URI flow = getClass().getResource("/loops/simple_loop.sl").toURI();
        Executable executable = compiler.preCompile(SlangSource.fromFile(flow));
        assertNotNull("executable is null", executable);
        Task task = ((Flow) executable).getWorkflow()
                                        .getTasks()
                                        .getFirst();
        assertTrue(task.getPreTaskActionData().containsKey("for"));
        LoopStatement forStatement = (LoopStatement) task.getPreTaskActionData()
                                .get("for");
        assertEquals("values", forStatement.getCollectionExpression());
        assertEquals("value", forStatement.getVarName());
    }

}
