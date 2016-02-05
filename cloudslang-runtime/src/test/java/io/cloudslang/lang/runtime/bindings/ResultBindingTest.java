package io.cloudslang.lang.runtime.bindings;
/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/


import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.*;

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
        List<Result> results = Arrays.asList(
                createResult(ScoreLangConstants.SUCCESS_RESULT, true),
                createResult(ScoreLangConstants.FAILURE_RESULT, "${ True and (not False) }")
        );
        String result = resultsBinding.resolveResult(new HashMap<String, Serializable>(), new HashMap<String, Serializable>(), EMPTY_SET, results, null);
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, result);
    }

    @Test
    public void testPrimitiveBooleanSecondResult() throws Exception {
        List<Result> results = Arrays.asList(
                createResult(ScoreLangConstants.SUCCESS_RESULT, false),
                createResult(ScoreLangConstants.FAILURE_RESULT, true)
        );
        String result = resultsBinding.resolveResult(new HashMap<String, Serializable>(), new HashMap<String, Serializable>(), EMPTY_SET, results, null);
        Assert.assertEquals(ScoreLangConstants.FAILURE_RESULT, result);
    }

    @Test
    public void testObjectBooleanFirstResult() throws Exception {
        List<Result> results = Arrays.asList(
                createResult(ScoreLangConstants.SUCCESS_RESULT, Boolean.TRUE),
                createResult(ScoreLangConstants.FAILURE_RESULT, "${ True and (not False) }")
        );
        String result = resultsBinding.resolveResult(new HashMap<String, Serializable>(), new HashMap<String, Serializable>(), EMPTY_SET, results, null);
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, result);
    }

    @Test
    public void testObjectBooleanSecondResult() throws Exception {
        List<Result> results = Arrays.asList(
                createResult(ScoreLangConstants.SUCCESS_RESULT, Boolean.FALSE),
                createResult(ScoreLangConstants.FAILURE_RESULT, Boolean.TRUE)
        );
        String result = resultsBinding.resolveResult(new HashMap<String, Serializable>(), new HashMap<String, Serializable>(), EMPTY_SET, results, null);
        Assert.assertEquals(ScoreLangConstants.FAILURE_RESULT, result);
    }

    @Test
    public void testConstExprChooseFirstResult() throws Exception {
        List<Result> results = Arrays.asList(createResult(ScoreLangConstants.SUCCESS_RESULT, "${ 1==1 }"),
                createResult(ScoreLangConstants.FAILURE_RESULT, "${ True and (not False) }"));
        String result = resultsBinding.resolveResult(new HashMap<String, Serializable>(), new HashMap<String, Serializable>(), EMPTY_SET, results, null);
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, result);
    }

    @Test
    public void testConstExprChooseSecondAResult() throws Exception {
        List<Result> results = Arrays.asList(createResult(ScoreLangConstants.SUCCESS_RESULT, "${ 1==2 }"),
                                                createResult(ScoreLangConstants.FAILURE_RESULT, "${ 1==1 }"));
        String result = resultsBinding.resolveResult(new HashMap<String, Serializable>(), new HashMap<String, Serializable>(), EMPTY_SET, results, null);
        Assert.assertEquals(ScoreLangConstants.FAILURE_RESULT, result);
    }

    @Test
    public void testBindInputFirstResult() throws Exception {
        List<Result> results = Arrays.asList(createResult(ScoreLangConstants.SUCCESS_RESULT, "${ int(status) == 1 }"),
                                                createResult(ScoreLangConstants.FAILURE_RESULT, "${ int(status) == -1 }"));
        HashMap<String, Serializable> context = new HashMap<>();
        context.put("status", "1");
        String result = resultsBinding.resolveResult(new HashMap<String, Serializable>(), context, EMPTY_SET, results, null);
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, result);
    }

    @Test
    public void testBindInputSecondResult() throws Exception {
        List<Result> results = Arrays.asList(createResult(ScoreLangConstants.SUCCESS_RESULT, "${ int(status) == 1 }"),
                                                createResult(ScoreLangConstants.FAILURE_RESULT, "${ int(status) == -1 }"));
        HashMap<String, Serializable> context = new HashMap<>();
        context.put("status", "-1");
        String result = resultsBinding.resolveResult(new HashMap<String, Serializable>(), context, EMPTY_SET, results, null);
        Assert.assertEquals(ScoreLangConstants.FAILURE_RESULT, result);
    }

    @Test(expected = RuntimeException.class)
    public void testIllegalResultExpressionThrowsException() throws Exception {
        List<Result> results = Arrays.asList(createResult(ScoreLangConstants.SUCCESS_RESULT, "${ str(status) }"),
                                                createResult(ScoreLangConstants.FAILURE_RESULT, "${ int(status) == -1 }"));
        HashMap<String, Serializable> context = new HashMap<>();
        context.put("status", "-1");
        resultsBinding.resolveResult(new HashMap<String, Serializable>(), context, EMPTY_SET, results, null);
    }

    @Test
    public void testBindInputNullResult() throws Exception {
        List<Result> results = Arrays.asList(createResult(ScoreLangConstants.SUCCESS_RESULT, "${ int(status) == 1 }"),
                                                createResult(ScoreLangConstants.FAILURE_RESULT, null));
        HashMap<String, Serializable> context = new HashMap<>();
        context.put("status", "-1");
        String result = resultsBinding.resolveResult(new HashMap<String, Serializable>(), context, EMPTY_SET, results, null);
        Assert.assertEquals(ScoreLangConstants.FAILURE_RESULT, result);
    }

    @Test(expected = RuntimeException.class)
    public void testNoResults() throws Exception {
        List<Result> results = Arrays.asList();
        HashMap<String, Serializable> context = new HashMap<>();
        context.put("status", "-1");
        resultsBinding.resolveResult(new HashMap<String, Serializable>(), context, EMPTY_SET, results, null);
    }

    @Test(expected = RuntimeException.class)
    public void testNoValidResultExpression() throws Exception {
        List<Result> results = Arrays.asList(createResult(ScoreLangConstants.SUCCESS_RESULT, "${ int(status) == 1 }"),
                createResult(ScoreLangConstants.FAILURE_RESULT, "${ int(status) == 0 }"));
        HashMap<String, Serializable> context = new HashMap<>();
        context.put("status", "-1");
        resultsBinding.resolveResult(new HashMap<String, Serializable>(), context, EMPTY_SET, results, null);
    }

    @Test
    public void testPresetResult() throws Exception {
        List<Result> results = Arrays.asList(createEmptyResult(ScoreLangConstants.SUCCESS_RESULT),
                createEmptyResult(ScoreLangConstants.FAILURE_RESULT));
        HashMap<String, Serializable> context = new HashMap<>();
        String result = resultsBinding.resolveResult(new HashMap<String, Serializable>(), context, EMPTY_SET, results, ScoreLangConstants.FAILURE_RESULT);
        Assert.assertEquals(ScoreLangConstants.FAILURE_RESULT, result);
    }

    @Test(expected = RuntimeException.class)
    public void testIllegalPresetResult() throws Exception {
        List<Result> results = Arrays.asList(createEmptyResult(ScoreLangConstants.SUCCESS_RESULT),
                createEmptyResult(ScoreLangConstants.FAILURE_RESULT));
        HashMap<String, Serializable> context = new HashMap<>();
        resultsBinding.resolveResult(new HashMap<String, Serializable>(), context, EMPTY_SET, results, "IllegalResult");
    }

    @Test(expected = RuntimeException.class)
    public void testIllegalResultExpression() throws Exception {
        List<Result> results = Arrays.asList(createResult(ScoreLangConstants.SUCCESS_RESULT, "${ status }"),
                createResult(ScoreLangConstants.FAILURE_RESULT, null));
        HashMap<String, Serializable> context = new HashMap<>();
        context.put("status", "-1");
        resultsBinding.resolveResult(new HashMap<String, Serializable>(), context, EMPTY_SET, results, null);
    }

    private Result createResult(String name, Serializable expression){
        return new Result(name, expression);
    }

    private Result createEmptyResult(String name){
        return new Result(name, null);
    }

    @Configuration
    static class Config{

        @Bean
        public ResultsBinding resultsBinding(){
            return new ResultsBinding();
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
