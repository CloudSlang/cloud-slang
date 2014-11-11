package com.hp.score.lang.compiler.transformers;
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

import com.hp.score.lang.compiler.SlangTextualKeys;
import com.hp.score.lang.compiler.domain.SlangFile;
import com.hp.score.lang.compiler.utils.YamlParser;
import com.hp.score.lang.entities.ScoreLangConstants;
import com.hp.score.lang.entities.bindings.Result;
import junit.framework.Assert;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * User: stoneo
 * Date: 10/11/2014
 * Time: 10:40
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ResultsTransformerTest.Config.class)
public class ResultsTransformerTest {

    @Autowired
    private ResultsTransformer resultsTransformer;

    @Autowired
    private YamlParser yamlParser;

    private List resultsMapOpWithData;

    private List resultsMapOpNoData;

    @Before
    public void init() throws URISyntaxException {
        resultsMapOpWithData = getResultsFromOperationFile("/operation_with_data.yaml", "test_op_2");
        resultsMapOpNoData = getResultsFromOperationFile("/operation.yaml", "test_op");
    }

    private List getResultsFromOperationFile(String fileName, String operationName) throws URISyntaxException {
        URL resource = getClass().getResource(fileName);
        SlangFile file = yamlParser.loadSlangFile(new File(resource.toURI()));
        Map<String, Map<String, Object>> op =  file.getOperations().iterator().next();
        Map<String,  Object> operationWithData = op.get(operationName);
        return (List)operationWithData.get(SlangTextualKeys.RESULT_KEY);
    }

    @Test
    public void testTransform() throws Exception {
        List<Result> results = resultsTransformer.transform(resultsMapOpWithData);
        Assert.assertTrue(CollectionUtils.isNotEmpty(results));
        Assert.assertEquals(3, results.size());
    }

    @Test
    public void testSimpleExpressionResult() throws Exception {
        List<Result> results = resultsTransformer.transform(resultsMapOpWithData);
        Result result = results.get(0);
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, result.getName());
        Assert.assertEquals("1 != 123456", result.getExpression());
    }

    @Test
    public void testBooleanExpressionResult() throws Exception {
        List<Result> results = resultsTransformer.transform(resultsMapOpWithData);
        Result result = results.get(1);
        Assert.assertEquals("NO_ACTION", result.getName());
        Assert.assertEquals("true", result.getExpression());
    }

    @Test
    public void testNoExpressionResult() throws Exception {
        List<Result> results = resultsTransformer.transform(resultsMapOpWithData);
        Result result = results.get(2);
        Assert.assertEquals(ScoreLangConstants.FAILURE_RESULT, result.getName());
        Assert.assertNull(result.getExpression());
    }

    @Test
    public void testFillDefaultResultsWhenNoResultsGiven() throws Exception {
        List<Result> results = resultsTransformer.transform(resultsMapOpNoData);
        Assert.assertTrue(CollectionUtils.isNotEmpty(results));
        Assert.assertEquals(2, results.size());
    }


    @Configuration
    static class Config {

        @Bean
        public Yaml yaml(){
            Yaml yaml = new Yaml();
            yaml.setBeanAccess(BeanAccess.FIELD);
            return yaml;
        }

        @Bean
        public YamlParser yamlParser(){
            return new YamlParser();
        }

        @Bean
        public ResultsTransformer resultsTransformer(){
            return new ResultsTransformer();
        }
    }
}
