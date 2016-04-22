package io.cloudslang.lang.tools.build.tester;

import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import io.cloudslang.lang.tools.build.tester.parse.TestCasesYamlParser;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.events.ScoreEventListener;
import java.util.Set;
import junit.framework.Assert;
import org.apache.commons.collections4.SetUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yaml.snakeyaml.Yaml;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by stoneo on 4/15/2015.
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangTestRunnerTest.Config.class)
public class SlangTestRunnerTest {


    @Autowired
    private SlangTestRunner slangTestRunner;

    @Autowired
    private TestCasesYamlParser parser;

    @Autowired
    private Slang slang;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private List<String> specialTestSuite = Arrays.asList("special");
    private List<String> specialRuntimeTestSuite = Arrays.asList("special","default");
    private List<String> defaultTestSuite = Arrays.asList("default");
    private Set<String> allAvailableExecutables = SetUtils.emptySet();


    @Before
    public void resetMocks() {
        Mockito.reset(parser);
        Mockito.reset(slang);
    }

    @Test
    public void createTestCaseWithNullTestPath() {
        exception.expect(NullPointerException.class);
        exception.expectMessage("path");
        slangTestRunner.createTestCases(null, allAvailableExecutables);
    }

    @Test
    public void createTestCaseWithEmptyTestPath(){
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
        Map<String, SlangTestCase> testCases = slangTestRunner.createTestCases(resource.getPath(), allAvailableExecutables);
        Assert.assertEquals("No test cases were supposed to be created", 0, testCases.size());
    }

    @Test
    public void createTestCaseWithEmptyName() throws Exception {
        URI resource = getClass().getResource("/test/valid").toURI();
        Map<String, SlangTestCase> testCases = new HashMap<>();
        testCases.put("", new SlangTestCase("", "path", "desc", null, null, null, null, null, null));
        when(parser.parseTestCases(Mockito.any(SlangSource.class))).thenReturn(testCases);
        Map<String, SlangTestCase> foundTestCases = slangTestRunner.createTestCases(resource.getPath(), allAvailableExecutables);
        Assert.assertEquals("1 test case was supposed to be created", 1, foundTestCases.size());
    }

    @Test
    public void createTestCaseWithPathWithValidTests() throws Exception {
        URI resource = getClass().getResource("/test/valid").toURI();
        Map<String, SlangTestCase> testCases = new HashMap<>();
        testCases.put("Test1", new SlangTestCase("Test1", "path", "desc", null, null, null, null, null, null));
        when(parser.parseTestCases(Mockito.any(SlangSource.class))).thenReturn(testCases);
        Map<String, SlangTestCase> foundTestCases = slangTestRunner.createTestCases(resource.getPath(), allAvailableExecutables);
        Assert.assertEquals("1 test case was supposed to be created", 1, foundTestCases.size());
    }

    @Test
    public void createTestCaseWithDuplicateName() throws Exception {
        URI resource = getClass().getResource("/test/duplicate").toURI();
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
        Map<String, SlangTestCase> foundTestCases = slangTestRunner.createTestCases(resource.getPath(), allAvailableExecutables);
        Assert.assertEquals("1 test case was supposed to be created", 1, foundTestCases.size());
        SlangTestCase testCase = foundTestCases.values().iterator().next();
        Assert.assertEquals("Test case should get the result value from the file name (FAILURE)", "FAILURE", testCase.getResult());
    }

