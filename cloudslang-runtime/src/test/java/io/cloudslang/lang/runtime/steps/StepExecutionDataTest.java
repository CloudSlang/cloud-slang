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
import io.cloudslang.lang.entities.ListLoopStatement;
import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.ScriptFunction;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.entities.encryption.DummyEncryptor;
import io.cloudslang.lang.runtime.bindings.ArgumentsBinding;
import io.cloudslang.lang.runtime.bindings.LoopsBinding;
import io.cloudslang.lang.runtime.bindings.OutputsBinding;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import io.cloudslang.lang.runtime.env.Context;
import io.cloudslang.lang.runtime.env.ForLoopCondition;
import io.cloudslang.lang.runtime.env.LoopCondition;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.python.google.common.collect.Lists;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = StepExecutionDataTest.Config.class)
public class StepExecutionDataTest {

    @Autowired
    private StepExecutionData stepExecutionData;

    @Autowired
    private ArgumentsBinding argumentsBinding;

    @Autowired
    private OutputsBinding outputsBinding;

    @Autowired
    private LoopsBinding loopsBinding;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private RunEnvironment createRunEnvironment() {
        RunEnvironment runEnvironment = new RunEnvironment();
        runEnvironment.getStack().pushContext(new Context(new HashMap<String, Value>()));
        return runEnvironment;
    }

    private LoopStatement createBasicForStatement(String varName, String collectionExpression) {
        return new ListLoopStatement(varName, collectionExpression,
                new HashSet<ScriptFunction>(), new HashSet<String>(), false);
    }

    @Before
    public void init() {
        reset(loopsBinding);
    }

    @Test
    public void testBeginStepEmptyInputs() throws Exception {
        RunEnvironment runEnv = createRunEnvironment();
        stepExecutionData
                .beginStep(new ArrayList<Argument>(), null, runEnv, createRuntimeServices(), "step1", 1L, 2L, "2");
        Map<String, Value> callArgs = runEnv.removeCallArguments();
        Assert.assertTrue(callArgs.isEmpty());
    }

    @Test
    public void testBeginStepSetNextPosition() throws Exception {
        RunEnvironment runEnv = createRunEnvironment();
        Long runningExecutionPlanId = 1L;
        Long nextStepId = 2L;
        Long subflowBeginStepId = 7L;
        String refExecutionPlanId = "2";

        HashMap<String, Long> runningPlansIds = new HashMap<>();
        runningPlansIds.put(refExecutionPlanId, 111L);
        HashMap<String, Long> beginStepsIds = new HashMap<>();
        beginStepsIds.put(refExecutionPlanId, subflowBeginStepId);
        ExecutionRuntimeServices runtimeServices = createRuntimeServicesWithSubflows(runningPlansIds, beginStepsIds);
        stepExecutionData
                .beginStep(new ArrayList<Argument>(), null, runEnv, runtimeServices,
                        "step1", runningExecutionPlanId, nextStepId, refExecutionPlanId);

        ParentFlowData parentFlowData = runEnv.getParentFlowStack().popParentFlowData();
        assertEquals(runningExecutionPlanId, parentFlowData.getRunningExecutionPlanId());
        assertEquals(nextStepId, parentFlowData.getPosition());
        assertEquals(subflowBeginStepId, runEnv.removeNextStepPosition());
    }

