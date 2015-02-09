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
import org.openscore.api.ExecutionPlan;
import org.openscore.lang.compiler.configuration.SlangCompilerSpringConfig;
import org.openscore.lang.compiler.modeller.model.Executable;
import org.openscore.lang.compiler.modeller.model.Flow;
import org.openscore.lang.entities.LoopStatement;
import org.openscore.lang.compiler.modeller.model.Task;
import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.lang.entities.ScoreLangConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class CompileLoopsFlowTest {

    @Autowired
    private SlangCompiler compiler;

    @Test
    public void testPreCompileLoopFlow() throws Exception {
        URI flow = getClass().getResource("/loops/simple_loop.sl").toURI();
        Executable executable = compiler.preCompile(SlangSource.fromFile(flow));
        assertNotNull("executable is null", executable);
        Task task = ((Flow) executable).getWorkflow()
                                        .getTasks()
                                        .getFirst();
        assertTrue(task.getPreTaskActionData().containsKey(SlangTextualKeys.FOR_KEY));
        LoopStatement forStatement = (LoopStatement) task.getPreTaskActionData()
                                .get(SlangTextualKeys.FOR_KEY);
        assertEquals("values", forStatement.getCollectionExpression());
        assertEquals("value", forStatement.getVarName());
        assertEquals(LoopStatement.Type.FOR, forStatement.getType());
    }

    @Test
    public void testCompileLoopFlow() throws Exception {
        URI flow = getClass().getResource("/loops/simple_loop.sl").toURI();
        URI operation = getClass().getResource("/loops/print.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        CompilationArtifact artifact = compiler.compile(SlangSource.fromFile(flow), path);
        assertNotNull("artifact is null", artifact);
        ExecutionPlan executionPlan = artifact.getExecutionPlan();
        assertNotNull("executionPlan is null", executionPlan);
        Map<String, ?> actionData = executionPlan.getStep(2L)
                                                 .getActionData();
        assertTrue(actionData.containsKey(ScoreLangConstants.LOOP_KEY));
        LoopStatement forStatement = (LoopStatement) actionData.get(ScoreLangConstants.LOOP_KEY);
        assertEquals("values", forStatement.getCollectionExpression());
        assertEquals("value", forStatement.getVarName());
        assertEquals(LoopStatement.Type.FOR, forStatement.getType());

    }

}
