/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package io.cloudslang.lang.tools.build.tester;

import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.tools.build.SlangBuildMain;
import io.cloudslang.lang.tools.build.SlangBuildMain.BulkRunMode;
import io.cloudslang.lang.tools.build.SlangBuildMain.TestCaseRunMode;
import io.cloudslang.lang.tools.build.tester.parallel.MultiTriggerTestCaseEventListener;
import io.cloudslang.lang.tools.build.tester.parallel.report.LoggingSlangTestCaseEventListener;
import io.cloudslang.lang.tools.build.tester.parallel.report.ThreadSafeRunTestResults;
import io.cloudslang.lang.tools.build.tester.parallel.services.ParallelTestCaseExecutorService;
import io.cloudslang.lang.tools.build.tester.parallel.services.TestCaseEventDispatchService;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.FailedSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import io.cloudslang.lang.tools.build.tester.parse.TestCasesYamlParser;
import io.cloudslang.lang.tools.build.tester.runconfiguration.TestSuiteRunInfoService;
import io.cloudslang.lang.tools.build.tester.runconfiguration.strategy.RunMultipleTestSuiteConflictResolutionStrategy;
import io.cloudslang.lang.tools.build.tester.runconfiguration.strategy.SequentialRunTestSuiteResolutionStrategy;
import io.cloudslang.score.events.EventConstants;
import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.Long.parseLong;
import static java.lang.String.valueOf;
import static java.lang.System.getProperty;
import static java.util.concurrent.TimeUnit.MINUTES;


@Component
public class SlangTestRunner {

    private final static String PROJECT_PATH_TOKEN = "${project_path}";
    public static final long MAX_TIME_PER_TESTCASE_IN_MINUTES = 10;
    public static final String TEST_CASE_TIMEOUT_IN_MINUTES_KEY = "test.case.timeout.in.minutes";
    public static final String PREFIX_BULLET = "    \u2022 ";

    @Autowired
    private TestCasesYamlParser parser;

    @Autowired
    private Slang slang;

    @Autowired
    private ParallelTestCaseExecutorService parallelTestCaseExecutorService;

    @Autowired
    private TestCaseEventDispatchService testCaseEventDispatchService;

    @Autowired
    private TestSuiteRunInfoService testSuiteRunInfoService;

    private String[] TEST_CASE_FILE_EXTENSIONS = {"yaml", "yml"};
    private static final String TEST_CASE_PASSED = "Test case passed: ";
    private static final String TEST_CASE_FAILED = "Test case failed: ";

    private static final Logger log = Logger.getLogger(SlangTestRunner.class);
    private static final String UNAVAILABLE_NAME = "N/A";

    public enum TestCaseRunState {
        PARALLEL,
        SEQUENTIAL,
        INACTIVE
    }

    public Map<String, SlangTestCase> createTestCases(String testPath, Set<String> allAvailableExecutables) {
        Validate.notEmpty(testPath, "You must specify a path for tests");
        File testPathDir = new File(testPath);
        Validate.isTrue(testPathDir.isDirectory(),
                "Directory path argument \'" + testPath + "\' does not lead to a directory");
        Collection<File> testCasesFiles = FileUtils.listFiles(testPathDir, TEST_CASE_FILE_EXTENSIONS, true);

        log.info("");
        log.info("--- parsing test cases ---");
        log.info("Start parsing all test cases files under: " + testPath);
        log.info(testCasesFiles.size() + " test cases files were found");

        Map<String, SlangTestCase> testCases = new HashMap<>();
        Set<SlangTestCase> testCasesWithMissingReference = new HashSet<>();
        for (File testCaseFile : testCasesFiles) {
            Validate.isTrue(testCaseFile.isFile(),
                    "file path \'" + testCaseFile.getAbsolutePath() + "\' must lead to a file");

            Map<String, SlangTestCase> testCasesFromCurrentFile = parser.parseTestCases(SlangSource.fromFile(testCaseFile));
            for (Map.Entry<String, SlangTestCase> currentTestCaseEntry : testCasesFromCurrentFile.entrySet()) {
                SlangTestCase currentTestCase = currentTestCaseEntry.getValue();
                String currentTestCaseName = currentTestCaseEntry.getKey();
                String testFlowPath = currentTestCase.getTestFlowPath();
                currentTestCase.setName(currentTestCaseName);
                if (StringUtils.isBlank(currentTestCase.getResult())) {
                    currentTestCase.setResult(getResultFromFileName(testFlowPath));
                }
                if (currentTestCase.getThrowsException() == null) {
                    currentTestCase.setThrowsException(false);
                }
                // Make sure the new test cases names are unique
                if (testCases.containsKey(currentTestCaseName)) {
                    throw new RuntimeException("Test case with the name: " + currentTestCaseName + " already exists. Test case name should be unique across the project"
                    );
                }
                if (!allAvailableExecutables.contains(testFlowPath)) {
                    testCasesWithMissingReference.add(currentTestCase);
                }
                testCases.put(currentTestCaseName, currentTestCase);
            }
        }
        printTestCasesWithMissingReference(testCasesWithMissingReference);
        return testCases;
    }

