/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
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
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Result;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Bonczidai Levente
 * @since 8/10/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CompileResultsTest {
    private List<Result> expectedOperationResults;
    private List<Result> expectedFlowResults;

    public CompileResultsTest() {
        expectedOperationResults = new ArrayList<>();
        expectedFlowResults = new ArrayList<>();
        Result success = new Result(ScoreLangConstants.SUCCESS_RESULT, null);
        Result failure = new Result(ScoreLangConstants.FAILURE_RESULT, null);
        expectedOperationResults.add(success);
        expectedFlowResults.add(success);
        expectedFlowResults.add(failure);
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private SlangCompiler compiler;

    @Test
    public void testOpDefaultResults() throws Exception {
        testResults("/results/op_1.sl", expectedOperationResults);
    }

    @Test
    public void testFlowDefaultResults() throws Exception {
        testResults("/results/flow_1.sl", expectedFlowResults);
    }

    @Test
    public void testOpDefaultResultNotLastPosition() throws Exception {
        expectMessage("Flow: 'op_2' syntax is illegal. Error compiling result: 'CUSTOM_2'." +
                " Default result should be on last position.");
        preCompileExecutable("/results/op_2.sl");
    }

    @Test
    public void testOpMissingDefaultResult() throws Exception {
        expectMessage("Flow: 'op_3' syntax is illegal. Error compiling result: 'CUSTOM_3'." +
                " Last result should be default result.");
        preCompileExecutable("/results/op_3.sl");
    }

    @Test
    public void testOpWrongResultValueBooleanFalse() throws Exception {
        expectMessage("Flow: 'op_4' syntax is illegal. Error compiling result: 'CUSTOM_2'." +
                " Value supports only expression or boolean true values.");
        preCompileExecutable("/results/op_4.sl");
    }

    @Test
    public void testDecisionWrongResultValueInteger() throws Exception {
        expectMessage("Flow: 'decision_1' syntax is illegal. Error compiling result: 'LESS_THAN'." +
                " Value supports only expression or boolean true values.");
        preCompileExecutable("/results/decision_1.sl");
    }


    @Test
    public void testDecisionMultipleDefaultResults() throws Exception {
        expectMessage("Flow: 'decision_1' syntax is illegal. Error compiling result: 'LESS_THAN'." +
                " Default result should be on last position.");
        preCompileExecutable("/results/decision_2.sl");
    }

    private void testResults(String source, List<Result> expectedResults) throws Exception {
        Executable executable = preCompileExecutable(source);
        Assert.assertNotNull(executable);
        List<Result> actualResults = executable.getResults();
        Assert.assertEquals(expectedResults, actualResults);
    }

    private Executable preCompileExecutable(String source) throws Exception {
        URL sourceUri = getClass().getResource(source);
        return compiler.preCompile(SlangSource.fromFile(sourceUri.toURI()));
    }

    private void expectMessage(String message) {
        exception.expect(RuntimeException.class);
        exception.expectMessage(message);
    }

}
