package io.cloudslang.lang.compiler.modeller.transformers;

/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/


import org.junit.Assert;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import io.cloudslang.lang.compiler.parser.YamlParser;
import io.cloudslang.lang.entities.bindings.Input;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = InputsTransformerTest.Config.class)
public class InputsTransformerTest {

    @Autowired
    private InputsTransformer inputTransformer;

    @Autowired
    private YamlParser yamlParser;

    private List inputsMap;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void init() throws URISyntaxException {
        inputsMap = getInputsFormSl("/operation_with_data.sl");
    }

    private List getInputsFormSl(String filePath) throws URISyntaxException {
        URL resource = getClass().getResource(filePath);
        ParsedSlang file = yamlParser.parse(SlangSource.fromFile(new File(resource.toURI())));
        Map op = file.getOperation();
        return (List) op.get("inputs");
    }

    @Test
    public void testTransform() throws Exception {
        @SuppressWarnings("unchecked") List<Input> inputs = inputTransformer.transform(inputsMap);
        Assert.assertFalse(inputs.isEmpty());
    }

    @Test
    public void testSimpleRefInput() throws Exception {
        @SuppressWarnings("unchecked") List<Input> inputs = inputTransformer.transform(inputsMap);
        Input input = inputs.get(0);
        Assert.assertEquals("input1", input.getName());
        Assert.assertNull(null, input.getExpression());
    }

    @Test
    public void testExplicitRefInput() throws Exception {
        @SuppressWarnings("unchecked") List<Input> inputs = inputTransformer.transform(inputsMap);
        Input input = inputs.get(1);
        Assert.assertEquals("input2", input.getName());
        Assert.assertEquals("input2", input.getExpression());
    }

    @Test
    public void testDefaultValueInput() throws Exception {
        @SuppressWarnings("unchecked") List<Input> inputs = inputTransformer.transform(inputsMap);
        Input input = inputs.get(2);
        Assert.assertEquals("input3", input.getName());
        Assert.assertEquals("str('value3')", input.getExpression());
    }

    @Test
    public void testInlineExprInput() throws Exception {
        @SuppressWarnings("unchecked") List<Input> inputs = inputTransformer.transform(inputsMap);
        Input input = inputs.get(3);
        Assert.assertEquals("input4", input.getName());
        Assert.assertEquals("'value4' if input3 == value3 else None", input.getExpression());
    }

    @Test
    public void testReqEncInput() throws Exception {
        @SuppressWarnings("unchecked") List<Input> inputs = inputTransformer.transform(inputsMap);
        Input input = inputs.get(4);
        Assert.assertEquals("input5", input.getName());
        Assert.assertEquals(null, input.getExpression());
        Assert.assertEquals(true, input.isEncrypted());
        Assert.assertEquals(true, input.isRequired());
    }

    @Test
    public void testDefaultExprReqInput() throws Exception {
        @SuppressWarnings("unchecked") List<Input> inputs = inputTransformer.transform(inputsMap);
        Input input = inputs.get(5);
        Assert.assertEquals("input6", input.getName());
        Assert.assertEquals("1 + 5", input.getExpression());
        Assert.assertEquals(false, input.isEncrypted());
        Assert.assertEquals(false, input.isRequired());
    }

    @Test
    public void testInlineConstInput() throws Exception {
        @SuppressWarnings("unchecked") List<Input> inputs = inputTransformer.transform(inputsMap);
        Input input = inputs.get(6);
        Assert.assertEquals("input7", input.getName());
        Assert.assertEquals("77", input.getExpression());
        Assert.assertEquals(false, input.isEncrypted());
        Assert.assertEquals(true, input.isRequired());
    }

    @Test
    public void testDefaultExprRefInput() throws Exception {
        @SuppressWarnings("unchecked") List<Input> inputs = inputTransformer.transform(inputsMap);
        Input input = inputs.get(7);
        Assert.assertEquals("input8", input.getName());
        Assert.assertEquals("input6", input.getExpression());
        Assert.assertEquals(false, input.isEncrypted());
        Assert.assertEquals(true, input.isRequired());
    }

    @Test
    public void testOverrideInput() throws Exception {
        @SuppressWarnings("unchecked") List<Input> inputs = inputTransformer.transform(inputsMap);
        Input input = inputs.get(8);
        Assert.assertEquals("input9", input.getName());
        Assert.assertEquals("input6", input.getExpression());
        Assert.assertFalse(input.isOverridable());
        Assert.assertFalse(input.isEncrypted());
        Assert.assertTrue(input.isRequired());
    }

    @Test
    public void testOverridableInputWithoutDefault() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("overridable");
        exception.expectMessage("default");
        exception.expectMessage("input_without_default");
        List inputs = getInputsFormSl("/non_overridable_input_without_default.sl");
        inputTransformer.transform(inputs);
    }

    @Test
    public void testOverridableInputWithoutDefaultButWithSysProp() throws Exception {
        List inputs = getInputsFormSl("/non_overridable_input_with_sys_prop.sl");
        List<Input> transformed_inputs = inputTransformer.transform(inputs);
        Assert.assertEquals("booya", transformed_inputs.get(0).getSystemPropertyName());
    }

    @Test
    public void testIllegalKeyInInput() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("known property");
        exception.expectMessage("input_with_illegal_key");
        exception.expectMessage("karambula");
        List inputs = getInputsFormSl("/illegal_key_in_input.sl");
        inputTransformer.transform(inputs);
    }

    @Test
    public void testLeadingSpaces() throws Exception {
        @SuppressWarnings("unchecked") List<Input> inputs = inputTransformer.transform(inputsMap);
        Input input = inputs.get(9);
        Assert.assertEquals("input10", input.getName());
        Assert.assertEquals("input5", input.getExpression());
    }

    @Test
     public void testLeadingAndTrailingSpaces() throws Exception {
        @SuppressWarnings("unchecked") List<Input> inputs = inputTransformer.transform(inputsMap);
        Input input = inputs.get(10);
        Assert.assertEquals("input11", input.getName());
        Assert.assertEquals("5 + 6", input.getExpression());
    }

    @Test
    public void testLeadingAndTrailingSpacesComplex() throws Exception {
        @SuppressWarnings("unchecked") List<Input> inputs = inputTransformer.transform(inputsMap);
        Input input = inputs.get(11);
        Assert.assertEquals("input12", input.getName());
        Assert.assertEquals("\"mighty\" + \" max\"   + varX", input.getExpression());
    }

    @Configuration
    public static class Config {

        @Bean
        public Yaml yaml() {
            Yaml yaml = new Yaml();
            yaml.setBeanAccess(BeanAccess.FIELD);
            return yaml;
        }

        @Bean
        public YamlParser yamlParser() {
            return new YamlParser();
        }

        @Bean
        public InputsTransformer inputTransformer() {
            return new InputsTransformer();
        }

    }
}