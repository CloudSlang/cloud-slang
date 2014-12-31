/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.lang.compiler.transformers;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openscore.lang.compiler.SlangSource;
import org.openscore.lang.compiler.SlangTextualKeys;
import org.openscore.lang.compiler.model.ParsedSlang;
import org.openscore.lang.compiler.utils.YamlParser;
import org.openscore.lang.entities.bindings.Output;
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

    private List outputsMap;

    @Before
    public void init() throws URISyntaxException {
        URL resource = getClass().getResource("/operation_with_data.yaml");
        ParsedSlang file = yamlParser.parse(SlangSource.fromFile(new File(resource.toURI())));
        Map op = file.getOperations().iterator().next();
        Map opProp = (Map) op.get("test_op_2");
        outputsMap = (List) opProp.get(SlangTextualKeys.OUTPUTS_KEY);
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testTransform() throws Exception {
        @SuppressWarnings("unchecked") List<Output> outputs = outputTransformer.transform(outputsMap);
        Assert.assertFalse(outputs.isEmpty());
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testNoExpression() throws Exception {
        @SuppressWarnings("unchecked") List<Output> outputs = outputTransformer.transform(outputsMap);
        Output output = outputs.get(3);
        Assert.assertEquals("output4", output.getName());
        Assert.assertEquals("output4", output.getExpression());
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testExpressionKeyFromActionReturnValues() throws Exception {
        @SuppressWarnings("unchecked") List<Output> outputs = outputTransformer.transform(outputsMap);
        Output output = outputs.get(0);
        Assert.assertEquals("output1", output.getName());
        Assert.assertEquals("input1", output.getExpression());
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testInputExpression() throws Exception {
        @SuppressWarnings("unchecked") List<Output> outputs = outputTransformer.transform(outputsMap);
        Output output = outputs.get(2);
        Assert.assertEquals("output3", output.getName());
        Assert.assertEquals("fromInputs['input1']", output.getExpression());
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
        public OutputsTransformer outputTransformer() {
            return new OutputsTransformer();
        }

    }
}
