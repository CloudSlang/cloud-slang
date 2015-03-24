/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package io.cloudslang.lang.tools.build.tester.parser;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.parser.YamlParser;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import io.cloudslang.lang.tools.build.tester.parse.TestCasesYamlParser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by stoneo on 3/16/2015.
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestCasesYamlParserTest.Config.class)
public class TestCasesYamlParserTest {

    @Autowired
    private TestCasesYamlParser parser;

    @Test
    public void testSimpleTestCasesParsing() throws URISyntaxException {
        String filePath = "/test/org/content/test_print_text-SUCCESS.inputs.yaml";
        URI fileUri = getClass().getResource(filePath).toURI();
        Map<String, SlangTestCase> testCases = parser.parse(SlangSource.fromFile(fileUri));
        SlangTestCase testPrintFinishesWithSuccess = testCases.get("testPrintFinishesWithSuccess");
        Assert.assertEquals("Tests that print_text operation finishes with SUCCESS", testPrintFinishesWithSuccess.getDescription());
        List<Map> expectedInputsList = new ArrayList<>();
        Map<String, Serializable> expectedInputs = new HashMap<>();
        expectedInputs.put("text", "text to print");
        expectedInputsList.add(expectedInputs);
        Assert.assertEquals(expectedInputsList, testPrintFinishesWithSuccess.getInputs());
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
