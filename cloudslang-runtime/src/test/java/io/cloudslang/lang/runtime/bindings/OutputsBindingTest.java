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
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import io.cloudslang.runtime.impl.python.PythonExecutionCachedEngine;
import io.cloudslang.runtime.impl.python.PythonExecutionEngine;
import io.cloudslang.runtime.impl.python.PythonRuntimeServiceImpl;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventBusImpl;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.python.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singletonList;

/**
 * Date: 11/7/2014
 *
 * @author Bonczidai Levente
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = OutputsBindingTest.Config.class)
public class OutputsBindingTest {

    private static final long DEFAULT_TIMEOUT = 10000;
    @SuppressWarnings("unchecked")
    private static final Set<SystemProperty> EMPTY_SET = Collections.EMPTY_SET;

    @Autowired
    private OutputsBinding outputsBinding;

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testOperationEmptyOutputs() {
        Map<String, Value> operationContext = new HashMap<>();
        Map<String, Value> actionReturnValues = new HashMap<>();
        List<Output> outputs = new LinkedList<>();

        Map<String, Value> result = outputsBinding
                .bindOutputs(operationContext, actionReturnValues, EMPTY_SET, outputs);

        Assert.assertTrue("result cannot be null", result != null);
        Assert.assertTrue("result should be empty", result.isEmpty());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testOperationOutputsNoExpression() {
        Map<String, Value> operationContext = prepareOperationContext();
        Map<String, Value> actionReturnValues = prepareActionReturnValues();
        List<Output> outputs = singletonList(createNoExpressionOutput("host1"));

        Map<String, Value> result = outputsBinding
                .bindOutputs(operationContext, actionReturnValues, EMPTY_SET, outputs);

        Map<String, Value> expectedOutputs = new HashMap<>();
        expectedOutputs.put("host1", ValueFactory.create("valueHost1"));

        Assert.assertEquals("Binding results are not as expected", expectedOutputs, result);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testOperationOutputsNoExpressionMultipleOutputs() {
        Map<String, Value> operationContext = prepareOperationContext();
        Map<String, Value> actionReturnValues = prepareActionReturnValues();
        List<Output> outputs = Arrays.asList(createNoExpressionOutput("host1"), createNoExpressionOutput("host2"));

        Map<String, Value> result = outputsBinding
                .bindOutputs(operationContext, actionReturnValues, EMPTY_SET, outputs);

        Map<String, Value> expectedOutputs = new HashMap<>();
        expectedOutputs.put("host1", ValueFactory.create("valueHost1"));
        expectedOutputs.put("host2", ValueFactory.create("valueHost2"));

        Assert.assertEquals("Binding results are not as expected", expectedOutputs, result);
    }

    @Test
    public void testOperationOutputsNoExpressionAtAll() {
        Map<String, Value> operationContext = prepareOperationContext();
        Map<String, Value> actionReturnValues = new HashMap<>();
        List<Output> outputs = singletonList(createNoExpressionOutput("actionOutputKey1"));

        Map<String, Value> boundOutputs = outputsBinding
                .bindOutputs(operationContext, actionReturnValues, EMPTY_SET, outputs);
        Assert.assertTrue(boundOutputs.containsKey("actionOutputKey1"));
        Assert.assertEquals(null, boundOutputs.get("actionOutputKey1").get());
    }

    @Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
    public void testOperationOutputsIllegalEvaluatedExpression() {
        Map<String, Value> operationContext = prepareOperationContext();
        Map<String, Value> actionReturnValues = new HashMap<>();
        List<Output> outputs = singletonList(createExpressionOutput("actionOutputKey1", "${ None + 'str' }"));

        outputsBinding.bindOutputs(operationContext, actionReturnValues, EMPTY_SET, outputs);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testOperationOutputsExpression() {
        Map<String, Value> operationContext = prepareOperationContext();
        Map<String, Value> actionReturnValues = prepareActionReturnValues();
        List<Output> outputs = singletonList(
                createExpressionOutput("hostFromExpression", "${ 'http://' + hostExpr + ':' + str(port) }"));

        Map<String, Value> result = outputsBinding
                .bindOutputs(operationContext, actionReturnValues, EMPTY_SET, outputs);

        Map<String, Value> expectedOutputs = new HashMap<>();
        expectedOutputs.put("hostFromExpression", ValueFactory.create("http://hostExpr:9999"));

        Assert.assertEquals("Binding results are not as expected", expectedOutputs, result);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testOutputsRetainOrder() {
        Map<String, Value> operationContext = prepareOperationContext();
        Map<String, Value> actionReturnValues = prepareActionReturnValues();
        List<Output> outputs = Lists.newArrayList(
                createExpressionOutput("output1", "1"),
                createExpressionOutput("output2", "2"),
                createExpressionOutput("output3", "3")
        );

        Map<String, Value> result = outputsBinding
                .bindOutputs(operationContext, actionReturnValues, EMPTY_SET, outputs);

        List<String> actualInputNames = Lists.newArrayList(result.keySet());
        List<String> expectedInputNames = Lists.newArrayList("output1", "output2", "output3");

        Assert.assertEquals("Binding results are not as expected", expectedInputNames, actualInputNames);
    }

    @Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
    public void testOperationOutputsInvalidExpression() {
        Map<String, Value> operationContext = prepareOperationContext();
        Map<String, Value> actionReturnValues = prepareActionReturnValues();
        List<Output> outputs = singletonList(
                createExpressionOutput("hostFromExpression",
                        "${ 'http://' + hostExpr + ':' + str(self[SHOULD_BE_STRING]) }"));

        outputsBinding.bindOutputs(operationContext, actionReturnValues, EMPTY_SET, outputs);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testOperationOutputsMixed() {
        Map<String, Value> operationContext = prepareOperationContext();
        Map<String, Value> actionReturnValues = prepareActionReturnValues();
        List<Output> outputs = Arrays.asList(
                createNoExpressionOutput("host1"),
                createExpressionOutput("hostFromExpression", "${ 'http://' + hostExpr + ':' + str(port) }"));

        Map<String, Value> result = outputsBinding
                .bindOutputs(operationContext, actionReturnValues, EMPTY_SET, outputs);

        Map<String, Value> expectedOutputs = new HashMap<>();
        expectedOutputs.put("hostFromExpression", ValueFactory.create("http://hostExpr:9999"));
        expectedOutputs.put("host1", ValueFactory.create("valueHost1"));

        Assert.assertEquals("Binding results are not as expected", expectedOutputs, result);
    }

    private Map<String, Value> prepareOperationContext() {
        Map<String, Value> operationContext = new HashMap<>();
        operationContext.put("operationContextKey1", ValueFactory.create("opContextValue1"));
        operationContext.put("host1", ValueFactory.create("host1"));
        operationContext.put("host", ValueFactory.create("host"));
        operationContext.put("port", ValueFactory.create(9999));
        return operationContext;
    }

    private Map<String, Value> prepareActionReturnValues() {
        Map<String, Value> returnValues = new HashMap<>();
        returnValues.put("host1", ValueFactory.create("valueHost1"));
        returnValues.put("host2", ValueFactory.create("valueHost2"));
        returnValues.put("hostExpr", ValueFactory.create("hostExpr"));
        returnValues.put("port", ValueFactory.create(9999));
        return returnValues;
    }

    private Output createNoExpressionOutput(String key) {
        return createExpressionOutput(key, "${ " + key + " }");
    }

    private Output createExpressionOutput(String key, String value) {
        return new Output(key, ValueFactory.create(value));
    }

    @Configuration
    static class Config {

        @Bean
        public OutputsBinding outputsBinding() {
            return new OutputsBinding();
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
