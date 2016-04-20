package io.cloudslang.lang.compiler.modeller.transformers;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.parser.YamlParser;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import io.cloudslang.lang.entities.bindings.Argument;
import junit.framework.Assert;
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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
@ContextConfiguration(classes = DoTransformerTest.Config.class)
public class DoTransformerTest {

    @Autowired
    private DoTransformer doTransformer;

    @Autowired
    private YamlParser yamlParser;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testTransformExpression() throws Exception {
        Map doArgumentsMap = loadFirstStepFromFile("/flow_with_data.yaml");
        @SuppressWarnings("unchecked") List<Argument> arguments = doTransformer.transform(doArgumentsMap);
        Assert.assertFalse(arguments.isEmpty());
        Assert.assertEquals(3, arguments.size());
        Argument argument = arguments.iterator().next();
        Assert.assertEquals("city",argument.getName());
        Assert.assertEquals("city_name", argument.getValue());
        Assert.assertEquals(true, argument.isPrivateArgument());
    }

    @Test
    public void testTransformNoValue() throws Exception {
        Map doArgumentsMap = loadFirstStepFromFile("/basic_flow.yaml");
        @SuppressWarnings("unchecked") List<Argument> arguments = doTransformer.transform(doArgumentsMap);
        Assert.assertFalse(arguments.isEmpty());
        Assert.assertEquals(3, arguments.size());
        Argument argument = arguments.get(1);
        Assert.assertEquals("port",argument.getName());
        Assert.assertEquals(false, argument.isPrivateArgument());
    }

    @Test
    public void testTransformConst() throws Exception {
        Map doArgumentsMap = loadFirstStepFromFile("/flow_with_data.yaml");
        @SuppressWarnings("unchecked") List<Argument> arguments = doTransformer.transform(doArgumentsMap);
        Assert.assertFalse(arguments.isEmpty());
        Assert.assertEquals(3, arguments.size());
        Argument argument = arguments.get(1);
        Assert.assertEquals("country", argument.getName());
        Assert.assertEquals("Israel", argument.getValue());
    }

    @Test
    public void testTransformEmptyArgumentExpression() throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> doArgumentsMap = loadFirstStepFromFile("/corrupted/flow_with_empty_argument_expression.yaml");
        @SuppressWarnings("unchecked") List<Argument> arguments = doTransformer.transform(doArgumentsMap);
        Assert.assertFalse(arguments.isEmpty());
        Assert.assertEquals(2, arguments.size());
        Argument argument = arguments.get(1);
        Assert.assertEquals("country", argument.getName());
        Assert.assertEquals(true, argument.isPrivateArgument());
    }

    @Test
    public void testTransformInvalidArgument() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("argument");
        exception.expectMessage("22");

        @SuppressWarnings("unchecked")
        Map<String, Object> doArgumentsMap = loadFirstStepFromFile("/corrupted/flow_with_invalid_argument.yaml");

        doTransformer.transform(doArgumentsMap);
    }

    @Test
    public void testOneLinerTransformIsInvalid() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step arguments should be defined using a standard YAML list.");

        @SuppressWarnings("unchecked")
        Map<String, Object> doArgumentsMap = loadFirstStepFromFile("/step-args-in-list/flow_arguments_one_liner.yaml");

        doTransformer.transform(doArgumentsMap);

    }

    private Map loadFirstStepFromFile(String path) throws URISyntaxException {
        Map doArgumentsMap = new LinkedHashMap();
        URL resource = getClass().getResource(path);
        File file = new File(resource.toURI());
        ParsedSlang parsedSlang = yamlParser.parse(SlangSource.fromFile(file));
        @SuppressWarnings("unchecked")
        List<Map<String, Map>> flow = (List<Map<String, Map>>) parsedSlang.getFlow().get(SlangTextualKeys.WORKFLOW_KEY);
        for(Map<String, Map> step : flow){
            if(step.keySet().iterator().next().equals("CheckWeather")){
                doArgumentsMap = (Map) step.values().iterator().next().get(SlangTextualKeys.DO_KEY);
                return doArgumentsMap;
            }
        }
        return doArgumentsMap;
    }

    @Configuration
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
        public DoTransformer inputTransformer() {
            return new DoTransformer();
        }

    }
}
