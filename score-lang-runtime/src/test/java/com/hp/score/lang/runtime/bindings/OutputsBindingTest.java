/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package com.hp.score.lang.runtime.bindings;

import com.hp.score.lang.entities.bindings.Output;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
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

    @Autowired
    private OutputsBinding outputsBinding;

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testOperationEmptyOutputs() throws Exception {
        Map<String, Serializable> operationContext = new HashMap<>();
        Map<String, String> actionReturnValues = new HashMap<>();
        List<Output> outputs = new LinkedList<>();

        Map<String, String> result = outputsBinding.bindOutputs(operationContext, actionReturnValues, outputs);

        Assert.assertTrue("result cannot be null", result != null);
        Assert.assertTrue("result should be empty", result.isEmpty());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testOperationOutputsNoExpression() throws Exception {
        Map<String, Serializable> operationContext = prepareOperationContext();
        Map<String, String> actionReturnValues = prepareActionReturnValues();
        List<Output> outputs = Arrays.asList(createNoExpressionOutput("host1"));

        Map<String, String> result = outputsBinding.bindOutputs(operationContext, actionReturnValues, outputs);

        Map<String, String> expectedOutputs = new HashMap<>();
        expectedOutputs.put("host1", "valueHost1");

        Assert.assertEquals("Binding results are not as expected", expectedOutputs, result);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testOperationOutputsNoExpressionMultipleOutputs() throws Exception {
        Map<String, Serializable> operationContext = prepareOperationContext();
        Map<String, String> actionReturnValues = prepareActionReturnValues();
        List<Output> outputs = Arrays.asList(createNoExpressionOutput("host1"), createNoExpressionOutput("host2"));

        Map<String, String> result = outputsBinding.bindOutputs(operationContext, actionReturnValues, outputs);

        Map<String, String> expectedOutputs = new HashMap<>();
        expectedOutputs.put("host1", "valueHost1");
        expectedOutputs.put("host2", "valueHost2");

        Assert.assertEquals("Binding results are not as expected", expectedOutputs, result);
    }

    //todo: do we want to throw an exception?
    @Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
    public void testOperationOutputsIllegalEvaluatedExpression() throws Exception {
        Map<String, Serializable> operationContext = prepareOperationContext();
        Map<String, String> actionReturnValues = new HashMap<>();
        List<Output> outputs = Arrays.asList(createNoExpressionOutput("actionOutputKey1"));

        outputsBinding.bindOutputs(operationContext, actionReturnValues, outputs);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testOperationOutputsExpression() throws Exception {
        Map<String, Serializable> operationContext = prepareOperationContext();
        Map<String, String> actionReturnValues = prepareActionReturnValues();
        List<Output> outputs = Arrays.asList(createExpressionOutput("hostFromExpression", "'http://' + hostExpr + ':' + str(fromInputs['port'])"));

        Map<String, String> result = outputsBinding.bindOutputs(operationContext, actionReturnValues, outputs);

        Map<String, String> expectedOutputs = new HashMap<>();
        expectedOutputs.put("hostFromExpression", "http://hostExpr:9999");

        Assert.assertEquals("Binding results are not as expected", expectedOutputs, result);
    }

    @Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
    public void testOperationOutputsWrongTypeExpression() throws Exception {
        Map<String, Serializable> operationContext = prepareOperationContext();
        Map<String, String> actionReturnValues = prepareActionReturnValues();
        List<Output> outputs = Arrays.asList(createExpressionOutput("hostFromExpression", "1"));

        outputsBinding.bindOutputs(operationContext, actionReturnValues, outputs);
    }

    @Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
    public void testMissingOperationOutputsNoExpression() throws Exception {
        Map<String, Serializable> operationContext = prepareOperationContext();
        Map<String, String> actionReturnValues = new HashMap<>();
        List<Output> outputs = Arrays.asList(new Output("actionOutputKey1", null));

        outputsBinding.bindOutputs(operationContext, actionReturnValues, outputs);
    }
    @Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
    public void testOperationOutputsInvalidExpression() throws Exception {
        Map<String, Serializable> operationContext = prepareOperationContext();
        Map<String, String> actionReturnValues = prepareActionReturnValues();
        List<Output> outputs = Arrays.asList(createExpressionOutput("hostFromExpression", "'http://' + hostExpr + ':' + str(fromInputs[SHOULD_BE_STRING])"));

        outputsBinding.bindOutputs(operationContext, actionReturnValues, outputs);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testOperationOutputsMixed() throws Exception {
        Map<String, Serializable> operationContext = prepareOperationContext();
        Map<String, String> actionReturnValues = prepareActionReturnValues();
        List<Output> outputs = Arrays.asList(
                createNoExpressionOutput("host1"),
                createExpressionOutput("hostFromExpression", "'http://' + hostExpr + ':' + str(fromInputs['port'])"));

        Map<String, String> result = outputsBinding.bindOutputs(operationContext, actionReturnValues, outputs);

        Map<String, String> expectedOutputs = new HashMap<>();
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

    private Map<String, String> prepareActionReturnValues() {
        Map<String, String> returnValues = new HashMap<>();
        returnValues.put("host1", "valueHost1");
        returnValues.put("host2", "valueHost2");
        returnValues.put("hostExpr", "hostExpr");
        returnValues.put("port", "9999");
        return returnValues;
    }

    private Output createNoExpressionOutput(String key) {
        return createExpressionOutput(key, key);
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
        public PythonInterpreter interpreter() {
            return new PythonInterpreter();
        }

        @Bean
        public ScriptEvaluator scriptEvaluator() {
            return new ScriptEvaluator();
        }

        @Bean
        public ScriptEngine scriptEngine() {
            return new ScriptEngineManager().getEngineByName("python");
        }


    }
}
