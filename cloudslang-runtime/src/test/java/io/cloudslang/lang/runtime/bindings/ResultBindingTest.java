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
import io.cloudslang.lang.entities.bindings.Result;
import org.junit.Assert;
import org.junit.Test;
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
import java.util.HashMap;
import java.util.List;

/**
 * User: stoneo
 * Date: 06/11/2014
 * Time: 10:02
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ResultBindingTest.Config.class)
public class ResultBindingTest {

    @Autowired
    private ResultsBinding resultsBinding;

    @Test
    public void testConstExprChooseFirstResult() throws Exception {
        List<Result> results = Arrays.asList(createResult(ScoreLangConstants.SUCCESS_RESULT, "1==1"),
                createResult(ScoreLangConstants.FAILURE_RESULT, "True and (not False)"));
        String result = resultsBinding.resolveResult(new HashMap<String, Serializable>(), new HashMap<String, Serializable>(), results, null);
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, result);
    }

    @Test
    public void testConstExprChooseSecondAResult() throws Exception {
        List<Result> results = Arrays.asList(createResult(ScoreLangConstants.SUCCESS_RESULT, "1==2"),
                                                createResult(ScoreLangConstants.FAILURE_RESULT, "1==1"));
        String result = resultsBinding.resolveResult(new HashMap<String, Serializable>(), new HashMap<String, Serializable>(), results, null);
        Assert.assertEquals(ScoreLangConstants.FAILURE_RESULT, result);
    }

    @Test
    public void testBindInputFirstResult() throws Exception {
        List<Result> results = Arrays.asList(createResult(ScoreLangConstants.SUCCESS_RESULT, "int(status) == 1"),
                                                createResult(ScoreLangConstants.FAILURE_RESULT, "int(status) == -1"));
        HashMap<String, Serializable> context = new HashMap<>();
        context.put("status", "1");
        String result = resultsBinding.resolveResult(new HashMap<String, Serializable>(), context, results, null);
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, result);
    }

    @Test
    public void testBindInputSecondResult() throws Exception {
        List<Result> results = Arrays.asList(createResult(ScoreLangConstants.SUCCESS_RESULT, "int(status) == 1"),
                                                createResult(ScoreLangConstants.FAILURE_RESULT, "int(status) == -1"));
        HashMap<String, Serializable> context = new HashMap<>();
        context.put("status", "-1");
        String result = resultsBinding.resolveResult(new HashMap<String, Serializable>(), context, results, null);
        Assert.assertEquals(ScoreLangConstants.FAILURE_RESULT, result);
    }

    @Test(expected = RuntimeException.class)
    public void testIllegalResultExpressionThrowsException() throws Exception {
        List<Result> results = Arrays.asList(createResult(ScoreLangConstants.SUCCESS_RESULT, "str(status)"),
                                                createResult(ScoreLangConstants.FAILURE_RESULT, "int(status) == -1"));
        HashMap<String, Serializable> context = new HashMap<>();
        context.put("status", "-1");
        resultsBinding.resolveResult(new HashMap<String, Serializable>(), context, results, null);
    }

    @Test
    public void testBindInputNullResult() throws Exception {
        List<Result> results = Arrays.asList(createResult(ScoreLangConstants.SUCCESS_RESULT, "int(status) == 1"),
                                                createResult(ScoreLangConstants.FAILURE_RESULT, null));
        HashMap<String, Serializable> context = new HashMap<>();
        context.put("status", "-1");
        String result = resultsBinding.resolveResult(new HashMap<String, Serializable>(), context, results, null);
        Assert.assertEquals(ScoreLangConstants.FAILURE_RESULT, result);
    }

    @Test
    public void testBindInputEmptyResult() throws Exception {
        List<Result> results = Arrays.asList(createResult(ScoreLangConstants.SUCCESS_RESULT, "int(status) == 1"),
                                            createResult(ScoreLangConstants.FAILURE_RESULT, ""));
        HashMap<String, Serializable> context = new HashMap<>();
        context.put("status", "-1");
        String result = resultsBinding.resolveResult(new HashMap<String, Serializable>(), context, results, null);
        Assert.assertEquals(ScoreLangConstants.FAILURE_RESULT, result);
    }

    @Test(expected = RuntimeException.class)
    public void testNoResults() throws Exception {
        List<Result> results = Arrays.asList();
        HashMap<String, Serializable> context = new HashMap<>();
        context.put("status", "-1");
        resultsBinding.resolveResult(new HashMap<String, Serializable>(), context, results, null);
    }

    @Test(expected = RuntimeException.class)
    public void testNoValidResultExpression() throws Exception {
        List<Result> results = Arrays.asList(createResult(ScoreLangConstants.SUCCESS_RESULT, "int(status) == 1"),
                createResult(ScoreLangConstants.FAILURE_RESULT, "int(status) == 0"));
        HashMap<String, Serializable> context = new HashMap<>();
        context.put("status", "-1");
        resultsBinding.resolveResult(new HashMap<String, Serializable>(), context, results, null);
    }

    @Test
    public void testPresetResult() throws Exception {
        List<Result> results = Arrays.asList(createEmptyResult(ScoreLangConstants.SUCCESS_RESULT),
                createEmptyResult(ScoreLangConstants.FAILURE_RESULT));
        HashMap<String, Serializable> context = new HashMap<>();
        String result = resultsBinding.resolveResult(new HashMap<String, Serializable>(), context, results, ScoreLangConstants.FAILURE_RESULT);
        Assert.assertEquals(ScoreLangConstants.FAILURE_RESULT, result);
    }

    @Test(expected = RuntimeException.class)
    public void testIllegalPresetResult() throws Exception {
        List<Result> results = Arrays.asList(createEmptyResult(ScoreLangConstants.SUCCESS_RESULT),
                createEmptyResult(ScoreLangConstants.FAILURE_RESULT));
        HashMap<String, Serializable> context = new HashMap<>();
        resultsBinding.resolveResult(new HashMap<String, Serializable>(), context, results, "IllegalResult");
    }

    @Test(expected = RuntimeException.class)
    public void testIllegalResultExpression() throws Exception {
        List<Result> results = Arrays.asList(createResult(ScoreLangConstants.SUCCESS_RESULT, "status"),
                createResult(ScoreLangConstants.FAILURE_RESULT, null));
        HashMap<String, Serializable> context = new HashMap<>();
        context.put("status", "-1");
        resultsBinding.resolveResult(new HashMap<String, Serializable>(), context, results, null);
    }
    private Result createResult(String name, String expression){
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
        public ScriptEngine scriptEngine(){
            return  new ScriptEngineManager().getEngineByName("python");
        }
    }

}
