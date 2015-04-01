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
import io.cloudslang.lang.entities.bindings.Output;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

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
    public void testPreCompileAsyncLoopFlow() throws Exception {
        Task task = getFirstTaskAfterPrecompileFlow("/loops/async_loop/simple_async_loop.sl");

        verifyAsyncLoopStatement(task);

        List<Output> aggregateValues = getAggregateOutputs(task);
        assertEquals("aggregate list is not empty", 0, aggregateValues.size());

        List<Output> publishValues = getPublishOutputs(task);
        assertEquals("aggregate list is not empty", 0, publishValues.size());

        Map<String, String> expectedNavigationStrings = new HashMap<>();
        expectedNavigationStrings.put("SUCCESS", "SUCCESS");
        expectedNavigationStrings.put("FAILURE", "FAILURE");
        verifyNavigationStrings(expectedNavigationStrings, task);

        assertTrue(task.isAsync());
    }

    @Test
    public void testPreCompileAsyncLoopFlowAggregate() throws Exception {
        Task task = getFirstTaskAfterPrecompileFlow("/loops/async_loop/async_loop_aggregate.sl");

        verifyAsyncLoopStatement(task);

        List<Output> aggregateValues = getAggregateOutputs(task);
        assertEquals(2, aggregateValues.size());
        assertEquals("map(lambda x:str(x['name']), branches_context)", aggregateValues.get(0).getExpression());

        List<Output> publishValues = getPublishOutputs(task);
        assertEquals("aggregate list is not empty", 2, publishValues.size());
        assertEquals("name", publishValues.get(0).getExpression());

        Map<String, String> expectedNavigationStrings = new HashMap<>();
        expectedNavigationStrings.put("SUCCESS", "SUCCESS");
        expectedNavigationStrings.put("FAILURE", "FAILURE");
        verifyNavigationStrings(expectedNavigationStrings, task);

        assertTrue(task.isAsync());
    }

    @Ignore // TODO - async loop
    @Test
    public void testCompileAsyncLoopFlow() throws Exception {
        URI flow = getClass().getResource("/loops/async_loop/simple_async_loop.sl").toURI();
        URI operation = getClass().getResource("/loops/async_loop/print.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        CompilationArtifact artifact = compiler.compile(SlangSource.fromFile(flow), path);
        assertNotNull("artifact is null", artifact);
    }

    @Ignore  // TODO - async loop
    @Test
    public void testCompileAsyncLoopFlowAggregate() throws Exception {
        URI flow = getClass().getResource("/loops/async_loop/async_loop_aggregate.sl").toURI();
        URI operation = getClass().getResource("/loops/async_loop/print.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        CompilationArtifact artifact = compiler.compile(SlangSource.fromFile(flow), path);
        assertNotNull("artifact is null", artifact);
    }

    private Task getFirstTaskAfterPrecompileFlow(String flowPath) throws URISyntaxException {
        URI flow = getClass().getResource(flowPath).toURI();
        Executable executable = compiler.preCompile(SlangSource.fromFile(flow));
        assertNotNull("executable is null", executable);

        return ((Flow) executable).getWorkflow()
                .getTasks()
                .getFirst();
    }

    private void verifyAsyncLoopStatement(Task task) {
        assertTrue(task.getPreTaskActionData().containsKey(ScoreLangConstants.ASYNC_LOOP_KEY));
        AsyncLoopStatement asyncLoopStatement = (AsyncLoopStatement) task.getPreTaskActionData()
                .get(ScoreLangConstants.ASYNC_LOOP_KEY);
        assertEquals("values", asyncLoopStatement.getExpression());
        assertEquals("value", asyncLoopStatement.getVarName());
    }

    private List<Output> getAggregateOutputs(Task task) {
        assertTrue(task.getPostTaskActionData().containsKey(SlangTextualKeys.AGGREGATE_KEY));
        List<Output> aggregateValues = (List<Output>) task.getPostTaskActionData().get(SlangTextualKeys.AGGREGATE_KEY);
        assertNotNull("aggregate list is null", aggregateValues);
        return aggregateValues;
    }

    private List<Output> getPublishOutputs(Task task) {
        assertTrue(task.getPostTaskActionData().containsKey(SlangTextualKeys.PUBLISH_KEY));
        List<Output> publishValues = (List<Output>) task.getPostTaskActionData().get(SlangTextualKeys.PUBLISH_KEY);
        assertNotNull("publish list is null", publishValues);
        return publishValues;
    }

    private void verifyNavigationStrings(Map<String, String> expectedNavigationStrings, Task task) {
        Map<String, String> actualNavigationStrings = task.getNavigationStrings();
        assertEquals(expectedNavigationStrings, actualNavigationStrings);
    }

}
