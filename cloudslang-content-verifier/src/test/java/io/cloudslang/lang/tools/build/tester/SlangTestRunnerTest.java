/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.tester;

import com.google.common.collect.Lists;
import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.commons.services.api.SlangSourceService;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.DependenciesHelper;
import io.cloudslang.lang.compiler.modeller.ExecutableBuilder;
import io.cloudslang.lang.compiler.modeller.TransformersHandler;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.transformers.PublishTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.ResultsTransformer;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.compiler.validator.ExecutableValidatorImpl;
import io.cloudslang.lang.compiler.validator.PreCompileValidator;
import io.cloudslang.lang.compiler.validator.PreCompileValidatorImpl;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidator;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidatorImpl;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.lang.tools.build.SlangBuildMain;
import io.cloudslang.lang.logging.LoggingService;
import io.cloudslang.lang.logging.LoggingServiceImpl;
import io.cloudslang.lang.tools.build.tester.SlangTestRunner.TestCaseRunState;
import io.cloudslang.lang.tools.build.tester.parallel.MultiTriggerTestCaseEventListener;
import io.cloudslang.lang.tools.build.tester.parallel.report.LoggingSlangTestCaseEventListener;
import io.cloudslang.lang.tools.build.tester.parallel.report.ThreadSafeRunTestResults;
import io.cloudslang.lang.tools.build.tester.parallel.services.ParallelTestCaseExecutorService;
import io.cloudslang.lang.tools.build.tester.parallel.services.TestCaseEventDispatchService;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.FailedSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.SlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import io.cloudslang.lang.tools.build.tester.parse.TestCasesYamlParser;
import io.cloudslang.lang.tools.build.tester.runconfiguration.BuildModeConfig;
import io.cloudslang.lang.tools.build.tester.runconfiguration.TestRunInfoService;
import io.cloudslang.lang.tools.build.tester.runconfiguration.TestRunInfoServiceImpl;
import io.cloudslang.lang.tools.build.tester.runconfiguration.strategy.ConflictResolutionStrategy;
import io.cloudslang.lang.tools.build.tester.runconfiguration.strategy.DefaultResolutionStrategy;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.events.ScoreEventListener;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.collections4.SetUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yaml.snakeyaml.Yaml;

import static io.cloudslang.lang.tools.build.SlangBuildMain.BulkRunMode.ALL_PARALLEL;
import static io.cloudslang.lang.tools.build.SlangBuildMain.BulkRunMode.ALL_SEQUENTIAL;
import static io.cloudslang.lang.tools.build.SlangBuildMain.BulkRunMode.POSSIBLY_MIXED;
import static io.cloudslang.lang.tools.build.tester.SlangTestRunner.MAX_TIME_PER_TESTCASE_IN_MINUTES;
import static io.cloudslang.lang.tools.build.tester.runconfiguration.BuildModeConfig.createChangedBuildModeConfig;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangTestRunnerTest.Config.class)
public class SlangTestRunnerTest {

    @Autowired
    private SlangTestRunner slangTestRunner;

    @Autowired
    private TestCasesYamlParser parser;

    @Autowired
    private Slang slang;

    @Autowired
    private TestCaseEventDispatchService testCaseEventDispatchService;

    @Autowired
    private ParallelTestCaseExecutorService parallelTestCaseExecutorService;

    @Autowired
    private TestRunInfoService testRunInfoService;

    @Autowired
    private LoggingService loggingService;

    @Autowired
    private LoggingSlangTestCaseEventListener loggingSlangTestCaseEventListener;

    @Autowired
    private DependenciesHelper dependenciesHelper;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private List<String> specialTestSuite = Collections.singletonList("special");
    private List<String> specialRuntimeTestSuite = asList("special", "default");
    private Set<String> allAvailableExecutables = SetUtils.emptySet();

    @Before
    public void resetMocks() {
        reset(parser);
        reset(slang);
    }

    @Test
    public void createTestCaseWithNullTestPath() {
        exception.expect(NullPointerException.class);
        exception.expectMessage("path");
        slangTestRunner.createTestCases(null, allAvailableExecutables);
    }

