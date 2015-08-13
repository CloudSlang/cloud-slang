/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/
package io.cloudslang.lang.runtime.steps;

import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.runtime.bindings.LoopsBinding;
import io.cloudslang.lang.runtime.bindings.OutputsBinding;
import io.cloudslang.lang.runtime.env.*;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.lang.entities.ListForLoopStatement;
import io.cloudslang.lang.entities.ScoreLangConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.runtime.bindings.InputsBinding;
import io.cloudslang.lang.runtime.bindings.ScriptEvaluator;
import io.cloudslang.lang.runtime.env.Context;
import io.cloudslang.lang.runtime.env.ParentFlowData;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.script.ScriptEngine;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TaskStepsTest.Config.class)
public class TaskStepsTest {

    @Autowired
    private TaskSteps taskSteps;

    @Autowired
    private InputsBinding inputsBinding;

    @Autowired
    private OutputsBinding outputsBinding;

    @Autowired
    private LoopsBinding loopsBinding;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private RunEnvironment createRunEnvironment() {
        RunEnvironment runEnvironment = new RunEnvironment();
        runEnvironment.getStack().pushContext(new Context(new HashMap<String, Serializable>()));
        return runEnvironment;
    }

    private LoopStatement createBasicForStatement(String varName, String collectionExpression) {
        return new ListForLoopStatement(varName, collectionExpression);
    }

    @Before
    public void init(){
        reset(loopsBinding);
    }

    @Test
    public void testBeginTaskEmptyInputs() throws Exception {
        RunEnvironment runEnv = createRunEnvironment();
        taskSteps.beginTask(new ArrayList<Input>(), null, runEnv, createRuntimeServices(),"task1", 1L, 2L, "2");
        Map<String,Serializable> callArgs = runEnv.removeCallArguments();
        Assert.assertTrue(callArgs.isEmpty());
    }

    @Test
    public void testBeginTaskSetNextPosition() throws Exception {
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
        taskSteps.beginTask(new ArrayList<Input>(), null, runEnv, runtimeServices, "task1", runningExecutionPlanId, nextStepId, refExecutionPlanId);

        ParentFlowData parentFlowData = runEnv.getParentFlowStack().popParentFlowData();
        Assert.assertEquals(runningExecutionPlanId, parentFlowData.getRunningExecutionPlanId());
        Assert.assertEquals(nextStepId, parentFlowData.getPosition());
        Assert.assertEquals(subflowBeginStepId, runEnv.removeNextStepPosition());
    }

    @Test(timeout = 3000L)
    public void testBeginTaskInputsEvents() throws Exception {
        RunEnvironment runEnv = createRunEnvironment();
        List<Input> inputs = Arrays.asList(new Input("input1", "input1"), new Input("input2", "input2"));
        Map<String,Serializable> resultMap = new HashMap<>();
        resultMap.put("input1", 5);
        resultMap.put("input2", 3);

        when(inputsBinding.bindInputs(eq(inputs), anyMap(), anyMap())).thenReturn(resultMap);

        ExecutionRuntimeServices runtimeServices = createRuntimeServices();
        taskSteps.beginTask(inputs, null, runEnv, runtimeServices, "task1", 1L, 2L, "2");
        Map<String,Serializable> callArgs = runEnv.removeCallArguments();
        Assert.assertFalse(callArgs.isEmpty());
        Assert.assertEquals(5, callArgs.get("input1"));
        Assert.assertEquals(3, callArgs.get("input2"));

        Collection<ScoreEvent> events = runtimeServices.getEvents();
        Assert.assertEquals(3,events.size());
        Iterator<ScoreEvent> eventsIterator = events.iterator();
        ScoreEvent taskStartEvent = eventsIterator.next();
        Assert.assertEquals(ScoreLangConstants.EVENT_TASK_START, taskStartEvent.getEventType());
        ScoreEvent inputStartEvent = eventsIterator.next();
        Assert.assertEquals(ScoreLangConstants.EVENT_INPUT_START, inputStartEvent.getEventType());
        ScoreEvent inputEndEvent = eventsIterator.next();
        Assert.assertEquals(ScoreLangConstants.EVENT_INPUT_END, inputEndEvent.getEventType());

        LanguageEventData startBindingEventData = (LanguageEventData)inputStartEvent.getData();
        Assert.assertEquals("task1",startBindingEventData.getStepName());
        Assert.assertEquals(LanguageEventData.StepType.TASK,startBindingEventData.getStepType());

        List<String> inputsToBind = (List<String>)startBindingEventData.get(LanguageEventData.INPUTS);
        Assert.assertTrue(inputsToBind.contains("input1"));
        Assert.assertTrue(inputsToBind.contains("input2"));

        LanguageEventData eventData = (LanguageEventData)inputEndEvent.getData();
        Assert.assertEquals("task1",eventData.getStepName());
        Assert.assertEquals(LanguageEventData.StepType.TASK,eventData.getStepType());

        Map<String,Serializable> boundInputs = (Map<String,Serializable>)eventData.get(LanguageEventData.BOUND_INPUTS);
        Assert.assertEquals(5,boundInputs.get("input1"));
        Assert.assertEquals(3,boundInputs.get("input2"));
    }

