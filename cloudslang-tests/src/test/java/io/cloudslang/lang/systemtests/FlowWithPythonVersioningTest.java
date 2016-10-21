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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import static com.google.common.collect.Sets.newHashSet;
import static io.cloudslang.lang.compiler.SlangSource.fromFile;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

@Ignore("Enable when `dependencies` tag will be added")
public class FlowWithPythonVersioningTest extends SystemsTestsParent {
    @Test
    public void testFlowWithOperationIfDifferentVersions() throws Exception {
        URI flow = getClass().getResource("/yaml/versioning/py_flow.yaml").toURI();
        URI operationSum3 = getClass().getResource("/yaml/versioning/py_dependency_sum3_op.sl").toURI();
        URI operationSum5 = getClass().getResource("/yaml/versioning/py_dependency_sum5_op.sl").toURI();
        URI operationMul3 = getClass().getResource("/yaml/versioning/py_dependency_mul3_op.sl").toURI();
        URI operationMul5 = getClass().getResource("/yaml/versioning/py_dependency_mul5_op.sl").toURI();

        Set<SlangSource> dependencies = newHashSet(fromFile(operationSum3), fromFile(operationSum5),
                fromFile(operationMul3), fromFile(operationMul5));
        CompilationArtifact compilationArtifact = slang.compile(fromFile(flow), dependencies);

        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("var1", ValueFactory.create("6"));
        userInputs.put("var2", ValueFactory.create("7"));
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

    private void testOperation(String operationPath,
                               String expectedResultName, String expectedResultValue) throws URISyntaxException {
        URI operationSum3 = getClass().getResource(operationPath).toURI();
        CompilationArtifact compilationArtifact = slang.compile(fromFile(operationSum3), null);

        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("var1", ValueFactory.create("6"));
        userInputs.put("var2", ValueFactory.create("7"));
        ScoreEvent event = trigger(compilationArtifact, userInputs, new HashSet<SystemProperty>());
        assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
        LanguageEventData languageEventData = (LanguageEventData) event.getData();
        String result = (String) languageEventData.getOutputs().get(expectedResultName);
        assertEquals(expectedResultValue, result);
    }

    @Test
    public void testOperationWithParallelLoop() throws Exception {
        URI flow = getClass().getResource("/yaml/versioning/py_flow_with_loop.sl").toURI();
        URI pyDependencyMulOp = getClass().getResource("/yaml/versioning/py_dependency_mul_op.sl").toURI();

        Set<SlangSource> dependencies = newHashSet(fromFile(pyDependencyMulOp));
        CompilationArtifact compilationArtifact = slang.compile(fromFile(flow), dependencies);

        for (int addedValue = 0; addedValue < 20; addedValue++) {
            Map<String, Value> userInputs = new HashMap<>();
            userInputs.put("addedValue", ValueFactory.create(Integer.toString(addedValue)));
            ScoreEvent event = trigger(compilationArtifact, userInputs, new HashSet<SystemProperty>());
            assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
            LanguageEventData languageEventData = (LanguageEventData) event.getData();

            String actualResult = (String) languageEventData.getOutputs().get("muls_result");
            System.out.println("For addedValue [" + addedValue + "] got result[" + actualResult + "]");
            assertNotNull("expected result 'muls_result' was not found", actualResult);
            assertTrue("Not found 'build([0*" + addedValue + "]):version(7.5-extream)'",
                    actualResult.contains("build([0*" + addedValue + "]):version(7.5-extream)"));
            assertTrue("Not found 'build([1*" + addedValue + "]):version(7.5-extream)'",
                    actualResult.contains("build([1*" + addedValue + "]):version(7.5-extream)"));
            assertTrue("Not found 'build([2*" + addedValue + "]):version(7.5-extream)'",
                    actualResult.contains("build([2*" + addedValue + "]):version(7.5-extream)"));
            assertTrue("Not found 'build([3*" + addedValue + "]):version(7.5-extream)'",
                    actualResult.contains("build([3*" + addedValue + "]):version(7.5-extream)"));
            assertTrue("Not found 'build([4*" + addedValue + "]):version(7.5-extream)'",
                    actualResult.contains("build([4*" + addedValue + "]):version(7.5-extream)"));
            assertTrue("Not found 'build([5*" + addedValue + "]):version(7.5-extream)'",
                    actualResult.contains("build([5*" + addedValue + "]):version(7.5-extream)"));
            assertTrue("Not found 'build([6*" + addedValue + "]):version(7.5-extream)'",
                    actualResult.contains("build([6*" + addedValue + "]):version(7.5-extream)"));
            assertTrue("Not found 'build([7*" + addedValue + "]):version(7.5-extream)'",
                    actualResult.contains("build([7*" + addedValue + "]):version(7.5-extream)"));
            assertTrue("Not found 'build([8*" + addedValue + "]):version(7.5-extream)'",
                    actualResult.contains("build([8*" + addedValue + "]):version(7.5-extream)"));
            assertTrue("Not found 'build([9*" + addedValue + "]):version(7.5-extream)'",
                    actualResult.contains("build([9*" + addedValue + "]):version(7.5-extream)"));
            assertTrue("Not found 'build([10*" + addedValue + "]):version(7.5-extream)'",
                    actualResult.contains("build([10*" + addedValue + "]):version(7.5-extream)"));
            assertTrue("Not found 'build([11*" + addedValue + "]):version(7.5-extream)'",
                    actualResult.contains("build([11*" + addedValue + "]):version(7.5-extream)"));
            assertTrue("Not found 'build([12*" + addedValue + "]):version(7.5-extream)'",
                    actualResult.contains("build([12*" + addedValue + "]):version(7.5-extream)"));
            assertTrue("Not found 'build([13*" + addedValue + "]):version(7.5-extream)'",
                    actualResult.contains("build([13*" + addedValue + "]):version(7.5-extream)"));
            assertTrue("Not found 'build([14*" + addedValue + "]):version(7.5-extream)'",
                    actualResult.contains("build([14*" + addedValue + "]):version(7.5-extream)"));
            assertTrue("Not found 'build([15*" + addedValue + "]):version(7.5-extream)'",
                    actualResult.contains("build([15*" + addedValue + "]):version(7.5-extream)"));
            assertTrue("Not found 'build([16*" + addedValue + "]):version(7.5-extream)'",
                    actualResult.contains("build([16*" + addedValue + "]):version(7.5-extream)"));
            assertTrue("Not found 'build([17*" + addedValue + "]):version(7.5-extream)'",
                    actualResult.contains("build([17*" + addedValue + "]):version(7.5-extream)"));
            assertTrue("Not found 'build([18*" + addedValue + "]):version(7.5-extream)'",
                    actualResult.contains("build([18*" + addedValue + "]):version(7.5-extream)"));
            assertTrue("Not found 'build([19*" + addedValue + "]):version(7.5-extream)'",
                    actualResult.contains("build([19*" + addedValue + "]):version(7.5-extream)"));
        }
    }
}

