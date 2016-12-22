/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.steps;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.dependency.api.services.MavenConfig;
import io.cloudslang.dependency.impl.services.DependencyServiceImpl;
import io.cloudslang.dependency.impl.services.MavenConfigImpl;
import io.cloudslang.lang.entities.ExecutableType;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.entities.bindings.values.SensitiveValue;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.entities.encryption.DummyEncryptor;
import io.cloudslang.lang.runtime.bindings.InputsBinding;
import io.cloudslang.lang.runtime.bindings.OutputsBinding;
import io.cloudslang.lang.runtime.bindings.ResultsBinding;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import io.cloudslang.lang.runtime.env.ParentFlowData;
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import io.cloudslang.runtime.impl.python.PythonExecutionCachedEngine;
import io.cloudslang.runtime.impl.python.PythonExecutionEngine;
import io.cloudslang.runtime.impl.python.PythonRuntimeServiceImpl;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventBusImpl;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ExecutableStepsTest.Config.class)
public class ExecutableStepsTest {

    @Autowired
    private ExecutableExecutionData executableSteps;

    @Autowired
    private InputsBinding inputsBinding;

    @Autowired
    private ResultsBinding resultsBinding;

    @Autowired
    private OutputsBinding outputsBinding;

    @Test
    public void testStart() throws Exception {
        executableSteps.startExecutable(new ArrayList<Input>(), new RunEnvironment(),
                new HashMap<String, Value>(), new ExecutionRuntimeServices(), "", 2L, ExecutableType.FLOW);
    }

    @Test
    public void testStartWithInput() throws Exception {
        List<Input> inputs = singletonList(new Input.InputBuilder("input1", "input1").build());
        RunEnvironment runEnv = new RunEnvironment();

        Map<String, Value> resultMap = new HashMap<>();
        resultMap.put("input1", ValueFactory.create(5));

        when(inputsBinding.bindInputs(eq(inputs), anyMap(), anySet())).thenReturn(resultMap);
        executableSteps.startExecutable(inputs, runEnv, new HashMap<String, Value>(),
                new ExecutionRuntimeServices(), "", 2L, ExecutableType.FLOW);

        Map<String, Value> opVars = runEnv.getStack().popContext().getImmutableViewOfVariables();
        Assert.assertTrue(opVars.containsKey("input1"));
        Assert.assertEquals(5, opVars.get("input1").get());

        Map<String, Value> callArg = runEnv.removeCallArguments();
        Assert.assertEquals(1, callArg.size());
        Assert.assertTrue(callArg.containsKey("input1"));
        Assert.assertEquals(5, callArg.get("input1").get());
    }

    @Test
    public void testBoundInputEvent() {
        List<Input> inputs = Arrays.asList(
                new Input.InputBuilder("input1", 5).build(),
                new Input.InputBuilder("input2", 3, true)
                        .withRequired(true)
                        .withPrivateInput(false)
                        .build()
        );
        final RunEnvironment runEnv = new RunEnvironment();
        final ExecutionRuntimeServices runtimeServices = new ExecutionRuntimeServices();
        Map<String, Value> resultMap = new HashMap<>();
        resultMap.put("input1", ValueFactory.create(inputs.get(0).getValue()));
        resultMap.put("input2", ValueFactory.create(inputs.get(1).getValue()));

        when(inputsBinding.bindInputs(eq(inputs), anyMap(), anySet())).thenReturn(resultMap);
        executableSteps.startExecutable(inputs, runEnv, new HashMap<String, Value>(),
                runtimeServices, "dockerizeStep", 2L, ExecutableType.FLOW);
        Collection<ScoreEvent> events = runtimeServices.getEvents();

        Assert.assertFalse(events.isEmpty());
        ScoreEvent boundInputEvent = null;
        for (ScoreEvent event : events) {
            if (event.getEventType().equals(ScoreLangConstants.EVENT_INPUT_END)) {
                boundInputEvent = event;
            }
        }
        Assert.assertNotNull(boundInputEvent);
        LanguageEventData eventData = (LanguageEventData) boundInputEvent.getData();
        Assert.assertTrue(eventData.containsKey(LanguageEventData.BOUND_INPUTS));
        @SuppressWarnings("unchecked") Map<String, Serializable> inputsBounded =
                (Map<String, Serializable>) eventData.get(LanguageEventData.BOUND_INPUTS);

        Assert.assertNotNull(eventData.getStepName());
        Assert.assertEquals(LanguageEventData.StepType.FLOW, eventData.getStepType());
        Assert.assertEquals("dockerizeStep", eventData.getStepName());

        // verify input names are in defined order and have the expected value
        Set<Map.Entry<String, Serializable>> inputEntries = inputsBounded.entrySet();
        Iterator<Map.Entry<String, Serializable>> inputNamesIterator = inputEntries.iterator();

        Map.Entry<String, Serializable> firstInput = inputNamesIterator.next();
        org.junit.Assert
                .assertEquals("Inputs are not in defined order in end inputs binding event",
                        "input1", firstInput.getKey());
        org.junit.Assert
                .assertEquals(5, firstInput.getValue());

        Map.Entry<String, Serializable> secondInput = inputNamesIterator.next();
        org.junit.Assert
                .assertEquals("Inputs are not in defined order in end inputs binding event",
                        "input2", secondInput.getKey());
        org.junit.Assert
                .assertEquals(SensitiveValue.SENSITIVE_VALUE_MASK, secondInput.getValue());
    }