    @Test
    public void runTestCasesFromEmptyMap(){
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", new HashMap<String, SlangTestCase>(), new HashMap<String, CompilationArtifact>(), null);
        Assert.assertEquals("No test cases should fail", 0, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCasesFromNullMap(){
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", null, new HashMap<String, CompilationArtifact>(), null);
        Assert.assertEquals("No test cases should fail", 0, runTestsResults.getFailedTests()
                                                                           .size());
    }

    @Test
    public void runNullTestCase(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        testCases.put("test1", null);
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", testCases, new HashMap<String, CompilationArtifact>(), null);
        Map<String, TestRun> failedTests = runTestsResults.getFailedTests();
        Assert.assertEquals("1 test case should fail", 1, failedTests.size());
        TestRun failedTest = failedTests.values().iterator().next();
        String errorMessage = failedTest.getMessage();
        Assert.assertTrue(errorMessage.contains("Test case"));
        Assert.assertTrue(errorMessage.contains("null"));
    }

    @Test
    public void runTestCaseWithNoTestFlowPathProperty(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", null, null, null, null, null, null, null, null);
        testCases.put("test1", testCase);
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", testCases, new HashMap<String, CompilationArtifact>(), defaultTestSuite);
        Map<String, TestRun> failedTests = runTestsResults.getFailedTests();
        Assert.assertEquals("1 test case should fail", 1, failedTests.size());
        TestRun failedTest = failedTests.values().iterator().next();
        String errorMessage = failedTest.getMessage();
        Assert.assertTrue(errorMessage.contains("testFlowPath"));
        Assert.assertTrue(errorMessage.contains("mandatory"));
    }

    @Test
    public void runTestCaseWithNoCompiledFlow(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", null, null, null, null, null, null, null);
        testCases.put("test1", testCase);
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", testCases, new HashMap<String, CompilationArtifact>(), defaultTestSuite);
        Map<String, TestRun> failedTests = runTestsResults.getFailedTests();
        Assert.assertEquals("1 test case should fail", 1, failedTests.size());
        TestRun failedTest = failedTests.values().iterator().next();
        String errorMessage = failedTest.getMessage();
        Assert.assertTrue(errorMessage.contains("testFlowPath"));
        Assert.assertTrue(errorMessage.contains("missing"));
    }

    @Test
    public void runTestCase(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", null, null, null, null, null, null, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSuccessResult();
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", testCases, compiledFlows, defaultTestSuite);
        Assert.assertEquals("No test cases should fail", 0, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCaseWithEmptyOutputs(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null, "mock", null, new ArrayList<Map>(), false, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSuccessResult();
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", testCases, compiledFlows,defaultTestSuite);
        Assert.assertEquals("No test cases should fail", 0, runTestsResults.getFailedTests()
                                                                           .size());
    }

    @Test
    public void runTestCaseWithStringOutputs(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        ArrayList<Map> outputs = new ArrayList<>();
        Map output1 = new HashMap();
        output1.put("output1", "value1");
        Map output2 = new HashMap();
        output2.put("output2", "value2");
        outputs.add(output1);
        outputs.add(output2);

        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null, "mock", null, outputs, null, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        Map<String, Serializable> convertedOutputs = new HashMap<>();
        convertedOutputs.put("output1", "value1");
        convertedOutputs.put("output2", "value2");
        prepareMockForEventListenerWithSuccessResultAndOutputs(convertedOutputs);
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", testCases, compiledFlows, defaultTestSuite);
        Assert.assertEquals("No test cases should fail", 0, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCaseWithNullValueOutput(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        ArrayList<Map> outputs = new ArrayList<>();
        Map output1 = new HashMap();
        output1.put("output1", null);
        outputs.add(output1);
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null, "mock", null, outputs, null, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        Map<String, Serializable> convertedOutputs = new HashMap<>();
        convertedOutputs.put("output1", null);
        prepareMockForEventListenerWithSuccessResultAndOutputs(convertedOutputs);
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", testCases, compiledFlows, defaultTestSuite);
        Assert.assertEquals("No test cases should fail", 0, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCaseWithIntOutput(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        ArrayList<Map> outputs = new ArrayList<>();
        Map output1 = new HashMap();
        output1.put("output1", 1);
        outputs.add(output1);
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null, "mock", null, outputs, null, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        Map<String, Serializable> convertedOutputs = new HashMap<>();
        convertedOutputs.put("output1", 1);
        prepareMockForEventListenerWithSuccessResultAndOutputs(convertedOutputs);
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", testCases, compiledFlows, defaultTestSuite);
        Assert.assertEquals("No test cases should fail", 0, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCaseWithWrongIntOutput(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        ArrayList<Map> outputs = new ArrayList<>();
        Map output1 = new HashMap();
        output1.put("output1", 1);
        outputs.add(output1);
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null, "mock", null, outputs, null, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        Map<String, Serializable> convertedOutputs = new HashMap<>();
        convertedOutputs.put("output1", 2);
        prepareMockForEventListenerWithSuccessResultAndOutputs(convertedOutputs);
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", testCases, compiledFlows, defaultTestSuite);
        Assert.assertEquals("1 test cases should fail", 1, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCaseWithBooleanOutput(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        ArrayList<Map> outputs = new ArrayList<>();
        Map output1 = new HashMap();
        output1.put("output1", Boolean.TRUE);
        outputs.add(output1);
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null, "mock", null, outputs, null, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        Map<String, Serializable> convertedOutputs = new HashMap<>();
        convertedOutputs.put("output1", Boolean.TRUE);
        prepareMockForEventListenerWithSuccessResultAndOutputs(convertedOutputs);
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", testCases, compiledFlows, defaultTestSuite);
        Assert.assertEquals("No test cases should fail", 0, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCaseThatExpectsException(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null, "mock", null, null, true, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSlangExceptionEvent();
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", testCases, compiledFlows, defaultTestSuite);
        Assert.assertEquals("No test case should fail", 0, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runFailTestCaseThatExpectsException(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null, "mock", null, null, true, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSuccessResult();
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", testCases, compiledFlows, defaultTestSuite);
        Assert.assertEquals("1 test case should fail", 1, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCaseWithUnexpectedException(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null, "mock", null, null, false, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSlangExceptionEvent();
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", testCases, compiledFlows, defaultTestSuite);
        Assert.assertEquals("1 test case should fail", 1, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCaseWithResult(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null, "mock", null, null, false, "SUCCESS");
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSuccessResult();
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", testCases, compiledFlows, defaultTestSuite);
        Assert.assertEquals("No test case should fail", 0, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runFailTestCaseWithWrongResult(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", null, "mock", null, null, false, "FAILURE");
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSuccessResult();
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", testCases, compiledFlows, defaultTestSuite);
        Assert.assertEquals("1 test case should fail", 1, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCaseWithSystemProperties(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", null, null, "mock", null, null, null, null);
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSuccessResult();
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", testCases, compiledFlows, defaultTestSuite);
        Assert.assertEquals("No test cases should fail", 0, runTestsResults.getFailedTests().size());
    }

    @Test
    public void runTestCaseWithSpecialTestSuiteSuccess(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", specialTestSuite, "mock", null, null, false, "SUCCESS");
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSuccessResult();
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", testCases, compiledFlows, specialRuntimeTestSuite);
        Assert.assertEquals("No test cases should be skipped", 0, runTestsResults.getSkippedTests().size());
    }

    @Test
    public void runTestCaseWithSpecialTestSuiteWhenSeveralTestCasesAreGiven(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", specialTestSuite, "mock", null, null, false, "SUCCESS");
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSuccessResult();
        List<String> runtimeTestSuites = new ArrayList<>(specialRuntimeTestSuite);
        runtimeTestSuites.add("anotherTestSuite");
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", testCases, compiledFlows, runtimeTestSuites);
        Assert.assertEquals("No test cases should be skipped", 0, runTestsResults.getSkippedTests().size());
    }

    @Test
    public void runTestCaseWithSeveralTestSuitesWithIntersection(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", Arrays.asList("special", "new"), "mock", null, null, false, "SUCCESS");
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSuccessResult();
        List<String> runtimeTestSuites = new ArrayList<>(specialRuntimeTestSuite);
        runtimeTestSuites.add("anotherTestSuite");
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", testCases, compiledFlows, runtimeTestSuites);
        Assert.assertEquals("No test cases should be skipped", 0, runTestsResults.getSkippedTests().size());
    }

    @Test
    public void runTestCaseWithSeveralTestSuitesWithNoIntersection(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", Arrays.asList("new", "newer"), "mock", null, null, false, "SUCCESS");
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSuccessResult();
        List<String> runtimeTestSuites = new ArrayList<>(specialRuntimeTestSuite);
        runtimeTestSuites.add("anotherTestSuite");
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", testCases, compiledFlows, runtimeTestSuites);
        Assert.assertEquals("1 test case should be skipped", 1, runTestsResults.getSkippedTests().size());
    }

    @Test
    public void runTestCaseWithUnsupportedSpecialTestSuite(){
        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCase = new SlangTestCase("test1", "testFlowPath", "desc", specialTestSuite, "mock", null, null, false, "SUCCESS");
        testCases.put("test1", testCase);
        HashMap<String, CompilationArtifact> compiledFlows = new HashMap<>();
        compiledFlows.put("testFlowPath", new CompilationArtifact(new ExecutionPlan(), null, null, null));
        prepareMockForEventListenerWithSuccessResult();
        RunTestsResults runTestsResults = slangTestRunner.runAllTests("path", testCases, compiledFlows, defaultTestSuite);
        Assert.assertEquals("1 test case should be skipped", 1, runTestsResults.getSkippedTests().size());
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
        public TestCasesYamlParser parser(){
            return mock(TestCasesYamlParser.class);
        }

        @Bean
        public Yaml yaml(){
            return mock(Yaml.class);
        }

        @Bean
        public Slang slang(){
            return mock(Slang.class);
        }
    }}