    /**
     *
     * @param projectPath
     * @param testCases
     * @param compiledFlows
     * @param runTestsResults is updated to reflect skipped, failed passes test cases.
     */
    public void runTestsSequential(String projectPath, Map<String, SlangTestCase> testCases,
                                   Map<String, CompilationArtifact> compiledFlows, IRunTestResults runTestsResults) {

        if (MapUtils.isEmpty(testCases)) {
            return;
        }
        printTestForActualRunSummary(TestCaseRunMode.SEQUENTIAL, testCases);

        for (Map.Entry<String, SlangTestCase> testCaseEntry : testCases.entrySet()) {
            SlangTestCase testCase = testCaseEntry.getValue();

            log.info("Running test: " + testCaseEntry.getKey() + " - " + testCase.getDescription());
            try {
                CompilationArtifact compiledTestFlow = getCompiledTestFlow(compiledFlows, testCase);
                runTest(testCase, compiledTestFlow, projectPath);
                runTestsResults.addPassedTest(testCase.getName(), new TestRun(testCase, null));
            } catch (RuntimeException e) {
                runTestsResults.addFailedTest(testCase.getName(), new TestRun(testCase, e.getMessage()));
            }
        }
    }

    public void runTestsParallel(String projectPath, Map<String, SlangTestCase> testCases,
                                 Map<String, CompilationArtifact> compiledFlows, ThreadSafeRunTestResults runTestsResults) {
        if (MapUtils.isEmpty(testCases)) {
            return;
        }
        printTestForActualRunSummary(TestCaseRunMode.PARALLEL, testCases);

        testCaseEventDispatchService.unregisterAllListeners();
        testCaseEventDispatchService.registerListener(runTestsResults); // for gathering of report data
        testCaseEventDispatchService.registerListener(new LoggingSlangTestCaseEventListener()); // for logging purpose

        MultiTriggerTestCaseEventListener multiTriggerTestCaseEventListener = new MultiTriggerTestCaseEventListener();
        slang.subscribeOnEvents(multiTriggerTestCaseEventListener, createListenerEventTypesSet());
        try {
            Map<SlangTestCase, Future<?>> testCaseFutures = new LinkedHashMap<>();
            for (Map.Entry<String, SlangTestCase> testCaseEntry : testCases.entrySet()) {
                SlangTestCase testCase = testCaseEntry.getValue();
                SlangTestCaseRunnable slangTestCaseRunnable = new SlangTestCaseRunnable(testCase, compiledFlows, projectPath, this, testCaseEventDispatchService, multiTriggerTestCaseEventListener);
                testCaseFutures.put(testCase, parallelTestCaseExecutorService.submitTestCase(slangTestCaseRunnable));
            }

            final long testCaseTimeoutMinutes = getTestCaseTimeoutInMinutes();
            for (Map.Entry<SlangTestCase, Future<?>> slangTestCaseFutureEntry : testCaseFutures.entrySet()) {
                SlangTestCase testCase = slangTestCaseFutureEntry.getKey();
                Future<?> testCaseFuture = slangTestCaseFutureEntry.getValue();
                try {
                    testCaseFuture.get(testCaseTimeoutMinutes, MINUTES);
                } catch (InterruptedException e) {
                    log.error("Interrupted while waiting for result: ", e);
                } catch (TimeoutException e) {
                    testCaseEventDispatchService.notifyListeners(new FailedSlangTestCaseEvent(testCase, "Timeout reached for test case " + testCase.getName(), e));
                } catch (Exception e) {
                    testCaseEventDispatchService.notifyListeners(new FailedSlangTestCaseEvent(testCase, e.getMessage(), e));
                }
            }
        } finally {
            testCaseEventDispatchService.unregisterAllListeners();
            slang.unSubscribeOnEvents(multiTriggerTestCaseEventListener);
        }
    }

