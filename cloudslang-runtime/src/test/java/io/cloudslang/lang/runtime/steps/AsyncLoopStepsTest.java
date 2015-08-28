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

import io.cloudslang.lang.entities.AsyncLoopStatement;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.runtime.RuntimeConstants;
import io.cloudslang.lang.runtime.bindings.*;
import io.cloudslang.lang.runtime.env.Context;
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.api.EndBranchDataContainer;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.python.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.script.ScriptEngine;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
* Date: 4/7/2015
*
* @author Bonczidai Levente
*/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AsyncLoopStepsTest.Config.class)
public class AsyncLoopStepsTest {

    public static final String BRANCH_EXCEPTION_MESSAGE = "Exception details placeholder";

    @Autowired
    private AsyncLoopSteps asyncLoopSteps;

    @Autowired
    private AsyncLoopBinding asyncLoopBinding;

    @Autowired
    private OutputsBinding outputsBinding;

    @Autowired
    private LoopsBinding loopsBinding;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void resetMocks() {
        reset(asyncLoopBinding);
        reset(outputsBinding);
        reset(loopsBinding);
    }

    @Test
    public void testBranchesAreCreated() throws Exception {
        // prepare arguments
        AsyncLoopStatement asyncLoopStatement = new AsyncLoopStatement("varName", "expression");

        RunEnvironment runEnvironment = new RunEnvironment();
        Map<String, Serializable> variables = new HashMap<>();
        Context context = new Context(variables);
        runEnvironment.getStack().pushContext(context);

        String nodeName = "nodeName";
        String refId = "branch_id";

        // prepare mocks
        ExecutionRuntimeServices executionRuntimeServices = mock(ExecutionRuntimeServices.class);
        List<Serializable> expectedSplitData = Lists.newArrayList((Serializable) 1, 2, 3);
        when(asyncLoopBinding.bindAsyncLoopList(eq(asyncLoopStatement), eq(context), eq(nodeName))).thenReturn(expectedSplitData);
        Long branchBeginStepID = 3L;

        // call method
        asyncLoopSteps.addBranches(
                asyncLoopStatement,
                runEnvironment,
                executionRuntimeServices,
                nodeName,
                1234L,
                5L,
                branchBeginStepID,
                refId
        );

        // verify expected behaviour
        ArgumentCaptor<Map> branchContextArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(executionRuntimeServices, times(3)).addBranch(eq(branchBeginStepID), eq(refId), branchContextArgumentCaptor.capture());

        List<Map> branchContexts = branchContextArgumentCaptor.getAllValues();
        List<Serializable> actualSplitData = Lists.newArrayList();
        for (Map branchContext : branchContexts) {
            Assert.assertTrue("runtime environment not found in branch context", branchContext.containsKey(ScoreLangConstants.RUN_ENV));
            RunEnvironment branchRunEnvironment = (RunEnvironment) branchContext.get(ScoreLangConstants.RUN_ENV);
            Map<String, Serializable> branchVariables = branchRunEnvironment.getStack().popContext().getImmutableViewOfVariables();
            actualSplitData.add(branchVariables.get("varName"));
        }
        Assert.assertEquals(expectedSplitData, actualSplitData);

        Assert.assertEquals(5, (long) runEnvironment.removeNextStepPosition());
    }

