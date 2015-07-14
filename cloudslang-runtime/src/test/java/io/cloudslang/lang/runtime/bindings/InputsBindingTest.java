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

import io.cloudslang.lang.entities.bindings.Input;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = InputsBindingTest.Config.class)
public class InputsBindingTest {

    @Autowired
    private InputsBinding inputsBinding;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testEmptyBindInputs() throws Exception {
        List<Input> inputs = Arrays.asList();
        Map<String,Serializable> result = bindInputs(inputs);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testDefaultValue() {
		List<Input> inputs = Arrays.asList(new Input("input1", "str('value')"));
        Map<String,Serializable> result = bindInputs(inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("value", result.get("input1"));
    }

    @Test
    public void testDefaultValueInt(){
        List<Input> inputs = Arrays.asList(new Input("input1", "2"));
        Map<String,Serializable> result = bindInputs(inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(2, result.get("input1"));
    }

	@Test
	public void testDefaultValueBoolean() {
		List<Input> inputs = Arrays.asList(new Input("input1", "true"), new Input("input2", "false"), new Input("input3", "str('phrase cantaining true and false')"));
		Map<String, Serializable> result = bindInputs(inputs);
		Assert.assertTrue((boolean)result.get("input1"));
		Assert.assertFalse((boolean)result.get("input2"));
		Assert.assertEquals("phrase cantaining true and false", result.get("input3"));
	}

    @Test
    public void testTwoInputs() {
		List<Input> inputs = Arrays.asList(new Input("input2", "'yyy'"), new Input("input1", "'zzz'"));
        Map<String,Serializable> result = bindInputs(inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("zzz", result.get("input1"));
        Assert.assertTrue(result.containsKey("input2"));
        Assert.assertEquals("yyy", result.get("input2"));
    }

    @Test
    public void testAssignFromInput() {
        Input input1 = new Input("input1", "input1", false, false, true, null);
        Input input2 = new Input("input2", "input1", false, false, true, null);
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
        List<Input> inputs =  Arrays.asList(new Input("input1","str(inputX)"));
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
        Input scriptInput = new Input("input1","3 + valX");
        List<Input> inputs = Arrays.asList(scriptInput);
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
        Input scriptInput = new Input("input1"," 'a' + valB + valC");
        List<Input> inputs = Arrays.asList(scriptInput);
        Map<String,Serializable> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("abc", result.get("input1"));
    }

    @Test
    public void testDefaultValueVsEmptyRef() {
        Map<String,Serializable> context = new HashMap<>();

		Input refInput = new Input("input1", "str('val')");
        List<Input> inputs = Arrays.asList(refInput);

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
		Input input = new Input("input1", "5+7");
        List<Input> inputs = Arrays.asList(input);

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
		Input input = new Input("input1", "5");
        List<Input> inputs = Arrays.asList(input);

        Map<String,Serializable> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(3, result.get("input1"));
    }

    @Test
    public void testComplexExpr(){
        Map<String,Serializable> context = new HashMap<>();
        context.put("input1",3);
		Input input = new Input("input2", " input1 + 3 * 2 ");
        List<Input> inputs = Arrays.asList(input);

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
		Input input = new Input("input1", "input2");
        List<Input> inputs = Arrays.asList(input);

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
		Input input = new Input("input1", "input2", false, false, false, null);
        List<Input> inputs = Arrays.asList(input);

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
        context.put("input1",5);
		Input input = new Input("input1", "3", false, false, false, null);
        List<Input> inputs = Arrays.asList(input);

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
		Input input = new Input("input1", null, false, false, false, null);
        List<Input> inputs = Arrays.asList(input);

        Map<String,Serializable> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("overridable disables the assignFrom func...",null, result.get("input1"));
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testOverrideFalse() {
        Map<String,Serializable> context = new HashMap<>();
        context.put("input1",5);
		Input input = new Input("input1", "6");
        List<Input> inputs = Arrays.asList(input);

        Map<String,Serializable> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(5, result.get("input1"));
        Assert.assertEquals(1, result.size());
    }

    @Test(expected = RuntimeException.class)
    public void testExpressionWithWrongRef() {
        Map<String,Serializable> context = new HashMap<>();

		Input input = new Input("input1", "input2", false, false, true, null);
        List<Input> inputs = Arrays.asList(input);

        bindInputs(inputs, context);
    }

    @Test
    public void testInputAssignFromAnotherInput() {
        Map<String,Serializable> context = new HashMap<>();

		Input input1 = new Input("input1", "5");
        Input input2 = new Input("input2","input1");
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

		Input input1 = new Input("input1", "5");
        Input input2 = new Input("input2","input1 + 5 + varX");
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

		Input input1 = new Input("input1", "\"mighty\" + ' max '   + varX");
        List<Input> inputs = Arrays.asList(input1);

        Map<String,Serializable> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("mighty max roles", result.get("input1"));
        Assert.assertEquals(1, result.size());

        Assert.assertEquals("orig context should not change",1,context.size());
    }

	@Test
	public void testSystemProperty() {
		String in = "input1";
		String fqspn = "docker.sys.props.port";
		List<Input> inputs = Arrays.asList(new Input(in, null, false, true, true, fqspn));
		Map<String, Serializable> result = bindInputs(inputs, new HashMap<String, Serializable>(), Collections.singletonMap(fqspn, 22));
		Assert.assertFalse(result.isEmpty());
		Assert.assertEquals(1, result.size());
		Assert.assertTrue(result.containsKey(in));
		Assert.assertEquals(22, result.get(in));
	}

	@Test
	public void testSystemPropertyMissing() {
		String in = "input1";
		String fqspn = "docker.sys.props.port";
		List<Input> inputs = Arrays.asList(new Input(in, null, false, false, true, fqspn));
		Map<String, Serializable> result = bindInputs(inputs, new HashMap<String, Serializable>());
		Assert.assertFalse(result.isEmpty());
		Assert.assertEquals(1, result.size());
		Assert.assertTrue(result.containsKey(in));
		Assert.assertEquals(null, result.get(in));
	}

    @Test
    public void testRequiredSystemPropertyMissing() throws Exception{
        String in = "input1";
        String fqspn = "docker.sys.props.port";

        exception.expect(RuntimeException.class);
        exception.expectMessage("Input with name:");
        exception.expectMessage("This value can also be supplied using a system property");

        List<Input> inputs = Arrays.asList(new Input(in, null, false, true, true, fqspn));
        Map<String, Serializable> result = bindInputs(inputs, new HashMap<String, Serializable>());
    }

	@Test
	public void testSystemPropertyContext() {
		String in = "input1";
		String fqspn = "docker.sys.props.port";
		List<Input> inputs = Arrays.asList(new Input(in, null, false, true, true, fqspn));
		Map<String, Serializable> result = bindInputs(inputs, Collections.singletonMap(in, 23), Collections.singletonMap(fqspn, 22));
		Assert.assertFalse(result.isEmpty());
		Assert.assertEquals(1, result.size());
		Assert.assertTrue(result.containsKey(in));
		Assert.assertEquals(23, result.get(in));
	}

	@Test
	public void testSystemPropertyOverride() {
		String in = "input1";
		String fqspn = "docker.sys.props.port";
		List<Input> inputs = Arrays.asList(new Input(in, null, false, true, false, fqspn));
		Map<String, Serializable> result = bindInputs(inputs, Collections.singletonMap(in, 23), Collections.singletonMap(fqspn, 22));
		Assert.assertFalse(result.isEmpty());
		Assert.assertEquals(1, result.size());
		Assert.assertTrue(result.containsKey(in));
		Assert.assertEquals(22, result.get(in));
	}

	private Map<String, Serializable> bindInputs(List<Input> inputs, Map<String, ? extends Serializable> context, Map<String, ? extends Serializable> systemProperties) {
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
        public ScriptEngine scriptEngine(){
            return  new ScriptEngineManager().getEngineByName("python");
        }
    }
}