    private void printTestForActualRunSummary(TestCaseRunMode runMode, Map<String, SlangTestCase> testCases) {
        if (!MapUtils.isEmpty(testCases)) {
            log.info("Running " + testCases.size() + " test(s) in " + runMode.toString().toLowerCase() + ": ");
            for (Map.Entry<String, SlangTestCase> stringSlangTestCaseEntry : testCases.entrySet()) {
                final SlangTestCase slangTestCase = stringSlangTestCaseEntry.getValue();
                log.info(PREFIX_BULLET + slangTestCase.getName());
            }
        }
    }

    /**
     * Processes skipped tests and also handles null testcase failures
     * @param testCases
     * @param testSuites active test suites
     * @param runTestsResults is updated to reflect skipped and fail fast scenarios
     * @return
     */
    public Map<TestCaseRunState, Map<String, SlangTestCase>> splitTestCasesByRunState(final BulkRunMode bulkRunMode, final Map<String, SlangTestCase> testCases, final List<String> testSuites,
                                                                                      final IRunTestResults runTestsResults) {
        Map<TestCaseRunState, Map<String, SlangTestCase>> resultMap = new HashMap<>();

        // Prepare the 3 categories inactive, parallel, sequential
        for (TestCaseRunState testCaseRunState : TestCaseRunState.values()) {
            resultMap.put(testCaseRunState, new LinkedHashMap<String, SlangTestCase>());
        }

        for (Map.Entry<String, SlangTestCase> testCaseEntry : testCases.entrySet()) {
            final SlangTestCase testCase = testCaseEntry.getValue();
            if (testCase == null) {
                processQuickFailTest(runTestsResults);
                continue;
            }

            if (isTestCaseInActiveSuite(testCase, testSuites)) {
                processActiveTest(bulkRunMode, resultMap, testCaseEntry, testCase);
            } else {
                processSkippedTest(runTestsResults, testCaseEntry, testCase, resultMap);
            }
        }

        return resultMap;
    }

    private void processQuickFailTest(final IRunTestResults runTestsResults) {
        runTestsResults.addFailedTest(UNAVAILABLE_NAME, new TestRun(null, "Test case cannot be null"));
    }

    private void processActiveTest(final BulkRunMode bulkRunMode, final Map<TestCaseRunState, Map<String, SlangTestCase>> resultMap, Map.Entry<String, SlangTestCase> testCaseEntry, SlangTestCase testCase) {
        if (bulkRunMode == BulkRunMode.POSSIBLY_MIXED) {
            TestCaseRunMode runModeForTestCase = testSuiteRunInfoService.getRunModeForTestCase(testCase, new RunMultipleTestSuiteConflictResolutionStrategy(),
                    new SequentialRunTestSuiteResolutionStrategy());
            if (runModeForTestCase == TestCaseRunMode.SEQUENTIAL) {
                resultMap.get(TestCaseRunState.SEQUENTIAL).put(testCaseEntry.getKey(), testCase);
            } else if (runModeForTestCase == TestCaseRunMode.PARALLEL) {
                resultMap.get(TestCaseRunState.PARALLEL).put(testCaseEntry.getKey(), testCase);
            }
        } else if (bulkRunMode == BulkRunMode.ALL_SEQUENTIAL) {
            resultMap.get(TestCaseRunState.SEQUENTIAL).put(testCaseEntry.getKey(), testCase);
        } else if (bulkRunMode == BulkRunMode.ALL_PARALLEL) {
            resultMap.get(TestCaseRunState.PARALLEL).put(testCaseEntry.getKey(), testCase);
        }
    }

