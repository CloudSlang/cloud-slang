package com.hp.score.lang.tests.runtime.bindings;
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

import com.hp.score.lang.entities.ScoreLangConstants;
import com.hp.score.lang.entities.bindings.Result;
import com.hp.score.lang.runtime.bindings.ResultsBinding;
import com.hp.score.lang.runtime.bindings.ScriptEvaluator;
import com.hp.score.lang.runtime.configuration.SlangRuntimeSpringConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.python.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;

/**
 * User: stoneo
 * Date: 06/11/2014
 * Time: 10:02
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ResultBindingTest {

    @Autowired
    private ResultsBinding resultsBinding;

    @Test
    public void testConstExprChooseFirstResult() throws Exception {
        List<Result> results = Lists.newArrayList(createResult(ScoreLangConstants.SUCCESS_RESULT, "1==1"),
                                                    createResult(ScoreLangConstants.FAILURE_RESULT, "1==2"));
        String result = resultsBinding.resolveResult(new HashMap<String, String>(), results);
        Assert.assertTrue(result.equals(ScoreLangConstants.SUCCESS_RESULT));
    }

    @Test
    public void testConstExprChooseSecondAResult() throws Exception {
        List<Result> results = Lists.newArrayList(createResult(ScoreLangConstants.SUCCESS_RESULT, "1==2"),
                                                createResult(ScoreLangConstants.FAILURE_RESULT, "1==1"));
        String result = resultsBinding.resolveResult(new HashMap<String, String>(), results);
        Assert.assertTrue(result.equals(ScoreLangConstants.FAILURE_RESULT));
    }

    @Test
    public void testBindInputFirstResult() throws Exception {
        List<Result> results = Lists.newArrayList(createResult(ScoreLangConstants.SUCCESS_RESULT, "int(status) == 1"),
                                                createResult(ScoreLangConstants.FAILURE_RESULT, "int(status) == -1"));
        HashMap<String, String> context = new HashMap<>();
        context.put("status", "1");
        String result = resultsBinding.resolveResult(context, results);
        Assert.assertTrue(result.equals(ScoreLangConstants.SUCCESS_RESULT));
    }

    @Test
    public void testBindInputSecondResult() throws Exception {
        List<Result> results = Lists.newArrayList(createResult(ScoreLangConstants.SUCCESS_RESULT, "int(status) == 1"),
                                                createResult(ScoreLangConstants.FAILURE_RESULT, "int(status) == -1"));
        HashMap<String, String> context = new HashMap<>();
        context.put("status", "-1");
        String result = resultsBinding.resolveResult(context, results);
        Assert.assertTrue(result.equals(ScoreLangConstants.FAILURE_RESULT));
    }

    @Test(expected = RuntimeException.class)
    public void testIllegalResultExpressionThrowsException() throws Exception {
        List<Result> results = Lists.newArrayList(createResult(ScoreLangConstants.SUCCESS_RESULT, "int(status)"),
                                                createResult(ScoreLangConstants.FAILURE_RESULT, "int(status) == -1"));
        HashMap<String, String> context = new HashMap<>();
        context.put("status", "-1");
        resultsBinding.resolveResult(context, results);
    }

    @Test
    public void testBindInputNullResult() throws Exception {
        List<Result> results = Lists.newArrayList(createResult(ScoreLangConstants.SUCCESS_RESULT, "int(status) == 1"),
                                                createResult(ScoreLangConstants.FAILURE_RESULT, null));
        HashMap<String, String> context = new HashMap<>();
        context.put("status", "-1");
        String result = resultsBinding.resolveResult(context, results);
        Assert.assertTrue(result.equals(ScoreLangConstants.FAILURE_RESULT));
    }

    @Test
    public void testBindInputEmptyResult() throws Exception {
        List<Result> results = Lists.newArrayList(createResult(ScoreLangConstants.SUCCESS_RESULT, "int(status) == 1"),
                                            createResult(ScoreLangConstants.FAILURE_RESULT, ""));
        HashMap<String, String> context = new HashMap<>();
        context.put("status", "-1");
        String result = resultsBinding.resolveResult(context, results);
        Assert.assertTrue(result.equals(ScoreLangConstants.FAILURE_RESULT));
    }

    @Test(expected = RuntimeException.class)
    public void testNoResults() throws Exception {
        List<Result> results = Lists.newArrayList();
        HashMap<String, String> context = new HashMap<>();
        context.put("status", "-1");
        String result = resultsBinding.resolveResult(context, results);
        Assert.assertTrue(result.equals(ScoreLangConstants.FAILURE_RESULT));
    }

    private Result createResult(String name, String expression){
        return new Result(name, expression);
    }

    @Configuration
    @Import(SlangRuntimeSpringConfig.class)
    static class Config{

        @Bean
        public ResultsBinding resultsBinding(){
            return new ResultsBinding();
        }

        @Bean
        public ScriptEvaluator scriptEvaluator(){
            return new ScriptEvaluator();
        }

    }

}
