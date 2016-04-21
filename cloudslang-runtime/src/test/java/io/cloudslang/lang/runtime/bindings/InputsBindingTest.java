/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package io.cloudslang.lang.runtime.bindings;

import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = InputsBindingTest.Config.class)
public class InputsBindingTest {

    @Autowired
    private InputsBinding inputsBinding;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testEmptyBindInputs() throws Exception {
        List<Input> inputs = Collections.emptyList();
        Map<String,Serializable> result = bindInputs(inputs);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testDefaultValue() {
		List<Input> inputs = Collections.singletonList(new Input.InputBuilder("input1", "value").build());
        Map<String,Serializable> result = bindInputs(inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("value", result.get("input1"));
    }

    @Test
    public void testDefaultValueInt(){
        List<Input> inputs = Collections.singletonList(new Input.InputBuilder("input1", 2).build());
        Map<String,Serializable> result = bindInputs(inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(2, result.get("input1"));
    }

	@Test
	public void testDefaultValueBoolean() {
		List<Input> inputs = Arrays.asList(
                new Input.InputBuilder("input1", true).build(),
                new Input.InputBuilder("input2", false).build(),
                new Input.InputBuilder("input3", "${ str('phrase containing true and false') }").build()
        );
		Map<String, Serializable> result = bindInputs(inputs);
		Assert.assertTrue((boolean) result.get("input1"));
		Assert.assertFalse((boolean) result.get("input2"));
		Assert.assertEquals("phrase containing true and false", result.get("input3"));
	}

    @Test
    public void testTwoInputs() {
		List<Input> inputs = Arrays.asList(
                new Input.InputBuilder("input2", "yyy").build(),
                new Input.InputBuilder("input1", "zzz").build()
        );
        Map<String,Serializable> result = bindInputs(inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("zzz", result.get("input1"));
        Assert.assertTrue(result.containsKey("input2"));
        Assert.assertEquals("yyy", result.get("input2"));
    }

    @Test
    public void testAssignFromInput() {
        Input input1 = new Input.InputBuilder("input1", "${ input1 }")
                .withEncrypted(false)
                .withRequired(false)
                .withPrivateInput(true)
                .build();
        Input input2 = new Input.InputBuilder("input2", "${ input1 }")
                .withEncrypted(false)
                .withRequired(false)
                .withPrivateInput(true)
                .build();
        List<Input> inputs = Arrays.asList(input1, input2);
        Map<String,Serializable> result = bindInputs(inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(null, result.get("input1"));
        Assert.assertTrue(result.containsKey("input2"));
        Assert.assertEquals(null, result.get("input2"));
    }

    @Test
    public void testInputRef() {
        Map<String,Serializable> context = new HashMap<>();
        context.put("inputX","xxx");
        List<Input> inputs = Collections.singletonList(new Input.InputBuilder("input1", "${ str(inputX) }").build());
        Map<String,Serializable> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("xxx", result.get("input1"));

        Assert.assertEquals(1,context.size());
    }

    @Test
    public void testInputScriptEval() {
        Map<String,Serializable> context = new HashMap<>();
        context.put("valX",5);
        Input scriptInput = new Input.InputBuilder("input1","${ 3 + valX }").build();
        List<Input> inputs = Collections.singletonList(scriptInput);
        Map<String,Serializable> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(8, result.get("input1"));

        Assert.assertEquals(1,context.size());
    }

    @Test
    public void testInputScriptEval2() {
        Map<String,Serializable> context = new HashMap<>();
        context.put("valB","b");
        context.put("valC","c");
        Input scriptInput = new Input.InputBuilder("input1","${ 'a' + valB + valC }").build();
        List<Input> inputs = Collections.singletonList(scriptInput);
        Map<String,Serializable> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("abc", result.get("input1"));
    }

    @Test
    public void testDefaultValueVsEmptyRef() {
        Map<String,Serializable> context = new HashMap<>();

		Input refInput = new Input.InputBuilder("input1", "${ str('val') }").build();
        List<Input> inputs = Collections.singletonList(refInput);

        Map<String,Serializable> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("val", result.get("input1"));

        Assert.assertTrue(context.isEmpty());
    }

    @Test
    public void testAssignFromAndExpr() {
        Map<String,Serializable> context = new HashMap<>();
        context.put("input1",3);
		Input input = new Input.InputBuilder("input1", "${ 5+7 }").build();
        List<Input> inputs = Collections.singletonList(input);

        Map<String,Serializable> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(3, result.get("input1"));

        Assert.assertEquals(1,context.size());
        Assert.assertEquals(3,context.get("input1"));
    }

    @Test
    public void testAssignFromAndConst() {
        Map<String,Serializable> context = new HashMap<>();
        context.put("input1",3);
		Input input = new Input.InputBuilder("input1", 5).build();
        List<Input> inputs = Collections.singletonList(input);

        Map<String,Serializable> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(3, result.get("input1"));
    }

    @Test
    public void testComplexExpr(){
        Map<String,Serializable> context = new HashMap<>();
        context.put("input1",3);
		Input input = new Input.InputBuilder("input2", "${ input1 + 3 * 2 }").build();
        List<Input> inputs = Collections.singletonList(input);

        Map<String,Serializable> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input2"));
        Assert.assertEquals(9, result.get("input2"));
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testAssignFromVsRef(){
        Map<String,Serializable> context = new HashMap<>();
        context.put("input2",3);
        context.put("input1",5);
		Input input = new Input.InputBuilder("input1", "${ input2 }").build();
        List<Input> inputs = Collections.singletonList(input);

        Map<String,Serializable> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(5, result.get("input1"));
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testOverrideAssignFrom(){
        Map<String,Serializable> context = new HashMap<>();
        context.put("input2",3);
        context.put("input1",5);
        Input input = new Input.InputBuilder("input1", "${ input2 }")
                .withEncrypted(false)
                .withRequired(false)
                .withPrivateInput(true)
                .build();
        List<Input> inputs = Collections.singletonList(input);

        Map<String,Serializable> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(3, result.get("input1"));
        Assert.assertEquals(1, result.size());

        Assert.assertEquals(2, context.size());
    }

    @Test
    public void testOverrideAssignFrom2(){
        Map<String,Serializable> context = new HashMap<>();
        context.put("input1", 5);
        Input input = new Input.InputBuilder("input1", 3)
                .withEncrypted(false)
                .withRequired(false)
                .withPrivateInput(true)
                .build();
        List<Input> inputs = Collections.singletonList(input);

        Map<String,Serializable> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(3, result.get("input1"));
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testOverrideAssignFrom3() {
        Map<String,Serializable> context = new HashMap<>();
        context.put("input1",5);
        Input input = new Input.InputBuilder("input1", null)
                .withEncrypted(false)
                .withRequired(false)
                .withPrivateInput(true)
                .build();
        List<Input> inputs = Collections.singletonList(input);

        Map<String,Serializable> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("'not private' disables the assignFrom func...",null, result.get("input1"));
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testOverrideFalse() {
        Map<String,Serializable> context = new HashMap<>();
        context.put("input1",5);
		Input input = new Input.InputBuilder("input1", 6).build();
        List<Input> inputs = Collections.singletonList(input);

        Map<String,Serializable> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(5, result.get("input1"));
        Assert.assertEquals(1, result.size());
    }

    @Test(expected = RuntimeException.class)
    public void testExpressionWithWrongRef() {
        Map<String,Serializable> context = new HashMap<>();

        Input input = new Input.InputBuilder("input1", "${ input2 }")
                .withEncrypted(false)
                .withRequired(false)
                .withPrivateInput(true)
                .build();
        List<Input> inputs = Arrays.asList(input);

        bindInputs(inputs, context);
    }

    @Test
    public void testInputAssignFromAnotherInput() {
        Map<String,Serializable> context = new HashMap<>();

		Input input1 = new Input.InputBuilder("input1", 5).build();
        Input input2 = new Input.InputBuilder("input2","${ input1 }").build();
        List<Input> inputs = Arrays.asList(input1,input2);

        Map<String,Serializable> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(5, result.get("input1"));
        Assert.assertTrue(result.containsKey("input2"));
        Assert.assertEquals(5, result.get("input2"));
        Assert.assertEquals(2, result.size());

        Assert.assertTrue("orig context should not change",context.isEmpty());
    }

    @Test
    public void testComplexExpressionInput() {
        Map<String,Serializable> context = new HashMap<>();
        context.put("varX",5);

		Input input1 = new Input.InputBuilder("input1", 5).build();
        Input input2 = new Input.InputBuilder("input2","${ input1 + 5 + varX }").build();
        List<Input> inputs = Arrays.asList(input1,input2);

        Map<String,Serializable> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(5, result.get("input1"));
        Assert.assertTrue(result.containsKey("input2"));
        Assert.assertEquals(15, result.get("input2"));
        Assert.assertEquals(2, result.size());

        Assert.assertEquals("orig context should not change",1,context.size());
    }

    @Test
    public void testComplexExpression2Input() {
        Map<String,Serializable> context = new HashMap<>();
        context.put("varX","roles");

        Input input1 = new Input.InputBuilder("input1", "${ 'mighty' + ' max '   + varX }").build();
        List<Input> inputs = Collections.singletonList(input1);

        Map<String,Serializable> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("mighty max roles", result.get("input1"));
        Assert.assertEquals(1, result.size());

        Assert.assertEquals("orig context should not change",1,context.size());
    }

	private Map<String, Serializable> bindInputs(List<Input> inputs, Map<String, ? extends Serializable> context, Set<SystemProperty> systemProperties) {
		return inputsBinding.bindInputs(inputs, context, systemProperties);
	}

	private Map<String, Serializable> bindInputs(List<Input> inputs, Map<String, ? extends Serializable> context) {
		return bindInputs(inputs, context, null);
	}

	private Map<String, Serializable> bindInputs(List<Input> inputs) {
		return bindInputs(inputs, new HashMap<String, Serializable>());
	}

    @Configuration
    static class Config{

        @Bean
        public InputsBinding inputsBinding(){
            return new InputsBinding();
        }

        @Bean
        public ScriptEvaluator scriptEvaluator(){
            return new ScriptEvaluator();
        }

        @Bean
        public PythonInterpreter evalInterpreter(){
            return new PythonInterpreter();
        }

    }
}
