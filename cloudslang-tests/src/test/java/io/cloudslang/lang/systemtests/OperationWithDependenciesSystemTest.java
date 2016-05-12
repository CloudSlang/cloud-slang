package io.cloudslang.lang.systemtests;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.score.api.ExecutionStep;
import io.cloudslang.score.events.ScoreEvent;
import static org.junit.Assert.*;
import org.junit.Test;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class OperationWithDependenciesSystemTest extends SystemsTestsParent {
    @Test
    public void testCompileJavaActionWithoutDependencies() throws Exception {
        URL resource = getClass().getResource("/yaml/action_wo_dependencies_java_test.sl");
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()), null);

        ExecutionStep step = compilationArtifact.getExecutionPlan().getStep(2L);
        List<String> dependencies = (List<String>) step.getActionData().get(ScoreLangConstants.ACTION_DEPENDENCIES);
        assertNotNull(dependencies);
        assertTrue(dependencies.isEmpty());

        //Trigger ExecutionPlan
        Map<String, Serializable> userInputs = new HashMap<>();
        ScoreEvent event = trigger(compilationArtifact, userInputs, new HashSet<SystemProperty>());
        assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
    }
    @Test
    public void testCompileJavaActionWithDependencies() throws Exception {
        URL resource = getClass().getResource("/yaml/action_w_dependencies_java_test.sl");
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()), null);

        ExecutionStep step = compilationArtifact.getExecutionPlan().getStep(2L);
        List<String> dependencies = (List<String>) step.getActionData().get(ScoreLangConstants.ACTION_DEPENDENCIES);
        assertNotNull(dependencies);

        assertTrue(dependencies.size() == 1);
        assertEquals("io.cloudslang:content.actions:1.1", dependencies.get(0));

        //Trigger ExecutionPlan
        Map<String, Serializable> userInputs = new HashMap<>();
        ScoreEvent event = trigger(compilationArtifact, userInputs, new HashSet<SystemProperty>());
        assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
    }

    @Test
    public void testCompilePythonActionWithoutDependencies() throws Exception {
        URL resource = getClass().getResource("/yaml/action_wo_dependencies_python_test.sl");
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()), null);

        ExecutionStep step = compilationArtifact.getExecutionPlan().getStep(2L);
        List<String> dependencies = (List<String>) step.getActionData().get(ScoreLangConstants.ACTION_DEPENDENCIES);
        assertNotNull(dependencies);
        assertTrue(dependencies.isEmpty());

        //Trigger ExecutionPlan
        Map<String, Serializable> userInputs = new HashMap<>();
        ScoreEvent event = trigger(compilationArtifact, userInputs, new HashSet<SystemProperty>());
        assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
    }
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

        //Trigger ExecutionPlan
        Map<String, Serializable> userInputs = new HashMap<>();
        ScoreEvent event = trigger(compilationArtifact, userInputs, new HashSet<SystemProperty>());
        assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
    }
}
