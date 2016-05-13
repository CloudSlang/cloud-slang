/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
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
import io.cloudslang.lang.compiler.parser.YamlParser;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import io.cloudslang.lang.entities.bindings.Output;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
 * Date: 11/11/2014
 *
 * @author Bonczidai Levente
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=OutputsTransformerTest.Config.class)
public class OutputsTransformerTest {

    private static final long DEFAULT_TIMEOUT = 10000;

    @Autowired
    private OutputsTransformer outputTransformer;

    @Autowired
    private YamlParser yamlParser;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private List outputsMap;

    @Before
    public void init() throws URISyntaxException {
        URL resource = getClass().getResource("/operation_with_data.sl");
        ParsedSlang file = yamlParser.parse(SlangSource.fromFile(new File(resource.toURI())));
        Map op = file.getOperation();
        outputsMap = (List) op.get(SlangTextualKeys.OUTPUTS_KEY);
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testTransform() throws Exception {
        @SuppressWarnings("unchecked") List<Output> outputs = outputTransformer.transform(outputsMap);
        Assert.assertFalse(outputs.isEmpty());
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testNoExpression() throws Exception {
        @SuppressWarnings("unchecked") List<Output> outputs = outputTransformer.transform(outputsMap);
        Output output = outputs.get(2);
        Assert.assertEquals("output3", output.getName());
        Assert.assertEquals("${output3}", output.getValue());
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testExpressionKeyFromActionReturnValues() throws Exception {
        @SuppressWarnings("unchecked") List<Output> outputs = outputTransformer.transform(outputsMap);
        Output output = outputs.get(0);
        Assert.assertEquals("output1", output.getName());
        Assert.assertEquals("${ input1 }", output.getValue());
    }

    @Configuration
    public static class Config {

        @Bean
        public Yaml yaml() {
            Yaml yaml = new Yaml();
            yaml.setBeanAccess(BeanAccess.FIELD);
            return yaml;
        }

        @Bean
        public YamlParser yamlParser() {
            return new YamlParser();
        }

        @Bean
        public ParserExceptionHandler parserExceptionHandler() {
            return new ParserExceptionHandler();
        }

        @Bean
        public OutputsTransformer outputTransformer() {
            return new OutputsTransformer();
        }

    }
}
