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
import io.cloudslang.lang.entities.bindings.Argument;
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
@ContextConfiguration(classes = ArgumentsBindingTest.Config.class)
public class ArgumentsBindingTest {

    @SuppressWarnings("unchecked")
    private static final Set<SystemProperty> EMPTY_SET = Collections.EMPTY_SET;

    @Autowired
    private ArgumentsBinding argumentsBinding;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testEmptyBindArguments() throws Exception {
        List<Argument> arguments = Collections.emptyList();
        Map<String,Serializable> result = bindArguments(arguments);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testDefaultValueNoExpression() {
		List<Argument> arguments = Collections.singletonList(new Argument("argument1", "value"));
        Map<String,Serializable> result = bindArguments(arguments);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals("value", result.get("argument1"));
    }

    @Test
    public void testDefaultValueExpression() {
        List<Argument> arguments = Collections.singletonList(new Argument("argument1", "${ 'value' }"));
        Map<String,Serializable> result = bindArguments(arguments);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals("value", result.get("argument1"));
    }

    @Test
    public void testDefaultValueInt() {
        List<Argument> arguments = Collections.singletonList(new Argument("argument1", 2));
        Map<String,Serializable> result = bindArguments(arguments);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals(2, result.get("argument1"));
    }

	@Test
	public void testDefaultValueBoolean() {
		List<Argument> arguments = Arrays.asList(
                new Argument("argument1", true),
                new Argument("argument2", false),
                new Argument("argument3", "phrase containing true and false")
        );
		Map<String, Serializable> result = bindArguments(arguments);
		Assert.assertTrue((boolean) result.get("argument1"));
		Assert.assertFalse((boolean) result.get("argument2"));
		Assert.assertEquals("phrase containing true and false", result.get("argument3"));
	}

    @Test
    public void testTwoArguments() {
		List<Argument> arguments = Arrays.asList(new Argument("argument2", "yyy"), new Argument("argument1", "zzz"));
        Map<String,Serializable> result = bindArguments(arguments);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals("zzz", result.get("argument1"));
        Assert.assertTrue(result.containsKey("argument2"));
        Assert.assertEquals("yyy", result.get("argument2"));
    }

    @Test
    public void testAssignNoExpression() {
        Argument argument1 = new Argument("argument1", "${ argument1 }");
        Argument argument2 = new Argument("argument2", "${ argument1 }");
        List<Argument> arguments = Arrays.asList(argument1, argument2);
        Map<String,Serializable> result = bindArguments(arguments);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals(null, result.get("argument1"));
        Assert.assertTrue(result.containsKey("argument2"));
        Assert.assertEquals(null, result.get("argument2"));
    }

    @Test
    public void testArgumentRef() {
        Map<String,Serializable> context = new HashMap<>();
        context.put("argumentX","xxx");
        List<Argument> arguments = Collections.singletonList(new Argument("argument1", "${ str(argumentX) }"));
        Map<String,Serializable> result = bindArguments(arguments, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals("xxx", result.get("argument1"));

        Assert.assertEquals(1,context.size());
    }

    @Test
    public void testArgumentScriptEval() {
        Map<String,Serializable> context = new HashMap<>();
        context.put("valX",5);
        Argument scriptArgument = new Argument("argument1","${ 3 + valX }");
        List<Argument> arguments = Collections.singletonList(scriptArgument);
        Map<String,Serializable> result = bindArguments(arguments, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals(8, result.get("argument1"));

        Assert.assertEquals(1, context.size());
    }

    @Test
    public void testArgumentScriptEval2() {
        Map<String,Serializable> context = new HashMap<>();
        context.put("valB","b");
        context.put("valC","c");
        Argument scriptArgument = new Argument("argument1","${ 'a' + valB + valC }");
        List<Argument> arguments = Collections.singletonList(scriptArgument);
        Map<String,Serializable> result = bindArguments(arguments, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals("abc", result.get("argument1"));
    }

    @Test
    public void testDefaultValueVsEmptyRef() {
        Map<String,Serializable> context = new HashMap<>();

		Argument refArgument = new Argument("argument1", "${ str('val') }");
        List<Argument> arguments = Collections.singletonList(refArgument);

        Map<String,Serializable> result = bindArguments(arguments, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals("val", result.get("argument1"));

        Assert.assertTrue(context.isEmpty());
    }

    @Test
    public void testPrivateBehaviour() {
        Map<String,Serializable> context = new HashMap<>();
        context.put("argument1",3);
		Argument argument = new Argument("argument1", "${ 5+7 }");
        List<Argument> arguments = Collections.singletonList(argument);

        Map<String,Serializable> result = bindArguments(arguments, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals(12, result.get("argument1"));

        Assert.assertEquals(1, context.size());
        Assert.assertEquals(3, context.get("argument1"));
    }

    @Test
    public void testComplexExpr(){
        Map<String,Serializable> context = new HashMap<>();
        context.put("argument1", 3);
		Argument argument = new Argument("argument2", "${ argument1 + 3 * 2 }");
        List<Argument> arguments = Collections.singletonList(argument);

        Map<String,Serializable> result = bindArguments(arguments, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument2"));
        Assert.assertEquals(9, result.get("argument2"));
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testOverrideAssignFromVar() {
        Map<String, Serializable> context = new HashMap<>();
        context.put("argument2", 3);
        context.put("argument1", 5);
        Argument argument = new Argument("argument1", "${ argument2 }");
        List<Argument> arguments = Collections.singletonList(argument);

        Map<String, Serializable> result = bindArguments(arguments, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals(3, result.get("argument1"));
        Assert.assertEquals(1, result.size());
    }

    @Test(expected = RuntimeException.class)
    public void testExpressionWithWrongRef() {
        Map<String,Serializable> context = new HashMap<>();

		Argument argument = new Argument("argument1", "${ argument2 }");
        List<Argument> arguments = Collections.singletonList(argument);

        bindArguments(arguments, context);
    }

    @Test
    public void testArgumentAssignFromAnotherArgument() {
        Map<String,Serializable> context = new HashMap<>();

		Argument argument1 = new Argument("argument1",5);
        Argument argument2 = new Argument("argument2","${ argument1 }");
        List<Argument> arguments = Arrays.asList(argument1,argument2);

        Map<String,Serializable> result = bindArguments(arguments, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals(5, result.get("argument1"));
        Assert.assertTrue(result.containsKey("argument2"));
        Assert.assertEquals(5, result.get("argument2"));
        Assert.assertEquals(2, result.size());

        Assert.assertTrue("orig context should not change", context.isEmpty());
    }

    @Test
    public void testComplexExpressionArgument() {
        Map<String,Serializable> context = new HashMap<>();
        context.put("varX",5);

		Argument argument1 = new Argument("argument1", 5);
        Argument argument2 = new Argument("argument2","${ argument1 + 5 + varX }");
        List<Argument> arguments = Arrays.asList(argument1,argument2);

        Map<String,Serializable> result = bindArguments(arguments, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals(5, result.get("argument1"));
        Assert.assertTrue(result.containsKey("argument2"));
        Assert.assertEquals(15, result.get("argument2"));
        Assert.assertEquals(2, result.size());

        Assert.assertEquals("orig context should not change",1,context.size());
    }

    @Test
    public void testComplexExpression2Argument() {
        Map<String,Serializable> context = new HashMap<>();
        context.put("varX","roles");

		Argument argument1 = new Argument("argument1", "${ 'mighty' + ' max '   + varX }");
        List<Argument> arguments = Collections.singletonList(argument1);

        Map<String,Serializable> result = bindArguments(arguments, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals("mighty max roles", result.get("argument1"));
        Assert.assertEquals(1, result.size());

        Assert.assertEquals("orig context should not change",1,context.size());
    }

	private Map<String, Serializable> bindArguments(
            List<Argument> arguments,
            Map<String, ? extends Serializable> context) {
		return argumentsBinding.bindArguments(arguments, context, EMPTY_SET);
	}

	private Map<String, Serializable> bindArguments(List<Argument> arguments) {
		return bindArguments(arguments, new HashMap<String, Serializable>());
	}

    @Configuration
    static class Config{

        @Bean
        public ArgumentsBinding argumentsBinding(){
            return new ArgumentsBinding();
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