    @Test
    public void testAddBranchesEventsAreFired() throws Exception {
        // prepare arguments
        AsyncLoopStatement asyncLoopStatement = new AsyncLoopStatement("varName", "expression");

        RunEnvironment runEnvironment = new RunEnvironment();
        Map<String, Serializable> variables = new HashMap<>();
        Context context = new Context(variables);
        runEnvironment.getStack().pushContext(context);

        String nodeName = "nodeName";
        String refId = "branch_id";

        // prepare mocks
        ExecutionRuntimeServices executionRuntimeServices = mock(ExecutionRuntimeServices.class);
        List<Serializable> expectedSplitData = Lists.newArrayList((Serializable) 1, 2, 3);
        when(asyncLoopBinding.bindAsyncLoopList(eq(asyncLoopStatement), eq(context), eq(nodeName))).thenReturn(expectedSplitData);
        Long branchBeginStepID = 0L;

        // call method
        asyncLoopSteps.addBranches(
                asyncLoopStatement,
                runEnvironment,
                executionRuntimeServices,
                nodeName,
                1234L,
                5L,
                branchBeginStepID,
                refId
        );

        // verify expected behaviour
        ArgumentCaptor<String> eventTypeArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(executionRuntimeServices, times(4)).addEvent(eventTypeArgumentCaptor.capture(), any(LanguageEventData.class));

        List<String> expectedEventTypesInOrder = Lists.newArrayList(
                ScoreLangConstants.EVENT_SPLIT_BRANCHES,
                ScoreLangConstants.EVENT_BRANCH_START,
                ScoreLangConstants.EVENT_BRANCH_START,
                ScoreLangConstants.EVENT_BRANCH_START
        );
        List<String> actualEventTypesInOrder = eventTypeArgumentCaptor.getAllValues();
        Assert.assertEquals(expectedEventTypesInOrder, actualEventTypesInOrder);

        Assert.assertEquals(5, (long) runEnvironment.removeNextStepPosition());
    }

    @Test
    public void testJoinBranchesAggregateContexts() throws Exception {
        // prepare arguments
        RunEnvironment runEnvironment = new RunEnvironment();
        runEnvironment.getExecutionPath().down();
        Map<String, Serializable> variables = new HashMap<>();
        Context context = new Context(variables);
        runEnvironment.getStack().pushContext(context);

        List<Output> taskAggregateValues = Lists.newArrayList(new Output("outputName", "outputExpression"));

        Map<String, ResultNavigation> taskNavigationValues = new HashMap<>();
        ResultNavigation successNavigation = new ResultNavigation(0L, ScoreLangConstants.SUCCESS_RESULT);
        ResultNavigation failureNavigation = new ResultNavigation(0L, ScoreLangConstants.FAILURE_RESULT);
        taskNavigationValues.put(ScoreLangConstants.SUCCESS_RESULT, successNavigation);
        taskNavigationValues.put(ScoreLangConstants.FAILURE_RESULT, failureNavigation);

        String nodeName = "nodeName";

        // prepare mocks
        Map<String, Serializable> runtimeContext1 = new HashMap<>();
        Map<String, Serializable> runtimeContext2 = new HashMap<>();
        Map<String, Serializable> runtimeContext3 = new HashMap<>();
        runtimeContext1.put("branch1Output", 1);
        runtimeContext2.put("branch2Output", 2);
        runtimeContext3.put("branch3Output", 3);

        ExecutionRuntimeServices executionRuntimeServices = createAndConfigureExecutionRuntimeServicesMock(
                runtimeContext1,
                runtimeContext2,
                runtimeContext3
        );

        // call method
        asyncLoopSteps.joinBranches(
                runEnvironment,
                executionRuntimeServices,
                taskAggregateValues,
                taskNavigationValues,
                nodeName
        );

        // verify expected behaviour
        ArgumentCaptor<Map> aggregateContextArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(outputsBinding).bindOutputs(eq(context.getImmutableViewOfVariables()), aggregateContextArgumentCaptor.capture(), eq(taskAggregateValues));

        @SuppressWarnings("unchecked")
        List<Map<String, Serializable>> expectedBranchContexts = Lists.newArrayList(runtimeContext1, runtimeContext2, runtimeContext3);
        @SuppressWarnings("unchecked")
        Map<String, Serializable> aggregateContext = aggregateContextArgumentCaptor.getValue();
        Assert.assertTrue(aggregateContext.containsKey(RuntimeConstants.BRANCHES_CONTEXT_KEY));
        @SuppressWarnings("unchecked")
        List<Map<String, Serializable>> actualBranchesContexts =
                (List<Map<String, Serializable>>) aggregateContext.get(RuntimeConstants.BRANCHES_CONTEXT_KEY);
        Assert.assertEquals(expectedBranchContexts, actualBranchesContexts);
    }