    @Test(timeout = 3000L)
    public void testBeginStepInputsEvents() throws Exception {
        RunEnvironment runEnv = createRunEnvironment();
        List<Argument> arguments = Arrays.asList(new Argument("input1", ValueFactory.create("input1")),
                new Argument("input2", ValueFactory.create("input2")));
        Map<String, Value> resultMap = new HashMap<>();
        resultMap.put("input1", ValueFactory.create(5));
        resultMap.put("input2", ValueFactory.create(3));

        when(argumentsBinding.bindArguments(
                eq(arguments),
                anyMapOf(String.class, Value.class),
                eq(runEnv.getSystemProperties())
        )).thenReturn(resultMap);

        ExecutionRuntimeServices runtimeServices = createRuntimeServices();
        stepExecutionData.beginStep(arguments, null, runEnv, runtimeServices, "step1", 1L, 2L, "2");
        Map<String, Value> callArgs = runEnv.removeCallArguments();
        Assert.assertFalse(callArgs.isEmpty());
        assertEquals(5, callArgs.get("input1").get());
        assertEquals(3, callArgs.get("input2").get());

        Collection<ScoreEvent> events = runtimeServices.getEvents();
        assertEquals(3, events.size());
        Iterator<ScoreEvent> eventsIterator = events.iterator();
        ScoreEvent stepStartEvent = eventsIterator.next();
        assertEquals(ScoreLangConstants.EVENT_STEP_START, stepStartEvent.getEventType());
        ScoreEvent inputStartEvent = eventsIterator.next();
        assertEquals(ScoreLangConstants.EVENT_ARGUMENT_START, inputStartEvent.getEventType());
        ScoreEvent inputEndEvent = eventsIterator.next();
        assertEquals(ScoreLangConstants.EVENT_ARGUMENT_END, inputEndEvent.getEventType());

        LanguageEventData startBindingEventData = (LanguageEventData) inputStartEvent.getData();
        assertEquals("step1", startBindingEventData.getStepName());
        assertEquals(LanguageEventData.StepType.STEP, startBindingEventData.getStepType());

        @SuppressWarnings("unchecked")
        List<String> inputsToBind = (List<String>) startBindingEventData.get(LanguageEventData.ARGUMENTS);
        assertEquals(
                "Inputs are not in defined order in start binding event",
                Lists.newArrayList("input1", "input2"),
                inputsToBind
        );

        LanguageEventData eventData = (LanguageEventData) inputEndEvent.getData();
        assertEquals("step1", eventData.getStepName());
        assertEquals(LanguageEventData.StepType.STEP, eventData.getStepType());

        @SuppressWarnings("unchecked")
        Map<String, Serializable> boundInputs =
                (Map<String, Serializable>) eventData.get(LanguageEventData.BOUND_ARGUMENTS);
        assertEquals(2, boundInputs.size());

        // verify input names are in defined order and have the expected value
        Set<Map.Entry<String, Serializable>> inputEntries = boundInputs.entrySet();
        Iterator<Map.Entry<String, Serializable>> inputNamesIterator = inputEntries.iterator();

        Map.Entry<String, Serializable> firstInput = inputNamesIterator.next();
        assertEquals("Inputs are not in defined order in end inputs binding event", "input1", firstInput.getKey());
        assertEquals(5, firstInput.getValue());

        Map.Entry<String, Serializable> secondInput = inputNamesIterator.next();
        assertEquals("Inputs are not in defined order in end inputs binding event", "input2", secondInput.getKey());
        assertEquals(3, secondInput.getValue());
    }

