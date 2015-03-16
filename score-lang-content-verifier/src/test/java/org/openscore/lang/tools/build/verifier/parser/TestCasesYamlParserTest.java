/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package org.openscore.lang.tools.build.verifier.parser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openscore.lang.compiler.parser.YamlParser;
import org.openscore.lang.tools.build.tester.parse.TestCasesYamlParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.net.URI;
import java.net.URISyntaxException;

import static org.mockito.Mockito.mock;

/**
 * Created by stoneo on 3/16/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestCasesYamlParserTest.Config.class)
public class TestCasesYamlParserTest {

    @Autowired
    private TestCasesYamlParser parser;

    @Test
    public void testSimpleTestCasersParsing() throws URISyntaxException {
        String filePath = "/test/org/content/test_print_text-SUCCESS.inputs.yaml";
        URI fileUri = getClass().getResource(filePath).toURI();
//        parser.parse(SlangSource.fromFile(fileUri));
    }


    @Configuration
    static class Config {

        @Bean
        public TestCasesYamlParser parser() {
            return new TestCasesYamlParser();
        }

        @Bean
        public YamlParser yamlParser(){
            return new YamlParser();
        }

        @Bean
        public Yaml yaml(){
            Yaml yaml = new Yaml();
            yaml.setBeanAccess(BeanAccess.FIELD);
            return yaml;
        }
    }
}