    @Test
    public void testJoinBranchesNavigationAllBranchesSucced() throws Exception {
        // prepare arguments
        RunEnvironment runEnvironment = new RunEnvironment();
        runEnvironment.getExecutionPath().down();
        Map<String, Serializable> variables = new HashMap<>();
        Context context = new Context(variables);
        runEnvironment.getStack().pushContext(context);

        List<Output> taskAggregateValues = Lists.newArrayList(new Output("outputName", "outputExpression"));

        Map<String, ResultNavigation> taskNavigationValues = new HashMap<>();
        ResultNavigation successNavigation = new ResultNavigation(0L, "CUSTOM_SUCCESS");
        ResultNavigation failureNavigation = new ResultNavigation(0L, "CUSTOM_FAILURE");
        taskNavigationValues.put(ScoreLangConstants.SUCCESS_RESULT, successNavigation);
        taskNavigationValues.put(ScoreLangConstants.FAILURE_RESULT, failureNavigation);

        String nodeName = "nodeName";

        // prepare mocks
        Map<String, Serializable> runtimeContext1 = new HashMap<>();
        Map<String, Serializable> runtimeContext2 = new HashMap<>();
        Map<String, Serializable> runtimeContext3 = new HashMap<>();
        runtimeContext1.put("branch1Output", 1);
        runtimeContext2.put("branch2Output", 2);
        runtimeContext3.put("branch3Output", 3);

        ExecutionRuntimeServices executionRuntimeServices = createAndConfigureExecutionRuntimeServicesMock(
                runtimeContext1,
                runtimeContext2,
                runtimeContext3
        );

        // call method
        asyncLoopSteps.joinBranches(
                runEnvironment,
                executionRuntimeServices,
                taskAggregateValues,
                taskNavigationValues,
                nodeName
        );

        // verify expected behaviour
        Assert.assertEquals(0, (long) runEnvironment.removeNextStepPosition());

        ReturnValues returnValues = runEnvironment.removeReturnValues();
        Assert.assertNotNull("return values not found in runtime environment", returnValues);
        Assert.assertEquals("CUSTOM_SUCCESS", returnValues.getResult());
    }

    @Test
    public void testJoinBranchesNavigationOneBranchFails() throws Exception {
        // prepare arguments
        RunEnvironment runEnvironment = new RunEnvironment();
        runEnvironment.getExecutionPath().down();
        Map<String, Serializable> variables = new HashMap<>();
        Context context = new Context(variables);
        runEnvironment.getStack().pushContext(context);

        List<Output> taskAggregateValues = Lists.newArrayList(new Output("outputName", "outputExpression"));

        Map<String, ResultNavigation> taskNavigationValues = new HashMap<>();
        ResultNavigation successNavigation = new ResultNavigation(0L, "CUSTOM_SUCCESS");
        ResultNavigation failureNavigation = new ResultNavigation(0L, "CUSTOM_FAILURE");
        taskNavigationValues.put(ScoreLangConstants.SUCCESS_RESULT, successNavigation);
        taskNavigationValues.put(ScoreLangConstants.FAILURE_RESULT, failureNavigation);

        String nodeName = "nodeName";

        // prepare mocks
        Map<String, Serializable> runtimeContext1 = new HashMap<>();
        Map<String, Serializable> runtimeContext2 = new HashMap<>();
        Map<String, Serializable> runtimeContext3 = new HashMap<>();
        runtimeContext1.put("branch1Output", 1);
        runtimeContext2.put("branch2Output", 2);
        runtimeContext3.put("branch3Output", 3);

        ReturnValues returnValues1 = new ReturnValues(new HashMap<String, Serializable>(), ScoreLangConstants.SUCCESS_RESULT);
        ReturnValues returnValues2 = new ReturnValues(new HashMap<String, Serializable>(), ScoreLangConstants.FAILURE_RESULT);
        ReturnValues returnValues3 = new ReturnValues(new HashMap<String, Serializable>(), ScoreLangConstants.SUCCESS_RESULT);
        ExecutionRuntimeServices executionRuntimeServices = createAndConfigureExecutionRuntimeServicesMock(
                runtimeContext1,
                runtimeContext2,
                runtimeContext3,
                returnValues1,
                returnValues2,
                returnValues3
        );

        // call method
        asyncLoopSteps.joinBranches(
                runEnvironment,
                executionRuntimeServices,
                taskAggregateValues,
                taskNavigationValues,
                nodeName
        );

        // verify expected behaviour
        Assert.assertEquals(0, (long) runEnvironment.removeNextStepPosition());

        ReturnValues returnValues = runEnvironment.removeReturnValues();
        Assert.assertNotNull("return values not found in runtime environment", returnValues);
        Assert.assertEquals("CUSTOM_FAILURE", returnValues.getResult());
    }