    private void processSkippedTest(final IRunTestResults runTestsResults, Map.Entry<String, SlangTestCase> testCaseEntry, SlangTestCase testCase,
                                    final Map<TestCaseRunState, Map<String, SlangTestCase>> resultMap) {
        String message = "Skipping test: " + testCaseEntry.getKey() + " because it is not in active test suites";
        log.info(message);

        runTestsResults.addSkippedTest(testCase.getName(), new TestRun(testCase, message));
        resultMap.get(TestCaseRunState.INACTIVE).put(testCaseEntry.getKey(), testCaseEntry.getValue());
    }

    private long getTestCaseTimeoutInMinutes() {
        try {
            return parseLong(getProperty(TEST_CASE_TIMEOUT_IN_MINUTES_KEY, valueOf(MAX_TIME_PER_TESTCASE_IN_MINUTES)));
        } catch (NumberFormatException nfEx) {
            log.warn(String.format("Misconfigured test case timeout '%s'. Using default timeout %d.", getProperty(TEST_CASE_TIMEOUT_IN_MINUTES_KEY), MAX_TIME_PER_TESTCASE_IN_MINUTES));
            return MAX_TIME_PER_TESTCASE_IN_MINUTES;
        }
    }

    boolean isTestCaseInActiveSuite(SlangTestCase testCase, List<String> testSuites) {
        return (CollectionUtils.isEmpty(testCase.getTestSuites()) && testSuites.contains(SlangBuildMain.DEFAULT_TESTS)) ||
                CollectionUtils.containsAny(testSuites, testCase.getTestSuites());
    }

    private void printTestCasesWithMissingReference(Set<SlangTestCase> testCasesWithMissingReference) {
        int testCasesWithMissingReferenceSize = testCasesWithMissingReference.size();
        if (testCasesWithMissingReferenceSize > 0) {
            log.info("");
            log.info(testCasesWithMissingReferenceSize + " test cases have missing test flow references:");
            for (SlangTestCase slangTestCase : testCasesWithMissingReference) {
                log.info(
                        "For test case: " + slangTestCase.getName() +
                                " testFlowPath reference not found: " +
                                slangTestCase.getTestFlowPath()
                );
            }
        }
    }

    public CompilationArtifact getCompiledTestFlow(Map<String, CompilationArtifact> compiledFlows, SlangTestCase testCase) {
        String testFlowPath = testCase.getTestFlowPath();
        if (StringUtils.isEmpty(testFlowPath)) {
            throw new RuntimeException("For test case: " + testCase.getName() + " testFlowPath property is mandatory");
        }
        String testFlowPathTransformed = testFlowPath.replace(File.separatorChar, '.');
        CompilationArtifact compiledTestFlow = compiledFlows.get(testFlowPathTransformed);
        if (compiledTestFlow == null) {
            throw new RuntimeException("Test flow: " + testFlowPath + " is missing. Referenced in test case: " + testCase.getName());
        }
        return compiledTestFlow;
    }

    public void runTest(SlangTestCase testCase, CompilationArtifact compiledTestFlow, String projectPath) {

        Map<String, Value> convertedInputs = getTestCaseInputsMap(testCase);
        Set<SystemProperty> systemProperties = getTestSystemProperties(testCase, projectPath);

        runTestCaseSequentiallyToCompletion(testCase, compiledTestFlow, convertedInputs, systemProperties);
    }

    public void runTestCaseParallel(SlangTestCase testCase, CompilationArtifact compiledTestFlow, String projectPath, MultiTriggerTestCaseEventListener multiTriggerTestCaseEventListener) {

        Map<String, Value> convertedInputs = getTestCaseInputsMap(testCase);
        Set<SystemProperty> systemProperties = getTestSystemProperties(testCase, projectPath);

        runTestCaseInParallelToCompletion(testCase, compiledTestFlow, convertedInputs, systemProperties, multiTriggerTestCaseEventListener);
    }

    private Set<SystemProperty> getTestSystemProperties(SlangTestCase testCase, String projectPath) {
        String systemPropertiesFile = testCase.getSystemPropertiesFile();
        if (StringUtils.isEmpty(systemPropertiesFile)) {
            return new HashSet<>();
        }
        systemPropertiesFile = StringUtils.replace(systemPropertiesFile, PROJECT_PATH_TOKEN, projectPath);
        return parser.parseProperties(systemPropertiesFile);
    }

