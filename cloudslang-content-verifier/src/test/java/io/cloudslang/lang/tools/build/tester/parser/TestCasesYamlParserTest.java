/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.tester.parser;

import configuration.SlangEntitiesSpringConfig;
import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.commons.services.api.SlangSourceService;
import io.cloudslang.lang.commons.services.impl.SlangSourceServiceImpl;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.parser.YamlParser;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.compiler.validator.ExecutableValidatorImpl;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidator;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidatorImpl;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.entities.encryption.DummyEncryptor;
import io.cloudslang.lang.logging.LoggingService;
import io.cloudslang.lang.logging.LoggingServiceImpl;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import io.cloudslang.lang.tools.build.tester.parse.TestCasesYamlParser;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by stoneo on 3/16/2015.
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestCasesYamlParserTest.Config.class, SlangEntitiesSpringConfig.class})
public class TestCasesYamlParserTest {

    @Autowired
    private TestCasesYamlParser parser;

    @Autowired
    private Slang slang;

    @Autowired
    private LoggingService loggingService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void emptyTestCaseFileParsing() throws Exception {
        String filePath = "/test/invalid/empty_file.inputs.yaml";
        URI fileUri = getClass().getResource(filePath).toURI();
        Map<String, SlangTestCase> testCases = parser.parseTestCases(SlangSource.fromFile(fileUri));
        Assert.assertEquals("There should have been no test cases in the file", 0, testCases.size());
    }

    @Test
    public void testSimpleTestCasesParsing() throws URISyntaxException {
        String filePath = "/test/base/test_print_text-SUCCESS.inputs.yaml";
        URI fileUri = getClass().getResource(filePath).toURI();
        Map<String, SlangTestCase> testCases = parser.parseTestCases(SlangSource.fromFile(fileUri));
        SlangTestCase testPrintFinishesWithSuccess = testCases.get("testPrintFinishesWithSuccess");
        Assert.assertEquals("Tests that print_text operation finishes with SUCCESS",
                testPrintFinishesWithSuccess.getDescription());
        List<Map> expectedInputsList = new ArrayList<>();
        Map<String, Serializable> expectedInput1 = new HashMap<>();
        expectedInput1.put("text", ValueFactory.create("text to print", false));
        Map<String, Serializable> expectedInput2 = new HashMap<>();
        expectedInput2.put("password", ValueFactory.create("password1", true));
        expectedInputsList.add(expectedInput1);
        expectedInputsList.add(expectedInput2);
        Assert.assertEquals(expectedInputsList, testPrintFinishesWithSuccess.getInputs());
    }

    @Test
    public void testSimpleTestCasesParsingInvalidKey() throws URISyntaxException {
        String filePath = "/test/base/test_print_text-unrecognized_tag.inputs.yaml";
        URI fileUri = getClass().getResource(filePath).toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Artifact {testPrintFinishesWithSuccess} has unrecognized tag {invalid_key}. " +
                "Please take a look at the supported features per versions link");
        parser.parseTestCases(SlangSource.fromFile(fileUri));
    }

    @Test
    public void testCaseFileParsingForNonTestCasesFile() throws Exception {
        String filePath = "/content/base/properties.prop.sl";
        final URI fileUri = getClass().getResource(filePath).toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage("problem");
        exception.expectMessage("parsing");
        exception.expectMessage("properties");
        parser.parseTestCases(SlangSource.fromFile(fileUri));
    }

    @Test
    public void illegalTestCaseFileParsing() throws Exception {
        String filePath = "/test/invalid/invalid_test_case.yaml";
        final URI fileUri = getClass().getResource(filePath).toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage("slang");
        exception.expectMessage("parsing");
        exception.expectMessage("invalid_test_case");
        parser.parseTestCases(SlangSource.fromFile(fileUri));
    }

    @Test
    public void parseSystemPropertiesFile() throws Exception {
        final URI filePath = getClass().getResource("/content/base/properties.prop.sl").toURI();
        SlangSource source = SlangSource.fromFile(filePath);
        Set<SystemProperty> props = new HashSet<>();

        when(slang.loadSystemProperties(eq(source))).thenReturn(props);
        parser.parseProperties(filePath.getPath());
        verify(slang).loadSystemProperties(eq(source));
    }

    @Test
    public void parseSystemPropertiesFileInvalidExtension() throws Exception {
        final URI filePath = getClass().getResource("/content/base/print_text.sl").toURI();

        exception.expect(RuntimeException.class);
        exception.expectMessage("print_text.sl");
        exception.expectMessage("extension");
        exception.expectMessage("prop.sl");

        parser.parseProperties(filePath.getPath());
    }

    @Test
    public void testExceptionContainsDetailsWhenInvalidSource() throws Exception {
        final URI fileUri = getClass().getResource("/test/invalid/invalid_field.inputs.yaml").toURI();

        exception.expect(RuntimeException.class);
        exception.expectMessage(
                "There was a problem parsing the YAML source: invalid_field.inputs.yaml."
        );
        exception.expectMessage(
                "Error parsing slang test case: Unrecognized field \"invalid_field\"" +
                        " (class io.cloudslang.lang.tools.build.tester.parse.SlangTestCase)," +
                        " not marked as ignorable (8 known properties: \"outputs\", \"testFlowPath\"," +
                        " \"name\", \"description\", \"testSuites\", \"systemPropertiesFile\", \"throwsException\"," +
                        " \"result\"])"
        );
        exception.expectMessage(
                " at [Source: {\"inputs\":[{\"text\":\"text to print\"}],\"description\":" +
                        "\"Tests that print_text operation finishes with SUCCESS\",\"testFlowPath\":" +
                        "\"base.print_property\",\"result\":\"SUCCESS\",\"invalid_field\":\"value\"};" +
                        " line: 1, column: 181] (through reference chain:" +
                        " io.cloudslang.lang.tools.build.tester.parse.SlangTestCase[\"invalid_field\"])"
        );

        parser.parseTestCases(SlangSource.fromFile(fileUri));
    }

    @Configuration
    static class Config {

        @Bean
        public TestCasesYamlParser parser() {
            return new TestCasesYamlParser();
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
        public Slang slang() {
            return mock(Slang.class);
        }

        @Bean
        public ParserExceptionHandler parserExceptionHandler() {
            return new ParserExceptionHandler();
        }

        @Bean
        public Yaml yaml() {
            Yaml yaml = new Yaml();
            yaml.setBeanAccess(BeanAccess.FIELD);
            return yaml;
        }

        @Bean
        public DummyEncryptor dummyEncryptor() {
            return new DummyEncryptor();
        }

        @Bean
        public SlangSourceService slangSourceService() {
            return new SlangSourceServiceImpl();
        }

        @Bean
        public ExecutableValidator executableValidator() {
            return new ExecutableValidatorImpl();
        }

        @Bean
        public SystemPropertyValidator systemPropertyValidator() {
            return new SystemPropertyValidatorImpl();
        }

        @Bean
        public LoggingService loggingService() {
            return new LoggingServiceImpl();
        }
    }
}
