/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.validation;

import com.google.common.collect.Lists;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Metadata;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.entities.bindings.ScriptFunction;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class StaticValidatorTest {

    private static final Set<String> SYSTEM_PROPERTY_DEPENDENCIES = Collections.emptySet();

    private StaticValidator staticValidator;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        staticValidator = new StaticValidatorImpl();
    }

    @Test
    public void missingDescriptionForInput() throws URISyntaxException {
        List<Input> inputList = Lists.newArrayList(new Input.InputBuilder("input1", "value1").build(),
                new Input.InputBuilder("input2", "value2").build(),
                new Input.InputBuilder("input3", "value3").build());
        final Flow newExecutable = new Flow(null, null, null, "no_dependencies", "empty_flow", inputList, null, null,
                new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
        Metadata metadata = new Metadata();
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("input1", "description1");
        inputMap.put("input2", "description2");
        metadata.setInputs(inputMap);

        Queue<RuntimeException> exceptions = new ArrayDeque<>();
        staticValidator.validateSlangFile(new File(getClass().getResource("/no_dependencies/empty_flow.sl").toURI()),
                newExecutable, metadata, true, exceptions);

        exception.expect(RuntimeException.class);
        exception.expectMessage("Error for executable no_dependencies.empty_flow: " +
                "Input 'input3' is missing description.");

        throw exceptions.peek();
    }

    @Test
    public void missingDescriptionForPrivateInputInput() throws URISyntaxException {
        List<Input> inputList = Lists.newArrayList(new Input.InputBuilder("input1", "value1").build(),
                new Input.InputBuilder("input2", "value2").build(),
                new Input.InputBuilder("input3", "value3").withPrivateInput(true).build());
        final Flow newExecutable = new Flow(null, null, null, "no_dependencies", "empty_flow", inputList, null, null,
                new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
        Metadata metadata = new Metadata();
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("input1", "description1");
        inputMap.put("input2", "description2");
        metadata.setInputs(inputMap);

        Queue<RuntimeException> exceptions = new ArrayDeque<>();

        staticValidator.validateSlangFile(new File(getClass().getResource("/no_dependencies/empty_flow.sl").toURI()),
                newExecutable, metadata, true, exceptions);
    }

    @Test
    public void missingDescriptionForOutput() throws URISyntaxException {
        List<Output> outputList = Lists.newArrayList(new Output("output1", ValueFactory.create("value1"),
                        Collections.<ScriptFunction>emptySet(), Collections.<String>emptySet()),
                new Output("output2", ValueFactory.create("value2"),
                        Collections.<ScriptFunction>emptySet(), Collections.<String>emptySet()),
                new Output("output3", ValueFactory.create("value3"),
                        Collections.<ScriptFunction>emptySet(), Collections.<String>emptySet()));
        final Flow newExecutable = new Flow(null, null, null, "no_dependencies", "empty_flow", null, outputList, null,
                new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
        Metadata metadata = new Metadata();
        Map<String, String> outputMap = new HashMap<>();
        outputMap.put("output1", "description1");
        outputMap.put("output2", "description2");
        metadata.setOutputs(outputMap);

        Queue<RuntimeException> exceptions = new ArrayDeque<>();

        staticValidator.validateSlangFile(new File(getClass().getResource("/no_dependencies/empty_flow.sl").toURI()),
                newExecutable, metadata, true, exceptions);
        exception.expect(RuntimeException.class);
        exception.expectMessage("Error for executable no_dependencies.empty_flow: " +
                "Output 'output3' is missing description.");
        throw exceptions.peek();
    }

    @Test
    public void missingDescriptionForResult() throws URISyntaxException {
        List<Result> resultList = Lists.newArrayList(new Result("result1", ValueFactory.create("value1")),
                new Result("result2", ValueFactory.create("value2")),
                new Result("result3", ValueFactory.create("value3")));
        final Flow newExecutable = new Flow(null, null, null, "no_dependencies", "empty_flow", null, null, resultList,
                new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
        Metadata metadata = new Metadata();
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("result1", "description1");
        inputMap.put("result2", "description2");
        metadata.setResults(inputMap);

        Queue<RuntimeException> exceptions = new ArrayDeque<>();
        staticValidator.validateSlangFile(new File(getClass().getResource("/no_dependencies/empty_flow.sl").toURI()),
                newExecutable, metadata, true, exceptions);

        exception.expect(RuntimeException.class);
        exception.expectMessage("Error for executable no_dependencies.empty_flow: " +
                "Result 'result3' is missing description.");
        throw exceptions.peek();
    }

    @Test
    public void missingDescriptionEntirelyForResult() throws URISyntaxException {
        List<Result> resultList = Lists.newArrayList(new Result("result1", ValueFactory.create("value1")),
                new Result("result2", ValueFactory.create("value2")),
                new Result("result3", ValueFactory.create("value3")));
        Flow newExecutable = new Flow(null, null, null, "no_dependencies", "empty_flow", null, null, resultList,
                new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
        Metadata metadata = new Metadata();

        Queue<RuntimeException> exceptions = new ArrayDeque<>();
        staticValidator.validateSlangFile(new File(getClass().getResource("/no_dependencies/empty_flow.sl").toURI()),
                newExecutable, metadata, true, exceptions);

        exception.expect(RuntimeException.class);
        exception.expectMessage("Error for executable no_dependencies.empty_flow: " +
                "Results are missing description entirely.");
        throw exceptions.peek();
    }

}