    @Test
    public void testJoinBranchesEventsAreFired() throws Exception {
        // prepare arguments
        RunEnvironment runEnvironment = new RunEnvironment();
        runEnvironment.getExecutionPath().down();
        Map<String, Serializable> variables = new HashMap<>();
        Context context = new Context(variables);
        runEnvironment.getStack().pushContext(context);

        List<Output> taskAggregateValues = Lists.newArrayList(new Output("outputName", "outputExpression"));

        Map<String, ResultNavigation> taskNavigationValues = new HashMap<>();
        ResultNavigation successNavigation = new ResultNavigation(0L, ScoreLangConstants.SUCCESS_RESULT);
        ResultNavigation failureNavigation = new ResultNavigation(0L, ScoreLangConstants.FAILURE_RESULT);
        taskNavigationValues.put(ScoreLangConstants.SUCCESS_RESULT, successNavigation);
        taskNavigationValues.put(ScoreLangConstants.FAILURE_RESULT, failureNavigation);

        String nodeName = "nodeName";

        // prepare mocks
        Map<String, Serializable> runtimeContext1 = new HashMap<>();
        Map<String, Serializable> runtimeContext2 = new HashMap<>();
        Map<String, Serializable> runtimeContext3 = new HashMap<>();
        runtimeContext1.put("branch1Output", 1);
        runtimeContext2.put("branch2Output", 2);
        runtimeContext3.put("branch3Output", 3);

        ExecutionRuntimeServices executionRuntimeServices = createAndConfigureExecutionRuntimeServicesMock(
                runtimeContext1,
                runtimeContext2,
                runtimeContext3
        );

        // call method
        asyncLoopSteps.joinBranches(
                runEnvironment,
                executionRuntimeServices,
                taskAggregateValues,
                taskNavigationValues,
                nodeName
        );

        // verify expected behaviour
        ArgumentCaptor<String> eventTypeArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(executionRuntimeServices, times(5)).addEvent(eventTypeArgumentCaptor.capture(), any(LanguageEventData.class));

        List<String> expectedEventTypesInOrder = Lists.newArrayList(
                ScoreLangConstants.EVENT_BRANCH_END,
                ScoreLangConstants.EVENT_BRANCH_END,
                ScoreLangConstants.EVENT_BRANCH_END,
                ScoreLangConstants.EVENT_JOIN_BRANCHES_START,
                ScoreLangConstants.EVENT_JOIN_BRANCHES_END
        );
        List<String> actualEventTypesInOrder = eventTypeArgumentCaptor.getAllValues();
        Assert.assertEquals(expectedEventTypesInOrder, actualEventTypesInOrder);
    }

    @Test
    public void testExceptionIsCapturedFromBranches() throws Exception {
        // prepare arguments
        RunEnvironment runEnvironment = new RunEnvironment();
        runEnvironment.getExecutionPath().down();
        Map<String, Serializable> variables = new HashMap<>();
        Context context = new Context(variables);
        runEnvironment.getStack().pushContext(context);

        ExecutionRuntimeServices executionRuntimeServices = createExecutionRuntimeServicesMockWithBranchException();

        exception.expectMessage(BRANCH_EXCEPTION_MESSAGE);
        exception.expect(RuntimeException.class);

        asyncLoopSteps.joinBranches(
                runEnvironment,
                executionRuntimeServices,
                new ArrayList<Output>(0),
                new HashMap<String, ResultNavigation>(),
                "nodeName"
        );
    }

