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
import io.cloudslang.lang.entities.PromptType;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.prompt.Prompt;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import io.cloudslang.lang.runtime.services.ScriptsService;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import io.cloudslang.runtime.impl.python.PythonExecutionCachedEngine;
import io.cloudslang.runtime.impl.python.PythonExecutionEngine;
import io.cloudslang.runtime.impl.python.PythonRuntimeServiceImpl;
import io.cloudslang.runtime.impl.python.external.ExternalPythonExecutionEngine;
import io.cloudslang.runtime.impl.python.external.ExternalPythonExecutorServiceImpl;
import io.cloudslang.runtime.impl.python.external.ExternalPythonRuntimeServiceImpl;
import io.cloudslang.runtime.impl.python.external.StatefulRestEasyClientsHolder;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventBusImpl;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = InputsBindingTest.Config.class)
public class InputsBindingTest {
    static {
        System.setProperty("python.expressionsEval", "jython");
    }

    @Autowired
    private InputsBinding inputsBinding;

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
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                bindInputs(inputs));
        Assert.assertTrue(exception.getMessage().contains("Error binding input: 'input1'"));
        Assert.assertTrue(exception.getMessage().contains("Error in evaluating expression: 'input1'"));
        Assert.assertTrue(exception.getMessage().contains("name 'input1' is not defined"));
    }

    @Test
    public void testInputMissingInContext() {
        Input input1 = new Input.InputBuilder("input1", "${ input1 }")
                .withRequired(false)
                .withPrivateInput(false)
                .build();
        List<Input> inputs = Collections.singletonList(input1);
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                bindInputs(inputs));
        Assert.assertTrue(exception.getMessage().contains("Error binding input: 'input1'"));
        Assert.assertTrue(exception.getMessage().contains("Error in evaluating expression: 'input1'"));
        Assert.assertTrue(exception.getMessage().contains("name 'input1' is not defined"));
    }

    @Test
    public void testInputMissing() {
        Input input1 = new Input.InputBuilder("input1", null)
                .withRequired(true)
                .withPrivateInput(false)
                .build();
        List<Input> inputs = Collections.singletonList(input1);
        List<Input> missingInputs = new ArrayList<>();
        bindInputs(inputs, new HashMap<>(), new HashMap<>(), new HashSet<>(), missingInputs);

        Assert.assertEquals(inputs, missingInputs);
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

    @Test
    public void testInputsWithPromptExpressions() {
        Map<String, Value> context = new HashMap<>();
        context.put("messageContainer1", ValueFactory.create("(What's the story?)"));
        context.put("messageContainer2", ValueFactory.create("Hey hey!"));
        context.put("messageContainer3", ValueFactory.create("Rock 'n' Roll"));
        context.put("singleChoiceDelimiter", ValueFactory.create("|"));
        context.put("singleChoiceOptions", ValueFactory.create("1|2|3"));
        context.put("multiChoiceDelimiter", ValueFactory.create("!"));
        context.put("multiChoiceOptions", ValueFactory.create("x!y!z"));

        Prompt textPrompt = new Prompt.PromptBuilder()
                .setPromptType(PromptType.TEXT)
                .setPromptMessage("${messageContainer1 + ' Morning glory'}")
                .build();

        Prompt singleChoicePrompt = new Prompt.PromptBuilder()
                .setPromptType(PromptType.SINGLE_CHOICE)
                .setPromptMessage("${messageContainer2 + ' My my!'}")
                .setPromptOptions("${singleChoiceOptions}")
                .setPromptDelimiter("${singleChoiceDelimiter}")
                .build();

        Prompt multiChoicePrompt = new Prompt.PromptBuilder()
                .setPromptType(PromptType.MULTI_CHOICE)
                .setPromptMessage("${messageContainer3 + ' will never die'}")
                .setPromptOptions("${multiChoiceOptions}")
                .setPromptDelimiter("${multiChoiceDelimiter}")
                .build();

        Input input1 = new Input.InputBuilder("input1", ValueFactory.create(""), false)
                .withPrompt(textPrompt)
                .build();

        Input input2 = new Input.InputBuilder("input2", ValueFactory.create(""), false)
                .withPrompt(singleChoicePrompt)
                .build();

        Input input3 = new Input.InputBuilder("input3", ValueFactory.create(""), false)
                .withPrompt(multiChoicePrompt)
                .build();


        List<Input> result = new ArrayList<>();
        bindInputs(Arrays.asList(input1, input2, input3), context, new HashMap<>(), new HashSet<>(), result);
        assertEquals(3, result.size());

        Input missingInput1 = result.get(0);
        assertEquals("(What's the story?) Morning glory", missingInput1.getPrompt().getPromptMessage());

        Input missingInput2 = result.get(1);
        assertEquals("Hey hey! My my!", missingInput2.getPrompt().getPromptMessage());
        assertEquals("|", missingInput2.getPrompt().getPromptDelimiter());
        assertEquals("1|2|3", missingInput2.getPrompt().getPromptOptions());

        Input missingInput3 = result.get(2);
        assertEquals("Rock 'n' Roll will never die", missingInput3.getPrompt().getPromptMessage());
        assertEquals("!", missingInput3.getPrompt().getPromptDelimiter());
        assertEquals("x!y!z", missingInput3.getPrompt().getPromptOptions());

    }

    private Map<String, Value> bindInputs(List<Input> inputs, Map<String, Value> context,
                                          Map<String, Value> promptArgs,
                                          Set<SystemProperty> systemProperties, List<Input> missingInputs) {
        return inputsBinding.bindInputs(inputs, context, promptArgs, systemProperties, missingInputs,
                false, new HashMap<>());
    }

    private Map<String, Value> bindInputs(List<Input> inputs, Map<String, Value> context) {
        return bindInputs(inputs, context, Collections.emptyMap(), null, null);
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
        public ScriptsService scriptsService() {
            return new ScriptsService();
        }

        @Bean
        public DependencyService mavenRepositoryService() {
            return new DependencyServiceImpl();
        }

        @Bean
        public MavenConfig mavenConfig() {
            return new MavenConfigImpl();
        }

        @Bean(name = "jythonRuntimeService")
        public PythonRuntimeService pythonRuntimeService() {
            return new PythonRuntimeServiceImpl();
        }

        @Bean(name = "jythonExecutionEngine")
        public PythonExecutionEngine pythonExecutionEngine() {
            return new PythonExecutionCachedEngine();
        }

        @Bean(name = "externalPythonExecutorService")
        public PythonRuntimeService externalPythonExecutorService() {
            return new ExternalPythonExecutorServiceImpl(mock(StatefulRestEasyClientsHolder.class),
                    new Semaphore(100), new Semaphore(50));
        }

        @Bean(name = "externalPythonRuntimeService")
        public PythonRuntimeService externalPythonRuntimeService() {
            return new ExternalPythonRuntimeServiceImpl(new Semaphore(100), new Semaphore(50));
        }

        @Bean(name = "externalPythonExecutionEngine")
        public PythonExecutionEngine externalPythonExecutionEngine() {
            return new ExternalPythonExecutionEngine();
        }

        @Bean
        public EventBus eventBus() {
            return new EventBusImpl();
        }
    }
}
