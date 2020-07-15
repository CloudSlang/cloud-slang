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
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import io.cloudslang.lang.entities.PromptType;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import io.cloudslang.lang.entities.bindings.prompt.Prompt;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

/**
 * @author Bonczidai Levente
 * @since 7/19/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PreCompileTransformersTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private SlangCompiler compiler;

    @Test
    public void testOpInvalidInput() throws Exception {
        URI resource = getClass().getResource("/corrupted/transformers/operation_input_private_no_default.sl").toURI();
        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));

        assertNotNull(result);

        Executable executable = result.getExecutable();
        assertNotNull(executable);
        List<Input> inputs = executable.getInputs();
        assertEquals(6, inputs.size());

        List<RuntimeException> errors = result.getErrors();
        assertNotNull(errors);
        assertTrue(errors.size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For operation 'operation_input_private_no_default' syntax is illegal.");
        exception.expectMessage("Input: 'input_private_no_default' is private " +
                "and required but no default value was specified");
        throw errors.get(0);
    }

    @Test
    public void testPreCompileSensitiveInputsAndOutputsWithAndWithoutDefault() throws Exception {
        URI resource = getClass().getResource("/check_weather_flow_sensitive_inputs_outputs.sl").toURI();
        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));

        assertNotNull(result);

        List<RuntimeException> errors = result.getErrors();
        assertNotNull(errors);
        assertTrue(errors.size() == 0);
        Executable executable = result.getExecutable();
        assertNotNull(executable);
        List<Input> inputs = executable.getInputs();
        assertEquals(3, inputs.size());
        assertEquals("flow_input_1", inputs.get(0).getName());
        assertEquals("defaultValue", inputs.get(0).getValue().get());
        assertEquals(false, inputs.get(0).getValue().isSensitive());
        assertEquals("flow_input_0", inputs.get(1).getName());
        assertEquals("${flow_input_1}", inputs.get(1).getValue().get());
        assertEquals(true, inputs.get(1).getValue().isSensitive());
        assertEquals("flow_input_sensitive", inputs.get(2).getName());
        assertEquals(null, inputs.get(2).getValue().get());
        assertEquals(true, inputs.get(2).getValue().isSensitive());
        List<Output> outputs = executable.getOutputs();
        assertEquals(2, outputs.size());
        assertEquals("flow_output_0", outputs.get(0).getName());
        assertEquals("${flow_input_1}", outputs.get(0).getValue().get());
        assertEquals(true, outputs.get(0).getValue().isSensitive());
        assertEquals("flow_output_1", outputs.get(1).getName());
        assertEquals("${flow_output_1}", outputs.get(1).getValue().get());
        assertEquals(true, outputs.get(1).getValue().isSensitive());
    }

    @Test
    public void testPropertiesOnStepPublishOutput() throws Exception {
        URI flow = getClass().getResource("/check_weather_flow_sensitive.sl").toURI();
        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(flow));
        List<RuntimeException> errors = result.getErrors();
        assertNotNull(errors);
        assertEquals(0, errors.size());
    }

    @Test
    public void testInputBoolean() throws Exception {
        URL resource = getClass().getResource("/corrupted/transformers/check_weather_optional_input_boolean.sl");
        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource.toURI()));

        List<RuntimeException> errors = result.getErrors();
        assertNotNull(errors);
        assertTrue(errors.size() > 0);

        exception.expect(RuntimeException.class);
        exception.expectMessage("For operation 'check_weather_optional_input_boolean' syntax is illegal.");
        exception.expectMessage("Input: 'city' should have a String value, but got value 'true' of type Boolean.");
        throw errors.get(0);
    }

    @Test
    public void testInputDefaultInteger() throws Exception {
        URL resource = getClass()
                .getResource("/corrupted/transformers/check_weather_optional_input_default_integer.sl");
        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource.toURI()));

        List<RuntimeException> errors = result.getErrors();
        assertNotNull(errors);
        assertTrue(errors.size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For operation 'check_weather_optional_input_default_integer' syntax is illegal.");
        exception.expectMessage("Input: 'input_with_default_value' should have a String value, but got value '2' " +
                "of type Integer.");
        throw errors.get(0);
    }

    @Test
    public void testStepInputDefaultDouble() throws Exception {
        URL resource = getClass().getResource("/corrupted/transformers/check_weather_flow_step_input_double.sl");

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource.toURI()));

        List<RuntimeException> errors = result.getErrors();
        assertNotNull(errors);
        assertTrue(errors.size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For step 'bootstrap_node' syntax is illegal.");
        exception.expectMessage("Step input: 'input_with_sensitive_no_default' should have a String value, " +
                "but got value '2.5' of type Double.");
        throw errors.get(0);
    }

    @Test
    public void testOutputDefaultDouble() throws Exception {
        URL resource = getClass().getResource("/corrupted/transformers/check_weather_flow_output_default_double.sl");

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource.toURI()));

        List<RuntimeException> errors = result.getErrors();
        assertNotNull(errors);
        assertTrue(errors.size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For flow 'check_weather_flow_output_default_double' syntax is illegal.");
        exception.expectMessage("Output / publish value: 'flow_output_0' should have a String value, " +
                "but got value '3.5' of type Double.");
        throw errors.get(0);
    }

    @Test
    public void testOpWrongOutput() throws Exception {
        URI resource = getClass().getResource("/corrupted/transformers/operation_output_wrong_property.sl").toURI();
        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));

        assertNotNull(result);

        Executable executable = result.getExecutable();
        assertNotNull(executable);
        List<Output> outputs = executable.getOutputs();
        assertEquals(2, outputs.size());

        List<RuntimeException> errors = result.getErrors();
        assertNotNull(errors);
        assertTrue(errors.size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For operation 'operation_output_wrong_property' syntax is illegal.");
        exception.expectMessage("Key: wrong_key in output: output_wrong_key is not a known property");
        throw errors.get(0);
    }

    @Test
    public void testOpWrongResult() throws Exception {
        URI resource = getClass().getResource("/corrupted/transformers/operation_duplicate_result.sl").toURI();
        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));

        assertNotNull(result);

        Executable executable = result.getExecutable();
        assertNotNull(executable);
        List<Result> results = executable.getResults();
        assertEquals(2, results.size());

        List<RuntimeException> errors = result.getErrors();
        assertNotNull(errors);
        assertTrue(errors.size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For operation 'operation_duplicate_result' syntax is illegal.");
        exception.expectMessage("Duplicate result found: SUCCESS");
        throw errors.get(0);
    }

    @Test
    public void testStepInputWithPrompt() throws URISyntaxException {
        URI resource = getClass().getResource("/corrupted/transformers/check_flow_input_prompt.sl").toURI();
        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));

        assertNotNull(result);

        Executable executable = result.getExecutable();
        assertNotNull(executable);

        List<Input> inputs = executable.getInputs();

        Input flowInput1 = inputs.get(0);
        Prompt prompt = flowInput1.getPrompt();
        assertEquals(prompt.getPromptType(), PromptType.TEXT);
        assertEquals(prompt.getPromptMessage(), "non-default-message");

        Input flowInput2 = inputs.get(1);
        Prompt prompt2 = flowInput2.getPrompt();
        assertEquals(prompt2.getPromptType(), PromptType.TEXT);
        assertEquals(prompt2.getPromptMessage(), "Enter a value for 'flow_input_2'");
        assertNull(prompt2.getPromptOptions());
        assertNull(prompt2.getPromptDelimiter());

        Input flowInput3 = inputs.get(2);
        Prompt prompt3 = flowInput3.getPrompt();
        assertEquals(prompt3.getPromptType(), PromptType.SINGLE_CHOICE);
        assertEquals(prompt3.getPromptOptions(), "opts");
        assertEquals(prompt3.getPromptDelimiter(), "|");

        Input flowInput4 = inputs.get(3);
        Prompt prompt4 = flowInput4.getPrompt();
        assertEquals(prompt4.getPromptType(), PromptType.MULTI_CHOICE);
        assertEquals(prompt4.getPromptOptions(), "opts");
        assertEquals(prompt4.getPromptDelimiter(), ",");
    }

}
