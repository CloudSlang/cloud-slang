/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.transformers;


import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.parser.YamlParser;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.compiler.validator.ExecutableValidatorImpl;
import io.cloudslang.lang.compiler.validator.PreCompileValidator;
import io.cloudslang.lang.compiler.validator.PreCompileValidatorImpl;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidator;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidatorImpl;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.ScriptFunction;
import io.cloudslang.lang.entities.encryption.DummyEncryptor;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InputsTransformerTest.Config.class, SlangCompilerSpringConfig.class})
public class InputsTransformerTest extends TransformersTestParent {

    @Autowired
    private InputsTransformer inputTransformer;

    @Autowired
    private YamlParser yamlParser;

    private List<Object> inputsMap;

    private List<Object> inputsMapWithFunctions;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void init() throws URISyntaxException {
        inputsMap = getInputsFormSl("/operation_with_data.sl");
        inputsMapWithFunctions = getInputsFormSl("/inputs_with_functions.sl");
    }

    private List getInputsFormSl(String filePath) throws URISyntaxException {
        URL resource = getClass().getResource(filePath);
        ParsedSlang file = yamlParser.parse(SlangSource.fromFile(new File(resource.toURI())));
        Map op = file.getOperation();
        return (List) op.get("inputs");
    }

    @Test
    public void testTransform() throws Exception {
        @SuppressWarnings("unchecked") List<Input> inputs = inputTransformer.transform(inputsMap).getTransformedData();
        Assert.assertFalse(inputs.isEmpty());
    }

    @Test
    public void testSimpleRefInput() throws Exception {
        @SuppressWarnings("unchecked") List<Input> inputs = inputTransformer.transform(inputsMap).getTransformedData();
        Input input = inputs.get(0);
        Assert.assertEquals("input1", input.getName());
        Assert.assertNull(null, input.getValue().get());
    }

    @Test
    public void testExplicitRefInput() throws Exception {
        @SuppressWarnings("unchecked") List<Input> inputs = inputTransformer.transform(inputsMap).getTransformedData();
        Input input = inputs.get(1);
        Assert.assertEquals("input2", input.getName());
        Assert.assertEquals("${ input2 }", input.getValue().get());
    }

    @Test
    public void testDefaultValueInput() throws Exception {
        @SuppressWarnings("unchecked")
        List<Input> inputs = inputTransformer.transform(inputsMap).getTransformedData();
        Input input = inputs.get(2);
        Assert.assertEquals("input3", input.getName());
        Assert.assertEquals("value3", input.getValue().get());
    }

    @Test
    public void testInlineExprInput() throws Exception {
        @SuppressWarnings("unchecked")
        List<Input> inputs = inputTransformer.transform(inputsMap).getTransformedData();
        Input input = inputs.get(3);
        Assert.assertEquals("input4", input.getName());
        Assert.assertEquals("${ 'value4' if input3 == value3 else None }", input.getValue().get());
    }

    @Test
    public void testReqEncInput() throws Exception {
        @SuppressWarnings("unchecked")
        List<Input> inputs = inputTransformer.transform(inputsMap).getTransformedData();
        Input input = inputs.get(4);
        Assert.assertEquals("input5", input.getName());
        Assert.assertEquals(null, input.getValue().get());
        Assert.assertEquals(true, input.isSensitive());
        Assert.assertEquals(true, input.isRequired());
    }

    @Test
    public void testDefaultExprReqInput() throws Exception {
        @SuppressWarnings("unchecked")
        List<Input> inputs = inputTransformer.transform(inputsMap).getTransformedData();
        Input input = inputs.get(5);
        Assert.assertEquals("input6", input.getName());
        Assert.assertEquals("${ 1 + 5 }", input.getValue().get());
        Assert.assertEquals(false, input.isSensitive());
        Assert.assertEquals(false, input.isRequired());
    }

    @Test
    public void testInlineConstInput() throws Exception {
        @SuppressWarnings("unchecked")
        List<Input> inputs = inputTransformer.transform(inputsMap).getTransformedData();
        Input input = inputs.get(6);
        Assert.assertEquals("input7", input.getName());
        Assert.assertTrue("77".equals(input.getValue().get()));
        Assert.assertEquals(false, input.isSensitive());
        Assert.assertEquals(true, input.isRequired());
    }

