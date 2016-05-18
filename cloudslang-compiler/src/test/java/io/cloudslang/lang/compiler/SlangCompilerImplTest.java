/**
 * ****************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 * <p/>
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * *****************************************************************************
 */
package io.cloudslang.lang.compiler;

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.modeller.SlangModeller;
import io.cloudslang.lang.compiler.parser.YamlParser;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import io.cloudslang.lang.compiler.scorecompiler.ScoreCompiler;
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
import org.yaml.snakeyaml.Yaml;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Bonczidai Levente
 * @since 4/12/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerImplTest.Config.class)
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
        when(yamlParserMock.parse(eq(slangSource))).thenReturn(parsedSlangInvalidTypeMock);
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
        when(parsedSlangMock.getType()).thenReturn(ParsedSlang.Type.SYSTEM_PROPERTY_FILE);

        String namespace = "a.b";
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

        @Bean
        public YamlParser yamlParser() {
            return mock(YamlParser.class);
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
        public Yaml yaml() {
            return mock(Yaml.class);
        }

        @Bean
        public ParserExceptionHandler parserExceptionHandler() {
            return mock(ParserExceptionHandler.class);
        }

        @Bean
        public SlangCompiler slangCompiler() {
            return new SlangCompilerImpl();
        }

    }
}
