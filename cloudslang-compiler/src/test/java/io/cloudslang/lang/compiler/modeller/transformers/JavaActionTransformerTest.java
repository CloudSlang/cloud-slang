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
import io.cloudslang.lang.entities.ScoreLangConstants;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
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
import org.yaml.snakeyaml.introspector.BeanAccess;

/**
 * Date: 11/11/2014
 *
 * @author Bonczidai Levente
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=JavaActionTransformerTest.Config.class)
public class JavaActionTransformerTest {

    @Autowired
    private JavaActionTransformer javaActionTransformer;
    @Autowired
    private YamlParser yamlParser;
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private Map initialJavaActionSimple;
    private Map initialJavaActionWithDependencies;
    private Map initialJavaActionInvalidKey;
    private Map<String, String> expectedJavaActionSimple;
    private Map<String, String> expectedJavaActionWithDependencies;

    @Before
    public void init() throws URISyntaxException {
        initialJavaActionSimple = loadJavaActionData("/java_action_simple.sl");
        initialJavaActionWithDependencies = loadJavaActionData("/java_action_with_dependencies.sl");
        initialJavaActionInvalidKey = loadJavaActionData("/corrupted/java_action_invalid_key.sl");

        expectedJavaActionSimple = new HashMap<>();
        expectedJavaActionSimple.put(ScoreLangConstants.JAVA_ACTION_CLASS_KEY, "com.hp.thing");
        expectedJavaActionSimple.put(ScoreLangConstants.JAVA_ACTION_METHOD_KEY, "someMethod");
        expectedJavaActionWithDependencies = new HashMap<>(expectedJavaActionSimple);
        expectedJavaActionWithDependencies.put(SlangTextualKeys.JAVA_ACTION_GAV_KEY, "g:a:v");
    }

    @Test
    public void testTransformSimple() throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, String> actualJavaActionSimple = javaActionTransformer.transform(initialJavaActionSimple);
        Assert.assertEquals(expectedJavaActionSimple, actualJavaActionSimple);
    }

    @Test
    public void testTransformWithDependencies() throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, String> actualJavaActionSimple = javaActionTransformer.transform(initialJavaActionWithDependencies);
        Assert.assertEquals(expectedJavaActionWithDependencies, actualJavaActionSimple);
    }

    @Test
    public void testTransformInvalidKey() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage(AbstractTransformer.INVALID_KEYS_ERROR_MESSAGE_PREFIX);
        exception.expectMessage("invalid_key");
        //noinspection unchecked
        javaActionTransformer.transform(initialJavaActionInvalidKey);
    }

    private Map loadJavaActionData(String filePath) throws URISyntaxException {
        URL resource = getClass().getResource(filePath);
        ParsedSlang file = yamlParser.parse(SlangSource.fromFile(new File(resource.toURI())));
        Map op = file.getOperation();
        return (Map) op.get(SlangTextualKeys.JAVA_ACTION_KEY);
    }

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
        public JavaActionTransformer javaActionTransformer() {
            return new JavaActionTransformer();
        }
    }
}
