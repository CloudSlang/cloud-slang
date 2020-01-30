/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.validator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;

import static io.cloudslang.lang.compiler.PythonScriptGeneratorUtils.generateScript;

public class ExternalPythonScriptValidatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();


    @Test
    public void testInvalidMethodInputs() {
        List<String> inputs = Arrays.asList("in1", "pass");
        ExternalPythonScriptValidator validator = new ExternalPythonScriptValidatorImpl();
        expectException("Illegal input names: pass");
        validator.validateExecutionMethodAndInputs(generateScript(inputs), inputs);
    }

    @Test
    public void testInvalidMethodInputs2() {
        List<String> inputs = Arrays.asList("in1", "in2");
        List<String> scriptInputs = Arrays.asList("in1", "invalid");
        ExternalPythonScriptValidator validator = new ExternalPythonScriptValidatorImpl();
        expectException("Inputs are not defined for all execute method parameters.");
        validator.validateExecutionMethodAndInputs(generateScript(scriptInputs), inputs);
    }

    @Test
    public void testValidMethodInputs() {
        List<String> inputs = Arrays.asList("in1", "in2");
        ExternalPythonScriptValidator validator = new ExternalPythonScriptValidatorImpl();
        validator.validateExecutionMethodAndInputs(generateScript(inputs), inputs);
    }

    @Test
    public void testValidMethodInputs2() {
        List<String> inputs = Arrays.asList("in1", "in2", "in3");
        List<String> scriptInputs = Arrays.asList("in1", "in2");
        ExternalPythonScriptValidator validator = new ExternalPythonScriptValidatorImpl();
        validator.validateExecutionMethodAndInputs(generateScript(scriptInputs), inputs);
    }

    private void expectException(String message) {
        exception.expect(RuntimeException.class);
        exception.expectMessage(message);
    }

}