    @Test
    public void testEndTaskEvents() throws Exception {
        RunEnvironment runEnv = createRunEnvironment();
        runEnv.putReturnValues(new ReturnValues(new HashMap<String,Serializable>(), ScoreLangConstants.SUCCESS_RESULT));
        Context context = new Context(new HashMap<String, Serializable>());
        runEnv.getStack().pushContext(context);

        when(outputsBinding.bindOutputs(anyMap(), anyMap(), anyList())).thenReturn(new HashMap<String, Serializable>());

        ExecutionRuntimeServices runtimeServices = createRuntimeServices();
        HashMap<String, ResultNavigation> taskNavigationValues = new HashMap<>();
        taskNavigationValues.put(ScoreLangConstants.SUCCESS_RESULT, new ResultNavigation(0, ScoreLangConstants.SUCCESS_RESULT));
        taskSteps.endTask(runEnv, new ArrayList<Output>(), taskNavigationValues,
                runtimeServices, 1L, new ArrayList<String>(), "task1", false);

        Collection<ScoreEvent> events = runtimeServices.getEvents();
        Assert.assertEquals(2,events.size());
        Iterator<ScoreEvent> eventsIter = events.iterator();
        ScoreEvent outputStart = eventsIter.next();
        Assert.assertEquals(ScoreLangConstants.EVENT_OUTPUT_START,outputStart.getEventType());

        LanguageEventData eventData = (LanguageEventData)outputStart.getData();
        Assert.assertEquals("task1",eventData.getStepName());
        Assert.assertEquals(LanguageEventData.StepType.TASK,eventData.getStepType());

        ScoreEvent outputEnd = eventsIter.next();
        Assert.assertEquals(ScoreLangConstants.EVENT_OUTPUT_END,outputEnd.getEventType());

        eventData = (LanguageEventData)outputEnd.getData();
        Assert.assertEquals("task1",eventData.getStepName());
        Assert.assertEquals(LanguageEventData.StepType.TASK,eventData.getStepType());

    }

