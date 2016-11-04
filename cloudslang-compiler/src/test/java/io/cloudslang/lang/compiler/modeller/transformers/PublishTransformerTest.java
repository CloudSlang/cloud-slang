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
import io.cloudslang.lang.entities.bindings.Output;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
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

/**
 * User: stoneo
 * Date: 12/11/2014
 * Time: 16:12
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PublishTransformerTest.Config.class, SlangCompilerSpringConfig.class})
public class PublishTransformerTest extends TransformersTestParent {

    private static final long DEFAULT_TIMEOUT = 10000;

    @Autowired
    private PublishTransformer publishTransformer;

    @Autowired
    private YamlParser yamlParser;

    private List<Object> publishMap;

    @Before
    public void init() throws URISyntaxException {
        URL resource = getClass().getResource("/flow_with_multiple_steps.yaml");
        ParsedSlang file = yamlParser.parse(SlangSource.fromFile(new File(resource.toURI())));
        List<Map<String, Map>> flow = (List<Map<String, Map>>) file.getFlow().get(SlangTextualKeys.WORKFLOW_KEY);
        for (Map<String, Map> step : flow) {
            if (step.keySet().iterator().next().equals("RealRealCheckWeather")) {
                publishMap = (List) step.values().iterator().next().get(SlangTextualKeys.PUBLISH_KEY);
                break;
            }
        }
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testTransform() throws Exception {
        @SuppressWarnings("unchecked")
        List<Output> publishValues = publishTransformer.transform(publishMap).getTransformedData();
        Assert.assertFalse(publishValues.isEmpty());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testNoValue() throws Exception {
        @SuppressWarnings("unchecked")
        List<Output> publishValues = publishTransformer.transform(publishMap).getTransformedData();
        Output publish = publishValues.get(0);
        Assert.assertEquals("weather", publish.getName());
        Assert.assertEquals("${weather}", publish.getValue().get());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testExpressionValue() throws Exception {
        @SuppressWarnings("unchecked")
        List<Output> publishValues = publishTransformer.transform(publishMap).getTransformedData();
        Output publish = publishValues.get(1);
        Assert.assertEquals("temp", publish.getName());
        Assert.assertEquals("${temperature}", publish.getValue().get());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testStringValue() throws Exception {
        @SuppressWarnings("unchecked")
        List<Output> publishValues = publishTransformer.transform(publishMap).getTransformedData();
        Output publish = publishValues.get(2);
        Assert.assertEquals("publish_str", publish.getName());
        Assert.assertEquals("publish_str_value", publish.getValue().get());
    }

    @Configuration
    public static class Config {

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
        public PublishTransformer publishTransformer() {
            return new PublishTransformer();
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
