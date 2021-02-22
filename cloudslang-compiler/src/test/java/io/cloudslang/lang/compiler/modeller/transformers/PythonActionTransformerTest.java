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
import io.cloudslang.lang.compiler.modeller.result.BasicTransformModellingResult;
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.compiler.parser.YamlParser;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.compiler.validator.ExecutableValidatorImpl;
import io.cloudslang.lang.compiler.validator.ExternalPythonScriptValidator;
import io.cloudslang.lang.compiler.validator.ExternalPythonScriptValidatorImpl;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidator;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidatorImpl;
import io.cloudslang.lang.entities.ScoreLangConstants;
import junit.framework.Assert;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.cloudslang.lang.compiler.modeller.transformers.AbstractTransformer.INVALID_KEYS_ERROR_MESSAGE_PREFIX;
import static io.cloudslang.lang.compiler.modeller.transformers.DependencyFormatValidator.INVALID_DEPENDENCY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Date: 11/11/2014
 *
 * @author Bonczidai Levente
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PythonActionTransformerTest.Config.class)
public class PythonActionTransformerTest extends TransformersTestParent {

    @Autowired
    private PythonActionTransformer pythonActionTransformer;
    @Autowired
    private YamlParser yamlParser;
    private Map<String, Serializable> initialPythonActionSimple;
    private Map<String, Serializable> initialPythonActionWithDependencies;
    private Map<String, Serializable> initialPythonActionInvalidKey;
    private Map<String, Serializable> initialExternalPythonAction1;
    private Map<String, Serializable> initialExternalPythonAction2;
    private Map<String, Serializable> initialExternalPythonInvalidAction;
    private Map<String, Serializable> initialExternalPythonInvalidAction2;
    private Map<String, Serializable> expectedPythonActionSimple;
    private Map<String, Serializable> commentedExecuteMethod;
    private Map<String, Serializable> executeInClass;

    @Before
    public void init() throws URISyntaxException {
        initialPythonActionSimple = loadPythonActionData("/python_action_simple.sl");
        initialPythonActionWithDependencies = loadPythonActionData("/python_action_with_dependencies.sl");
        initialPythonActionInvalidKey = loadPythonActionData("/corrupted/python_action_invalid_key.sl");
        initialExternalPythonAction1 = loadPythonActionData("/python_external_valid_action1.sl", "a,b,c");
        initialExternalPythonAction2 = loadPythonActionData("/python_external_valid_action2.sl");
        initialExternalPythonInvalidAction = loadPythonActionData("/python_external_invalid_action1.sl");
        initialExternalPythonInvalidAction2 = loadPythonActionData("/python_external_invalid_action2.sl");
        commentedExecuteMethod = loadPythonActionData("/python_external_commented_overloaded_execute.sl");
        executeInClass = loadPythonActionData("/python_external_execute_in_class.sl");


        expectedPythonActionSimple = new LinkedHashMap<>();
        expectedPythonActionSimple.put(SlangTextualKeys.PYTHON_ACTION_SCRIPT_KEY, "pass");
        expectedPythonActionSimple.put(SlangTextualKeys.INPUTS_KEY, new ArrayList<>());
        expectedPythonActionSimple.put(ScoreLangConstants.PYTHON_ACTION_USE_JYTHON_KEY, true);
    }

