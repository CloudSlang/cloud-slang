/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.entities.bindings.ScriptFunction;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;

import java.io.IOException;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DeserializeTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DeserializeTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper().enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    private <T> void testToAndFromJson(Object objToTest, Class<T> type) throws IOException {
        //jackson
        String objAsString = mapper.writeValueAsString(objToTest);
        T objAfterDeserialize = mapper.readValue(objAsString, type);
        assertEquals(objToTest, objAfterDeserialize);
    }

    @Test
    public void testDeserializeInput() throws IOException {
        Input input = new Input.InputBuilder("new_input", "some_expression", true)
                .withRequired(true)
                .withPrivateInput(true)
                .withFunctionDependencies(Sets.newHashSet(ScriptFunction.GET))
                .withSystemPropertyDependencies(Sets.newHashSet("a.b.c.prop1", "a.b.c.prop2"))
                .build();
        testToAndFromJson(input, Input.class);
    }

    @Test
    public void testDeserializeArgument() throws IOException {
        Argument argument = new Argument(
                "new_argument",
                ValueFactory.create("some_expression")
        );
        testToAndFromJson(argument, Argument.class);
    }

    @Test
    public void testDeserializeOutput() throws IOException {
        Output output = new Output(
                "new_output",
                ValueFactory.create("some_expression"));
        testToAndFromJson(output, Output.class);
    }

    @Test
    public void testDeserializeResult() throws IOException {
        Result result = new Result(
                "new_result",
                ValueFactory.create("some_expression"));
        testToAndFromJson(result, Result.class);
    }

    @Test
    public void testDeserializeResultNavigation() throws IOException {
        ResultNavigation resultNavigation = new ResultNavigation(
                1L,
                "a preset result");
        testToAndFromJson(resultNavigation, ResultNavigation.class);
    }

    @Test
    public void testDeserializeListForLoopStatement() throws IOException {
        LoopStatement listForLoopStatement = new ListLoopStatement("varName", "expression",
                new HashSet<ScriptFunction>(), new HashSet<String>(), false);
        testToAndFromJson(listForLoopStatement, ListLoopStatement.class);
    }

    @Test
    public void testDeserializeMapForLoopStatement() throws IOException {
        MapLoopStatement mapLoopStatement = new MapLoopStatement("keyName", "valueName", "expression",
                new HashSet<ScriptFunction>(), new HashSet<String>());
        testToAndFromJson(mapLoopStatement, MapLoopStatement.class);
    }

    @Test
    public void testDeserializeParallelLoopStatement() throws IOException {
        ListLoopStatement parallelLoopStatement = new ListLoopStatement("varName", "expression",
                new HashSet<ScriptFunction>(), new HashSet<String>(), true);
        testToAndFromJson(parallelLoopStatement, ListLoopStatement.class);
    }

    @Test
    public void testDeserializeSystemProperty() throws IOException {
        SystemProperty systemProperty = new SystemProperty("a.b", "c.host", "localhost");
        testToAndFromJson(systemProperty, SystemProperty.class);
    }

    @Configuration
    @ComponentScan("io.cloudslang.lang.entities")
    static class Config {
    }
}