    @Test
    public void testStartExecutableSetNextPosition() throws Exception {
        List<Input> inputs = Arrays.asList();
        RunEnvironment runEnv = new RunEnvironment();

        Long nextStepPosition = 2L;
        executableSteps.startExecutable(inputs, runEnv, new HashMap<String, Value>(),
                new ExecutionRuntimeServices(), "", nextStepPosition, ExecutableType.FLOW);

        Assert.assertEquals(nextStepPosition, runEnv.removeNextStepPosition());
    }

    @Test
    public void testFinishExecutableWithResult() throws Exception {
        List<Result> results = singletonList(new Result(ScoreLangConstants.SUCCESS_RESULT,
                ValueFactory.create("true")));
        RunEnvironment runEnv = new RunEnvironment();
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Value>(), null));
        runEnv.getExecutionPath().down();

        when(resultsBinding.resolveResult(
                isNull(Map.class),
                anyMapOf(String.class, Value.class),
                eq(runEnv.getSystemProperties()),
                eq(results),
                isNull(String.class)
        )).thenReturn(ScoreLangConstants.SUCCESS_RESULT);
        executableSteps.finishExecutable(runEnv, new ArrayList<Output>(), results,
                new ExecutionRuntimeServices(), "", ExecutableType.FLOW);

        ReturnValues returnValues = runEnv.removeReturnValues();
        Assert.assertTrue(returnValues.getResult().equals(ScoreLangConstants.SUCCESS_RESULT));
    }

    @Test
    public void testFinishExecutableWithOutput() throws Exception {
        final List<Output> possibleOutputs = Arrays.asList(new Output("name", ValueFactory.create("name")));
        RunEnvironment runEnv = new RunEnvironment();
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Value>(), null));
        runEnv.getExecutionPath().down();

        Map<String, Value> boundOutputs = new HashMap<>();
        boundOutputs.put("name", ValueFactory.create("John"));

        when(outputsBinding.bindOutputs(
                isNull(Map.class),
                anyMapOf(String.class, Value.class),
                eq(runEnv.getSystemProperties()),
                eq(possibleOutputs)
        )).thenReturn(boundOutputs);
        executableSteps.finishExecutable(runEnv, possibleOutputs, new ArrayList<Result>(),
                new ExecutionRuntimeServices(), "", ExecutableType.FLOW);

        ReturnValues returnValues = runEnv.removeReturnValues();
        Map<String, Value> outputs = returnValues.getOutputs();
        Assert.assertEquals(1, outputs.size());
        Assert.assertEquals("John", outputs.get("name").get());
    }

    @Test
    public void testFinishExecutableSetNextPositionToParentFlow() throws Exception {
        RunEnvironment runEnv = new RunEnvironment();
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Value>(), null));
        runEnv.getExecutionPath().down();
        Long parentFirstStepPosition = 2L;
        runEnv.getParentFlowStack().pushParentFlowData(new ParentFlowData(111L, parentFirstStepPosition));

        executableSteps.finishExecutable(runEnv, new ArrayList<Output>(), new ArrayList<Result>(),
                new ExecutionRuntimeServices(), "", ExecutableType.FLOW);

        Assert.assertEquals(parentFirstStepPosition, runEnv.removeNextStepPosition());
    }

    @Test
    public void testFinishExecutableSetNextPositionNoParentFlow() throws Exception {
        RunEnvironment runEnv = new RunEnvironment();
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Value>(), null));
        runEnv.getExecutionPath().down();

        executableSteps.finishExecutable(runEnv, new ArrayList<Output>(), new ArrayList<Result>(),
                new ExecutionRuntimeServices(), "", ExecutableType.FLOW);

        Assert.assertEquals(null, runEnv.removeNextStepPosition());
    }

    @Test
    public void testFinishExecutableEvents() {
        final List<Output> possibleOutputs = singletonList(new Output("name", ValueFactory.create("name", false)));
        final List<Result> possibleResults = singletonList(new Result(ScoreLangConstants.SUCCESS_RESULT,
                ValueFactory.create("true")));
        RunEnvironment runEnv = new RunEnvironment();
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Value>(), null));
        runEnv.getExecutionPath().down();

        Map<String, Value> boundOutputs = new HashMap<>();
        boundOutputs.put("name", ValueFactory.create("John"));
        String boundResult = ScoreLangConstants.SUCCESS_RESULT;

        when(outputsBinding.bindOutputs(
                isNull(Map.class),
                anyMapOf(String.class, Value.class),
                eq(runEnv.getSystemProperties()),
                eq(possibleOutputs)
        )).thenReturn(boundOutputs);
        when(resultsBinding.resolveResult(
                isNull(Map.class),
                anyMapOf(String.class, Value.class),
                eq(runEnv.getSystemProperties()),
                eq(possibleResults),
                isNull(String.class)
        )).thenReturn(boundResult);

        ExecutionRuntimeServices runtimeServices = new ExecutionRuntimeServices();
        executableSteps.finishExecutable(runEnv, possibleOutputs, possibleResults,
                runtimeServices, "step1", ExecutableType.FLOW);

        Collection<ScoreEvent> events = runtimeServices.getEvents();

        Assert.assertFalse(events.isEmpty());
        ScoreEvent boundOutputEvent = null;
        ScoreEvent startOutputEvent = null;
        ScoreEvent executableFinishedEvent = null;
        for (ScoreEvent event : events) {
            if (event.getEventType().equals(ScoreLangConstants.EVENT_OUTPUT_END)) {
                boundOutputEvent = event;
            } else if (event.getEventType().equals(ScoreLangConstants.EVENT_OUTPUT_START)) {
                startOutputEvent = event;
            } else if (event.getEventType().equals(ScoreLangConstants.EVENT_EXECUTION_FINISHED)) {
                executableFinishedEvent = event;
            }
        }
        Assert.assertNotNull(startOutputEvent);
        LanguageEventData eventData = (LanguageEventData) startOutputEvent.getData();
        Assert.assertTrue(eventData.containsKey(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY));
        Assert.assertTrue(eventData.containsKey(ScoreLangConstants.EXECUTABLE_RESULTS_KEY));
        List<Output> outputs = (List<Output>) eventData.get(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY);
        List<Result> results = (List<Result>) eventData.get(ScoreLangConstants.EXECUTABLE_RESULTS_KEY);
        Assert.assertEquals(possibleOutputs, outputs);
        Assert.assertEquals(possibleResults, results);

        Assert.assertNotNull(boundOutputEvent);
        eventData = (LanguageEventData) boundOutputEvent.getData();
        Assert.assertTrue(eventData.containsKey(LanguageEventData.OUTPUTS));
        Map<String, Serializable> returnOutputs = eventData.getOutputs();
        String returnResult = (String) eventData.get(LanguageEventData.RESULT);
        Assert.assertEquals("step1", eventData.getStepName());
        Assert.assertEquals(LanguageEventData.StepType.FLOW, eventData.getStepType());
        Assert.assertEquals(1, returnOutputs.size());
        Assert.assertEquals("John", returnOutputs.get("name"));
        Assert.assertTrue(returnResult.equals(ScoreLangConstants.SUCCESS_RESULT));

        Assert.assertNotNull(executableFinishedEvent);
        eventData = (LanguageEventData) executableFinishedEvent.getData();
        String result = (String) eventData.get(LanguageEventData.RESULT);
        Map<String, Serializable> eventOutputs = (Map<String, Serializable>) eventData.get(LanguageEventData.OUTPUTS);
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, result);
        Assert.assertEquals(boundOutputs.size(), eventOutputs.size());
        for (Map.Entry<String, Value> entry : boundOutputs.entrySet()) {
            Assert.assertEquals(entry.getValue().get(), eventOutputs.get(entry.getKey()));
        }

    }

    @Configuration
    static class Config {

        @Bean
        public InputsBinding inputsBinding() {
            return mock(InputsBinding.class);
        }

        @Bean
        public OutputsBinding outputsBinding() {
            return mock(OutputsBinding.class);
        }

        @Bean
        public ResultsBinding resultsBinding() {
            return mock(ResultsBinding.class);
        }

        @Bean
        public ScriptEvaluator scriptEvaluator() {
            return mock(ScriptEvaluator.class);
        }

        @Bean
        public DependencyService mavenRepositoryService() {
            return new DependencyServiceImpl();
        }

        @Bean
        public MavenConfig mavenConfig() {
            return new MavenConfigImpl();
        }

        @Bean
        public PythonRuntimeService pythonRuntimeService() {
            return new PythonRuntimeServiceImpl();
        }

        @Bean
        public PythonExecutionEngine pythonExecutionEngine() {
            return new PythonExecutionCachedEngine();
        }

        @Bean
        public ExecutableExecutionData operationSteps() {
            return new ExecutableExecutionData();
        }

        @Bean
        public DummyEncryptor dummyEncryptor() {
            return new DummyEncryptor();
        }

        @Bean
        public EventBus eventBus() {
            return new EventBusImpl();
        }
    }
}