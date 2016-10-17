/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.systemtests;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.score.api.ExecutionStep;
import io.cloudslang.score.events.ScoreEvent;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OperationWithDependenciesSystemTest extends SystemsTestsParent {
    @Test(expected = RuntimeException.class)
    public void testCompileJavaActionWithoutDependencies() throws Exception {
        URL resource = getClass().getResource("/yaml/action_wo_dependencies_java_test.sl");
        slang.compile(SlangSource.fromFile(resource.toURI()), null);
    }

    @Test
    public void testCompileJavaActionWithDependencies() throws Exception {
        URL resource = getClass().getResource("/yaml/action_w_dependencies_java_test.sl");
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()), null);

        ExecutionStep step = compilationArtifact.getExecutionPlan().getStep(2L);
        String gav = (String) step.getActionData().get(ScoreLangConstants.JAVA_ACTION_GAV_KEY);
        assertNotNull(gav);
        assertEquals("io.cloudslang:content.actions:1.1", gav);
    }

    @Test
    public void testCompilePythonActionWithoutDependencies() throws Exception {
        URL resource = getClass().getResource("/yaml/action_wo_dependencies_python_test.sl");
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()), null);

        ExecutionStep step = compilationArtifact.getExecutionPlan().getStep(2L);
        assertNull(step.getActionData().get(ScoreLangConstants.ACTION_DEPENDENCIES));

        //Trigger ExecutionPlan
        Map<String, Value> userInputs = new HashMap<>();
        ScoreEvent event = trigger(compilationArtifact, userInputs, new HashSet<SystemProperty>());
        assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
    }

    @Ignore("Enable when `dependencies` tag will be added")
    @Test
    public void testCompilePythonActionWithDependencies() throws Exception {
        URL resource = getClass().getResource("/yaml/action_w_dependencies_python_test.sl");
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()), null);

        ExecutionStep step = compilationArtifact.getExecutionPlan().getStep(2L);
        List<String> dependencies = (List<String>) step.getActionData().get(ScoreLangConstants.ACTION_DEPENDENCIES);
        assertNotNull(dependencies);

        assertTrue(dependencies.size() == 2);
        assertEquals("some.group:some.artifact:some_version-1.1", dependencies.get(0));
        assertEquals("some.group1:some.artifact:some_version-2.1", dependencies.get(1));
    }
}
