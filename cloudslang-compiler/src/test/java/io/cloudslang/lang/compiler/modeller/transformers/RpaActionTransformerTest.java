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
import io.cloudslang.lang.compiler.modeller.model.RpaStep;
import io.cloudslang.lang.compiler.modeller.result.BasicTransformModellingResult;
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.compiler.parser.YamlParser;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.compiler.validator.ExecutableValidatorImpl;
import io.cloudslang.lang.compiler.validator.PreCompileValidator;
import io.cloudslang.lang.compiler.validator.PreCompileValidatorImpl;
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

import java.io.File;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static io.cloudslang.lang.entities.ScoreLangConstants.RPA_ACTION_GAV_KEY;
import static io.cloudslang.lang.entities.ScoreLangConstants.RPA_STEPS_KEY;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RpaActionTransformerTest.Config.class})
public class RpaActionTransformerTest extends TransformersTestParent {

    @Autowired
    private RpaActionTransformer rpaActionTransformer;

    @Autowired
    private YamlParser yamlParser;

    @Autowired
    private RpaStepsTransformer rpaStepsTransformer;

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private Map<String, Serializable> initialRpaActionSimple;

    @Before
    public void init() throws URISyntaxException {
        initialRpaActionSimple = loadRpaActionData("/rpa-operation/simple_valid_rpa_op.sl");

    }

    @Test
    public void testTransformSimple() {
        doReturn(new BasicTransformModellingResult<>(newArrayList(new RpaStep()), new ArrayList<>()))
                .when(rpaStepsTransformer).transform(any(), any());

        Map<String, Serializable> expectedRpaActionSimple = new LinkedHashMap<>();
        expectedRpaActionSimple.put(RPA_ACTION_GAV_KEY, "rpa:rpaf.simple_valid_rpa_op:1.0.0");
        expectedRpaActionSimple.put(RPA_STEPS_KEY, newArrayList(new RpaStep()));

        TransformModellingResult<Map<String, Serializable>> transformedAction = rpaActionTransformer
                .transform(new HashMap<>(initialRpaActionSimple));
        assertEquals(expectedRpaActionSimple, transformedAction.getTransformedData());
        assertThat(transformedAction.getErrors(), is(empty()));
    }

    @Test
    public void testTransformWithInvalidGav() {
        doReturn(new BasicTransformModellingResult<>(newArrayList(new RpaStep()), new ArrayList<>()))
                .when(rpaStepsTransformer).transform(any(), any());

        HashMap<String, Serializable> rawData = new HashMap<>(initialRpaActionSimple);
        rawData.put(RPA_ACTION_GAV_KEY, "invalid-format");

        TransformModellingResult<Map<String, Serializable>> transformedAction = rpaActionTransformer
                .transform(rawData);
        assertNull(transformedAction.getTransformedData());
        assertThat(transformedAction.getErrors(), is(not(empty())));
        assertEquals(transformedAction.getErrors().get(0).getMessage(),
                "Dependency definition should contain exactly [3] non empty parts separated by ':'");
    }

    @Test
    public void testTransformWithIllegalKeys() {
        doReturn(new BasicTransformModellingResult<>(newArrayList(new RpaStep()), new ArrayList<>()))
                .when(rpaStepsTransformer).transform(any(), any());

        HashMap<String, Serializable> rawData = new HashMap<>(initialRpaActionSimple);
        rawData.put("illegal-arg", "illegal-arg-value");

        TransformModellingResult<Map<String, Serializable>> transformedAction = rpaActionTransformer
                .transform(rawData);
        assertNull(transformedAction.getTransformedData());
        assertThat(transformedAction.getErrors(), is(not(empty())));
        assertEquals(transformedAction.getErrors().get(0).getMessage(),
                "Following tags are invalid: [illegal-arg]. " +
                        "Please take a look at the supported features per versions link");
    }

    @Test
    public void testTransformWithMissingKeys() {
        doReturn(new BasicTransformModellingResult<>(newArrayList(new RpaStep()), new ArrayList<>()))
                .when(rpaStepsTransformer).transform(any(), any());

        HashMap<String, Serializable> rawData = new HashMap<>(initialRpaActionSimple);
        rawData.remove(RPA_ACTION_GAV_KEY);
        rawData.remove(RPA_STEPS_KEY);

        TransformModellingResult<Map<String, Serializable>> transformedAction = rpaActionTransformer
                .transform(rawData);
        assertNull(transformedAction.getTransformedData());
        assertThat(transformedAction.getErrors(), is(not(empty())));
        assertEquals(transformedAction.getErrors().get(0).getMessage(),
                "Following tags are missing: [gav, steps]");
    }

    @Test
    public void testTransformWithNoSteps() {
        doReturn(new BasicTransformModellingResult<>(newArrayList(), new ArrayList<>()))
                .when(rpaStepsTransformer).transform(any(), any());

        HashMap<String, Serializable> rawData = new HashMap<>(initialRpaActionSimple);
        rawData.put(RPA_STEPS_KEY, new ArrayList<>());
        Map<String, Serializable> expectedRpaActionSimple = new LinkedHashMap<>();
        expectedRpaActionSimple.put(RPA_ACTION_GAV_KEY, "rpa:rpaf.simple_valid_rpa_op:1.0.0");
        expectedRpaActionSimple.put(RPA_STEPS_KEY, newArrayList());

        TransformModellingResult<Map<String, Serializable>> transformedAction = rpaActionTransformer
                .transform(rawData);
        assertEquals(transformedAction.getTransformedData(), expectedRpaActionSimple);
        assertThat(transformedAction.getErrors(), is(not(empty())));
        assertEquals(transformedAction.getErrors().get(0).getMessage(),
                "Error compiling rpa operation: missing 'steps' data.");
    }

    @Test
    public void testTransformWithNullMap() {
        doReturn(new BasicTransformModellingResult<>(newArrayList(new RpaStep()), new ArrayList<>()))
                .when(rpaStepsTransformer).transform(any(), any());

        Map<String, Serializable> actualRpaActionSimple = rpaActionTransformer
                .transform(null).getTransformedData();
        assertNull(actualRpaActionSimple);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Serializable> loadRpaActionData(String filePath) throws URISyntaxException {
        URL resource = getClass().getResource(filePath);
        ParsedSlang file = yamlParser.parse(SlangSource.fromFile(new File(resource.toURI())));
        Map op = file.getOperation();
        return (Map<String, Serializable>) op.get(SlangTextualKeys.RPA_ACTION_KEY);
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
        public RpaActionTransformer rpaActionTransformer(RpaStepsTransformer rpaStepsTransformer) {
            return new RpaActionTransformer(dependencyFormatValidator(),
                    preCompileValidatorImpl(),
                    rpaStepsTransformer);
        }

        @Bean
        public DependencyFormatValidator dependencyFormatValidator() {
            return new DependencyFormatValidator();
        }

        @Bean
        public PreCompileValidator preCompileValidatorImpl() {
            PreCompileValidatorImpl preCompileValidator = new PreCompileValidatorImpl();
            preCompileValidator.setExecutableValidator(executableValidator());
            return preCompileValidator;
        }

        @Bean
        public RpaStepsTransformer rpaStepsTransformer() {
            return mock(RpaStepsTransformer.class);
        }

        @Bean
        public ExecutableValidator executableValidator() {
            return new ExecutableValidatorImpl();
        }

    }
}
