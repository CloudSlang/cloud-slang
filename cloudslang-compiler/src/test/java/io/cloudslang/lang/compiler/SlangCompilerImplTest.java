/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler;

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.caching.CachedPrecompileService;
import io.cloudslang.lang.compiler.caching.CachedPrecompileServiceImpl;
import io.cloudslang.lang.compiler.modeller.SlangModeller;
import io.cloudslang.lang.compiler.parser.YamlParser;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import io.cloudslang.lang.compiler.scorecompiler.ScoreCompiler;
import io.cloudslang.lang.compiler.validator.CompileValidator;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.compiler.validator.ExecutableValidatorImpl;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidator;
import io.cloudslang.lang.entities.SystemProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Bonczidai Levente
 * @since 4/12/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SlangCompilerImplTest.Config.class})
public class SlangCompilerImplTest {

    private SlangSource slangSource;
    @Autowired
    private SlangCompiler slangCompiler;
    @Autowired
    private YamlParser yamlParserMock;
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        slangSource = new SlangSource("source_content", "source_name");
    }

    @Test
    public void testInvalidParsedSlangType() throws Exception {
        ParsedSlang parsedSlangInvalidTypeMock = mock(ParsedSlang.class);
        doReturn(parsedSlangInvalidTypeMock).when(yamlParserMock).parse(any(SlangSource.class));
        doReturn(parsedSlangInvalidTypeMock).when(yamlParserMock).validateAndThrowFirstError(any(ParsedSlang.class));

        when(parsedSlangInvalidTypeMock.getType()).thenReturn(ParsedSlang.Type.FLOW);
        when(parsedSlangInvalidTypeMock.getName()).thenReturn("flow_name");

        exception.expect(RuntimeException.class);
        exception.expectMessage("flow_name");
        exception.expectMessage(SlangCompilerImpl.NOT_A_VALID_SYSTEM_PROPERTY_FILE_ERROR_MESSAGE_SUFFIX);

        slangCompiler.loadSystemProperties(slangSource);
    }

    @Test
    public void testLoadSystemProperties() throws Exception {
        ParsedSlang parsedSlangMock = mock(ParsedSlang.class);
        when(yamlParserMock.parse(eq(slangSource))).thenReturn(parsedSlangMock);
        when(yamlParserMock.validateAndThrowFirstError(eq(parsedSlangMock))).thenReturn(parsedSlangMock);
        when(parsedSlangMock.getType()).thenReturn(ParsedSlang.Type.SYSTEM_PROPERTY_FILE);

        final String namespace = "a.b";
        String key1 = "c.key1";
        String key2 = "c.key2";
        String value1 = "value1";
        String value2 = "value2";
        Map<String, Object> property1 = new HashMap<>();
        Map<String, Object> property2 = new HashMap<>();
        property1.put(key1, value1);
        property2.put(key2, value2);
        List<Map<String, Object>> properties = new ArrayList<>();
        properties.add(property1);
        properties.add(property2);
        when(parsedSlangMock.getNamespace()).thenReturn(namespace);
        when(parsedSlangMock.getProperties()).thenReturn(properties);

        Set<SystemProperty> expectedSystemProperties = Sets.newHashSet(
                new SystemProperty(namespace, key1, value1),
                new SystemProperty(namespace, key2, value2)
        );

        Set<SystemProperty> actualSystemProperties = slangCompiler.loadSystemProperties(slangSource);

        Assert.assertEquals(expectedSystemProperties, actualSystemProperties);
    }

    static class Config {

        private static YamlParser yamlParserReference;

        @Bean
        public YamlParser yamlParser() {
            if (yamlParserReference != null) {
                return yamlParserReference;
            }
            YamlParser mock = mock(YamlParser.class);
            mock.setExecutableValidator(executableValidator());
            mock.setParserExceptionHandler(parserExceptionHandler());
            yamlParserReference = mock;
            return mock;
        }

        @Bean
        public SlangModeller slangModeller() {
            return mock(SlangModeller.class);
        }

        @Bean
        public ScoreCompiler scoreCompiler() {
            return mock(ScoreCompiler.class);
        }

        @Bean
        public ParserExceptionHandler parserExceptionHandler() {
            return mock(ParserExceptionHandler.class);
        }

        @Bean
        public SlangCompiler slangCompiler() {
            SlangCompilerImpl slangCompiler = new SlangCompilerImpl();

            slangCompiler.setCachedPrecompileService(cachePrecompileService());
            slangCompiler.setCompileValidator(compileValidator());
            slangCompiler.setScoreCompiler(scoreCompiler());
            slangCompiler.setSlangModeller(slangModeller());
            slangCompiler.setSystemPropertyValidator(systemPropertyValidator());
            slangCompiler.setYamlParser(yamlParser());

            return slangCompiler;
        }

        @Bean
        public CachedPrecompileService cachePrecompileService() {
            return new CachedPrecompileServiceImpl();
        }

        @Bean
        public CompileValidator compileValidator() {
            return mock(CompileValidator.class);
        }

        @Bean
        public SystemPropertyValidator systemPropertyValidator() {
            return mock(SystemPropertyValidator.class);
        }

        @Bean
        public ExecutableValidator executableValidator() {
            return new ExecutableValidatorImpl();
        }

    }
}