    @Test
    public void testDefaultExprRefInput() throws Exception {
        @SuppressWarnings("unchecked")
        List<Input> inputs = inputTransformer.transform(inputsMap).getTransformedData();
        Input input = inputs.get(7);
        Assert.assertEquals("input8", input.getName());
        Assert.assertEquals("${ input6 }", input.getValue().get());
        Assert.assertEquals(false, input.isSensitive());
        Assert.assertEquals(true, input.isRequired());
    }

    @Test
    public void testOverrideInput() throws Exception {
        @SuppressWarnings("unchecked")
        List<Input> inputs = inputTransformer.transform(inputsMap).getTransformedData();
        Input input = inputs.get(8);
        Assert.assertEquals("input9", input.getName());
        Assert.assertEquals("${ input6 }", input.getValue().get());
        Assert.assertFalse(!input.isPrivateInput());
        Assert.assertFalse(input.isSensitive());
        Assert.assertTrue(input.isRequired());
    }

    @Test
    public void testPrivateRequiredInputWithEmptyStringDefault() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("Input: 'input1' is private and required but no default value was specified");
        List inputs = getInputsFormSl("/corrupted/operation_with_data_invalid_input.sl");
        transformAndThrowFirstException(inputTransformer, inputs);
    }

    @Test
    public void testPrivateRequiredInputWithNoneDefault() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("Input: 'input1' is private and required but no default value was specified");
        List inputs = getInputsFormSl("/corrupted/operation_with_data_invalid_input.sl");
        transformAndThrowFirstException(inputTransformer, inputs);
    }

    @Test
    public void testInputPrivateNotRequiredEmptyDefaultWorks() throws Exception {
        List inputs = getInputsFormSl("/input_private_not_req_empty.sl");
        transformAndAssertNoErrorsTransformer(inputTransformer, inputs);
    }

    @Test
    public void testInputPrivateNotRequiredNullDefaultWorks() throws Exception {
        List inputs = getInputsFormSl("/input_private_not_req_null.sl");
        transformAndAssertNoErrorsTransformer(inputTransformer, inputs);
    }

    @Test
    public void testInputPrivateNotRequiredMissingDefaultWorks() throws Exception {
        List inputs = getInputsFormSl("/input_private_not_req_missing.sl");
        transformAndAssertNoErrorsTransformer(inputTransformer, inputs);
    }

    @Test
    public void testPrivateInputWithoutDefault() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("private");
        exception.expectMessage("default");
        exception.expectMessage("input_without_default");
        List inputs = getInputsFormSl("/private_input_without_default.sl");
        transformAndThrowFirstException(inputTransformer, inputs);
    }

    @Test
    public void testIllegalKeyInInput() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("known property");
        exception.expectMessage("input_with_illegal_key");
        exception.expectMessage("karambula");
        List inputs = getInputsFormSl("/illegal_key_in_input.sl");
        transformAndThrowFirstException(inputTransformer, inputs);
    }

    @Test
    public void testLeadingSpaces() throws Exception {
        @SuppressWarnings("unchecked") List<Input> inputs = inputTransformer.transform(inputsMap).getTransformedData();
        Input input = inputs.get(9);
        Assert.assertEquals("input10", input.getName());
        Assert.assertEquals("${ input5 }", input.getValue().get());
    }

    @Test
    public void testLeadingAndTrailingSpaces() throws Exception {
        @SuppressWarnings("unchecked") List<Input> inputs = inputTransformer.transform(inputsMap).getTransformedData();
        Input input = inputs.get(10);
        Assert.assertEquals("input11", input.getName());
        Assert.assertEquals("${ 5 + 6 }", input.getValue().get());
    }

    @Test
    public void testLeadingAndTrailingSpacesComplex() throws Exception {
        @SuppressWarnings("unchecked") List<Input> inputs = inputTransformer.transform(inputsMap).getTransformedData();
        Input input = inputs.get(11);
        Assert.assertEquals("input12", input.getName());
        Assert.assertEquals("${ \"mighty\" + \" max\"   + varX }", input.getValue().get());
    }

    @Test
    public void testFunctionsAndSpDependencies() throws Exception {
        @SuppressWarnings("unchecked")
        List<Input> inputs = inputTransformer.transform(inputsMapWithFunctions).getTransformedData();

        // prepare parameters
        Set<ScriptFunction> setGet = Sets.newHashSet(ScriptFunction.GET);
        Set<ScriptFunction> setSp = Sets.newHashSet(ScriptFunction.GET_SYSTEM_PROPERTY);
        Set<ScriptFunction> setGetAndSp = new HashSet<>(setGet);
        setGetAndSp.addAll(setSp);
        final Set<String> props1 = Sets.newHashSet("a.b.c.key");
        final Set<String> props2 = Sets.newHashSet("a.b.c.key", "d.e.f.key");
        Set<ScriptFunction> emptySetScriptFunction = new HashSet<>();
        Set<String> emptySetString = new HashSet<>();
        Set<ScriptFunction> setGetAndCheckEmpty = new HashSet<>(setGet);
        setGetAndCheckEmpty.add(ScriptFunction.CHECK_EMPTY);

        Assert.assertEquals("inputs size not as expected", 14, inputs.size());

        verifyFunctionsAndSpDependencies(inputs, 0, emptySetScriptFunction, emptySetString);
        verifyFunctionsAndSpDependencies(inputs, 1, emptySetScriptFunction, emptySetString);
        verifyFunctionsAndSpDependencies(inputs, 2, setGet, emptySetString);
        verifyFunctionsAndSpDependencies(inputs, 3, setSp, props1);
        verifyFunctionsAndSpDependencies(inputs, 4, setSp, props1);
        verifyFunctionsAndSpDependencies(inputs, 5, setGetAndSp, props1);
        verifyFunctionsAndSpDependencies(inputs, 6, setGetAndSp, props1);
        verifyFunctionsAndSpDependencies(inputs, 7, setGetAndSp, props2);
        verifyFunctionsAndSpDependencies(inputs, 8, setGetAndSp, props1);
        verifyFunctionsAndSpDependencies(inputs, 9, emptySetScriptFunction, emptySetString);
        verifyFunctionsAndSpDependencies(inputs, 10, setSp, props1);
        verifyFunctionsAndSpDependencies(inputs, 11, setGet, emptySetString);
        verifyFunctionsAndSpDependencies(inputs, 12, setSp, props2);
        verifyFunctionsAndSpDependencies(inputs, 13, setGetAndCheckEmpty, emptySetString);
    }

    private void verifyFunctionsAndSpDependencies(
            List<Input> inputs,
            int inputIndex,
            Set<ScriptFunction> expectedFunctions,
            Set<String> expectedSystemProperties) {
        Input input = inputs.get(inputIndex);
        Assert.assertEquals(expectedFunctions, input.getFunctionDependencies());
        Assert.assertEquals(expectedSystemProperties, input.getSystemPropertyDependencies());
    }

    @Configuration
    public static class Config {

        @Bean
        @Scope("prototype")
        public Yaml yaml() {
            Yaml yaml = new Yaml();
            yaml.setBeanAccess(BeanAccess.FIELD);
            return yaml;
        }

        @Bean
        public YamlParser yamlParser() {
            return new YamlParser() {
                @Override
                public Yaml getYaml() {
                    return yaml();
                }
            };
        }

        @Bean
        public ParserExceptionHandler parserExceptionHandler() {
            return new ParserExceptionHandler();
        }

        @Bean
        public InputsTransformer inputTransformer() {
            return new InputsTransformer();
        }

        @Bean
        public DummyEncryptor dummyEncryptor() {
            return new DummyEncryptor();
        }

        @Bean
        public PreCompileValidator preCompileValidator() {
            return new PreCompileValidatorImpl();
        }

        @Bean
        public ExecutableValidator executableValidator() {
            return new ExecutableValidatorImpl();
        }

        @Bean
        public SystemPropertyValidator systemPropertyValidator() {
            return new SystemPropertyValidatorImpl();
        }

    }
}