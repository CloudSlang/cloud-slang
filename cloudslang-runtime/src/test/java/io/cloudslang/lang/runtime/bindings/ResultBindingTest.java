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
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import io.cloudslang.runtime.impl.python.PythonExecutionCachedEngine;
import io.cloudslang.runtime.impl.python.PythonExecutionEngine;
import io.cloudslang.runtime.impl.python.PythonRuntimeServiceImpl;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventBusImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * User: stoneo
 * Date: 06/11/2014
 * Time: 10:02
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ResultBindingTest.Config.class)
public class ResultBindingTest {

    @SuppressWarnings("unchecked")
    private static final Set<SystemProperty> EMPTY_SET = Collections.EMPTY_SET;

    @Autowired
    private ResultsBinding resultsBinding;

    @Test
    public void testPrimitiveBooleanFirstResult() throws Exception {
        List<Result> results = asList(
                createResult(ScoreLangConstants.SUCCESS_RESULT, ValueFactory.create(true)),
                createResult(ScoreLangConstants.FAILURE_RESULT, ValueFactory.create("${ True and (not False) }"))
        );
        String result = resultsBinding
                .resolveResult(new HashMap<String, Value>(), new HashMap<String, Value>(), EMPTY_SET, results, null);
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, result);
    }

    @Test
    public void testObjectBooleanFirstResult() throws Exception {
        List<Result> results = asList(
                createResult(ScoreLangConstants.SUCCESS_RESULT, ValueFactory.create(Boolean.TRUE)),
                createResult(ScoreLangConstants.FAILURE_RESULT, ValueFactory.create("${ True and (not False) }"))
        );
        String result = resultsBinding
                .resolveResult(new HashMap<String, Value>(), new HashMap<String, Value>(), EMPTY_SET, results, null);
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, result);
    }

    @Test
    public void testConstExprChooseFirstResult() throws Exception {
        List<Result> results = asList(createResult(ScoreLangConstants.SUCCESS_RESULT, ValueFactory.create("${ 1==1 }")),
                createResult(ScoreLangConstants.FAILURE_RESULT, ValueFactory.create("${ True and (not False) }")));
        String result = resultsBinding
                .resolveResult(new HashMap<String, Value>(), new HashMap<String, Value>(), EMPTY_SET, results, null);
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, result);
    }

    @Test
    public void testConstExprChooseSecondAResult() throws Exception {
        List<Result> results = asList(createResult(ScoreLangConstants.SUCCESS_RESULT, ValueFactory.create("${ 1==2 }")),
                createResult(ScoreLangConstants.FAILURE_RESULT, ValueFactory.create("${ 1==1 }")));
        String result = resultsBinding
                .resolveResult(new HashMap<String, Value>(), new HashMap<String, Value>(), EMPTY_SET, results, null);
        Assert.assertEquals(ScoreLangConstants.FAILURE_RESULT, result);
    }

    @Test
    public void testBindInputFirstResult() throws Exception {
        List<Result> results = asList(
                createResult(ScoreLangConstants.SUCCESS_RESULT, ValueFactory.create("${ int(status) == 1 }")),
                createResult(ScoreLangConstants.FAILURE_RESULT, ValueFactory.create("${ int(status) == -1 }")));
        HashMap<String, Value> context = new HashMap<>();
        context.put("status", ValueFactory.create("1"));
        String result = resultsBinding.resolveResult(new HashMap<String, Value>(), context, EMPTY_SET, results, null);
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, result);
    }

    @Test
    public void testBindInputSecondResult() throws Exception {
        List<Result> results = asList(
                createResult(ScoreLangConstants.SUCCESS_RESULT, ValueFactory.create("${ int(status) == 1 }")),
                createResult(ScoreLangConstants.FAILURE_RESULT, ValueFactory.create("${ int(status) == -1 }")));
        HashMap<String, Value> context = new HashMap<>();
        context.put("status", ValueFactory.create("-1"));
        String result = resultsBinding.resolveResult(new HashMap<String, Value>(), context, EMPTY_SET, results, null);
        Assert.assertEquals(ScoreLangConstants.FAILURE_RESULT, result);
    }

    @Test(expected = RuntimeException.class)
    public void testIllegalResultExpressionThrowsException() throws Exception {
        List<Result> results = asList(
                createResult(ScoreLangConstants.SUCCESS_RESULT, ValueFactory.create("${ str(status) }")),
                createResult(ScoreLangConstants.FAILURE_RESULT, ValueFactory.create("${ int(status) == -1 }")));
        HashMap<String, Value> context = new HashMap<>();
        context.put("status", ValueFactory.create("-1"));
        resultsBinding.resolveResult(new HashMap<String, Value>(), context, EMPTY_SET, results, null);
    }

    @Test
    public void testBindInputNullResult() throws Exception {
        List<Result> results = asList(
                createResult(ScoreLangConstants.SUCCESS_RESULT, ValueFactory.create("${ int(status) == 1 }")),
                createResult(ScoreLangConstants.FAILURE_RESULT, null));
        HashMap<String, Value> context = new HashMap<>();
        context.put("status", ValueFactory.create("-1"));
        String result = resultsBinding.resolveResult(new HashMap<String, Value>(), context, EMPTY_SET, results, null);
        Assert.assertEquals(ScoreLangConstants.FAILURE_RESULT, result);
    }

    @Test(expected = RuntimeException.class)
    public void testNoResults() throws Exception {
        List<Result> results = Collections.emptyList();
        HashMap<String, Value> context = new HashMap<>();
        context.put("status", ValueFactory.create("-1"));
        resultsBinding.resolveResult(new HashMap<String, Value>(), context, EMPTY_SET, results, null);
    }

    @Test(expected = RuntimeException.class)
    public void testNoValidResultExpression() throws Exception {
        List<Result> results = asList(
                createResult(ScoreLangConstants.SUCCESS_RESULT, ValueFactory.create("${ int(status) == 1 }")),
                createResult(ScoreLangConstants.FAILURE_RESULT, ValueFactory.create("${ int(status) == 0 }")));
        HashMap<String, Value> context = new HashMap<>();
        context.put("status", ValueFactory.create("-1"));
        resultsBinding.resolveResult(new HashMap<String, Value>(), context, EMPTY_SET, results, null);
    }

    @Test
    public void testPresetResult() throws Exception {
        List<Result> results = asList(createEmptyResult(ScoreLangConstants.SUCCESS_RESULT),
                createEmptyResult(ScoreLangConstants.FAILURE_RESULT));
        HashMap<String, Value> context = new HashMap<>();
        String result = resultsBinding
                .resolveResult(new HashMap<String, Value>(), context, EMPTY_SET,
                        results, ScoreLangConstants.FAILURE_RESULT);
        Assert.assertEquals(ScoreLangConstants.FAILURE_RESULT, result);
    }

    @Test(expected = RuntimeException.class)
    public void testIllegalPresetResult() throws Exception {
        List<Result> results = asList(createEmptyResult(ScoreLangConstants.SUCCESS_RESULT),
                createEmptyResult(ScoreLangConstants.FAILURE_RESULT));
        HashMap<String, Value> context = new HashMap<>();
        resultsBinding.resolveResult(new HashMap<String, Value>(), context, EMPTY_SET, results, "IllegalResult");
    }

    @Test(expected = RuntimeException.class)
    public void testIllegalResultExpression() throws Exception {
        List<Result> results = asList(
                createResult(ScoreLangConstants.SUCCESS_RESULT, ValueFactory.create("${ status }")),
                createResult(ScoreLangConstants.FAILURE_RESULT, ValueFactory.create(null)));
        HashMap<String, Value> context = new HashMap<>();
        context.put("status", ValueFactory.create("-1"));
        resultsBinding.resolveResult(new HashMap<String, Value>(), context, EMPTY_SET, results, null);
    }

    private Result createResult(String name, Value expression) {
        return new Result(name, expression);
    }

    private Result createEmptyResult(String name) {
        return new Result(name, null);
    }

    @Configuration
    static class Config {

        @Bean
        public ResultsBinding resultsBinding() {
            return new ResultsBinding();
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
