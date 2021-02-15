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

import com.google.common.collect.ImmutableMap;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.parser.YamlParser;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.compiler.validator.ExecutableValidatorImpl;
import io.cloudslang.lang.compiler.validator.PreCompileValidator;
import io.cloudslang.lang.compiler.validator.PreCompileValidatorImpl;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidator;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidatorImpl;
import io.cloudslang.lang.entities.bindings.Argument;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DoTransformerTest.Config.class, SlangCompilerSpringConfig.class})
public class DoTransformerTest extends TransformersTestParent {

    @Autowired
    private DoTransformer doTransformer;

    @Autowired
    private YamlParser yamlParser;

    @Test
    public void testTransformExpression() throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> doArgumentsMap = loadFirstStepFromFile("/flow_with_data.yaml");
        @SuppressWarnings("unchecked")
        List<Argument> arguments = doTransformer.transform(doArgumentsMap).getTransformedData();
        Assert.assertFalse(arguments.isEmpty());
        Assert.assertEquals(3, arguments.size());
        Argument argument = arguments.iterator().next();
        Assert.assertEquals("city", argument.getName());
        Assert.assertEquals("city_name", argument.getValue().get());
        Assert.assertEquals(true, argument.isPrivateArgument());
    }

    @Test
    public void testTransformNoValue() throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> doArgumentsMap = loadFirstStepFromFile("/basic_flow.yaml");
        @SuppressWarnings("unchecked")
        List<Argument> arguments = doTransformer.transform(doArgumentsMap).getTransformedData();
        Assert.assertFalse(arguments.isEmpty());
        Assert.assertEquals(3, arguments.size());
        Argument argument = arguments.get(1);
        Assert.assertEquals("port", argument.getName());
        Assert.assertEquals(false, argument.isPrivateArgument());
    }

    @Test
    public void testTransformConst() throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> doArgumentsMap = loadFirstStepFromFile("/flow_with_data.yaml");
        @SuppressWarnings("unchecked")
        List<Argument> arguments = doTransformer.transform(doArgumentsMap).getTransformedData();
        Assert.assertFalse(arguments.isEmpty());
        Assert.assertEquals(3, arguments.size());
        Argument argument = arguments.get(1);
        Assert.assertEquals("country", argument.getName());
        Assert.assertEquals("Israel", argument.getValue().get());
    }

    @Test
    public void testTransformEmptyArgumentExpression() throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> doArgumentsMap =
                loadFirstStepFromFile("/corrupted/flow_with_empty_argument_expression.yaml");
        @SuppressWarnings("unchecked")
        List<Argument> arguments = doTransformer.transform(doArgumentsMap).getTransformedData();
        Assert.assertFalse(arguments.isEmpty());
        Assert.assertEquals(2, arguments.size());
        Argument argument = arguments.get(1);
        Assert.assertEquals("country", argument.getName());
        Assert.assertEquals(true, argument.isPrivateArgument());
    }

    @Test
    public void testTransformInvalidArgument() throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> doArgumentsMap = loadFirstStepFromFile("/corrupted/flow_with_invalid_argument.yaml");
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transformAndThrowFirstException(doTransformer, doArgumentsMap));
        assertEquals("Could not transform step argument: 22", exception.getMessage());

    }

    @Test
    public void testOneLinerTransformIsInvalid() throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> doArgumentsMap = loadFirstStepFromFile("/step-args-in-list/flow_arguments_one_liner.yaml");
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transformAndThrowFirstException(doTransformer, doArgumentsMap));
        assertEquals("Step arguments should be defined using a standard YAML list.", exception.getMessage());
    }

    @Test
    public void testTransformArgumentsWithPrompts() throws URISyntaxException {
        Map doArgumentsMap = loadFirstStepFromFile("/step-args-in-list/flow_arguments_with_prompts.yaml");

        transformAndAssertNoErrorsTransformer(doTransformer, doArgumentsMap);
    }

    private Map loadFirstStepFromFile(String path) throws URISyntaxException {
        Map doArgumentsMap = new LinkedHashMap();
        URL resource = getClass().getResource(path);
        File file = new File(resource.toURI());
        ParsedSlang parsedSlang = yamlParser.parse(SlangSource.fromFile(file));
        @SuppressWarnings("unchecked")
        List<Map<String, Map>> flow = (List<Map<String, Map>>) parsedSlang.getFlow().get(SlangTextualKeys.WORKFLOW_KEY);
        for (Map<String, Map> step : flow) {
            if (step.keySet().iterator().next().equals("CheckWeather")) {
                Object doValue = step.values().iterator().next().get(SlangTextualKeys.DO_KEY);
                if (doValue instanceof Map) {
                    doArgumentsMap = (Map) doValue;
                } else if (doValue instanceof List) {
                    doArgumentsMap = ImmutableMap.of(SlangTextualKeys.DO_KEY, doValue);
                }
                return doArgumentsMap;
            }
        }
        return doArgumentsMap;
    }

    @Configuration
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
        public DoTransformer inputTransformer() {
            return new DoTransformer();
        }

        @Bean
        public PreCompileValidator preCompileValidator() {
            return new PreCompileValidatorImpl();
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