    @Test
    public void testEndStepEvents() throws Exception {
        RunEnvironment runEnv = createRunEnvironment();
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Value>(), ScoreLangConstants.SUCCESS_RESULT));
        Context context = new Context(new HashMap<String, Value>());
        runEnv.getStack().pushContext(context);

        when(outputsBinding.bindOutputs(
                anyMapOf(String.class, Value.class),
                anyMapOf(String.class, Value.class),
                eq(runEnv.getSystemProperties()),
                anyListOf(Output.class)))
                .thenReturn(new HashMap<String, Value>());

        ExecutionRuntimeServices runtimeServices = createRuntimeServices();
        HashMap<String, ResultNavigation> stepNavigationValues = new HashMap<>();
        stepNavigationValues
                .put(ScoreLangConstants.SUCCESS_RESULT, new ResultNavigation(0, ScoreLangConstants.SUCCESS_RESULT));
        stepExecutionData.endStep(runEnv, new ArrayList<Output>(), stepNavigationValues,
                runtimeServices, 1L, new ArrayList<String>(), "step1", false);

        Collection<ScoreEvent> events = runtimeServices.getEvents();
        assertEquals(2, events.size());
        Iterator<ScoreEvent> eventsIter = events.iterator();
        ScoreEvent outputStart = eventsIter.next();
        assertEquals(ScoreLangConstants.EVENT_OUTPUT_START, outputStart.getEventType());

        LanguageEventData eventData = (LanguageEventData) outputStart.getData();
        assertEquals("step1", eventData.getStepName());
        assertEquals(LanguageEventData.StepType.STEP, eventData.getStepType());

        ScoreEvent outputEnd = eventsIter.next();
        assertEquals(ScoreLangConstants.EVENT_OUTPUT_END, outputEnd.getEventType());

        eventData = (LanguageEventData) outputEnd.getData();
        assertEquals("step1", eventData.getStepName());
        assertEquals(LanguageEventData.StepType.STEP, eventData.getStepType());

    }

    @Test
    public void testEndStepWithPublish() throws Exception {
        final List<Output> possiblePublishValues = singletonList(new Output("name", ValueFactory.create("name")));
        RunEnvironment runEnv = createRunEnvironment();
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Value>(), ScoreLangConstants.SUCCESS_RESULT));
        Context context = new Context(new HashMap<String, Value>());
        runEnv.getStack().pushContext(context);

        Map<String, Value> boundPublish = new HashMap<>();
        boundPublish.put("name", ValueFactory.create("John"));

        when(outputsBinding.bindOutputs(
                anyMapOf(String.class, Value.class),
                anyMapOf(String.class, Value.class),
                eq(runEnv.getSystemProperties()),
                eq(possiblePublishValues)))
                .thenReturn(boundPublish);
        HashMap<String, ResultNavigation> stepNavigationValues = new HashMap<>();
        stepNavigationValues
                .put(ScoreLangConstants.SUCCESS_RESULT, new ResultNavigation(0, ScoreLangConstants.SUCCESS_RESULT));
        stepExecutionData.endStep(runEnv, possiblePublishValues, stepNavigationValues,
                createRuntimeServices(), 1L, new ArrayList<String>(), "step1", false);

        Map<String, Value> flowVars = runEnv.getStack().popContext().getImmutableViewOfVariables();
        Assert.assertTrue(flowVars.containsKey("name"));
        assertEquals("John", flowVars.get("name").get());
    }

    @Test
    public void testEndStepSetNextPosition() throws Exception {
        RunEnvironment runEnv = createRunEnvironment();
        String result = ScoreLangConstants.SUCCESS_RESULT;
        Context context = new Context(new HashMap<String, Value>());
        runEnv.getStack().pushContext(context);
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Value>(), result));

        Long nextStepPosition = 5L;

        HashMap<String, ResultNavigation> stepNavigationValues = new HashMap<>();
        ResultNavigation successNavigation = new ResultNavigation(nextStepPosition, null);
        stepNavigationValues.put(ScoreLangConstants.SUCCESS_RESULT, successNavigation);
        ResultNavigation failureNavigation = new ResultNavigation(1, null);
        stepNavigationValues.put(ScoreLangConstants.FAILURE_RESULT, failureNavigation);
        stepExecutionData.endStep(runEnv, new ArrayList<Output>(), stepNavigationValues,
                createRuntimeServices(), 1L, new ArrayList<String>(), "step1", false);

        assertEquals(runEnv.removeNextStepPosition(), nextStepPosition);
    }

    @Test
    public void testEndStepMissingNavigationForExecutableResult() throws Exception {
        RunEnvironment runEnv = createRunEnvironment();
        String result = "CUSTOM";
        Context context = new Context(new HashMap<String, Value>());
        runEnv.getStack().pushContext(context);
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Value>(), result));

        Long nextStepPosition = 5L;

        HashMap<String, ResultNavigation> stepNavigationValues = new HashMap<>();
        ResultNavigation successNavigation = new ResultNavigation(nextStepPosition, null);
        stepNavigationValues.put(ScoreLangConstants.SUCCESS_RESULT, successNavigation);
        ResultNavigation failureNavigation = new ResultNavigation(1, null);
        stepNavigationValues.put(ScoreLangConstants.FAILURE_RESULT, failureNavigation);
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step1");
        exception.expectMessage("CUSTOM");
        exception.expectMessage("navigation");
        stepExecutionData.endStep(runEnv, new ArrayList<Output>(), stepNavigationValues,
                createRuntimeServices(), 1L, new ArrayList<String>(), "Step1", false);
    }

    @Test
    public void testEndStepParallelLoopReturnValues() throws Exception {
        RunEnvironment runEnv = createRunEnvironment();
        String result = ScoreLangConstants.SUCCESS_RESULT;
        Context context = new Context(new HashMap<String, Value>());
        runEnv.getStack().pushContext(context);
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Value>(), result));
        Long nextStepPosition = 5L;

        HashMap<String, ResultNavigation> stepNavigationValues = new HashMap<>();
        ResultNavigation successNavigation = new ResultNavigation(nextStepPosition, "CUSTOM1");
        stepNavigationValues.put(ScoreLangConstants.SUCCESS_RESULT, successNavigation);
        ResultNavigation failureNavigation = new ResultNavigation(1, "CUSTOM2");
        stepNavigationValues.put(ScoreLangConstants.FAILURE_RESULT, failureNavigation);
        stepExecutionData.endStep(runEnv, new ArrayList<Output>(), stepNavigationValues,
                createRuntimeServices(), 1L, new ArrayList<String>(), "step1", true);

        assertEquals(
                "next step position should be null for parallel loop endStep method",
                null,
                runEnv.removeNextStepPosition()
        );
        assertEquals(
                "executable result should be returned in parallel loop endStep method",
                ScoreLangConstants.SUCCESS_RESULT,
                runEnv.removeReturnValues().getResult()
        );
    }

    /////////
    //loops//
    /////////

    @Test
    public void whenLoopKeyProvidedLoopConditionIsRequested() {
        String collectionExpression = "collection";
        LoopStatement statement = createBasicForStatement("x", collectionExpression);
        String nodeName = "step1";
        Context context = new Context(new HashMap<String, Value>());
        RunEnvironment runEnv = new RunEnvironment();
        when(loopsBinding.getOrCreateLoopCondition(statement, context, runEnv.getSystemProperties(), nodeName))
                .thenReturn(new ForLoopCondition(Arrays.asList(ValueFactory.create("1"), ValueFactory.create("2"))));
        runEnv.getStack().pushContext(context);
        stepExecutionData.beginStep(new ArrayList<Argument>(), statement,
                runEnv, createRuntimeServices(), nodeName, 1L, 2L, "2");
        verify(loopsBinding).getOrCreateLoopCondition(statement, context, runEnv.getSystemProperties(), nodeName);
    }

    @Test
    public void whenLoopConditionHasNoMoreNextStepIdSetToEndStep() {
        String collectionExpression = "collection";
        LoopStatement statement = createBasicForStatement("x", collectionExpression);
        String nodeName = "step1";
        Context context = new Context(new HashMap<String, Value>());
        LoopCondition mockLoopCondition = mock(LoopCondition.class);
        RunEnvironment runEnv = new RunEnvironment();
        when(mockLoopCondition.hasMore()).thenReturn(false);
        when(loopsBinding.getOrCreateLoopCondition(statement, context, runEnv.getSystemProperties(), nodeName))
                .thenReturn(mockLoopCondition);
        runEnv.getStack().pushContext(context);
        Long nextStepId = 2L;
        ExecutionRuntimeServices runtimeServices = createRuntimeServices();
        stepExecutionData.beginStep(new ArrayList<Argument>(), statement, runEnv,
                runtimeServices, nodeName, 1L, nextStepId, "2");
        assertEquals(nextStepId, runEnv.removeNextStepPosition());
        assertEquals(context, runEnv.getStack().popContext());
        Assert.assertNull(runtimeServices.pullRequestForChangingExecutionPlan());
    }

    @Test
    public void whenLoopConditionHasMoreNextStepIdSetToEndStep() {
        String collectionExpression = "collection";
        LoopStatement statement = createBasicForStatement("x", collectionExpression);
        String nodeName = "step1";
        Context context = new Context(new HashMap<String, Value>());
        LoopCondition mockLoopCondition = mock(LoopCondition.class);
        RunEnvironment runEnv = new RunEnvironment();
        when(mockLoopCondition.hasMore()).thenReturn(true);
        when(loopsBinding.getOrCreateLoopCondition(statement, context, runEnv.getSystemProperties(), nodeName))
                .thenReturn(mockLoopCondition);
        runEnv.getStack().pushContext(context);
        Long nextStepId = 2L;
        ExecutionRuntimeServices runtimeServices = mock(ExecutionRuntimeServices.class);
        Long subflowFirstStepId = 11L;
        when(runtimeServices.getSubFlowBeginStep(anyString())).thenReturn(subflowFirstStepId);
        stepExecutionData.beginStep(new ArrayList<Argument>(), statement, runEnv,
                runtimeServices, nodeName, 1L, nextStepId, "2");
        assertEquals(subflowFirstStepId, runEnv.removeNextStepPosition());
        assertEquals(context, runEnv.getStack().popContext());
        Assert.assertNotNull(runtimeServices.pullRequestForChangingExecutionPlan());
    }

    @Test
    public void whenLoopConditionIsOfForTypeStartStepWillIncrementIt() {
        String collectionExpression = "collection";
        LoopStatement statement = createBasicForStatement("x", collectionExpression);
        String nodeName = "step1";
        Context context = new Context(new HashMap<String, Value>());
        ForLoopCondition mockLoopCondition = mock(ForLoopCondition.class);
        RunEnvironment runEnv = new RunEnvironment();
        when(mockLoopCondition.hasMore()).thenReturn(true);
        when(loopsBinding.getOrCreateLoopCondition(statement, context, runEnv.getSystemProperties(), nodeName))
                .thenReturn(mockLoopCondition);
        runEnv.getStack().pushContext(context);
        stepExecutionData.beginStep(new ArrayList<Argument>(), statement, runEnv,
                createRuntimeServices(), nodeName, 1L, 2L, "2");
        verify(loopsBinding).incrementListForLoop("x", context, mockLoopCondition);
    }

    @Test
    public void whenLoopConditionHasMoreEndStepSetNextPositionIdToBeginStep() throws Exception {
        RunEnvironment runEnv = new RunEnvironment();
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Value>(), "SUCCESS"));
        HashMap<String, ResultNavigation> stepNavigationValues = new HashMap<>();
        stepNavigationValues.put("SUCCESS", new ResultNavigation(3L, "SUCCESS"));
        Context context = new Context(new HashMap<String, Value>());
        runEnv.getStack().pushContext(context);
        LoopCondition mockLoopCondition = mock(LoopCondition.class);
        context.putLanguageVariable(LoopCondition.LOOP_CONDITION_KEY, ValueFactory.create(mockLoopCondition));
        when(mockLoopCondition.hasMore()).thenReturn(true);

        Long previousStepId = 1L;
        stepExecutionData.endStep(runEnv, new ArrayList<Output>(), stepNavigationValues,
                createRuntimeServices(), previousStepId, new ArrayList<String>(), "stepName", false);

        assertEquals(previousStepId, runEnv.removeNextStepPosition());
        assertEquals(context, runEnv.getStack().popContext());
    }

    @Test
    public void whenLoopConditionHasMoreButShouldBreakEndStepDeletesKeyFromLangVars() throws Exception {
        RunEnvironment runEnv = new RunEnvironment();
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Value>(), ScoreLangConstants.SUCCESS_RESULT));
        HashMap<String, ResultNavigation> stepNavigationValues = new HashMap<>();
        Long nextStepId = 3L;
        stepNavigationValues.put(ScoreLangConstants.SUCCESS_RESULT,
                new ResultNavigation(nextStepId, ScoreLangConstants.SUCCESS_RESULT));
        Context context = new Context(new HashMap<String, Value>());
        runEnv.getStack().pushContext(context);
        LoopCondition mockLoopCondition = mock(LoopCondition.class);
        context.putLanguageVariable(LoopCondition.LOOP_CONDITION_KEY, ValueFactory.create(mockLoopCondition));
        when(mockLoopCondition.hasMore()).thenReturn(true);

        Long previousStepId = 1L;
        stepExecutionData
                .endStep(runEnv, new ArrayList<Output>(), stepNavigationValues,
                createRuntimeServices(), previousStepId,
                        singletonList(ScoreLangConstants.SUCCESS_RESULT), "stepName", false);

        assertEquals(nextStepId, runEnv.removeNextStepPosition());
        Assert.assertFalse(context.getImmutableViewOfLanguageVariables().containsKey(LoopCondition.LOOP_CONDITION_KEY));
    }

    @Test
    public void whenLoopConditionHasNoMoreEndStepDeletesKeyFromLangVars() throws Exception {
        RunEnvironment runEnv = new RunEnvironment();
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Value>(), "SUCCESS"));
        HashMap<String, ResultNavigation> stepNavigationValues = new HashMap<>();
        stepNavigationValues.put("SUCCESS", new ResultNavigation(3L, "SUCCESS"));
        Context context = new Context(new HashMap<String, Value>());
        runEnv.getStack().pushContext(context);
        LoopCondition mockLoopCondition = mock(LoopCondition.class);
        context.putLanguageVariable(LoopCondition.LOOP_CONDITION_KEY, ValueFactory.create(mockLoopCondition));
        when(mockLoopCondition.hasMore()).thenReturn(false);

        stepExecutionData.endStep(runEnv, new ArrayList<Output>(), stepNavigationValues,
                createRuntimeServices(), 1L, new ArrayList<String>(), "stepName", false);

        Assert.assertFalse(context.getImmutableViewOfLanguageVariables().containsKey(LoopCondition.LOOP_CONDITION_KEY));
    }

    private ExecutionRuntimeServices createRuntimeServices() {
        ExecutionRuntimeServices runtimeServices = new ExecutionRuntimeServices();
        runtimeServices.setSubFlowsData(new HashMap<String, Long>(), new HashMap<String, Long>());
        return runtimeServices;
    }

    private ExecutionRuntimeServices createRuntimeServicesWithSubflows(HashMap<String, Long> runningPlansIds,
                                                                       HashMap<String, Long> beginStepsIds) {
        ExecutionRuntimeServices runtimeServices = createRuntimeServices();
        runtimeServices.setSubFlowsData(runningPlansIds, beginStepsIds);
        return runtimeServices;
    }

    @Configuration
    static class Config {

        @Bean
        public ArgumentsBinding argumentsBinding() {
            return mock(ArgumentsBinding.class);
        }

        @Bean
        public OutputsBinding outputsBinding() {
            return mock(OutputsBinding.class);
        }

        @Bean
        public LoopsBinding loopsBinding() {
            return mock(LoopsBinding.class);
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
        public StepExecutionData stepSteps() {
            return new StepExecutionData();
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