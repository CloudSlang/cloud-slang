package io.cloudslang.lang.systemtests;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 16/05/2016.
 */

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.events.ScoreEvent;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class FlowWithPythonVersioningTest extends SystemsTestsParent {
    @Test
    public void testFlowWithOperationIfDifferentVersions() throws Exception {
        URI flow = getClass().getResource("/yaml/versioning/py_flow.yaml").toURI();
        URI operation_sum3 = getClass().getResource("/yaml/versioning/py_dependency_sum3_op.sl").toURI();
        URI operation_sum5 = getClass().getResource("/yaml/versioning/py_dependency_sum5_op.sl").toURI();
        URI operation_mul3 = getClass().getResource("/yaml/versioning/py_dependency_mul3_op.sl").toURI();
        URI operation_mul5 = getClass().getResource("/yaml/versioning/py_dependency_mul5_op.sl").toURI();

        Set<SlangSource> dependencies = Sets.newHashSet(SlangSource.fromFile(operation_sum3), SlangSource.fromFile(operation_sum5),
                SlangSource.fromFile(operation_mul3), SlangSource.fromFile(operation_mul5));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(flow), dependencies);

        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("var1", ValueFactory.create(6));
        userInputs.put("var2", ValueFactory.create(7));
        ScoreEvent event = trigger(compilationArtifact, userInputs, new HashSet<SystemProperty>());
        assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
        LanguageEventData languageEventData = (LanguageEventData) event.getData();

        String result = (String) languageEventData.getOutputs().get("result_sum3");
        assertEquals("build(13):version(3)", result);

        result = (String) languageEventData.getOutputs().get("result_sum5");
        assertEquals("build(13):version(5)", result);

        result = (String) languageEventData.getOutputs().get("result_mul3");
        assertEquals("build(42):version(3)", result);

        result = (String) languageEventData.getOutputs().get("result_mul5");
        assertEquals("build(42):version(5)", result);
    }

    @Test
    public void testOperationSum3() throws Exception {
        testOperation("/yaml/versioning/py_dependency_sum3_op.sl", "version_sum3", "build(13):version(3)");
    }

    @Test
    public void testOperationSum5() throws Exception {
        testOperation("/yaml/versioning/py_dependency_sum5_op.sl", "version_sum5", "build(13):version(5)");
    }

    @Test
    public void testOperationMul3() throws Exception {
        testOperation("/yaml/versioning/py_dependency_mul3_op.sl", "version_mul3", "build(42):version(3)");
    }

    @Test
    public void testOperationMul5() throws Exception {
        testOperation("/yaml/versioning/py_dependency_mul5_op.sl", "version_mul5", "build(42):version(5)");
    }

    private void testOperation(String operationPath, String expectedResultName, String expectedResultValue) throws URISyntaxException {
        URI operationSum3 = getClass().getResource(operationPath).toURI();
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(operationSum3), null);

        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("var1", ValueFactory.create(6));
        userInputs.put("var2", ValueFactory.create(7));
        ScoreEvent event = trigger(compilationArtifact, userInputs, new HashSet<SystemProperty>());
        assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
        LanguageEventData languageEventData = (LanguageEventData) event.getData();
        String result = (String) languageEventData.getOutputs().get(expectedResultName);
        assertEquals(expectedResultValue, result);
    }
}