    @Test
    public void testEndTaskWithPublish() throws Exception {
        List<Output> possiblePublishValues = Arrays.asList(new Output("name", "name"));
        RunEnvironment runEnv = createRunEnvironment();
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Serializable>(), ScoreLangConstants.SUCCESS_RESULT));
        Context context = new Context(new HashMap<String, Serializable>());
        runEnv.getStack().pushContext(context);

        Map<String, Serializable> boundPublish = new HashMap<>();
        boundPublish.put("name", "John");

        when(outputsBinding.bindOutputs(
                anyMapOf(String.class, Serializable.class), anyMapOf(String.class, Serializable.class), eq(possiblePublishValues)))
                .thenReturn(boundPublish);
        HashMap<String, ResultNavigation> taskNavigationValues = new HashMap<>();
        taskNavigationValues.put(ScoreLangConstants.SUCCESS_RESULT, new ResultNavigation(0, ScoreLangConstants.SUCCESS_RESULT));
        taskSteps.endTask(runEnv, possiblePublishValues, taskNavigationValues,
                createRuntimeServices(), 1L, new ArrayList<String>(), "task1", false);

        Map<String,Serializable> flowVars = runEnv.getStack().popContext().getImmutableViewOfVariables();
        Assert.assertTrue(flowVars.containsKey("name"));
        Assert.assertEquals("John" ,flowVars.get("name"));
    }

    @Test
    public void testEndTaskSetNextPosition() throws Exception {
        RunEnvironment runEnv = createRunEnvironment();
        String result = ScoreLangConstants.SUCCESS_RESULT;
        Context context = new Context(new HashMap<String, Serializable>());
        runEnv.getStack().pushContext(context);
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Serializable>(), result));

        Long nextStepPosition = 5L;

        HashMap<String, ResultNavigation> taskNavigationValues = new HashMap<>();
        ResultNavigation successNavigation = new ResultNavigation(nextStepPosition, null);
        taskNavigationValues.put(ScoreLangConstants.SUCCESS_RESULT, successNavigation);
        ResultNavigation failureNavigation = new ResultNavigation(1, null);
        taskNavigationValues.put(ScoreLangConstants.FAILURE_RESULT, failureNavigation);
        taskSteps.endTask(runEnv, new ArrayList<Output>(), taskNavigationValues,
                createRuntimeServices(), 1L, new ArrayList<String>(),"task1", false);

        Assert.assertEquals(runEnv.removeNextStepPosition(), nextStepPosition);
    }

    @Test
    public void testEndTaskMissingNavigationForExecutableResult() throws Exception {
        RunEnvironment runEnv = createRunEnvironment();
        String result = "CUSTOM";
        Context context = new Context(new HashMap<String, Serializable>());
        runEnv.getStack().pushContext(context);
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Serializable>(), result));

        Long nextStepPosition = 5L;

        HashMap<String, ResultNavigation> taskNavigationValues = new HashMap<>();
        ResultNavigation successNavigation = new ResultNavigation(nextStepPosition, null);
        taskNavigationValues.put(ScoreLangConstants.SUCCESS_RESULT, successNavigation);
        ResultNavigation failureNavigation = new ResultNavigation(1, null);
        taskNavigationValues.put(ScoreLangConstants.FAILURE_RESULT, failureNavigation);
        exception.expect(RuntimeException.class);
        exception.expectMessage("Task1");
        exception.expectMessage("CUSTOM");
        exception.expectMessage("navigation");
        taskSteps.endTask(runEnv, new ArrayList<Output>(), taskNavigationValues,
                createRuntimeServices(), 1L, new ArrayList<String>(), "Task1", false);
    }

    @Test
    public void testEndTaskAsyncLoopReturnValues() throws Exception {
        RunEnvironment runEnv = createRunEnvironment();
        String result = ScoreLangConstants.SUCCESS_RESULT;
        Context context = new Context(new HashMap<String, Serializable>());
        runEnv.getStack().pushContext(context);
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Serializable>(), result));
        Long nextStepPosition = 5L;

        HashMap<String, ResultNavigation> taskNavigationValues = new HashMap<>();
        ResultNavigation successNavigation = new ResultNavigation(nextStepPosition, "CUSTOM1");
        taskNavigationValues.put(ScoreLangConstants.SUCCESS_RESULT, successNavigation);
        ResultNavigation failureNavigation = new ResultNavigation(1, "CUSTOM2");
        taskNavigationValues.put(ScoreLangConstants.FAILURE_RESULT, failureNavigation);
        taskSteps.endTask(runEnv, new ArrayList<Output>(), taskNavigationValues,
                createRuntimeServices(), 1L, new ArrayList<String>(),"task1", true);

        Assert.assertEquals(
                "next step position should be null for async endTask method",
                null,
                runEnv.removeNextStepPosition()
        );
        Assert.assertEquals(
                "executable result should be returned in async endTask method",
                ScoreLangConstants.SUCCESS_RESULT,
                runEnv.removeReturnValues().getResult()
        );
    }

    /////////
    //loops//
    /////////

    @Test
    public void whenLoopKeyProvidedLoopConditionIsRequested(){
        String collectionExpression = "collection";
        LoopStatement statement = createBasicForStatement("x", collectionExpression);
        String nodeName = "task1";
        Context context = new Context(new HashMap<String, Serializable>());
        when(loopsBinding.getOrCreateLoopCondition(statement, context, nodeName))
                .thenReturn(new ForLoopCondition(Arrays.asList("1", "2")));
        RunEnvironment runEnv = new RunEnvironment();
        runEnv.getStack().pushContext(context);
        taskSteps.beginTask(new ArrayList<Input>(), statement, runEnv, createRuntimeServices(), nodeName, 1L, 2L, "2");
        verify(loopsBinding).getOrCreateLoopCondition(statement, context, nodeName);
    }

    @Test
    public void whenLoopConditionHasNoMoreNextStepIdSetToEndTask(){
        String collectionExpression = "collection";
        LoopStatement statement = createBasicForStatement("x", collectionExpression);
        String nodeName = "task1";
        Context context = new Context(new HashMap<String, Serializable>());
        LoopCondition mockLoopCondition = mock(LoopCondition.class);
        when(mockLoopCondition.hasMore()).thenReturn(false);
        when(loopsBinding.getOrCreateLoopCondition(statement, context, nodeName))
                .thenReturn(mockLoopCondition);
        RunEnvironment runEnv = new RunEnvironment();
        runEnv.getStack().pushContext(context);
        Long nextStepId = 2L;
        ExecutionRuntimeServices runtimeServices = createRuntimeServices();
        taskSteps.beginTask(new ArrayList<Input>(), statement, runEnv, runtimeServices, nodeName, 1L, nextStepId, "2");
        Assert.assertEquals(nextStepId, runEnv.removeNextStepPosition());
        Assert.assertEquals(context, runEnv.getStack().popContext());
        Assert.assertNull(runtimeServices.pullRequestForChangingExecutionPlan());
    }

    @Test
    public void whenLoopConditionHasMoreNextStepIdSetToEndTask(){
        String collectionExpression = "collection";
        LoopStatement statement = createBasicForStatement("x", collectionExpression);
        String nodeName = "task1";
        Context context = new Context(new HashMap<String, Serializable>());
        LoopCondition mockLoopCondition = mock(LoopCondition.class);
        when(mockLoopCondition.hasMore()).thenReturn(true);
        when(loopsBinding.getOrCreateLoopCondition(statement, context, nodeName))
                .thenReturn(mockLoopCondition);
        RunEnvironment runEnv = new RunEnvironment();
        runEnv.getStack().pushContext(context);
        Long nextStepId = 2L;
        ExecutionRuntimeServices runtimeServices = mock(ExecutionRuntimeServices.class);
        Long subflowFirstStepId = 11L;
        when(runtimeServices.getSubFlowBeginStep(anyString())).thenReturn(subflowFirstStepId);
        taskSteps.beginTask(new ArrayList<Input>(), statement, runEnv, runtimeServices, nodeName, 1L, nextStepId, "2");
        Assert.assertEquals(subflowFirstStepId, runEnv.removeNextStepPosition());
        Assert.assertEquals(context, runEnv.getStack().popContext());
        Assert.assertNotNull(runtimeServices.pullRequestForChangingExecutionPlan());
    }

    @Test
    public void whenLoopConditionIsOfForTypeStartTaskWillIncrementIt(){
        String collectionExpression = "collection";
        LoopStatement statement = createBasicForStatement("x", collectionExpression);
        String nodeName = "task1";
        Context context = new Context(new HashMap<String, Serializable>());
        ForLoopCondition mockLoopCondition = mock(ForLoopCondition.class);
        when(mockLoopCondition.hasMore()).thenReturn(true);
        when(loopsBinding.getOrCreateLoopCondition(statement, context, nodeName))
                .thenReturn(mockLoopCondition);
        RunEnvironment runEnv = new RunEnvironment();
        runEnv.getStack().pushContext(context);
        taskSteps.beginTask(new ArrayList<Input>(), statement, runEnv, createRuntimeServices(), nodeName, 1L, 2L, "2");
        verify(loopsBinding).incrementListForLoop("x", context, mockLoopCondition);
    }

    @Test
    public void whenLoopConditionHasMoreEndTaskSetNextPositionIdToBeginTask() throws Exception {
        RunEnvironment runEnv = new RunEnvironment();
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Serializable>(), "SUCCESS"));
        HashMap<String, ResultNavigation> taskNavigationValues = new HashMap<>();
        taskNavigationValues.put("SUCCESS", new ResultNavigation(3L, "SUCCESS"));
        Context context = new Context(new HashMap<String, Serializable>());
        runEnv.getStack().pushContext(context);
        LoopCondition mockLoopCondition = mock(LoopCondition.class);
        context.getLangVariables().put(LoopCondition.LOOP_CONDITION_KEY, mockLoopCondition);
        when(mockLoopCondition.hasMore()).thenReturn(true);

        Long previousStepId = 1L;
        taskSteps.endTask(runEnv, new ArrayList<Output>(), taskNavigationValues,
                createRuntimeServices(), previousStepId, new ArrayList<String>(),"taskName", false);

        Assert.assertEquals(previousStepId, runEnv.removeNextStepPosition());
        Assert.assertEquals(context, runEnv.getStack().popContext());
    }

    @Test
    public void whenLoopConditionHasMoreButShouldBreakEndTaskDeletesKeyFromLangVars() throws Exception {
        RunEnvironment runEnv = new RunEnvironment();
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Serializable>(), ScoreLangConstants.SUCCESS_RESULT));
        HashMap<String, ResultNavigation> taskNavigationValues = new HashMap<>();
        Long nextStepId = 3L;
        taskNavigationValues.put(ScoreLangConstants.SUCCESS_RESULT, new ResultNavigation(nextStepId, ScoreLangConstants.SUCCESS_RESULT));
        Context context = new Context(new HashMap<String, Serializable>());
        runEnv.getStack().pushContext(context);
        LoopCondition mockLoopCondition = mock(LoopCondition.class);
        context.getLangVariables().put(LoopCondition.LOOP_CONDITION_KEY, mockLoopCondition);
        when(mockLoopCondition.hasMore()).thenReturn(true);

        Long previousStepId = 1L;
        taskSteps.endTask(runEnv, new ArrayList<Output>(), taskNavigationValues,
                createRuntimeServices(), previousStepId, Arrays.asList(ScoreLangConstants.SUCCESS_RESULT),"taskName", false);

        Assert.assertEquals(nextStepId, runEnv.removeNextStepPosition());
        Assert.assertFalse(context.getLangVariables().containsKey(LoopCondition.LOOP_CONDITION_KEY));
    }

    @Test
    public void whenLoopConditionHasNoMoreEndTaskDeletesKeyFromLangVars() throws Exception {
        RunEnvironment runEnv = new RunEnvironment();
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Serializable>(), "SUCCESS"));
        HashMap<String, ResultNavigation> taskNavigationValues = new HashMap<>();
        taskNavigationValues.put("SUCCESS", new ResultNavigation(3L, "SUCCESS"));
        Context context = new Context(new HashMap<String, Serializable>());
        runEnv.getStack().pushContext(context);
        LoopCondition mockLoopCondition = mock(LoopCondition.class);
        context.getLangVariables().put(LoopCondition.LOOP_CONDITION_KEY, mockLoopCondition);
        when(mockLoopCondition.hasMore()).thenReturn(false);

        taskSteps.endTask(runEnv, new ArrayList<Output>(), taskNavigationValues,
                createRuntimeServices(), 1L,  new ArrayList<String>(), "taskName", false);

        Assert.assertFalse(context.getLangVariables().containsKey(LoopCondition.LOOP_CONDITION_KEY));
    }

    private ExecutionRuntimeServices createRuntimeServices(){
        ExecutionRuntimeServices runtimeServices = new ExecutionRuntimeServices();
        runtimeServices.setSubFlowsData(new HashMap<String, Long>(), new HashMap<String, Long>());
        return runtimeServices;
    }

    private ExecutionRuntimeServices createRuntimeServicesWithSubflows(HashMap<String, Long> runningPlansIds, HashMap<String, Long> beginStepsIds) {
        ExecutionRuntimeServices runtimeServices = createRuntimeServices();
        runtimeServices.setSubFlowsData(runningPlansIds, beginStepsIds);
        return runtimeServices;
    }

    @Configuration
    static class Config{

        @Bean
        public InputsBinding inputsBinding(){
            return mock(InputsBinding.class);
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
        public ScriptEvaluator scriptEvaluator(){
            return mock(ScriptEvaluator.class);
        }

        @Bean
        public ScriptEngine scriptEngine(){
            return mock(ScriptEngine.class);
        }

        @Bean
        public TaskSteps taskSteps(){
            return new TaskSteps();
        }

    }
}