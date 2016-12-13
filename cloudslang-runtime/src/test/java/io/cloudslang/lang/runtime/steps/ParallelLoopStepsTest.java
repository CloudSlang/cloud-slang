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
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.ScriptFunction;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.entities.encryption.DummyEncryptor;
import io.cloudslang.lang.runtime.RuntimeConstants;
import io.cloudslang.lang.runtime.bindings.LoopsBinding;
import io.cloudslang.lang.runtime.bindings.OutputsBinding;
import io.cloudslang.lang.runtime.bindings.ParallelLoopBinding;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import io.cloudslang.lang.runtime.env.Context;
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import io.cloudslang.runtime.impl.python.PythonExecutionCachedEngine;
import io.cloudslang.runtime.impl.python.PythonExecutionEngine;
import io.cloudslang.runtime.impl.python.PythonRuntimeServiceImpl;
import io.cloudslang.score.api.EndBranchDataContainer;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventBusImpl;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.python.google.common.collect.Lists.newArrayList;

/**
 * Date: 4/7/2015
 *
 * @author Bonczidai Levente
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ParallelLoopStepsTest.Config.class)
public class ParallelLoopStepsTest {

    private static final String BRANCH_EXCEPTION_MESSAGE = "Exception details placeholder";
    private static final String SUCCESS_RESULT = "SUCCESS";

    @Autowired
    private ParallelLoopExecutionData parallelLoopSteps;

    @Autowired
    private ParallelLoopBinding parallelLoopBinding;

    @Autowired
    private OutputsBinding outputsBinding;

    @Autowired
    private LoopsBinding loopsBinding;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void resetMocks() {
        reset(parallelLoopBinding);
        reset(outputsBinding);
        reset(loopsBinding);
    }

    @Test
    public void testBranchesAreCreated() throws Exception {
        // prepare arguments
        ListLoopStatement parallelLoopStatement = new ListLoopStatement("varName", "expression",
                new HashSet<ScriptFunction>(), new HashSet<String>(), true);

        RunEnvironment runEnvironment = new RunEnvironment();
        Map<String, Value> variables = new HashMap<>();
        Context context = new Context(variables);
        runEnvironment.getStack().pushContext(context);

        String nodeName = "nodeName";
        String refId = "branch_id";

        // prepare mocks
        ExecutionRuntimeServices executionRuntimeServices = mock(ExecutionRuntimeServices.class);
        List<Value> expectedSplitData = newArrayList(ValueFactory.create(1),
                ValueFactory.create(2), ValueFactory.create(3));
        when(parallelLoopBinding.bindParallelLoopList(eq(parallelLoopStatement),
                eq(context), eq(runEnvironment.getSystemProperties()), eq(nodeName)))
                .thenReturn(expectedSplitData);
        Long branchBeginStepId = 3L;

        // call method
        parallelLoopSteps.addBranches(
                parallelLoopStatement,
                runEnvironment,
                executionRuntimeServices,
                nodeName,
                1234L,
                5L,
                branchBeginStepId,
                refId
        );

        // verify expected behaviour
        ArgumentCaptor<Map> branchContextArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        //noinspection unchecked
        verify(executionRuntimeServices, times(3))
                .addBranch(eq(branchBeginStepId), eq(refId), branchContextArgumentCaptor.capture());

        List<Map> branchContexts = branchContextArgumentCaptor.getAllValues();
        List<Value> actualSplitData = newArrayList();
        for (Map branchContext : branchContexts) {
            assertTrue("runtime environment not found in branch context",
                    branchContext.containsKey(ScoreLangConstants.RUN_ENV));
            RunEnvironment branchRunEnvironment = (RunEnvironment) branchContext.get(ScoreLangConstants.RUN_ENV);
            Map<String, Value> branchVariables =
                    branchRunEnvironment.getStack().popContext().getImmutableViewOfVariables();
            actualSplitData.add(branchVariables.get("varName"));
        }
        Assert.assertEquals(expectedSplitData, actualSplitData);

        Assert.assertEquals(5, (long) runEnvironment.removeNextStepPosition());
    }

    @Test
    public void testAddBranchesEventsAreFired() throws Exception {
        // prepare arguments
        ListLoopStatement parallelLoopStatement = new ListLoopStatement("varName", "expression",
                new HashSet<ScriptFunction>(), new HashSet<String>(), true);

        RunEnvironment runEnvironment = new RunEnvironment();
        Map<String, Value> variables = new HashMap<>();
        Context context = new Context(variables);
        runEnvironment.getStack().pushContext(context);

        String nodeName = "nodeName";
        String refId = "branch_id";

        // prepare mocks
        ExecutionRuntimeServices executionRuntimeServices = mock(ExecutionRuntimeServices.class);
        List<Value> expectedSplitData = newArrayList(ValueFactory.create(1),
                ValueFactory.create(2), ValueFactory.create(3));
        when(parallelLoopBinding.bindParallelLoopList(eq(parallelLoopStatement),
                eq(context), eq(runEnvironment.getSystemProperties()), eq(nodeName)))
                .thenReturn(expectedSplitData);
        Long branchBeginStepId = 0L;

        // call method
        parallelLoopSteps.addBranches(
                parallelLoopStatement,
                runEnvironment,
                executionRuntimeServices,
                nodeName,
                1234L,
                5L,
                branchBeginStepId,
                refId
        );

        // verify expected behaviour
        ArgumentCaptor<String> eventTypeArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(executionRuntimeServices, times(4))
                .addEvent(eventTypeArgumentCaptor.capture(), any(LanguageEventData.class));

        List<String> expectedEventTypesInOrder = newArrayList(
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
    public void testJoinBranchesPublish() throws Exception {
        // prepare arguments
        RunEnvironment runEnvironment = new RunEnvironment();
        runEnvironment.getExecutionPath().down();
        Map<String, Value> variables = new HashMap<>();
        Context context = new Context(variables);
        runEnvironment.getStack().pushContext(context);


        Map<String, ResultNavigation> stepNavigationValues = new HashMap<>();
        ResultNavigation successNavigation = new ResultNavigation(0L, ScoreLangConstants.SUCCESS_RESULT);
        ResultNavigation failureNavigation = new ResultNavigation(0L, ScoreLangConstants.FAILURE_RESULT);
        stepNavigationValues.put(ScoreLangConstants.SUCCESS_RESULT, successNavigation);
        stepNavigationValues.put(ScoreLangConstants.FAILURE_RESULT, failureNavigation);


        // prepare mocks
        Map<String, Serializable> runtimeContext1 = new HashMap<>();
        runtimeContext1.put("branch1Output", 1);
        runtimeContext1.put(ScoreLangConstants.BRANCH_RESULT_KEY, SUCCESS_RESULT);

        Map<String, Serializable> runtimeContext2 = new HashMap<>();
        runtimeContext2.put("branch2Output", 2);
        runtimeContext2.put(ScoreLangConstants.BRANCH_RESULT_KEY, SUCCESS_RESULT);

        Map<String, Serializable> runtimeContext3 = new HashMap<>();
        runtimeContext3.put("branch3Output", 3);
        runtimeContext3.put(ScoreLangConstants.BRANCH_RESULT_KEY, SUCCESS_RESULT);

        List<Output> stepPublishValues =
                newArrayList(new Output("outputName", ValueFactory.create("outputExpression")));
        String nodeName = "nodeName";
        ExecutionRuntimeServices executionRuntimeServices =
                createAndConfigureExecutionRuntimeServicesMock(runtimeContext1, runtimeContext2, runtimeContext3);

        // call method
        parallelLoopSteps.joinBranches(runEnvironment, executionRuntimeServices,
                stepPublishValues, stepNavigationValues, nodeName);

        // verify expected behaviour
        ArgumentCaptor<Map> aggregateContextArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        //noinspection unchecked
        verify(outputsBinding)
                .bindOutputs(eq(context.getImmutableViewOfVariables()), aggregateContextArgumentCaptor.capture(),
                eq(runEnvironment.getSystemProperties()), eq(stepPublishValues));

        @SuppressWarnings("unchecked")
        List<Map<String, Serializable>> expectedBranchContexts =
                newArrayList(runtimeContext1, runtimeContext2, runtimeContext3);
        @SuppressWarnings("unchecked")
        Map<String, Value> aggregateContext = aggregateContextArgumentCaptor.getValue();
        assertTrue(aggregateContext.containsKey(RuntimeConstants.BRANCHES_CONTEXT_KEY));
        @SuppressWarnings("unchecked")
        List<Map<String, Value>> actualBranchesContexts =
                (List<Map<String, Value>>) aggregateContext.get(RuntimeConstants.BRANCHES_CONTEXT_KEY).get();
        Assert.assertEquals(expectedBranchContexts, actualBranchesContexts);
    }

    @Test
    public void testJoinBranchesNavigationAllBranchesSucced() throws Exception {
        // prepare arguments
        RunEnvironment runEnvironment = new RunEnvironment();
        runEnvironment.getExecutionPath().down();
        Map<String, Value> variables = new HashMap<>();
        Context context = new Context(variables);
        runEnvironment.getStack().pushContext(context);

        final List<Output> stepPublishValues =
                newArrayList(new Output("outputName", ValueFactory.create("outputExpression")));

        Map<String, ResultNavigation> stepNavigationValues = new HashMap<>();
        ResultNavigation successNavigation = new ResultNavigation(0L, "CUSTOM_SUCCESS");
        ResultNavigation failureNavigation = new ResultNavigation(0L, "CUSTOM_FAILURE");
        stepNavigationValues.put(ScoreLangConstants.SUCCESS_RESULT, successNavigation);
        stepNavigationValues.put(ScoreLangConstants.FAILURE_RESULT, failureNavigation);

        final String nodeName = "nodeName";

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
        parallelLoopSteps.joinBranches(
                runEnvironment,
                executionRuntimeServices,
                stepPublishValues,
                stepNavigationValues,
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
        Map<String, Value> variables = new HashMap<>();
        Context context = new Context(variables);
        runEnvironment.getStack().pushContext(context);

        final List<Output> stepPublishValues =
                newArrayList(new Output("outputName", ValueFactory.create("outputExpression")));

        Map<String, ResultNavigation> stepNavigationValues = new HashMap<>();
        ResultNavigation successNavigation = new ResultNavigation(0L, "CUSTOM_SUCCESS");
        ResultNavigation failureNavigation = new ResultNavigation(0L, "CUSTOM_FAILURE");
        stepNavigationValues.put(ScoreLangConstants.SUCCESS_RESULT, successNavigation);
        stepNavigationValues.put(ScoreLangConstants.FAILURE_RESULT, failureNavigation);

        final String nodeName = "nodeName";

        // prepare mocks
        Map<String, Serializable> runtimeContext1 = new HashMap<>();
        Map<String, Serializable> runtimeContext2 = new HashMap<>();
        Map<String, Serializable> runtimeContext3 = new HashMap<>();
        runtimeContext1.put("branch1Output", 1);
        runtimeContext2.put("branch2Output", 2);
        runtimeContext3.put("branch3Output", 3);

        ReturnValues returnValues1 = new ReturnValues(new HashMap<String, Value>(), ScoreLangConstants.SUCCESS_RESULT);
        ReturnValues returnValues2 = new ReturnValues(new HashMap<String, Value>(), ScoreLangConstants.FAILURE_RESULT);
        ReturnValues returnValues3 = new ReturnValues(new HashMap<String, Value>(), ScoreLangConstants.SUCCESS_RESULT);
        ExecutionRuntimeServices executionRuntimeServices = createAndConfigureExecutionRuntimeServicesMock(
                runtimeContext1,
                runtimeContext2,
                runtimeContext3,
                returnValues1,
                returnValues2,
                returnValues3
        );

        // call method
        parallelLoopSteps.joinBranches(
                runEnvironment,
                executionRuntimeServices,
                stepPublishValues,
                stepNavigationValues,
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
        Map<String, Value> variables = new HashMap<>();
        Context context = new Context(variables);
        runEnvironment.getStack().pushContext(context);

        final List<Output> stepPublishValues =
                newArrayList(new Output("outputName", ValueFactory.create("outputExpression")));

        Map<String, ResultNavigation> stepNavigationValues = new HashMap<>();
        ResultNavigation successNavigation = new ResultNavigation(0L, ScoreLangConstants.SUCCESS_RESULT);
        ResultNavigation failureNavigation = new ResultNavigation(0L, ScoreLangConstants.FAILURE_RESULT);
        stepNavigationValues.put(ScoreLangConstants.SUCCESS_RESULT, successNavigation);
        stepNavigationValues.put(ScoreLangConstants.FAILURE_RESULT, failureNavigation);

        final String nodeName = "nodeName";

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
        parallelLoopSteps.joinBranches(
                runEnvironment,
                executionRuntimeServices,
                stepPublishValues,
                stepNavigationValues,
                nodeName
        );

        // verify expected behaviour
        ArgumentCaptor<String> eventTypeArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(executionRuntimeServices, times(5))
                .addEvent(eventTypeArgumentCaptor.capture(), any(LanguageEventData.class));

        List<String> expectedEventTypesInOrder = newArrayList(
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
        Map<String, Value> variables = new HashMap<>();
        Context context = new Context(variables);
        runEnvironment.getStack().pushContext(context);

        ExecutionRuntimeServices executionRuntimeServices = createExecutionRuntimeServicesMockWithBranchException();

        exception.expectMessage(BRANCH_EXCEPTION_MESSAGE);
        exception.expect(RuntimeException.class);

        parallelLoopSteps.joinBranches(
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
        ReturnValues returnValues1 = new ReturnValues(new HashMap<String, Value>(), ScoreLangConstants.SUCCESS_RESULT);
        ReturnValues returnValues2 = new ReturnValues(new HashMap<String, Value>(), ScoreLangConstants.SUCCESS_RESULT);
        ReturnValues returnValues3 = new ReturnValues(new HashMap<String, Value>(), ScoreLangConstants.SUCCESS_RESULT);

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
        final ExecutionRuntimeServices executionRuntimeServices = mock(ExecutionRuntimeServices.class);

        final Map<String, Serializable> branchContext1 = new HashMap<>();
        final Map<String, Serializable> branchContext2 = new HashMap<>();
        final Map<String, Serializable> branchContext3 = new HashMap<>();
        RunEnvironment branchRuntimeEnvironment1 = new RunEnvironment();
        RunEnvironment branchRuntimeEnvironment2 = new RunEnvironment();
        RunEnvironment branchRuntimeEnvironment3 = new RunEnvironment();
        branchRuntimeEnvironment1.getExecutionPath().down();
        branchRuntimeEnvironment2.getExecutionPath().down();
        branchRuntimeEnvironment3.getExecutionPath().down();
        branchRuntimeEnvironment1.getStack().pushContext(createContext(runtimeContext1));
        branchRuntimeEnvironment2.getStack().pushContext(createContext(runtimeContext2));
        branchRuntimeEnvironment3.getStack().pushContext(createContext(runtimeContext3));
        branchRuntimeEnvironment1.putReturnValues(returnValues1);
        branchRuntimeEnvironment2.putReturnValues(returnValues2);
        branchRuntimeEnvironment3.putReturnValues(returnValues3);
        branchContext1.put(ScoreLangConstants.RUN_ENV, branchRuntimeEnvironment1);
        branchContext2.put(ScoreLangConstants.RUN_ENV, branchRuntimeEnvironment2);
        branchContext3.put(ScoreLangConstants.RUN_ENV, branchRuntimeEnvironment3);

        List<EndBranchDataContainer> branchesContainers = newArrayList(
                new EndBranchDataContainer(branchContext1, new HashMap<String, Serializable>(), null),
                new EndBranchDataContainer(branchContext2, new HashMap<String, Serializable>(), null),
                new EndBranchDataContainer(branchContext3, new HashMap<String, Serializable>(), null)
        );
        when(executionRuntimeServices.getFinishedChildBranchesData()).thenReturn(branchesContainers);
        return executionRuntimeServices;
    }

    private Context createContext(Map<String, Serializable> runtimeContext) {
        Map<String, Value> context = new HashMap<>(runtimeContext.size());
        for (Map.Entry<String, Serializable> entry : runtimeContext.entrySet()) {
            context.put(entry.getKey(), ValueFactory.create(entry.getValue()));
        }
        return new Context(context);
    }

    private ExecutionRuntimeServices createExecutionRuntimeServicesMockWithBranchException() {
        ExecutionRuntimeServices executionRuntimeServices = mock(ExecutionRuntimeServices.class);
        List<EndBranchDataContainer> branchesContainers = newArrayList(
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
        public ParallelLoopBinding parallelLoopBinding() {
            return mock(ParallelLoopBinding.class);
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
        public ParallelLoopExecutionData parallelLoopSteps() {
            return new ParallelLoopExecutionData();
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
        public DummyEncryptor dummyEncryptor() {
            return new DummyEncryptor();
        }

        @Bean
        public EventBus eventBus() {
            return new EventBusImpl();
        }
    }
}
