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
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Task;
import io.cloudslang.lang.entities.AsyncLoopStatement;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Date: 3/25/2015
 *
 * @author Bonczidai Levente
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class CompileAsyncLoopFlowTest {

    @Autowired
    private SlangCompiler compiler;

    @Test
    public void testPreCompileLoopFlow() throws Exception {
        URI flow = getClass().getResource("/loops/async_loop/simple_async_loop.sl").toURI();
        Executable executable = compiler.preCompile(SlangSource.fromFile(flow));
        assertNotNull("executable is null", executable);
        Task task = ((Flow) executable).getWorkflow()
                .getTasks()
                .getFirst();
        assertTrue(task.getPreTaskActionData().containsKey(ScoreLangConstants.ASYNC_LOOP_KEY));
        AsyncLoopStatement asyncLoopStatement = (AsyncLoopStatement) task.getPreTaskActionData()
                .get(ScoreLangConstants.ASYNC_LOOP_KEY);
        assertEquals("values", asyncLoopStatement.getExpression());
        assertEquals("value", asyncLoopStatement.getVarName());
    }


    @Test
    public void testCompileLoopFlow() throws Exception {
        URI flow = getClass().getResource("/loops/async_loop/simple_async_loop.sl").toURI();
        URI operation = getClass().getResource("/loops/async_loop/print.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        CompilationArtifact artifact = compiler.compile(SlangSource.fromFile(flow), path);
        assertNotNull("artifact is null", artifact);
    }
}