    private Map<String, Value> getTestCaseInputsMap(SlangTestCase testCase) {
        List<Map> inputs = testCase.getInputs();
        Map<String, Serializable> convertedInputs = new HashMap<>();
        convertedInputs = convertMapParams(inputs, convertedInputs);
        return io.cloudslang.lang.entities.utils.MapUtils.convertMapNonSensitiveValues(convertedInputs);
    }

    private Map<String, Serializable> convertMapParams(List<Map> params, Map<String, Serializable> convertedInputs) {
        if (CollectionUtils.isNotEmpty(params)) {
            for (Map param : params) {
                convertedInputs.put(
                        (String) param.keySet().iterator().next(),
                        (Serializable) param.values().iterator().next());
            }
        }
        return convertedInputs;
    }

    private Map<String, Serializable> getTestCaseOutputsMap(SlangTestCase testCase) {
        List<Map> outputs = testCase.getOutputs();
        Map<String, Serializable> convertedOutputs = new HashMap<>();
        return convertMapParams(outputs, convertedOutputs);
    }

    /**
     * This method will trigger the flow in a synchronize matter, meaning only one flow can run at a time.
     *
     * @param compilationArtifact the artifact to trigger
     * @param inputs              : flow inputs
     * @return executionId
     */
    public Long runTestCaseSequentiallyToCompletion(SlangTestCase testCase, CompilationArtifact compilationArtifact,
                                                    Map<String, Value> inputs,
                                                    Set<SystemProperty> systemProperties) {

        String testCaseName = testCase.getName();
        String result = testCase.getResult();
        Map<String, Serializable> outputs = getTestCaseOutputsMap(testCase);
        String flowName = testCase.getTestFlowPath();

        TriggerTestCaseEventListener testsEventListener = new TriggerTestCaseEventListener();
        slang.subscribeOnEvents(testsEventListener, createListenerEventTypesSet());

        Long executionId = slang.run(compilationArtifact, inputs, systemProperties);

        while (!testsEventListener.isFlowFinished()) {
            poll();
        }
        slang.unSubscribeOnEvents(testsEventListener);

        String errorMessageFlowExecution = testsEventListener.getErrorMessage();

        String message;
        if (BooleanUtils.isTrue(testCase.getThrowsException())) {
            if (StringUtils.isBlank(errorMessageFlowExecution)) {
                message = TEST_CASE_FAILED + testCaseName + " - " + testCase.getDescription() + "\n\tFlow " +
                        compilationArtifact.getExecutionPlan().getName() + " did not throw an exception as expected";
                log.info(message);
                throw new RuntimeException(message);
            }
            log.info(TEST_CASE_PASSED + testCaseName + ". Finished running: " + flowName + " with exception as expected");
            return executionId;
        }

        if (StringUtils.isNotBlank(errorMessageFlowExecution)) {
            // unexpected exception occurred during flow execution
            message = "Error occurred while running test: " + testCaseName + " - " + testCase.getDescription() + "\n\t" + errorMessageFlowExecution;
            log.info(message);
            throw new RuntimeException(message);
        }

        String executionResult = testsEventListener.getResult();
        if (result != null && !result.equals(executionResult)) {
            message = TEST_CASE_FAILED + testCaseName + " - " + testCase.getDescription() + "\n\tExpected result: " + result + "\n\tActual result: " + executionResult;
            log.error(message);
            throw new RuntimeException(message);
        }

        Map<String, Serializable> executionOutputs = testsEventListener.getOutputs();
        handleTestCaseFailuresFromOutputs(testCase, testCaseName, outputs, executionOutputs);

        log.info(TEST_CASE_PASSED + testCaseName + ". Finished running: " + flowName + " with result: " + executionResult);
        return executionId;
    }

