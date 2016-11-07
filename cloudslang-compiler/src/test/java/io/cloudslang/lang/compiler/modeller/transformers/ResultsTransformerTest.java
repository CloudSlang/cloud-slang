/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.transformers;


import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.parser.YamlParser;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.compiler.validator.ExecutableValidatorImpl;
import io.cloudslang.lang.compiler.validator.PreCompileValidator;
import io.cloudslang.lang.compiler.validator.PreCompileValidatorImpl;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidator;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidatorImpl;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Result;
import junit.framework.Assert;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
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
@ContextConfiguration(classes = {ResultsTransformerTest.Config.class, SlangCompilerSpringConfig.class})
public class ResultsTransformerTest {

    @Autowired
    private ResultsTransformer resultsTransformer;

    @Autowired
    private YamlParser yamlParser;

    private List resultsMapOpWithData;

    private List resultsMapOpNoData;

    @Before
    public void init() throws URISyntaxException {
        resultsMapOpWithData = getResultsFromOperationFile("/operation_with_data.sl");
        resultsMapOpNoData = getResultsFromOperationFile("/test_op_1.sl");
    }

    private List getResultsFromOperationFile(String fileName) throws URISyntaxException {
        URL resource = getClass().getResource(fileName);
        ParsedSlang file = yamlParser.parse(SlangSource.fromFile(new File(resource.toURI())));
        Map<String, Object> op = file.getOperation();
        return (List) op.get(SlangTextualKeys.RESULTS_KEY);
    }

    @Test
    public void testTransform() throws Exception {
        List<Result> results = resultsTransformer.transform(resultsMapOpWithData).getTransformedData();
        Assert.assertTrue(CollectionUtils.isNotEmpty(results));
        Assert.assertEquals(3, results.size());
    }

    @Test
    public void testSimpleExpressionResult() throws Exception {
        List<Result> results = resultsTransformer.transform(resultsMapOpWithData).getTransformedData();
        Result result = results.get(0);
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, result.getName());
        Assert.assertEquals("${ 1 != 123456 }", result.getValue().get());
    }

    @Test
    public void testBooleanExpressionResult() throws Exception {
        List<Result> results = resultsTransformer.transform(resultsMapOpWithData).getTransformedData();
        Result result = results.get(1);
        Assert.assertEquals("NO_ACTION", result.getName());
        Assert.assertEquals("${ true }", result.getValue().get());
    }

    @Test
    public void testNoExpressionResult() throws Exception {
        List<Result> results = resultsTransformer.transform(resultsMapOpWithData).getTransformedData();
        Result result = results.get(2);
        Assert.assertEquals(ScoreLangConstants.FAILURE_RESULT, result.getName());
        Assert.assertNull(result.getValue());
    }

    @Test
    public void testFillDefaultResultsWhenNoResultsGiven() throws Exception {
        List<Result> results = resultsTransformer.transform(resultsMapOpNoData).getTransformedData();
        Assert.assertTrue(CollectionUtils.isEmpty(results));
    }

    @Configuration
    static class Config {

        @Bean
        @Scope("prototype")
        public Yaml yaml() {
            Yaml yaml = new Yaml();
            yaml.setBeanAccess(BeanAccess.FIELD);
            return yaml;
        }

        @Bean
        public YamlParser yamlParser() {
            return new YamlParser() {
                @Override
                public Yaml getYaml() {
                    return yaml();
                }
            };
        }

        @Bean
        public ParserExceptionHandler parserExceptionHandler() {
            return new ParserExceptionHandler();
        }

        @Bean
        public ResultsTransformer resultsTransformer() {
            return new ResultsTransformer();
        }

        @Bean
        public PreCompileValidator preCompileValidator() {
            return new PreCompileValidatorImpl();
        }

        @Bean
        public ExecutableValidator executableValidator() {
            return new ExecutableValidatorImpl();
        }

        @Bean
        public SystemPropertyValidator systemPropertyValidator() {
            return new SystemPropertyValidatorImpl();
        }

    }
}
