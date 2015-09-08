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

import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.runtime.bindings.InputsBinding;
import io.cloudslang.lang.runtime.bindings.OutputsBinding;
import io.cloudslang.lang.runtime.bindings.ResultsBinding;
import io.cloudslang.lang.runtime.bindings.ScriptEvaluator;
import io.cloudslang.lang.runtime.env.ParentFlowData;
import junit.framework.Assert;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.script.ScriptEngine;
import java.io.Serializable;
import java.util.*;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ExecutableStepsTest.Config.class)
public class ExecutableStepsTest {

    @Autowired
    private ExecutableSteps executableSteps;

    @Autowired
    private InputsBinding inputsBinding;

    @Autowired
    private ResultsBinding resultsBinding;

    @Autowired
    private OutputsBinding outputsBinding;

    @Test
    public void testStart() throws Exception {
        executableSteps.startExecutable(new ArrayList<Input>(), new RunEnvironment(), new HashMap<String, Serializable>(), new ExecutionRuntimeServices(),"", 2L);
    }

    @Test
    public void testStartWithInput() throws Exception {
        List<Input> inputs = Arrays.asList(new Input("input1","input1"));
        RunEnvironment runEnv = new RunEnvironment();

        Map<String,Serializable> resultMap = new HashMap<>();
        resultMap.put("input1",5);

        when(inputsBinding.bindInputs(eq(inputs), anyMap(), anyMap())).thenReturn(resultMap);
        executableSteps.startExecutable(inputs, runEnv, new HashMap<String, Serializable>(), new ExecutionRuntimeServices(),"", 2L);

        Map<String,Serializable> opVars = runEnv.getStack().popContext().getImmutableViewOfVariables();
        Assert.assertTrue(opVars.containsKey("input1"));
        Assert.assertEquals(5, opVars.get("input1"));

        Map<String,Serializable> callArg = runEnv.removeCallArguments();
        Assert.assertEquals(1,callArg.size());
        Assert.assertTrue(callArg.containsKey("input1"));
        Assert.assertEquals(5,callArg.get("input1"));
    }

    @Test
    public void testBoundInputEvent(){
        List<Input> inputs = Arrays.asList(new Input("input1","input1"),new Input("input2", "3", true, true, false, null));
        RunEnvironment runEnv = new RunEnvironment();
        ExecutionRuntimeServices runtimeServices = new ExecutionRuntimeServices();
        Map<String,Serializable> resultMap = new HashMap<>();
        resultMap.put("input1", 5);
        resultMap.put("input2", 3);

        when(inputsBinding.bindInputs(eq(inputs), anyMap(), anyMap())).thenReturn(resultMap);
        executableSteps.startExecutable(inputs, runEnv, new HashMap<String, Serializable>(), runtimeServices, "dockerizeStep", 2L);
        Collection<ScoreEvent> events = runtimeServices.getEvents();

        Assert.assertFalse(events.isEmpty());
        ScoreEvent boundInputEvent = null;
        for(ScoreEvent event:events){
            if(event.getEventType().equals(ScoreLangConstants.EVENT_INPUT_END)){
                boundInputEvent = event;
            }
        }
        Assert.assertNotNull(boundInputEvent);
        LanguageEventData eventData = (LanguageEventData)boundInputEvent.getData();
        Assert.assertTrue(eventData.containsKey(LanguageEventData.BOUND_INPUTS));
        Map<String,Serializable> inputsBounded = (Map<String,Serializable>)eventData.get(LanguageEventData.BOUND_INPUTS);

        Assert.assertNotNull(eventData.getStepName());
        Assert.assertEquals(LanguageEventData.StepType.EXECUTABLE, eventData.getStepType());
        Assert.assertEquals("dockerizeStep", eventData.getStepName());

        // verify input names are in defined order and have the expected value
        Set<Map.Entry<String, Serializable>> inputEntries = inputsBounded.entrySet();
        Iterator<Map.Entry<String, Serializable>> inputNamesIterator = inputEntries.iterator();

        Map.Entry<String, Serializable> firstInput =  inputNamesIterator.next();
        org.junit.Assert.assertEquals("Inputs are not in defined order in end inputs binding event", "input1", firstInput.getKey());
        org.junit.Assert.assertEquals(5, firstInput.getValue());

        Map.Entry<String, Serializable> secondInput =  inputNamesIterator.next();
        org.junit.Assert.assertEquals("Inputs are not in defined order in end inputs binding event", "input2", secondInput.getKey());
        org.junit.Assert.assertEquals(LanguageEventData.ENCRYPTED_VALUE, secondInput.getValue());
    }