    public Long runTestCaseInParallelToCompletion(SlangTestCase testCase, CompilationArtifact compilationArtifact,
                                                  Map<String, Value> inputs,
                                                  Set<SystemProperty> systemProperties, MultiTriggerTestCaseEventListener globalListener) {

        String testCaseName = testCase.getName();
        String result = testCase.getResult();
        Map<String, Serializable> outputs = getTestCaseOutputsMap(testCase);
        String flowName = testCase.getTestFlowPath();

        Long executionId = slang.run(compilationArtifact, inputs, systemProperties);

        while (!globalListener.isFlowFinishedByExecutionId(executionId)) {
            poll();
        }

        String errorMessageFlowExecution = globalListener.getErrorMessageByExecutionId(executionId);

        String message;
        if (BooleanUtils.isTrue(testCase.getThrowsException())) {
            if (StringUtils.isBlank(errorMessageFlowExecution)) {

                message = TEST_CASE_FAILED + testCaseName + " - " + testCase.getDescription() + "\n\tFlow " +
                        compilationArtifact.getExecutionPlan().getName() + " did not throw an exception as expected";
                log.info(message);
                throw new RuntimeException(message);
            }
            log.info(TEST_CASE_PASSED + testCaseName + ". Finished running: " + flowName + " with exception as expected");
            return executionId;
        }

        if (StringUtils.isNotBlank(errorMessageFlowExecution)) {
            // unexpected exception occurred during flow execution
            message = "Error occurred while running test: " + testCaseName + " - " + testCase.getDescription() + "\n\t" + errorMessageFlowExecution;
            log.info(message);
            throw new RuntimeException(message);
        }

        String executionResult = globalListener.getResultByExecutionId(executionId);
        if (result != null && !result.equals(executionResult)) {
            message = TEST_CASE_FAILED + testCaseName + " - " + testCase.getDescription() + "\n\tExpected result: " + result + "\n\tActual result: " + executionResult;
            log.error(message);
            throw new RuntimeException(message);
        }

        Map<String, Serializable> executionOutputs = globalListener.getOutputsByExecutionId(executionId);
        handleTestCaseFailuresFromOutputs(testCase, testCaseName, outputs, executionOutputs);

        log.info(TEST_CASE_PASSED + testCaseName + ". Finished running: " + flowName + " with result: " + executionResult);
        return executionId;
    }

    private void poll() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException ignore) {
        }
    }

    private void handleTestCaseFailuresFromOutputs(SlangTestCase testCase, String testCaseName, Map<String, Serializable> outputs, Map<String, Serializable> executionOutputs) {
        String message;
        if (MapUtils.isNotEmpty(outputs)) {
            for (Map.Entry<String, Serializable> output : outputs.entrySet()) {
                String outputName = output.getKey();
                Serializable outputValue = output.getValue();
                Serializable executionOutputValue = executionOutputs.get(outputName);
                if (!executionOutputs.containsKey(outputName) ||
                        !outputsAreEqual(outputValue, executionOutputValue)) {
                    message = TEST_CASE_FAILED + testCaseName + " - " + testCase.getDescription() + "\n\tFor output: " + outputName + "\n\tExpected value: " + outputValue + "\n\tActual value: " + executionOutputValue;
                    log.error(message);
                    throw new RuntimeException(message);
                }
            }
        }
    }

    private boolean outputsAreEqual(Serializable outputValue, Serializable executionOutputValue) {
        return executionOutputValue == outputValue ||
                StringUtils.equals(executionOutputValue.toString(), outputValue.toString());
    }

    private Set<String> createListenerEventTypesSet() {
        Set<String> handlerTypes = new HashSet<>();
        handlerTypes.add(ScoreLangConstants.EVENT_EXECUTION_FINISHED);
        handlerTypes.add(ScoreLangConstants.SLANG_EXECUTION_EXCEPTION);
        handlerTypes.add(ScoreLangConstants.EVENT_OUTPUT_END);
        handlerTypes.add(EventConstants.SCORE_ERROR_EVENT);
        handlerTypes.add(EventConstants.SCORE_FAILURE_EVENT);
        handlerTypes.add(EventConstants.SCORE_FINISHED_EVENT);
        return handlerTypes;
    }

    private String getResultFromFileName(String fileName) {

        int dashPosition = fileName.lastIndexOf('-');
        if (dashPosition > 0 && dashPosition < fileName.length()) {
            return fileName.substring(dashPosition + 1);
        } else {
            return null;
        }
    }
}
