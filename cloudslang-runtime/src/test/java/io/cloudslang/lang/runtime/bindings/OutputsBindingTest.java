/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.lang.runtime.bindings;

import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.python.google.common.collect.Lists;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.*;

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
        Map<String, Serializable> operationContext = new HashMap<>();
        Map<String, Serializable> actionReturnValues = new HashMap<>();
        List<Output> outputs = new LinkedList<>();

        Map<String, Serializable> result = outputsBinding.bindOutputs(operationContext, actionReturnValues, EMPTY_SET, outputs);

        Assert.assertTrue("result cannot be null", result != null);
        Assert.assertTrue("result should be empty", result.isEmpty());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testOperationOutputsNoExpression() {
        Map<String, Serializable> operationContext = prepareOperationContext();
        Map<String, Serializable> actionReturnValues = prepareActionReturnValues();
        List<Output> outputs = Arrays.asList(createNoExpressionOutput("host1"));

        Map<String, Serializable> result = outputsBinding.bindOutputs(operationContext, actionReturnValues, EMPTY_SET, outputs);

        Map<String, Serializable> expectedOutputs = new HashMap<>();
        expectedOutputs.put("host1", "valueHost1");

        Assert.assertEquals("Binding results are not as expected", expectedOutputs, result);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testOperationOutputsNoExpressionMultipleOutputs() {
        Map<String, Serializable> operationContext = prepareOperationContext();
        Map<String, Serializable> actionReturnValues = prepareActionReturnValues();
        List<Output> outputs = Arrays.asList(createNoExpressionOutput("host1"), createNoExpressionOutput("host2"));

        Map<String, Serializable> result = outputsBinding.bindOutputs(operationContext, actionReturnValues, EMPTY_SET, outputs);

        Map<String, Serializable> expectedOutputs = new HashMap<>();
        expectedOutputs.put("host1", "valueHost1");
        expectedOutputs.put("host2", "valueHost2");

        Assert.assertEquals("Binding results are not as expected", expectedOutputs, result);
    }

    @Test
    public void testOperationOutputsNoExpressionAtAll() {
        Map<String, Serializable> operationContext = prepareOperationContext();
        Map<String, Serializable> actionReturnValues = new HashMap<>();
        List<Output> outputs = Arrays.asList(createNoExpressionOutput("actionOutputKey1"));

        Map<String, Serializable> boundOutputs = outputsBinding.bindOutputs(operationContext, actionReturnValues, EMPTY_SET, outputs);
        Assert.assertTrue(boundOutputs.containsKey("actionOutputKey1"));
        Assert.assertEquals(null, boundOutputs.get("actionOutputKey1"));
    }

    @Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
    public void testOperationOutputsIllegalEvaluatedExpression() {
        Map<String, Serializable> operationContext = prepareOperationContext();
        Map<String, Serializable> actionReturnValues = new HashMap<>();
        List<Output> outputs = Arrays.asList(createExpressionOutput("actionOutputKey1", "${ None + 'str' }"));

        outputsBinding.bindOutputs(operationContext, actionReturnValues, EMPTY_SET, outputs);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testOperationOutputsExpression() {
        Map<String, Serializable> operationContext = prepareOperationContext();
        Map<String, Serializable> actionReturnValues = prepareActionReturnValues();
        List<Output> outputs = Collections.singletonList(createExpressionOutput("hostFromExpression", "${ 'http://' + hostExpr + ':' + str(port) }"));

        Map<String, Serializable> result = outputsBinding.bindOutputs(operationContext, actionReturnValues, EMPTY_SET, outputs);

        Map<String, Serializable> expectedOutputs = new HashMap<>();
        expectedOutputs.put("hostFromExpression", "http://hostExpr:9999");

        Assert.assertEquals("Binding results are not as expected", expectedOutputs, result);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testOutputsRetainOrder() {
        Map<String, Serializable> operationContext = prepareOperationContext();
        Map<String, Serializable> actionReturnValues = prepareActionReturnValues();
        List<Output> outputs = Lists.newArrayList(
                createExpressionOutput("output1", "1"),
                createExpressionOutput("output2", "2"),
                createExpressionOutput("output3", "3")
        );

        Map<String, Serializable> result = outputsBinding.bindOutputs(operationContext, actionReturnValues, EMPTY_SET, outputs);

        List<String> actualInputNames = Lists.newArrayList(result.keySet());
        List<String> expectedInputNames = Lists.newArrayList("output1", "output2", "output3");

        Assert.assertEquals("Binding results are not as expected", expectedInputNames, actualInputNames);
    }

    @Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
    public void testOperationOutputsInvalidExpression() {
        Map<String, Serializable> operationContext = prepareOperationContext();
        Map<String, Serializable> actionReturnValues = prepareActionReturnValues();
        List<Output> outputs = Arrays.asList(createExpressionOutput("hostFromExpression", "${ 'http://' + hostExpr + ':' + str(self[SHOULD_BE_STRING]) }"));

        outputsBinding.bindOutputs(operationContext, actionReturnValues, EMPTY_SET, outputs);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testOperationOutputsMixed() {
        Map<String, Serializable> operationContext = prepareOperationContext();
        Map<String, Serializable> actionReturnValues = prepareActionReturnValues();
        List<Output> outputs = Arrays.asList(
                createNoExpressionOutput("host1"),
                createExpressionOutput("hostFromExpression", "${ 'http://' + hostExpr + ':' + str(port) }"));

        Map<String, Serializable> result = outputsBinding.bindOutputs(operationContext, actionReturnValues, EMPTY_SET, outputs);

        Map<String, Serializable> expectedOutputs = new HashMap<>();
        expectedOutputs.put("hostFromExpression", "http://hostExpr:9999");
        expectedOutputs.put("host1", "valueHost1");

        Assert.assertEquals("Binding results are not as expected", expectedOutputs, result);
    }

    private Map<String, Serializable> prepareOperationContext() {
        Map<String, Serializable> operationContext = new HashMap<>();
        operationContext.put("operationContextKey1", "opContextValue1");
        operationContext.put("host1", "host1");
        operationContext.put("host", "host");
        operationContext.put("port", 9999);
        return operationContext;
    }

    private Map<String, Serializable> prepareActionReturnValues() {
        Map<String, Serializable> returnValues = new HashMap<>();
        returnValues.put("host1", "valueHost1");
        returnValues.put("host2", "valueHost2");
        returnValues.put("hostExpr", "hostExpr");
        returnValues.put("port", 9999);
        return returnValues;
    }

    private Output createNoExpressionOutput(String key) {
        return createExpressionOutput(key, "${ " + key + " }");
    }

    private Output createExpressionOutput(String key, String value) {
        return new Output(key, value);
    }

    @Configuration
    static class Config {

        @Bean
        public OutputsBinding outputsBinding() {
            return new OutputsBinding();
        }

        @Bean
        public PythonInterpreter evalInterpreter() {
            return new PythonInterpreter();
        }

        @Bean
        public ScriptEvaluator scriptEvaluator() {
            return new ScriptEvaluator();
        }

    }
}
