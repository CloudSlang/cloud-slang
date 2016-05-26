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
import org.junit.Assume;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class FlowWithJavaVersioningTest extends SystemsTestsParent {

    @Test
    public void testFlowWithOperationIfDifferentVersions() throws Exception {
        Assume.assumeTrue(shouldRunMaven);
        URI flow = getClass().getResource("/yaml/versioning/java_flow.yaml").toURI();
        URI operation11 = getClass().getResource("/yaml/versioning/javaOneAnother11.sl").toURI();
        URI operation12 = getClass().getResource("/yaml/versioning/javaOneAnother12.sl").toURI();
        URI operation13 = getClass().getResource("/yaml/versioning/javaOneAnother13.sl").toURI();
        URI operation21 = getClass().getResource("/yaml/versioning/javaOneAnother21.sl").toURI();
        URI operation22 = getClass().getResource("/yaml/versioning/javaOneAnother22.sl").toURI();
        URI operation23 = getClass().getResource("/yaml/versioning/javaOneAnother23.sl").toURI();
        URI operation31 = getClass().getResource("/yaml/versioning/javaOneAnother31.sl").toURI();
        URI operation32 = getClass().getResource("/yaml/versioning/javaOneAnother32.sl").toURI();
        URI operation33 = getClass().getResource("/yaml/versioning/javaOneAnother33.sl").toURI();

        Set<SlangSource> dependencies = Sets.newHashSet(
                SlangSource.fromFile(operation11), SlangSource.fromFile(operation12), SlangSource.fromFile(operation13),
                SlangSource.fromFile(operation21), SlangSource.fromFile(operation22), SlangSource.fromFile(operation23),
                SlangSource.fromFile(operation31), SlangSource.fromFile(operation32), SlangSource.fromFile(operation33));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(flow), dependencies);

        ScoreEvent event = trigger(compilationArtifact, new HashMap<String, Value>(), new HashSet<SystemProperty>());
        assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
        LanguageEventData languageEventData = (LanguageEventData) event.getData();

        String result = (String) languageEventData.getOutputs().get("result11");
        assertEquals("The version is One 1 and [The version is Another 1]", result);
        result = (String) languageEventData.getOutputs().get("result12");
        assertEquals("The version is One 1 and [The version is Another 2]", result);
        result = (String) languageEventData.getOutputs().get("result13");
        assertEquals("The version is One 1 and [The version is Another 3]", result);

        result = (String) languageEventData.getOutputs().get("result21");
        assertEquals("The version is One 2 and [The version is Another 1]", result);
        result = (String) languageEventData.getOutputs().get("result22");
        assertEquals("The version is One 2 and [The version is Another 2]", result);
        result = (String) languageEventData.getOutputs().get("result23");
        assertEquals("The version is One 2 and [The version is Another 3]", result);

        result = (String) languageEventData.getOutputs().get("result31");
        assertEquals("The version is One 3 and [The version is Another 1]", result);
        result = (String) languageEventData.getOutputs().get("result32");
        assertEquals("The version is One 3 and [The version is Another 2]", result);
        result = (String) languageEventData.getOutputs().get("result33");
        assertEquals("The version is One 3 and [The version is Another 3]", result);
    }

    @Test
    public void testOneAnother11() throws Exception {
        Assume.assumeTrue(shouldRunMaven);
        testOperation("/yaml/versioning/javaOneAnother11.sl", "The version is One 1 and [The version is Another 1]");
    }

    @Test
    public void testOneAnother12() throws Exception {
        Assume.assumeTrue(shouldRunMaven);
        testOperation("/yaml/versioning/javaOneAnother12.sl", "The version is One 1 and [The version is Another 2]");
    }

    @Test
    public void testOneAnother13() throws Exception {
        Assume.assumeTrue(shouldRunMaven);
        testOperation("/yaml/versioning/javaOneAnother13.sl", "The version is One 1 and [The version is Another 3]");
    }

    @Test
    public void testOneAnother21() throws Exception {
        Assume.assumeTrue(shouldRunMaven);
        testOperation("/yaml/versioning/javaOneAnother21.sl", "The version is One 2 and [The version is Another 1]");
    }

    @Test
    public void testOneAnother22() throws Exception {
        Assume.assumeTrue(shouldRunMaven);
        testOperation("/yaml/versioning/javaOneAnother22.sl", "The version is One 2 and [The version is Another 2]");
    }

    @Test
    public void testOneAnother23() throws Exception {
        Assume.assumeTrue(shouldRunMaven);
        testOperation("/yaml/versioning/javaOneAnother23.sl", "The version is One 2 and [The version is Another 3]");
    }

    @Test
    public void testOneAnother31() throws Exception {
        Assume.assumeTrue(shouldRunMaven);
        testOperation("/yaml/versioning/javaOneAnother31.sl", "The version is One 3 and [The version is Another 1]");
    }

    @Test
    public void testOneAnother32() throws Exception {
        Assume.assumeTrue(shouldRunMaven);
        testOperation("/yaml/versioning/javaOneAnother32.sl", "The version is One 3 and [The version is Another 2]");
    }

    @Test
    public void testOneAnother33() throws Exception {
        Assume.assumeTrue(shouldRunMaven);
        testOperation("/yaml/versioning/javaOneAnother33.sl", "The version is One 3 and [The version is Another 3]");
    }

    @Test
    public void testMultOfSumOpWithParameters() throws Exception {
        Assume.assumeTrue(shouldRunMaven);

        URI operationSum3 = getClass().getResource("/yaml/versioning/math/javaMulOfSum.sl").toURI();
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(operationSum3), null);

        HashMap<String, Value> userInputs = new HashMap<>();
        userInputs.put("var1", ValueFactory.create(4));
        userInputs.put("var2", ValueFactory.create(7));

        ScoreEvent event = trigger(compilationArtifact, userInputs, new HashSet<SystemProperty>());
        assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
        LanguageEventData languageEventData = (LanguageEventData) event.getData();
        Integer result = (Integer) languageEventData.getOutputs().get("result");
        assertEquals(121, result.intValue());
    }

    @Test
    public void testSumOfMulOpWithParameters() throws Exception {
        Assume.assumeTrue(shouldRunMaven);

        URI operationSum3 = getClass().getResource("/yaml/versioning/math/javaSumOfMul.sl").toURI();
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(operationSum3), null);

        HashMap<String, Value> userInputs = new HashMap<>();
        userInputs.put("var1", ValueFactory.create(3));
        userInputs.put("var2", ValueFactory.create(4));

        ScoreEvent event = trigger(compilationArtifact, userInputs, new HashSet<SystemProperty>());
        assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
        LanguageEventData languageEventData = (LanguageEventData) event.getData();
        Integer result = (Integer) languageEventData.getOutputs().get("result");
        assertEquals(24, result.intValue());
    }

    private void testOperation(String operationPath, String expectedResultValue) throws URISyntaxException {
        URI operationSum3 = getClass().getResource(operationPath).toURI();
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(operationSum3), null);

        ScoreEvent event = trigger(compilationArtifact,  new HashMap<String, Value>(), new HashSet<SystemProperty>());
        assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
        LanguageEventData languageEventData = (LanguageEventData) event.getData();
        String result = (String) languageEventData.getOutputs().get("version");
        assertEquals(expectedResultValue, result);
    }
}
