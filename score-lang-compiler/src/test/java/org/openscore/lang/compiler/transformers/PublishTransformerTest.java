package org.openscore.lang.compiler.transformers;
/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/


import org.openscore.lang.compiler.SlangTextualKeys;
import org.openscore.lang.compiler.model.SlangFile;
import org.openscore.lang.compiler.utils.YamlParser;
import org.openscore.lang.entities.bindings.Output;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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
 * Date: 12/11/2014
 * Time: 16:12
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=PublishTransformerTest.Configuration.class)
public class PublishTransformerTest {

    private static final long DEFAULT_TIMEOUT = 10000;

    @Autowired
    private Transformer publishTransformer;

    @Autowired
    private YamlParser yamlParser;

    private Object publishMap;

    @Before
    public void init() throws URISyntaxException {
        URL resource = getClass().getResource("/flow_with_multiple_steps.yaml");
        SlangFile file = yamlParser.loadSlangFile(new File(resource.toURI()));
        Map flow = (Map)file.getFlow().get(SlangTextualKeys.WORKFLOW_KEY);
        Map task= (Map)flow.get("RealRealCheckWeather");
        publishMap = task.get(SlangTextualKeys.PUBLISH_KEY);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testTransform() throws Exception {
        List<Output> publishValues = (List<Output>) publishTransformer.transform(publishMap);
        Assert.assertFalse(publishValues.isEmpty());
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testNoExpression() throws Exception {
        List<Output> publishValues = (List<Output>) publishTransformer.transform(publishMap);
        Output publish = publishValues.get(0);
        Assert.assertEquals("weather", publish.getName());
        Assert.assertEquals("weather", publish.getExpression());
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testExpressionKey() throws Exception {
        List<Output> publishValues = (List<Output>) publishTransformer.transform(publishMap);
        Output publish = publishValues.get(1);
        Assert.assertEquals("temp", publish.getName());
        Assert.assertEquals("temperature", publish.getExpression());
    }

    @org.springframework.context.annotation.Configuration
    public static class Configuration {

        @Bean
        public Yaml yaml() {
            Yaml yaml = new Yaml();
            yaml.setBeanAccess(BeanAccess.FIELD);
            return yaml;
        }

        @Bean
        public YamlParser yamlParser() {
            YamlParser yamlParser = new YamlParser();
            return yamlParser;
        }

        @Bean
        public Transformer publishTransformer() {
            return new PublishTransformer();
        }

    }
}