    @Test
    public void createTestCaseWithEmptyTestPath() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("path");
        slangTestRunner.createTestCases("", allAvailableExecutables);
    }

    @Test
    public void createTestCaseWithInvalidTestPath() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("directory");
        slangTestRunner.createTestCases("aaa", allAvailableExecutables);
    }

    @Test
    public void createTestCaseWithPathWithNoTests() throws Exception {
        URI resource = getClass().getResource("/dependencies").toURI();
        Map<String, SlangTestCase> testCases = slangTestRunner
                .createTestCases(resource.getPath(), allAvailableExecutables);
        assertEquals("No test cases were supposed to be created", 0, testCases.size());
    }

    @Test
    public void createTestCaseWithEmptyName() throws Exception {
        URI resource = getClass().getResource("/test/valid").toURI();
        Map<String, SlangTestCase> testCases = new HashMap<>();
        testCases.put("", new SlangTestCase("", "path", "desc", null, null, null, null, null, null));
        when(parser.parseTestCases(Mockito.any(SlangSource.class))).thenReturn(testCases);
        Map<String, SlangTestCase> foundTestCases = slangTestRunner
                .createTestCases(resource.getPath(), allAvailableExecutables);
        assertEquals("1 test case was supposed to be created", 1, foundTestCases.size());
    }

    @Test
    public void createTestCaseWithPathWithValidTests() throws Exception {
        URI resource = getClass().getResource("/test/valid").toURI();
        Map<String, SlangTestCase> testCases = new HashMap<>();
        testCases.put("Test1", new SlangTestCase("Test1", "path", "desc", null, null, null, null, null, null));
        when(parser.parseTestCases(Mockito.any(SlangSource.class))).thenReturn(testCases);
        Map<String, SlangTestCase> foundTestCases = slangTestRunner
                .createTestCases(resource.getPath(), allAvailableExecutables);
        assertEquals("1 test case was supposed to be created", 1, foundTestCases.size());
    }

    @Test
    public void createTestCaseWithDuplicateName() throws Exception {
        final URI resource = getClass().getResource("/test/duplicate").toURI();
        Map<String, SlangTestCase> testCases = new HashMap<>();
        testCases.put("Test1", new SlangTestCase("Test1", "path", "desc", null, null, null, null, null, null));
        testCases.put("Test2", new SlangTestCase("Test1", "path2", "desc2", null, null, null, null, null, null));
        when(parser.parseTestCases(Mockito.any(SlangSource.class))).thenReturn(testCases);
        exception.expect(RuntimeException.class);
        exception.expectMessage("name");
        exception.expectMessage("Test1");
        exception.expectMessage("exists");
        slangTestRunner.createTestCases(resource.getPath(), allAvailableExecutables);
    }

    @Test
    public void createTestCaseWithResultFromFileName() throws Exception {
        URI resource = getClass().getResource("/test/valid").toURI();
        Map<String, SlangTestCase> testCases = new HashMap<>();
        testCases.put("Test1", new SlangTestCase("Test1", "path-FAILURE", "desc", null, null, null, null, null, null));
        when(parser.parseTestCases(Mockito.any(SlangSource.class))).thenReturn(testCases);
        Map<String, SlangTestCase> foundTestCases = slangTestRunner
                .createTestCases(resource.getPath(), allAvailableExecutables);
        assertEquals("1 test case was supposed to be created", 1, foundTestCases.size());
        SlangTestCase testCase = foundTestCases.values().iterator().next();
        assertEquals("Test case should get the result value from the file name (FAILURE)",
                "FAILURE", testCase.getResult());
    }

    @Test
    public void runTestCasesFromEmptyMap() {
        final RunTestsResults runTestsResults = new RunTestsResults();
        slangTestRunner
                .runTestsSequential("path", new HashMap<String, SlangTestCase>(),
                        new HashMap<String, CompilationArtifact>(), runTestsResults);
        assertEquals("No test cases should fail", 0, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCasesFromNullMap() {
        final RunTestsResults runTestsResults = new RunTestsResults();
        slangTestRunner.runTestsSequential("path", null, new HashMap<String, CompilationArtifact>(), runTestsResults);
        assertEquals("No test cases should fail", 0, runTestsResults.getFailedTests()
                .size());
    }

    @Test
    public void runTestCaseWithNoTestFlowPathProperty() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", null, null, null, null, null, null, null, null);
        testCases.put("test1", testCase);
        final RunTestsResults runTestsResults = new RunTestsResults();
        slangTestRunner.runTestsSequential("path", testCases,
                new HashMap<String, CompilationArtifact>(), runTestsResults);
        Map<String, TestRun> failedTests = runTestsResults.getFailedTests();
        assertEquals("1 test case should fail", 1, failedTests.size());
        TestRun failedTest = failedTests.values().iterator().next();
        String errorMessage = failedTest.getMessage();
        Assert.assertTrue(errorMessage.contains("testFlowPath"));
        Assert.assertTrue(errorMessage.contains("mandatory"));
    }

    @Test
    public void runTestCaseWithNoCompiledFlow() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", null, null, null, null, null, null, null);
        testCases.put("test1", testCase);
        final RunTestsResults runTestsResults = new RunTestsResults();
        slangTestRunner
                .runTestsSequential("path", testCases, new HashMap<String, CompilationArtifact>(), runTestsResults);
        Map<String, TestRun> failedTests = runTestsResults.getFailedTests();
        assertEquals("1 test case should fail", 1, failedTests.size());
        TestRun failedTest = failedTests.values().iterator().next();
        String errorMessage = failedTest.getMessage();
        Assert.assertTrue(errorMessage.contains("testFlowPath"));
        Assert.assertTrue(errorMessage.contains("missing"));
    }

    @Test
    public void runTestCase() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", null, null, null, null, null, null, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSuccessResult();
        final RunTestsResults runTestsResults = new RunTestsResults();
        slangTestRunner.runTestsSequential("path", testCases, compiledFlows, runTestsResults);
        assertEquals("No test cases should fail", 0, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCaseParallel() throws InterruptedException, ExecutionException, TimeoutException {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase1 = new SlangTestCase("test1", "testFlowPath1", null, null, null, null, null, null, null);
        SlangTestCase testCase2 = new SlangTestCase("test2", "testFlowPath2", null, null, null, null, null, null, null);
        testCases.put("test1", testCase1);
        testCases.put("test2", testCase2);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath1", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        compiledFlows.put("testFlowPath2", new CompilationArtifact(new ExecutionPlan(), null, null, null));

        doNothing().when(testCaseEventDispatchService).unregisterAllListeners();
        doNothing().when(testCaseEventDispatchService).registerListener(any(ISlangTestCaseEventListener.class));

        final SubscribeArgumentsHolder subscribeArgumentsHolder = new SubscribeArgumentsHolder();
        // Get the global event listener that was created
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] arguments = invocationOnMock.getArguments();
                MultiTriggerTestCaseEventListener listener = (MultiTriggerTestCaseEventListener) arguments[0];
                subscribeArgumentsHolder.setMultiTriggerTestCaseEventListener(listener);
                @SuppressWarnings("unchecked")
                Set<String> argument = (Set<String>) arguments[1];
                subscribeArgumentsHolder.setEventTypes(argument);
                return null;
            }
        }).when(slang).subscribeOnEvents(any(ScoreEventListener.class), anySetOf(String.class));

        final Future futureTestCase1 = mock(Future.class);
        final Future futureTestCase2 = mock(Future.class);
        final List<Runnable> runnableList = Lists.newArrayList();
        // Collect the Runnable objects created inside, so that later we can verify them
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                int size = runnableList.size();
                Object[] arguments = invocationOnMock.getArguments();
                runnableList.add((Runnable) arguments[0]);
                return size == 0 ? futureTestCase1 : futureTestCase2;
            }
        }).when(parallelTestCaseExecutorService).submitTestCase(any(Runnable.class));


        doThrow(new TimeoutException("timeout")).when(futureTestCase1).get(anyLong(), any(TimeUnit.class));
        doThrow(new RuntimeException("unknown exception")).when(futureTestCase2).get(anyLong(), any(TimeUnit.class));

        doNothing().when(slang).unSubscribeOnEvents(any(ScoreEventListener.class));
        doNothing().when(testCaseEventDispatchService).notifyListeners(any(SlangTestCaseEvent.class));
        final ThreadSafeRunTestResults runTestsResults = new ThreadSafeRunTestResults();
        slangTestRunner.runTestsParallel("path", testCases, compiledFlows, runTestsResults);

        verify(testCaseEventDispatchService, times(2)).unregisterAllListeners();
        verify(testCaseEventDispatchService).registerListener(isA(ThreadSafeRunTestResults.class));
        verify(testCaseEventDispatchService).registerListener(isA(LoggingSlangTestCaseEventListener.class));

        verify(slang).subscribeOnEvents(eq(subscribeArgumentsHolder.getMultiTriggerTestCaseEventListener()),
                eq(subscribeArgumentsHolder.getEventTypes()));

        InOrder inOrderTestCases = inOrder(parallelTestCaseExecutorService);
        inOrderTestCases.verify(parallelTestCaseExecutorService).submitTestCase(eq(runnableList.get(0)));
        inOrderTestCases.verify(parallelTestCaseExecutorService).submitTestCase(eq(runnableList.get(1)));
        inOrderTestCases.verifyNoMoreInteractions();

        verify(futureTestCase1).get(eq(MAX_TIME_PER_TESTCASE_IN_MINUTES), eq(TimeUnit.MINUTES));
        verify(futureTestCase2).get(eq(MAX_TIME_PER_TESTCASE_IN_MINUTES), eq(TimeUnit.MINUTES));
        verify(testCaseEventDispatchService, times(2)).notifyListeners(isA(FailedSlangTestCaseEvent.class));

        verify(slang).unSubscribeOnEvents(eq(subscribeArgumentsHolder.getMultiTriggerTestCaseEventListener()));
    }

    @Test
    public void runTestCaseWithEmptyOutputs() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null, "mock", null,
                new ArrayList<Map>(), false, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSuccessResult();
        final RunTestsResults runTestsResults = new RunTestsResults();
        slangTestRunner.runTestsSequential("path", testCases, compiledFlows, runTestsResults);
        assertEquals("No test cases should fail", 0, runTestsResults.getFailedTests()
                .size());
    }

    @Test
    public void runTestCaseWithStringOutputs() {
        final Map<String, SlangTestCase> testCases = new HashMap<>();
        ArrayList<Map> outputs = new ArrayList<>();
        Map<String, Serializable> output1 = new HashMap<>();
        output1.put("output1", "value1");
        Map<String, Serializable> output2 = new HashMap<>();
        output2.put("output2", "value2");
        outputs.add(output1);
        outputs.add(output2);

        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null,
                "mock", null, outputs, null, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        Map<String, Serializable> convertedOutputs = new HashMap<>();
        convertedOutputs.put("output1", "value1");
        convertedOutputs.put("output2", "value2");
        prepareMockForEventListenerWithSuccessResultAndOutputs(convertedOutputs);
        final RunTestsResults runTestsResults = new RunTestsResults();
        slangTestRunner.runTestsSequential("path", testCases, compiledFlows, runTestsResults);
        assertEquals("No test cases should fail", 0, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCaseWithNullValueOutput() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        ArrayList<Map> outputs = new ArrayList<>();
        Map<String, Value> output1 = new HashMap<>();
        output1.put("output1", null);
        outputs.add(output1);
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null,
                "mock", null, outputs, null, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        Map<String, Serializable> convertedOutputs = new HashMap<>();
        convertedOutputs.put("output1", null);
        prepareMockForEventListenerWithSuccessResultAndOutputs(convertedOutputs);
        final RunTestsResults runTestsResults = new RunTestsResults();
        slangTestRunner.runTestsSequential("path", testCases, compiledFlows, runTestsResults);
        assertEquals("No test cases should fail", 0, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCaseWithIntOutput() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        ArrayList<Map> outputs = new ArrayList<>();
        Map<String, Serializable> output1 = new HashMap<>();
        output1.put("output1", 1);
        outputs.add(output1);
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null,
                "mock", null, outputs, null, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        Map<String, Serializable> convertedOutputs = new HashMap<>();
        convertedOutputs.put("output1", 1);
        prepareMockForEventListenerWithSuccessResultAndOutputs(convertedOutputs);
        final RunTestsResults runTestsResults = new RunTestsResults();
        slangTestRunner.runTestsSequential("path", testCases, compiledFlows, runTestsResults);
        assertEquals("No test cases should fail", 0, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCaseWithWrongIntOutput() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        ArrayList<Map> outputs = new ArrayList<>();
        Map<String, Serializable> output1 = new HashMap<>();
        output1.put("output1", 1);
        outputs.add(output1);
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null,
                "mock", null, outputs, null, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        Map<String, Serializable> convertedOutputs = new HashMap<>();
        convertedOutputs.put("output1", 2);
        prepareMockForEventListenerWithSuccessResultAndOutputs(convertedOutputs);
        final RunTestsResults runTestsResults = new RunTestsResults();
        slangTestRunner.runTestsSequential("path", testCases, compiledFlows, runTestsResults);
        assertEquals("1 test cases should fail", 1, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCaseWithBooleanOutput() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        ArrayList<Map> outputs = new ArrayList<>();
        Map<String, Serializable> output1 = new HashMap<>();
        output1.put("output1", Boolean.TRUE);
        outputs.add(output1);
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null,
                "mock", null, outputs, null, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        Map<String, Serializable> convertedOutputs = new HashMap<>();
        convertedOutputs.put("output1", Boolean.TRUE);
        prepareMockForEventListenerWithSuccessResultAndOutputs(convertedOutputs);
        final RunTestsResults runTestsResults = new RunTestsResults();
        slangTestRunner.runTestsSequential("path", testCases, compiledFlows, runTestsResults);
        assertEquals("No test cases should fail", 0, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCaseThatExpectsException() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null,
                "mock", null, null, true, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSlangExceptionEvent();
        final RunTestsResults runTestsResults = new RunTestsResults();
        slangTestRunner.runTestsSequential("path", testCases, compiledFlows, runTestsResults);
        assertEquals("No test case should fail", 0, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runFailTestCaseThatExpectsException() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null,
                "mock", null, null, true, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSuccessResult();
        final RunTestsResults runTestsResults = new RunTestsResults();
        slangTestRunner.runTestsSequential("path", testCases, compiledFlows, runTestsResults);
        assertEquals("1 test case should fail", 1, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCaseWithUnexpectedException() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null,
                "mock", null, null, false, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSlangExceptionEvent();
        final RunTestsResults runTestsResults = new RunTestsResults();
        slangTestRunner.runTestsSequential("path", testCases, compiledFlows, runTestsResults);
        assertEquals("1 test case should fail", 1, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCaseWithResult() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null,
                "mock", null, null, false, "SUCCESS");
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSuccessResult();
        final RunTestsResults runTestsResults = new RunTestsResults();
        slangTestRunner.runTestsSequential("path", testCases, compiledFlows, runTestsResults);
        assertEquals("No test case should fail", 0, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runFailTestCaseWithWrongResult() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null,
                "mock", null, null, false, "FAILURE");
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSuccessResult();
        final RunTestsResults runTestsResults = new RunTestsResults();
        slangTestRunner.runTestsSequential("path", testCases, compiledFlows, runTestsResults);
        assertEquals("1 test case should fail", 1, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCaseWithSystemProperties() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", null, null,
                "mock", null, null, null, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSuccessResult();
        final RunTestsResults runTestsResults = new RunTestsResults();
        slangTestRunner.runTestsSequential("path", testCases, compiledFlows, runTestsResults);
        assertEquals("No test cases should fail", 0, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCaseWithSpecialTestSuiteSuccess() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", specialTestSuite,
                "mock", null, null, false, "SUCCESS");
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSuccessResult();
        final RunTestsResults runTestsResults = new RunTestsResults();
        slangTestRunner.runTestsSequential("path", testCases, compiledFlows, runTestsResults);
        assertEquals("No test cases should be skipped", 0, runTestsResults.getSkippedTests().size());
    }

    @Test
    public void runTestCaseWithSpecialTestSuiteWhenSeveralTestCasesAreGiven() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", specialTestSuite,
                "mock", null, null, false, "SUCCESS");
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSuccessResult();
        List<String> runtimeTestSuites = new ArrayList<>(specialRuntimeTestSuite);
        runtimeTestSuites.add("anotherTestSuite");
        final RunTestsResults runTestsResults = new RunTestsResults();
        slangTestRunner.runTestsSequential("path", testCases, compiledFlows, runTestsResults);
        assertEquals("No test cases should be skipped", 0, runTestsResults.getSkippedTests().size());
    }

    @Test
    public void runTestCaseWithSeveralTestSuitesWithIntersection() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", asList("special", "new"),
                "mock", null, null, false, "SUCCESS");
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSuccessResult();
        List<String> runtimeTestSuites = new ArrayList<>(specialRuntimeTestSuite);
        runtimeTestSuites.add("anotherTestSuite");
        final RunTestsResults runTestsResults = new RunTestsResults();
        slangTestRunner.runTestsSequential("path", testCases, compiledFlows, runTestsResults);
        assertEquals("No test cases should be skipped", 0, runTestsResults.getSkippedTests().size());
    }

    @Test
    public void testSplitTestCasesByRunStateAllSequential() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc",
                asList("special", "new"), "mock", null, null, false, "SUCCESS");
        testCases.put("test1", testCase);
        List<String> testSuites = Lists.newArrayList("special");
        IRunTestResults runTestResults = new RunTestsResults();
        BuildModeConfig buildModeConfig = BuildModeConfig.createBasicBuildModeConfig();

        // Tested call
        Map<TestCaseRunState, Map<String, SlangTestCase>> testCaseRunStateMapMap = slangTestRunner
                .splitTestCasesByRunState(ALL_SEQUENTIAL, testCases, testSuites, runTestResults, buildModeConfig);

        assertEquals(3, testCaseRunStateMapMap.size());
        assertEquals(0, testCaseRunStateMapMap.get(TestCaseRunState.INACTIVE).size());
        assertEquals(1, testCaseRunStateMapMap.get(TestCaseRunState.SEQUENTIAL).size());
        assertEquals(0, testCaseRunStateMapMap.get(TestCaseRunState.PARALLEL).size());

        assertEquals(testCase, testCaseRunStateMapMap.get(TestCaseRunState.SEQUENTIAL).values().iterator().next());
    }

    @Test
    public void testSplitTestCasesByRunStateAllParallel() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc",
                asList("abc", "new"), "mock", null, null, false, "SUCCESS");
        testCases.put("test1", testCase);
        List<String> testSuites = Lists.newArrayList("new", "new1");
        IRunTestResults runTestResults = new RunTestsResults();
        BuildModeConfig buildModeConfig = BuildModeConfig.createBasicBuildModeConfig();

        // Tested call
        Map<TestCaseRunState, Map<String, SlangTestCase>> testCaseRunStateMapMap = slangTestRunner
                .splitTestCasesByRunState(ALL_PARALLEL, testCases, testSuites, runTestResults, buildModeConfig);

        assertEquals(3, testCaseRunStateMapMap.size());
        assertEquals(0, testCaseRunStateMapMap.get(TestCaseRunState.INACTIVE).size());
        assertEquals(0, testCaseRunStateMapMap.get(TestCaseRunState.SEQUENTIAL).size());
        assertEquals(1, testCaseRunStateMapMap.get(TestCaseRunState.PARALLEL).size());

        assertEquals(testCase, testCaseRunStateMapMap.get(TestCaseRunState.PARALLEL).values().iterator().next());
    }

    @Test
    public void testSplitTestCasesByRunStateAllSkipped() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase1 = new SlangTestCase("test1", "testFlowPath", "desc",
                asList("abc", "new"), "mock", null, null, false, "SUCCESS");
        SlangTestCase testCase2 = new SlangTestCase("test2", "testFlowPath", "desc",
                asList("efg", "new"), "mock", null, null, false, "SUCCESS");
        testCases.put("test1", testCase1);
        testCases.put("test2", testCase2);
        List<String> testSuites = Lists.newArrayList("ghf");
        IRunTestResults runTestResults = new RunTestsResults();
        BuildModeConfig buildModeConfig = BuildModeConfig.createBasicBuildModeConfig();

        // Tested call
        Map<TestCaseRunState, Map<String, SlangTestCase>> testCaseRunStateMapMap = slangTestRunner
                .splitTestCasesByRunState(ALL_PARALLEL, testCases, testSuites, runTestResults, buildModeConfig);

        assertEquals(3, testCaseRunStateMapMap.size());
        assertEquals(2, testCaseRunStateMapMap.get(TestCaseRunState.INACTIVE).size());
        assertEquals(0, testCaseRunStateMapMap.get(TestCaseRunState.SEQUENTIAL).size());
        assertEquals(0, testCaseRunStateMapMap.get(TestCaseRunState.PARALLEL).size());

        Assert.assertTrue(testCaseRunStateMapMap.get(TestCaseRunState.INACTIVE).values().contains(testCase1));
        Assert.assertTrue(testCaseRunStateMapMap.get(TestCaseRunState.INACTIVE).values().contains(testCase2));
    }

    @Test
    public void testSplitTestCasesByRunStateDependencyHelperIsCalled() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase1 = new SlangTestCase("test1", "testFlowPath1", "desc",
                asList("special", "new"), "mock", null, null, false, "SUCCESS");
        SlangTestCase testCase2 = new SlangTestCase("test2", "testFlowPath2", "desc",
                asList("special", "new"), "mock", null, null, false, "SUCCESS");
        testCases.put("test1", testCase1);
        testCases.put("test2", testCase2);
        final List<String> testSuites = Lists.newArrayList("special");
        final IRunTestResults runTestResults = new RunTestsResults();
        Set<String> changedFiles = new HashSet<>();
        Map<String, Executable> allTestedFlowModels = new HashMap<>();
        Executable executable = mock(Executable.class);
        allTestedFlowModels.put("testFlowPath1", executable);
        allTestedFlowModels.put("testFlowPath2", executable);
        BuildModeConfig buildModeConfig = createChangedBuildModeConfig(changedFiles, allTestedFlowModels);
        when(dependenciesHelper.fetchDependencies(any(Executable.class), anyMapOf(String.class, Executable.class)))
                .thenReturn(new HashSet<String>());

        // Tested call
        slangTestRunner
                .splitTestCasesByRunState(ALL_SEQUENTIAL, testCases, testSuites, runTestResults, buildModeConfig);

        verify(dependenciesHelper, times(2)).fetchDependencies(eq(executable), eq(allTestedFlowModels));
    }

    @Test
    public void testSplitTestCasesByRunStatePossiblyMixed() {
        Map<String, SlangTestCase> testCases = new LinkedHashMap<>();
        SlangTestCase testCase1 = new SlangTestCase("test1", "testFlowPath", "desc",
                asList("abc", "new"), "mock", null, null, false, "SUCCESS");
        SlangTestCase testCase2 = new SlangTestCase("test2", "testFlowPath", "desc",
                asList("efg", "new"), "mock", null, null, false, "SUCCESS");
        SlangTestCase testCase3 = new SlangTestCase("test3", "testFlowPath", "desc",
                asList("new", "new2"), "mock", null, null, false, "SUCCESS");
        SlangTestCase testCase4 = new SlangTestCase("test4", "testFlowPath", "desc",
                asList("jjj", "new2"), "mock", null, null, false, "SUCCESS");
        SlangTestCase testCase5 = new SlangTestCase("test5", "testFlowPath", "desc",
                asList("hhh", "jjj", "abc"), "mock", null, null, false, "SUCCESS");
        testCases.put("test1", testCase1);
        testCases.put("test2", testCase2);
        testCases.put("test3", testCase3);
        testCases.put("test4", testCase4);
        testCases.put("test5", testCase5);
        List<String> testSuites = Lists.newArrayList("abc", "new");
        IRunTestResults runTestResults = new RunTestsResults();
        BuildModeConfig buildModeConfig = BuildModeConfig.createBasicBuildModeConfig();

        // Fourth call is skipped so only three calls are expected to testRunInfoService
        doReturn(SlangBuildMain.TestCaseRunMode.SEQUENTIAL)
                .doReturn(SlangBuildMain.TestCaseRunMode.PARALLEL)
                .doReturn(SlangBuildMain.TestCaseRunMode.PARALLEL)
                .doReturn(SlangBuildMain.TestCaseRunMode.SEQUENTIAL)
                .when(testRunInfoService).getRunModeForTestCase(any(SlangTestCase.class),
                any(ConflictResolutionStrategy.class),
                any(DefaultResolutionStrategy.class));

        // Tested call
        Map<TestCaseRunState, Map<String, SlangTestCase>> testCaseRunStateMapMap = slangTestRunner
                .splitTestCasesByRunState(POSSIBLY_MIXED, testCases, testSuites, runTestResults, buildModeConfig);

        assertEquals(3, testCaseRunStateMapMap.size());
        assertEquals(1, testCaseRunStateMapMap.get(TestCaseRunState.INACTIVE).size());
        assertEquals(2, testCaseRunStateMapMap.get(TestCaseRunState.SEQUENTIAL).size());
        assertEquals(2, testCaseRunStateMapMap.get(TestCaseRunState.PARALLEL).size());

        Assert.assertTrue(testCaseRunStateMapMap.get(TestCaseRunState.INACTIVE).values().contains(testCase4));

        Assert.assertTrue(testCaseRunStateMapMap.get(TestCaseRunState.SEQUENTIAL).values().contains(testCase1));
        Assert.assertTrue(testCaseRunStateMapMap.get(TestCaseRunState.SEQUENTIAL).values().contains(testCase5));

        Assert.assertTrue(testCaseRunStateMapMap.get(TestCaseRunState.PARALLEL).values().contains(testCase2));
        Assert.assertTrue(testCaseRunStateMapMap.get(TestCaseRunState.PARALLEL).values().contains(testCase3));
    }

    private void prepareMockForEventListenerWithSuccessResult() {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                ScoreEventListener listener = (ScoreEventListener) invocationOnMock.getArguments()[0];
                LanguageEventData data = new LanguageEventData();
                data.setResult("SUCCESS");
                listener.onEvent(new ScoreEvent(ScoreLangConstants.EVENT_EXECUTION_FINISHED, data));
                return listener;
            }
        }).when(slang).subscribeOnEvents(any(ScoreEventListener.class), anySetOf(String.class));
    }

    private void prepareMockForEventListenerWithSuccessResultAndOutputs(final Map<String, Serializable> outputs) {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                ScoreEventListener listener = (ScoreEventListener) invocationOnMock.getArguments()[0];
                LanguageEventData data = new LanguageEventData();
                data.setOutputs(outputs);
                data.setPath("0");
                listener.onEvent(new ScoreEvent(ScoreLangConstants.EVENT_OUTPUT_END, data));
                data = new LanguageEventData();
                data.setResult("SUCCESS");
                listener.onEvent(new ScoreEvent(ScoreLangConstants.EVENT_EXECUTION_FINISHED, data));
                return listener;
            }
        }).when(slang).subscribeOnEvents(any(ScoreEventListener.class), anySetOf(String.class));
    }

    private void prepareMockForEventListenerWithSlangExceptionEvent() {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                ScoreEventListener listener = (ScoreEventListener) invocationOnMock.getArguments()[0];
                LanguageEventData data = new LanguageEventData();
                data.setException("Error");
                listener.onEvent(new ScoreEvent(EventConstants.SCORE_ERROR_EVENT, data));
                return listener;
            }
        }).when(slang).subscribeOnEvents(any(ScoreEventListener.class), anySetOf(String.class));
    }


    @Configuration
    static class Config {

        @Bean
        public SlangTestRunner slangTestRunner() {
            return new SlangTestRunner();
        }

        @Bean
        public TestCasesYamlParser parser() {
            return mock(TestCasesYamlParser.class);
        }

        @Bean
        public Yaml yaml() {
            return mock(Yaml.class);
        }

        @Bean
        public Slang slang() {
            return mock(Slang.class);
        }

        @Bean
        public SlangSourceService slangSourceService() {
            return mock(SlangSourceService.class);
        }

        @Bean
        public TestCaseEventDispatchService testCaseEventDispatchService() {
            return mock(TestCaseEventDispatchService.class);
        }

        @Bean
        public ParallelTestCaseExecutorService parallelTestCaseExecutorService() {
            return mock(ParallelTestCaseExecutorService.class);
        }

        @Bean
        public TestRunInfoService testRunInfoServiceImpl() {
            return mock(TestRunInfoServiceImpl.class);
        }

        @Bean
        public LoggingService loggingService() {
            return new LoggingServiceImpl();
        }

        @Bean
        public LoggingSlangTestCaseEventListener loggingSlangTestCaseEventListener() {
            return new LoggingSlangTestCaseEventListener();
        }

        @Bean
        public ExecutableBuilder executableBuilder() {
            return new ExecutableBuilder();
        }

        ////////////////////// Context for DependenciesHelper ////////////////////////////
        @Bean
        public DependenciesHelper dependenciesHelper() {
            return Mockito.mock(DependenciesHelper.class);
        }

        @Bean
        public PublishTransformer publishTransformer() {
            return Mockito.mock(PublishTransformer.class);
        }

        @Bean
        public TransformersHandler transformersHandler() {
            return Mockito.mock(TransformersHandler.class);
        }

        @Bean
        public PreCompileValidator preCompileValidator() {
            return new PreCompileValidatorImpl();
        }

        @Bean
        public ResultsTransformer resultsTransformer() {
            return Mockito.mock(ResultsTransformer.class);
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

    private static class SubscribeArgumentsHolder {

        private MultiTriggerTestCaseEventListener multiTriggerTestCaseEventListener;
        private Set<String> eventTypes;

        public SubscribeArgumentsHolder() {
        }

        public MultiTriggerTestCaseEventListener getMultiTriggerTestCaseEventListener() {
            return multiTriggerTestCaseEventListener;
        }

        public Set<String> getEventTypes() {
            return eventTypes;
        }

        public void setMultiTriggerTestCaseEventListener(MultiTriggerTestCaseEventListener listener) {
            this.multiTriggerTestCaseEventListener = listener;
        }

        public void setEventTypes(Set<String> eventTypes) {
            this.eventTypes = eventTypes;
        }
    }

}
