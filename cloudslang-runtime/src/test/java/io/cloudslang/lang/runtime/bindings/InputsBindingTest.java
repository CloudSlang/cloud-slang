/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.bindings;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.dependency.api.services.MavenConfig;
import io.cloudslang.dependency.impl.services.DependencyServiceImpl;
import io.cloudslang.dependency.impl.services.MavenConfigImpl;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import io.cloudslang.runtime.impl.python.PythonExecutionCachedEngine;
import io.cloudslang.runtime.impl.python.PythonExecutionEngine;
import io.cloudslang.runtime.impl.python.PythonRuntimeServiceImpl;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventBusImpl;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
        Map<String, Value> result = bindInputs(inputs);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testDefaultValue() {
        List<Input> inputs = Collections.singletonList(new Input.InputBuilder("input1", "value").build());
        Map<String, Value> result = bindInputs(inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("value", result.get("input1").get());
    }

    @Ignore("Remove when types are supported")
    @Test
    public void testDefaultValueInt() {
        List<Input> inputs = Collections.singletonList(new Input.InputBuilder("input1", 2).build());
        Map<String, Value> result = bindInputs(inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(2, result.get("input1").get());
    }

    @Ignore("Remove when types are supported")
    @Test
    public void testDefaultValueBoolean() {
        List<Input> inputs = Arrays.asList(
                new Input.InputBuilder("input1", true).build(),
                new Input.InputBuilder("input2", false).build(),
                new Input.InputBuilder("input3", "${ str('phrase containing true and false') }").build()
        );
        Map<String, Value> result = bindInputs(inputs);
        Assert.assertTrue((boolean) result.get("input1").get());
        Assert.assertFalse((boolean) result.get("input2").get());
        Assert.assertEquals("phrase containing true and false", result.get("input3").get());
    }

    @Test
    public void testTwoInputs() {
        List<Input> inputs = Arrays.asList(
                new Input.InputBuilder("input2", "yyy").build(),
                new Input.InputBuilder("input1", "zzz").build()
        );
        Map<String, Value> result = bindInputs(inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("zzz", result.get("input1").get());
        Assert.assertTrue(result.containsKey("input2"));
        Assert.assertEquals("yyy", result.get("input2").get());
    }

    @Test
    public void testAssignFromInput() {
        Input input1 = new Input.InputBuilder("input1", "val1", false)
                .withRequired(false)
                .withPrivateInput(true)
                .build();
        Input input2 = new Input.InputBuilder("input2", "${ input1 }", false)
                .withRequired(false)
                .withPrivateInput(true)
                .build();
        List<Input> inputs = Arrays.asList(input1, input2);
        Map<String, Value> result = bindInputs(inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("val1", result.get("input1").get());
        Assert.assertTrue(result.containsKey("input2"));
        Assert.assertEquals("val1", result.get("input2").get());
    }

    @Test
    public void testPrivateInputMissingInContext() {
        Input input1 = new Input.InputBuilder("input1", "${ input1 }")
                .withRequired(false)
                .withPrivateInput(true)
                .build();
        List<Input> inputs = Collections.singletonList(input1);
        exception.expect(RuntimeException.class);
        exception.expectMessage(new BaseMatcher<String>() {
            public void describeTo(Description description) {
            }

            public boolean matches(Object o) {
                String message = o.toString();
                return message.contains("Error binding input: 'input1'") &&
                        message.contains("Error is: Error in running script expression: 'input1'") &&
                        message.contains("Exception is: name 'input1' is not defined");
            }
        });
        bindInputs(inputs);
    }

    @Test
    public void testInputMissingInContext() {
        Input input1 = new Input.InputBuilder("input1", "${ input1 }")
                .withRequired(false)
                .withPrivateInput(false)
                .build();
        List<Input> inputs = Collections.singletonList(input1);
        exception.expect(RuntimeException.class);
        exception.expectMessage(new BaseMatcher<String>() {
            public void describeTo(Description description) {
            }

            public boolean matches(Object o) {
                String message = o.toString();
                return message.contains("Error binding input: 'input1'") &&
                        message.contains("Error is: Error in running script expression: 'input1'") &&
                        message.contains("Exception is: name 'input1' is not defined");
            }
        });
        bindInputs(inputs);
    }

    @Test
    public void testInputMissing() {
        Input input1 = new Input.InputBuilder("input1", null)
                .withRequired(true)
                .withPrivateInput(false)
                .build();
        List<Input> inputs = Collections.singletonList(input1);
        exception.expect(RuntimeException.class);
        exception.expectMessage("Input with name: 'input1' is Required, but value is empty");
        bindInputs(inputs);
    }

    @Test
    public void testInputWithDefaultValueNull() {
        Input input1 = new Input.InputBuilder("input1", null)
                .withRequired(false)
                .withPrivateInput(false)
                .build();
        List<Input> inputs = Collections.singletonList(input1);
        Map<String, Value> result = bindInputs(inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(null, result.get("input1").get());
    }

    @Test
    public void testInputRef() {
        Map<String, Value> context = new HashMap<>();
        context.put("inputX", ValueFactory.create("xxx"));
        List<Input> inputs = Collections.singletonList(new Input.InputBuilder("input1", "${ str(inputX) }").build());
        Map<String, Value> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("xxx", result.get("input1").get());

        Assert.assertEquals(1, context.size());
    }

    @Test
    public void testInputScriptEval() {
        Map<String, Value> context = new HashMap<>();
        context.put("valX", ValueFactory.create("5"));
        Input scriptInput = new Input.InputBuilder("input1", "${ \"3\" + valX }").build();
        List<Input> inputs = Collections.singletonList(scriptInput);
        Map<String, Value> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("35", result.get("input1").get());

        Assert.assertEquals(1, context.size());
    }

    @Test
    public void testInputScriptEval2() {
        Map<String, Value> context = new HashMap<>();
        context.put("valB", ValueFactory.create("b"));
        context.put("valC", ValueFactory.create("c"));
        Input scriptInput = new Input.InputBuilder("input1", "${ 'a' + valB + valC }").build();
        List<Input> inputs = Collections.singletonList(scriptInput);
        Map<String, Value> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("abc", result.get("input1").get());
    }

    @Test
    public void testDefaultValueVsEmptyRef() {
        Map<String, Value> context = new HashMap<>();

        Input refInput = new Input.InputBuilder("input1", "${ str('val') }").build();
        List<Input> inputs = Collections.singletonList(refInput);

        Map<String, Value> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("val", result.get("input1").get());

        Assert.assertTrue(context.isEmpty());
    }

    @Test
    public void testAssignFromAndExpr() {
        Map<String, Value> context = new HashMap<>();
        context.put("input1", ValueFactory.create("3"));
        Input input = new Input.InputBuilder("input1", "${ 5+7 }").build();
        List<Input> inputs = Collections.singletonList(input);

        Map<String, Value> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("3", result.get("input1").get());

        Assert.assertEquals(1, context.size());
        Assert.assertEquals("3", context.get("input1").get());
    }

    @Test
    public void testAssignFromAndConst() {
        Map<String, Value> context = new HashMap<>();
        context.put("input1", ValueFactory.create("3"));
        Input input = new Input.InputBuilder("input1", 5).build();
        List<Input> inputs = Collections.singletonList(input);

        Map<String, Value> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("3", result.get("input1").get());
    }

    @Test
    public void testComplexExpr() {
        Map<String, Value> context = new HashMap<>();
        context.put("input1", ValueFactory.create("3"));
        Input input = new Input.InputBuilder("input2", "${ input1 + \"3 * 2\" }").build();
        List<Input> inputs = Collections.singletonList(input);

        Map<String, Value> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input2"));
        Assert.assertEquals("33 * 2", result.get("input2").get());
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testAssignFromVsRef() {
        Map<String, Value> context = new HashMap<>();
        context.put("input2", ValueFactory.create(3));
        context.put("input1", ValueFactory.create("5"));
        Input input = new Input.InputBuilder("input1", "${ input2 }").build();
        List<Input> inputs = Collections.singletonList(input);

        Map<String, Value> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("5", result.get("input1").get());
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testOverrideAssignFrom() {
        Map<String, Value> context = new HashMap<>();
        context.put("input2", ValueFactory.create("3"));
        context.put("input1", ValueFactory.create("5"));
        Input input = new Input.InputBuilder("input1", "${ input2 }", false)
                .withRequired(false)
                .withPrivateInput(true)
                .build();
        List<Input> inputs = Collections.singletonList(input);

        Map<String, Value> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("3", result.get("input1").get());
        Assert.assertEquals(1, result.size());

        Assert.assertEquals(2, context.size());
    }

    @Test
    public void testOverrideAssignFrom2() {
        Map<String, Value> context = new HashMap<>();
        context.put("input1", ValueFactory.create(5));
        Input input = new Input.InputBuilder("input1", "3", false)
                .withRequired(false)
                .withPrivateInput(true)
                .build();
        List<Input> inputs = Collections.singletonList(input);

        Map<String, Value> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("3", result.get("input1").get());
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testOverrideAssignFrom3() {
        Map<String, Value> context = new HashMap<>();
        context.put("input1", ValueFactory.create(5));
        Input input = new Input.InputBuilder("input1", null, false)
                .withRequired(false)
                .withPrivateInput(true)
                .build();
        List<Input> inputs = Collections.singletonList(input);

        Map<String, Value> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("'not private' disables the assignFrom func...", null, result.get("input1").get());
        Assert.assertEquals(1, result.size());
    }

    @Ignore("Remove when types are supported")
    @Test
    public void testOverrideFalse() {
        Map<String, Value> context = new HashMap<>();
        context.put("input1", ValueFactory.create(5));
        Input input = new Input.InputBuilder("input1", 6).build();
        List<Input> inputs = Collections.singletonList(input);

        Map<String, Value> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(5, result.get("input1").get());
        Assert.assertEquals(1, result.size());
    }

    @Test(expected = RuntimeException.class)
    public void testExpressionWithWrongRef() {
        Map<String, Value> context = new HashMap<>();

        Input input = new Input.InputBuilder("input1", "${ input2 }", false)
                .withRequired(false)
                .withPrivateInput(true)
                .build();
        List<Input> inputs = Collections.singletonList(input);

        bindInputs(inputs, context);
    }

    @Test
    public void testInputAssignFromAnotherInput() {
        Map<String, Value> context = new HashMap<>();

        Input input1 = new Input.InputBuilder("input1", "5").build();
        Input input2 = new Input.InputBuilder("input2", "${ input1 }").build();
        List<Input> inputs = Arrays.asList(input1, input2);

        Map<String, Value> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("5", result.get("input1").get());
        Assert.assertTrue(result.containsKey("input2"));
        Assert.assertEquals("5", result.get("input2").get());
        Assert.assertEquals(2, result.size());

        Assert.assertTrue("orig context should not change", context.isEmpty());
    }

    @Test
    public void testComplexExpressionInput() {
        Map<String, Value> context = new HashMap<>();
        context.put("varX", ValueFactory.create("5"));

        Input input1 = new Input.InputBuilder("input1", "5").build();
        Input input2 = new Input.InputBuilder("input2", "${ input1 + \"5\" + varX }").build();
        List<Input> inputs = Arrays.asList(input1, input2);

        Map<String, Value> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("5", result.get("input1").get());
        Assert.assertTrue(result.containsKey("input2"));
        Assert.assertEquals("555", result.get("input2").get());
        Assert.assertEquals(2, result.size());

        Assert.assertEquals("orig context should not change", 1, context.size());
    }

    @Test
    public void testComplexExpression2Input() {
        Map<String, Value> context = new HashMap<>();
        context.put("varX", ValueFactory.create("roles"));

        Input input1 = new Input.InputBuilder("input1", "${ 'mighty' + ' max '   + varX }").build();
        List<Input> inputs = Collections.singletonList(input1);

        Map<String, Value> result = bindInputs(inputs, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("mighty max roles", result.get("input1").get());
        Assert.assertEquals(1, result.size());

        Assert.assertEquals("orig context should not change", 1, context.size());
    }

    private Map<String, Value> bindInputs(List<Input> inputs, Map<String, Value> context,
                                          Set<SystemProperty> systemProperties) {
        return inputsBinding.bindInputs(inputs, context, systemProperties);
    }

    private Map<String, Value> bindInputs(List<Input> inputs, Map<String, Value> context) {
        return bindInputs(inputs, context, null);
    }

    private Map<String, Value> bindInputs(List<Input> inputs) {
        return bindInputs(inputs, new HashMap<String, Value>());
    }

    @Configuration
    static class Config {

        @Bean
        public InputsBinding inputsBinding() {
            return new InputsBinding();
        }

        @Bean
        public ScriptEvaluator scriptEvaluator() {
            return new ScriptEvaluator();
        }

        @Bean
        public DependencyService mavenRepositoryService() {
            return new DependencyServiceImpl();
        }

        @Bean
        public MavenConfig mavenConfig() {
            return new MavenConfigImpl();
        }

        @Bean
        public PythonRuntimeService pythonRuntimeService() {
            return new PythonRuntimeServiceImpl();
        }

        @Bean
        public PythonExecutionEngine pythonExecutionEngine() {
            return new PythonExecutionCachedEngine();
        }

        @Bean
        public EventBus eventBus() {
            return new EventBusImpl();
        }
    }
}