    @Test
    public void testTransformSimple() throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Serializable> actualPythonActionSimple =
                transformAndThrowErrorIfExists(pythonActionTransformer, initialPythonActionSimple);
        Assert.assertEquals(expectedPythonActionSimple, actualPythonActionSimple);
    }

    @Test
    public void testTransformWithDependencies() throws Exception {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transformAndThrowErrorIfExists(pythonActionTransformer, initialPythonActionWithDependencies));
        assertEquals("Following tags are invalid: [dependencies]. " +
                "Please take a look at the supported features per versions link", exception.getMessage());
    }

    @Ignore("Enable when `dependencies` tag will be added")
    @Test
    public void testTransformWithEmptyOneEmptyPart() throws Exception {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transformAndThrowFirstException(pythonActionTransformer,
                        loadPythonActionData("/python_action_with_dependencies_1_empty_part.sl")));
        assertTrue(exception.getMessage().contains(INVALID_DEPENDENCY));
    }

    @Ignore("Enable when `dependencies` tag will be added")
    @Test
    public void testTransformWithEmptyDependencies() throws Exception {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transformAndThrowFirstException(pythonActionTransformer,
                        loadPythonActionData("/python_action_with_dependencies_1_part.sl")));
        assertTrue(exception.getMessage().contains(INVALID_DEPENDENCY));
    }

    @Ignore("Enable when `dependencies` tag will be added")
    @Test
    public void testTransformWithAllEmptyParts() throws Exception {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transformAndThrowFirstException(pythonActionTransformer,
                        loadPythonActionData("/python_action_with_dependencies_2_parts.sl")));
        assertTrue(exception.getMessage().contains(INVALID_DEPENDENCY));

    }

    @Ignore("Enable when `dependencies` tag will be added")
    @Test
    public void testTransformWithOnePart() throws Exception {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transformAndThrowFirstException(pythonActionTransformer,
                        loadPythonActionData("/python_action_with_dependencies_all_empty_parts.sl")));
        assertTrue(exception.getMessage().contains(INVALID_DEPENDENCY));
    }

    @Ignore("Enable when `dependencies` tag will be added")
    @Test
    public void testTransformWithTwoEmptyParts() throws Exception {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transformAndThrowFirstException(pythonActionTransformer,
                        loadPythonActionData("/python_action_with_dependencies_empty.sl")));
        assertTrue(exception.getMessage().contains(INVALID_DEPENDENCY));
    }

    @Test
    public void testTransformInvalidKey() throws Exception {
        //noinspection unchecked
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transformAndThrowFirstException(pythonActionTransformer, initialPythonActionInvalidKey));
        assertTrue(exception.getMessage().contains(INVALID_KEYS_ERROR_MESSAGE_PREFIX));

    }

    @Test
    public void testTransformWithExternalPythonValid1() {
        transformAndThrowErrorIfExists(pythonActionTransformer, initialExternalPythonAction1);
    }

    @Test
    public void testTransformWithExternalPythonValid2() {
        transformAndThrowErrorIfExists(pythonActionTransformer, initialExternalPythonAction2);
    }

    @Test
    public void testTransformWithExternalInvalidPythonValid() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transformAndThrowErrorIfExists(pythonActionTransformer, initialExternalPythonInvalidAction));
        assertEquals("Method {execute} is missing or is invalid.", exception.getMessage());

    }

    @Test
    public void testTransformWithExternalInvalidPythonValid2() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transformAndThrowErrorIfExists(pythonActionTransformer, initialExternalPythonInvalidAction2));
        assertEquals("Overload of the execution method is not allowed.", exception.getMessage());
    }

    @Test
    public void testTransformerWithCommentedAndUncommentedExecuteMethod() {
        transformAndThrowErrorIfExists(pythonActionTransformer, commentedExecuteMethod);
    }

    @Test
    public void testTransformerWithExecuteInClass() {
        transformAndThrowErrorIfExists(pythonActionTransformer, executeInClass);
    }

    private Map<String, Serializable> loadPythonActionData(String filePath) throws URISyntaxException {
        return loadPythonActionData(filePath, "");
    }

    private Map<String, Serializable> loadPythonActionData(String filePath, String inputs) throws URISyntaxException {
        URL resource = getClass().getResource(filePath);
        ParsedSlang file = yamlParser.parse(SlangSource.fromFile(new File(resource.toURI())));
        Map op = file.getOperation();
        @SuppressWarnings("unchecked")
        Map<String, Serializable> returnMap = (Map) op.get(SlangTextualKeys.PYTHON_ACTION_KEY);
        ArrayList<String> inputsList = new ArrayList<>();
        if (StringUtils.isNotBlank(inputs)) {
            inputsList = new ArrayList<>(Arrays.asList(inputs.split(",")));
        }
        returnMap.put(SlangTextualKeys.INPUTS_KEY, inputsList);
        return returnMap;
    }


    private Map<String, Serializable> transformAndThrowErrorIfExists(PythonActionTransformer pythonActionTransformer,
                                                                     Map<String, Serializable> rawData) {
        TransformModellingResult<Map<String, Serializable>> transformModellingResult =
                pythonActionTransformer.transform(rawData);
        BasicTransformModellingResult<Map<String, Serializable>> basicTransformModellingResult =
                (BasicTransformModellingResult<Map<String, Serializable>>) transformModellingResult;
        List<RuntimeException> errors = basicTransformModellingResult.getErrors();
        if (CollectionUtils.isNotEmpty(errors)) {
            throw errors.get(0);
        } else {
            return basicTransformModellingResult.getTransformedData();
        }
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
        public PythonActionTransformer pythonActionTransformer() {
            PythonActionTransformer pythonActionTransformer = new PythonActionTransformer();
            pythonActionTransformer.setExternalPythonScriptValidator(externalPythonScriptValidator());
            pythonActionTransformer.setDependencyFormatValidator(dependencyFormatValidator());
            return pythonActionTransformer;
        }

        @Bean
        public DependencyFormatValidator dependencyFormatValidator() {
            return new DependencyFormatValidator();
        }

        @Bean
        public ExternalPythonScriptValidator externalPythonScriptValidator() {
            return new ExternalPythonScriptValidatorImpl();
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
