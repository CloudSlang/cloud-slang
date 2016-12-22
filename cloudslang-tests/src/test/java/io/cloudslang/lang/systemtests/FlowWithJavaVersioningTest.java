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
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.cloudslang.lang.compiler.SlangSource.fromFile;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

public class FlowWithJavaVersioningTest extends SystemsTestsParent {

    @Test
    public void testFlowWithOperationIfDifferentVersions() throws Exception {
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
                fromFile(operation11), fromFile(operation12), fromFile(operation13),
                fromFile(operation21), fromFile(operation22), fromFile(operation23),
                fromFile(operation31), fromFile(operation32), fromFile(operation33));
        CompilationArtifact compilationArtifact = slang.compile(fromFile(flow), dependencies);

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
        testOperation("/yaml/versioning/javaOneAnother11.sl", "The version is One 1 and [The version is Another 1]");
    }

    @Test
    public void testOneAnother12() throws Exception {
        testOperation("/yaml/versioning/javaOneAnother12.sl", "The version is One 1 and [The version is Another 2]");
    }

    @Test
    public void testOneAnother13() throws Exception {
        testOperation("/yaml/versioning/javaOneAnother13.sl", "The version is One 1 and [The version is Another 3]");
    }

    @Test
    public void testOneAnother21() throws Exception {
        testOperation("/yaml/versioning/javaOneAnother21.sl", "The version is One 2 and [The version is Another 1]");
    }

    @Test
    public void testOneAnother22() throws Exception {
        testOperation("/yaml/versioning/javaOneAnother22.sl", "The version is One 2 and [The version is Another 2]");
    }

    @Test
    public void testOneAnother23() throws Exception {
        testOperation("/yaml/versioning/javaOneAnother23.sl", "The version is One 2 and [The version is Another 3]");
    }

    @Test
    public void testOneAnother31() throws Exception {
        testOperation("/yaml/versioning/javaOneAnother31.sl", "The version is One 3 and [The version is Another 1]");
    }

    @Test
    public void testOneAnother32() throws Exception {
        testOperation("/yaml/versioning/javaOneAnother32.sl", "The version is One 3 and [The version is Another 2]");
    }

    @Test
    public void testOneAnother33() throws Exception {
        testOperation("/yaml/versioning/javaOneAnother33.sl", "The version is One 3 and [The version is Another 3]");
    }

    @Test
    public void testMultOfSumOpWithParameters() throws Exception {
        URI operationSum3 = getClass().getResource("/yaml/versioning/math/javaMulOfSum.sl").toURI();
        CompilationArtifact compilationArtifact = slang.compile(fromFile(operationSum3), null);

        HashMap<String, Value> userInputs = new HashMap<>();
        userInputs.put("var1", ValueFactory.create("4"));
        userInputs.put("var2", ValueFactory.create("7"));

        ScoreEvent event = trigger(compilationArtifact, userInputs, new HashSet<SystemProperty>());
        assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
        LanguageEventData languageEventData = (LanguageEventData) event.getData();
        String result = (String) languageEventData.getOutputs().get("result");
        assertEquals("121", result);
    }

    @Test
    public void testSumOfMulOpWithParameters() throws Exception {
        URI operationSum3 = getClass().getResource("/yaml/versioning/math/javaSumOfMul.sl").toURI();
        CompilationArtifact compilationArtifact = slang.compile(fromFile(operationSum3), null);

        HashMap<String, Value> userInputs = new HashMap<>();
        userInputs.put("var1", ValueFactory.create("3"));
        userInputs.put("var2", ValueFactory.create("4"));

        ScoreEvent event = trigger(compilationArtifact, userInputs, new HashSet<SystemProperty>());
        assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
        LanguageEventData languageEventData = (LanguageEventData) event.getData();
        String result = (String) languageEventData.getOutputs().get("result");
        assertEquals("24", result);
    }

    @Test
    public void testFlowWithGlobalSession() throws Exception {
        URI resource = getClass()
                .getResource("/yaml/versioning/testglobals/flow_using_global_session_dependencies.sl").toURI();
        URI operation1 = getClass()
                .getResource("/yaml/versioning/testglobals/set_global_session_object_dependencies.sl").toURI();
        URI operation2 = getClass()
                .getResource("/yaml/versioning/testglobals/get_global_session_object_dependencies.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(fromFile(operation1), fromFile(operation2));
        CompilationArtifact compilationArtifact = slang.compile(fromFile(resource), path);

        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("object_value", ValueFactory.create("SessionValue"));
        ScoreEvent event = trigger(compilationArtifact, userInputs, Collections.<SystemProperty>emptySet());
        Assert.assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
        LanguageEventData data = (LanguageEventData) event.getData();
        Serializable resultObjectValue = data.getOutputs().get("result_object_value");
        assertEquals("SessionValue", resultObjectValue);
    }

    private void testOperation(String operationPath, String expectedResultValue) throws URISyntaxException {
        URI operationSum3 = getClass().getResource(operationPath).toURI();
        CompilationArtifact compilationArtifact = slang.compile(fromFile(operationSum3), null);

        ScoreEvent event = trigger(compilationArtifact, new HashMap<String, Value>(), new HashSet<SystemProperty>());
        assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
        LanguageEventData languageEventData = (LanguageEventData) event.getData();
        String result = (String) languageEventData.getOutputs().get("version");
        assertEquals(expectedResultValue, result);
    }

    @Test
    public void testOperationWithParallelLoop() throws Exception {
        URI flow = getClass().getResource("/yaml/versioning/java_flow_with_loop.sl").toURI();
        URI pyDependencyMulOp = getClass().getResource("/yaml/versioning/javaMulOfSum.sl").toURI();

        Set<SlangSource> dependencies = Sets.newHashSet(fromFile(pyDependencyMulOp));
        final CompilationArtifact compilationArtifact = slang.compile(fromFile(flow), dependencies);

        for (int iteration = 0; iteration < 20; iteration++) {
            String addedValue = String.valueOf(iteration);
            Integer sumOfMulSum = 0;
            for (int i = 0; i < 20; i++) {
                sumOfMulSum += (i + iteration) * (i + iteration);
            }
            Map<String, Value> userInputs = new HashMap<>();
            userInputs.put("addedValue", ValueFactory.create(addedValue));
            ScoreEvent event = trigger(compilationArtifact, userInputs, new HashSet<SystemProperty>());
            assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
            LanguageEventData languageEventData = (LanguageEventData) event.getData();

            String actualResult = (String) languageEventData.getOutputs().get("sums_result");
            System.out.println("Expected [" + actualResult + "] for addedValue [" + addedValue + "]");
            assertNotNull("expected result 'muls_result' was not found", actualResult);
            assertEquals(sumOfMulSum.toString(), actualResult);
        }
    }

    @Test
    public void testClasspathIsolationSerializableSessionDataParallelLoop() throws Exception {
        URI flow = getClass().getResource("/yaml/versioning/flow_sdk_serilizable_session_object.sl").toURI();
        URI op = getClass().getResource("/yaml/versioning/op_sdk_serilizable_session_object.sl").toURI();
        URI noop = getClass().getResource("/yaml/noop.sl").toURI();

        Set<SlangSource> dependencies = Sets.newHashSet(fromFile(op), fromFile(noop));
        CompilationArtifact compilationArtifact = slang.compile(fromFile(flow), dependencies);
        RuntimeInformation runtimeInformation =
                triggerWithData(compilationArtifact, new HashMap<String, Value>(), new HashSet<SystemProperty>());

        // assert all the steps were invoked
        assertEquals(6, runtimeInformation.getSteps().size());
        // flow should pass without exception
    }
}
