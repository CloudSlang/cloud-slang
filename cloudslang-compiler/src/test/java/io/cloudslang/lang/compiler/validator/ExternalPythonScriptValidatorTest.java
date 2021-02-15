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

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static io.cloudslang.utils.PythonScriptGeneratorUtils.generateScript;
import static org.junit.Assert.assertThrows;


public class ExternalPythonScriptValidatorTest {

    @Test
    public void testInvalidMethodInputs() {
        List<String> inputs = Arrays.asList("in1", "pass");
        ExternalPythonScriptValidator validator = new ExternalPythonScriptValidatorImpl();
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                validator.validateExecutionMethodAndInputs(generateScript(inputs), inputs));
        Assert.assertEquals("Illegal input names: pass", exception.getMessage());
    }

    @Test
    public void testInvalidMethodInputs2() {
        List<String> inputs = Arrays.asList("in1", "in2");
        List<String> scriptInputs = Arrays.asList("in1", "invalid");
        ExternalPythonScriptValidator validator = new ExternalPythonScriptValidatorImpl();
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                validator.validateExecutionMethodAndInputs(generateScript(scriptInputs), inputs));
        Assert.assertEquals("Inputs are not defined for all execute method parameters.", exception.getMessage());
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
}