    private ExecutionRuntimeServices createAndConfigureExecutionRuntimeServicesMock(
            Map<String, Serializable> runtimeContext1,
            Map<String, Serializable> runtimeContext2,
            Map<String, Serializable> runtimeContext3) {
        ReturnValues returnValues1 = new ReturnValues(new HashMap<String, Serializable>(), ScoreLangConstants.SUCCESS_RESULT);
        ReturnValues returnValues2 = new ReturnValues(new HashMap<String, Serializable>(), ScoreLangConstants.SUCCESS_RESULT);
        ReturnValues returnValues3 = new ReturnValues(new HashMap<String, Serializable>(), ScoreLangConstants.SUCCESS_RESULT);

        return createAndConfigureExecutionRuntimeServicesMock(
                runtimeContext1,
                runtimeContext2,
                runtimeContext3,
                returnValues1,
                returnValues2,
                returnValues3
        );
    }

    private ExecutionRuntimeServices createAndConfigureExecutionRuntimeServicesMock(
            Map<String, Serializable> runtimeContext1,
            Map<String, Serializable> runtimeContext2,
            Map<String, Serializable> runtimeContext3,
            ReturnValues returnValues1,
            ReturnValues returnValues2,
            ReturnValues returnValues3) {
        ExecutionRuntimeServices executionRuntimeServices = mock(ExecutionRuntimeServices.class);

        Map<String, Serializable> branchContext1 = new HashMap<>();
        Map<String, Serializable> branchContext2 = new HashMap<>();
        Map<String, Serializable> branchContext3 = new HashMap<>();
        RunEnvironment branchRuntimeEnvironment1 = new RunEnvironment();
        RunEnvironment branchRuntimeEnvironment2 = new RunEnvironment();
        RunEnvironment branchRuntimeEnvironment3 = new RunEnvironment();
        branchRuntimeEnvironment1.getExecutionPath().down();
        branchRuntimeEnvironment2.getExecutionPath().down();
        branchRuntimeEnvironment3.getExecutionPath().down();
        branchRuntimeEnvironment1.getStack().pushContext(new Context(runtimeContext1));
        branchRuntimeEnvironment2.getStack().pushContext(new Context(runtimeContext2));
        branchRuntimeEnvironment3.getStack().pushContext(new Context(runtimeContext3));
        branchRuntimeEnvironment1.putReturnValues(returnValues1);
        branchRuntimeEnvironment2.putReturnValues(returnValues2);
        branchRuntimeEnvironment3.putReturnValues(returnValues3);
        branchContext1.put(ScoreLangConstants.RUN_ENV, branchRuntimeEnvironment1);
        branchContext2.put(ScoreLangConstants.RUN_ENV, branchRuntimeEnvironment2);
        branchContext3.put(ScoreLangConstants.RUN_ENV, branchRuntimeEnvironment3);

        List<EndBranchDataContainer> branchesContainers = Lists.newArrayList(
                new EndBranchDataContainer(branchContext1, new HashMap<String, Serializable>(), null),
                new EndBranchDataContainer(branchContext2, new HashMap<String, Serializable>(), null),
                new EndBranchDataContainer(branchContext3, new HashMap<String, Serializable>(), null)
        );
        when(executionRuntimeServices.getFinishedChildBranchesData()).thenReturn(branchesContainers);
        return executionRuntimeServices;
    }

    private ExecutionRuntimeServices createExecutionRuntimeServicesMockWithBranchException() {
        ExecutionRuntimeServices executionRuntimeServices = mock(ExecutionRuntimeServices.class);
        List<EndBranchDataContainer> branchesContainers = Lists.newArrayList(
                new EndBranchDataContainer(
                        new HashMap<String, Serializable>(),
                        new HashMap<String, Serializable>(),
                        BRANCH_EXCEPTION_MESSAGE)
        );
        when(executionRuntimeServices.getFinishedChildBranchesData()).thenReturn(branchesContainers);
        return executionRuntimeServices;
    }

    @Configuration
    static class Config {

        @Bean
        public AsyncLoopBinding asyncLoopBinding() {
            return mock(AsyncLoopBinding.class);
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
        public AsyncLoopSteps asyncLoopSteps() {
            return new AsyncLoopSteps();
        }

        @Bean
        public ScriptEvaluator scriptEvaluator(){
            return mock(ScriptEvaluator.class);
        }

        @Bean
        public ScriptEngine scriptEngine(){
            return mock(ScriptEngine.class);
        }

    }
}
