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
import io.cloudslang.lang.compiler.validator.SystemPropertyValidator;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidatorImpl;
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
import org.springframework.context.annotation.Scope;
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
@ContextConfiguration(classes = {JavaActionTransformerTest.Config.class, SlangCompilerSpringConfig.class})
public class JavaActionTransformerTest extends TransformersTestParent {

    @Autowired
    private JavaActionTransformer javaActionTransformer;

    @Autowired
    private YamlParser yamlParser;

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private Map<String, String> initialJavaActionSimple;
    private Map<String, String> initialJavaActionWithDependencies;
    private Map<String, String> initialJavaActionInvalidKey;
    private Map<String, String> expectedJavaActionSimple;
    private Map<String, String> expectedJavaActionWithDependencies;

    @Before
    public void init() throws URISyntaxException {
        initialJavaActionSimple = loadJavaActionData("/java_action_simple.sl");
        initialJavaActionWithDependencies = loadJavaActionData("/java_action_with_dependencies.sl");
        initialJavaActionInvalidKey = loadJavaActionData("/corrupted/java_action_invalid_key.sl");

        expectedJavaActionSimple = new HashMap<>();
        expectedJavaActionSimple.put(ScoreLangConstants.JAVA_ACTION_GAV_KEY, "some.group:some.artifact:some.version");
        expectedJavaActionSimple.put(ScoreLangConstants.JAVA_ACTION_CLASS_KEY, "com.hp.thing");
        expectedJavaActionSimple.put(ScoreLangConstants.JAVA_ACTION_METHOD_KEY, "someMethod");
        expectedJavaActionWithDependencies = new HashMap<>(expectedJavaActionSimple);
        expectedJavaActionWithDependencies.put(SlangTextualKeys.JAVA_ACTION_GAV_KEY, "g:a:v");
    }

    @Test
    public void testTransformSimple() throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, String> actualJavaActionSimple =
                javaActionTransformer.transform(initialJavaActionSimple).getTransformedData();
        Assert.assertEquals(expectedJavaActionSimple, actualJavaActionSimple);
    }

    @Test
    public void testTransformWithDependencies() throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, String> actualJavaActionSimple =
                javaActionTransformer.transform(initialJavaActionWithDependencies).getTransformedData();
        Assert.assertEquals(expectedJavaActionWithDependencies, actualJavaActionSimple);
    }

    @Test
    public void testTransformInvalidKey() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage(AbstractTransformer.INVALID_KEYS_ERROR_MESSAGE_PREFIX);
        exception.expectMessage("invalid_key");
        //noinspection unchecked
        transformAndThrowFirstException(javaActionTransformer, initialJavaActionInvalidKey);
    }

    @Test
    public void testTransformWithoutGav() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("Following tags are missing: [gav]");
        transformAndThrowFirstException(javaActionTransformer, loadJavaActionData("/java_action_wo_dependencies.sl"));
    }

    @Test
    public void testTransformWithEmptyOneEmptyPart() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage(DependencyFormatValidator.INVALID_DEPENDENCY);
        transformAndThrowFirstException(javaActionTransformer,
                loadJavaActionData("/java_action_with_dependencies_1_empty_part.sl"));
    }

    @Test
    public void testTransformWithEmptyDependencies() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage(DependencyFormatValidator.INVALID_DEPENDENCY);
        transformAndThrowFirstException(javaActionTransformer,
                loadJavaActionData("/java_action_with_dependencies_empty.sl"));
    }

    @Test
    public void testTransformWithAllEmptyParts() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage(DependencyFormatValidator.INVALID_DEPENDENCY);
        transformAndThrowFirstException(javaActionTransformer,
                loadJavaActionData("/java_action_with_dependencies_all_empty_parts.sl"));
    }

    @Test
    public void testTransformWithOnePart() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage(DependencyFormatValidator.INVALID_DEPENDENCY);
        transformAndThrowFirstException(javaActionTransformer,
                loadJavaActionData("/java_action_with_dependencies_1_part.sl"));
    }

    @Test
    public void testTransformWithTwoEmptyParts() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage(DependencyFormatValidator.INVALID_DEPENDENCY);
        transformAndThrowFirstException(javaActionTransformer,
                loadJavaActionData("/java_action_with_dependencies_2_parts.sl"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> loadJavaActionData(String filePath) throws URISyntaxException {
        URL resource = getClass().getResource(filePath);
        ParsedSlang file = yamlParser.parse(SlangSource.fromFile(new File(resource.toURI())));
        Map op = file.getOperation();
        return (Map<String, String>) op.get(SlangTextualKeys.JAVA_ACTION_KEY);
    }

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
        public JavaActionTransformer javaActionTransformer() {
            return new JavaActionTransformer();
        }

        @Bean
        public DependencyFormatValidator dependencyFormatValidator() {
            return new DependencyFormatValidator();
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
