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

import com.google.common.collect.Lists;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.parser.YamlParser;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import java.io.File;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
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
@ContextConfiguration(classes=PythonActionTransformerTest.Config.class)
public class PythonActionTransformerTest {

    @Autowired
    private PythonActionTransformer pythonActionTransformer;
    @Autowired
    private YamlParser yamlParser;
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private Map initialPythonActionSimple;
    private Map initialPythonActionWithDependencies;
    private Map initialPythonActionInvalidKey;
    private Map<String, Serializable> expectedPythonActionSimple;
    private Map<String, Serializable> expectedPythonActionWithDependencies;

    @Before
    public void init() throws URISyntaxException {
        initialPythonActionSimple = loadPythonActionData("/python_action_simple.sl");
        initialPythonActionWithDependencies = loadPythonActionData("/python_action_with_dependencies.sl");
        initialPythonActionInvalidKey = loadPythonActionData("/corrupted/python_action_invalid_key.sl");

        expectedPythonActionSimple = new HashMap<>();
        expectedPythonActionSimple.put(SlangTextualKeys.PYTHON_ACTION_SCRIPT_KEY, "pass");
        expectedPythonActionWithDependencies = new HashMap<>(expectedPythonActionSimple);
        ArrayList<String> dependencies = Lists.newArrayList("dep1", "dep2");
        expectedPythonActionWithDependencies.put(SlangTextualKeys.PYTHON_ACTION_DEPENDENCIES_KEY, dependencies);
    }

    @Test
    public void testTransformSimple() throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Serializable> actualPythonActionSimple = pythonActionTransformer.transform(initialPythonActionSimple);
        Assert.assertEquals(expectedPythonActionSimple, actualPythonActionSimple);
    }

    @Test
    public void testTransformWithDependencies() throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Serializable> actualPythonActionSimple = pythonActionTransformer.transform(initialPythonActionWithDependencies);
        Assert.assertEquals(expectedPythonActionWithDependencies, actualPythonActionSimple);
    }

    @Test
    public void testTransformInvalidKey() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage(AbstractTransformer.INVALID_KEYS_ERROR_MESSAGE_PREFIX);
        exception.expectMessage("invalid_key");
        //noinspection unchecked
        pythonActionTransformer.transform(initialPythonActionInvalidKey);
    }

    private Map loadPythonActionData(String filePath) throws URISyntaxException {
        URL resource = getClass().getResource(filePath);
        ParsedSlang file = yamlParser.parse(SlangSource.fromFile(new File(resource.toURI())));
        Map op = file.getOperation();
        return (Map) op.get(SlangTextualKeys.PYTHON_ACTION_KEY);
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
        public PythonActionTransformer pythonActionTransformer() {
            return new PythonActionTransformer();
        }
    }
}