    @Test
    public void testStartExecutableSetNextPosition() throws Exception {
        List<Input> inputs = Arrays.asList();
        RunEnvironment runEnv = new RunEnvironment();

        Long nextStepPosition = 2L;
        executableSteps.startExecutable(inputs, runEnv, new HashMap<String, Serializable>(), new ExecutionRuntimeServices(), "", nextStepPosition);

        Assert.assertEquals(nextStepPosition, runEnv.removeNextStepPosition());
    }

    @Test
    public void testFinishExecutableWithResult() throws Exception {
        List<Result> results = Arrays.asList(new Result(ScoreLangConstants.SUCCESS_RESULT,"true"));
        RunEnvironment runEnv = new RunEnvironment();
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Serializable>(), null));
        runEnv.getExecutionPath().down();

        when(resultsBinding.resolveResult(isNull(Map.class), anyMapOf(String.class, Serializable.class), eq(results), isNull(String.class))).thenReturn(ScoreLangConstants.SUCCESS_RESULT);
        executableSteps.finishExecutable(runEnv, new ArrayList<Output>(), results, new ExecutionRuntimeServices(),"");

        ReturnValues returnValues= runEnv.removeReturnValues();
        Assert.assertTrue(returnValues.getResult().equals(ScoreLangConstants.SUCCESS_RESULT));
    }

    @Test
    public void testFinishExecutableWithOutput() throws Exception {
        List<Output> possibleOutputs = Arrays.asList(new Output("name", "name"));
        RunEnvironment runEnv = new RunEnvironment();
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Serializable>(), null));
        runEnv.getExecutionPath().down();

        Map<String, Serializable> boundOutputs = new HashMap<>();
        boundOutputs.put("name", "John");

        when(outputsBinding.bindOutputs(isNull(Map.class), anyMapOf(String.class, Serializable.class), eq(possibleOutputs))).thenReturn(boundOutputs);
        executableSteps.finishExecutable(runEnv, possibleOutputs, new ArrayList<Result>(), new ExecutionRuntimeServices(),"");

        ReturnValues returnValues= runEnv.removeReturnValues();
        Map<String, Serializable> outputs = returnValues.getOutputs();
        Assert.assertEquals(1, outputs.size());
        Assert.assertEquals("John", outputs.get("name"));
    }

    @Test
    public void testFinishExecutableSetNextPositionToParentFlow() throws Exception {
        RunEnvironment runEnv = new RunEnvironment();
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Serializable>(), null));
        runEnv.getExecutionPath().down();
        Long parentFirstStepPosition = 2L;
        runEnv.getParentFlowStack().pushParentFlowData(new ParentFlowData(111L, parentFirstStepPosition));

        executableSteps.finishExecutable(runEnv, new ArrayList<Output>(), new ArrayList<Result>(), new ExecutionRuntimeServices(), "");

        Assert.assertEquals(parentFirstStepPosition, runEnv.removeNextStepPosition());
    }

    @Test
    public void testFinishExecutableSetNextPositionNoParentFlow() throws Exception {
        RunEnvironment runEnv = new RunEnvironment();
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Serializable>(), null));
        runEnv.getExecutionPath().down();

        executableSteps.finishExecutable(runEnv, new ArrayList<Output>(), new ArrayList<Result>(), new ExecutionRuntimeServices(), "");

        Assert.assertEquals(null, runEnv.removeNextStepPosition());
    }

    //todo: split to several methods
    @Test
    public void testFinishExecutableEvents(){
        List<Output> possibleOutputs = Arrays.asList(new Output("name", "name"));
        List<Result> possibleResults = Arrays.asList(new Result(ScoreLangConstants.SUCCESS_RESULT,"true"));
        RunEnvironment runEnv = new RunEnvironment();
        runEnv.putReturnValues(new ReturnValues(new HashMap<String, Serializable>(), null));
        runEnv.getExecutionPath().down();

        Map<String, Serializable> boundOutputs = new HashMap<>();
        boundOutputs.put("name", "John");
        String boundResult = ScoreLangConstants.SUCCESS_RESULT;

        when(outputsBinding.bindOutputs(isNull(Map.class), anyMapOf(String.class, Serializable.class), eq(possibleOutputs))).thenReturn(boundOutputs);
        when(resultsBinding.resolveResult(isNull(Map.class), anyMapOf(String.class, Serializable.class), eq(possibleResults), isNull(String.class))).thenReturn(boundResult);

        ExecutionRuntimeServices runtimeServices = new ExecutionRuntimeServices();
        executableSteps.finishExecutable(runEnv, possibleOutputs, possibleResults, runtimeServices,"task1");

        Collection<ScoreEvent> events = runtimeServices.getEvents();

        Assert.assertFalse(events.isEmpty());
        ScoreEvent boundOutputEvent = null;
        ScoreEvent startOutputEvent = null;
        ScoreEvent executableFinishedEvent = null;
        for(ScoreEvent event:events){
            if(event.getEventType().equals(ScoreLangConstants.EVENT_OUTPUT_END)){
                boundOutputEvent = event;
            } else if(event.getEventType().equals(ScoreLangConstants.EVENT_OUTPUT_START)){
                startOutputEvent = event;
            } else if(event.getEventType().equals(ScoreLangConstants.EVENT_EXECUTION_FINISHED)){
                executableFinishedEvent = event;
            }
        }
        Assert.assertNotNull(startOutputEvent);
        LanguageEventData eventData = (LanguageEventData)startOutputEvent.getData();
        Assert.assertTrue(eventData.containsKey(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY));
        Assert.assertTrue(eventData.containsKey(ScoreLangConstants.EXECUTABLE_RESULTS_KEY));
        List<Output> outputs= (List<Output>)eventData.get(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY);
        List<Result> results= (List<Result>)eventData.get(ScoreLangConstants.EXECUTABLE_RESULTS_KEY);
        Assert.assertEquals(possibleOutputs, outputs);
        Assert.assertEquals(possibleResults, results);

        Assert.assertNotNull(boundOutputEvent);
        eventData = (LanguageEventData)boundOutputEvent.getData();
        Assert.assertTrue(eventData.containsKey(LanguageEventData.OUTPUTS));
        Map<String, Serializable> returnOutputs= eventData.getOutputs();
        String returnResult= (String)eventData.get(LanguageEventData.RESULT);
        Assert.assertEquals("task1",eventData.getStepName());
        Assert.assertEquals(LanguageEventData.StepType.EXECUTABLE, eventData.getStepType());
        Assert.assertEquals(1, returnOutputs.size());
        Assert.assertEquals("John", returnOutputs.get("name"));
        Assert.assertTrue(returnResult.equals(ScoreLangConstants.SUCCESS_RESULT));

        Assert.assertNotNull(executableFinishedEvent);
        eventData = (LanguageEventData)executableFinishedEvent.getData();
        String result = (String)eventData.get(LanguageEventData.RESULT);
        Map<String, String> eventOutputs = (Map<String, String>)eventData.get(LanguageEventData.OUTPUTS);
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, result);
        Assert.assertEquals(boundOutputs, eventOutputs);

    }

    @Configuration
    static class Config{

        @Bean
        public InputsBinding inputsBinding(){
            return mock(InputsBinding.class);
        }

        @Bean
        public OutputsBinding outputsBinding(){
            return mock(OutputsBinding.class);
        }

        @Bean
        public ResultsBinding resultsBinding(){
            return mock(ResultsBinding.class);
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
        public ExecutableSteps operationSteps(){
            return new ExecutableSteps();
        }

    }
}