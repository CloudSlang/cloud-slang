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
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.tools.build.SlangBuildMain;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import io.cloudslang.lang.tools.build.tester.parse.TestCasesYamlParser;
import io.cloudslang.score.events.EventConstants;
import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by stoneo on 3/15/2015.
 */
@Component
public class SlangTestRunner {

    private final static String PROJECT_PATH_TOKEN = "${project_path}";

    @Autowired
    private TestCasesYamlParser parser;

    @Autowired
    private Slang slang;

    private String[] TEST_CASE_FILE_EXTENSIONS = {"yaml", "yml"};
    public static final String TEST_CASE_PASSED = "Test case passed: ";
    public static final String TEST_CASE_FAILED = "Test case failed: ";

    private final static Logger log = Logger.getLogger(SlangTestRunner.class);
    private final String UNAVAILABLE_NAME = "N/A";

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
                //todo: temporary solution
                currentTestCase.setName(currentTestCaseName);
                if(StringUtils.isBlank(currentTestCase.getResult())){
                    currentTestCase.setResult(getResultFromFileName(testFlowPath));
                }
                if(currentTestCase.getThrowsException() == null){
                    currentTestCase.setThrowsException(false);
                }
                // Make sure the new test cases names are unique
                if (testCases.containsKey(currentTestCaseName)){
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
     * @param testSuites
     * @return RunTestsResults containing maps of passed, failed & skipped tests
     */
    public RunTestsResults runAllTests(String projectPath, Map<String, SlangTestCase> testCases,
                            Map<String, CompilationArtifact> compiledFlows, List<String> testSuites) {

        RunTestsResults runTestsResults = new RunTestsResults();
        if(MapUtils.isEmpty(testCases)){
            return runTestsResults;
        }
        for (Map.Entry<String, SlangTestCase> testCaseEntry : testCases.entrySet()) {
            SlangTestCase testCase = testCaseEntry.getValue();
            if (testCase == null) {
                runTestsResults.addFailedTest(UNAVAILABLE_NAME, new TestRun(testCase, "Test case cannot be null"));
                continue;
            }
            if ((CollectionUtils.isEmpty(testCase.getTestSuites()) && testSuites.contains(SlangBuildMain.DEFAULT_TESTS)) ||
                    CollectionUtils.containsAny(testSuites, testCase.getTestSuites())) {
                log.info("Running test: " + testCaseEntry.getKey() + " - " + testCase.getDescription());
                try {
                    CompilationArtifact compiledTestFlow = getCompiledTestFlow(compiledFlows, testCase);
                    runTest(testCase, compiledTestFlow, projectPath);
                    runTestsResults.addPassedTest(testCase.getName(), new TestRun(testCase, null));
                } catch (RuntimeException e) {
                    runTestsResults.addFailedTest(testCase.getName(), new TestRun(testCase, e.getMessage()));
                }
            } else {
                String message = "Skipping test: " + testCaseEntry.getKey() + " because it is not in active test suites";
                log.info(message);
                runTestsResults.addSkippedTest(testCase.getName(), new TestRun(testCase, message));
            }
        }
        return runTestsResults;
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

    private static CompilationArtifact getCompiledTestFlow(Map<String, CompilationArtifact> compiledFlows, SlangTestCase testCase) {
        String testFlowPath = testCase.getTestFlowPath();
        if(StringUtils.isEmpty(testFlowPath)){
            throw new RuntimeException("For test case: " + testCase.getName() + " testFlowPath property is mandatory");
        }
        String testFlowPathTransformed = testFlowPath.replace(File.separatorChar, '.');
        CompilationArtifact compiledTestFlow = compiledFlows.get(testFlowPathTransformed);
        if(compiledTestFlow == null) {
            throw new RuntimeException("Test flow: " + testFlowPath + " is missing. Referenced in test case: " + testCase.getName());
        }
        return compiledTestFlow;
    }

    private void runTest(SlangTestCase testCase, CompilationArtifact compiledTestFlow, String projectPath) {

        Map<String, Serializable> convertedInputs = getTestCaseInputsMap(testCase);
        Set<SystemProperty> systemProperties = getTestSystemProperties(testCase, projectPath);

        trigger(testCase, compiledTestFlow, convertedInputs, systemProperties);
    }

    private Set<SystemProperty> getTestSystemProperties(SlangTestCase testCase, String projectPath) {
        String systemPropertiesFile = testCase.getSystemPropertiesFile();
        if(StringUtils.isEmpty(systemPropertiesFile)){
            return new HashSet<>();
        }
        systemPropertiesFile = StringUtils.replace(systemPropertiesFile, PROJECT_PATH_TOKEN, projectPath);
        return parser.parseProperties(systemPropertiesFile);
    }

    private Map<String, Serializable> getTestCaseInputsMap(SlangTestCase testCase) {
        List<Map> inputs = testCase.getInputs();
        Map<String, Serializable> convertedInputs = new HashMap<>();
        return convertMapParams(inputs, convertedInputs);
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
    public Long trigger(SlangTestCase testCase, CompilationArtifact compilationArtifact,
                        Map<String, ? extends Serializable> inputs,
                        Set<SystemProperty> systemProperties) {

        String testCaseName = testCase.getName();
        String result = testCase.getResult();
        Map<String, Serializable> outputs = getTestCaseOutputsMap(testCase);
        String flowName = testCase.getTestFlowPath();

        TriggerTestCaseEventListener testsEventListener = new TriggerTestCaseEventListener();
        slang.subscribeOnEvents(testsEventListener, createListenerEventTypesSet());

        Long executionId = slang.run(compilationArtifact, inputs, systemProperties);

        while (!testsEventListener.isFlowFinished()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignore) {}
        }
        slang.unSubscribeOnEvents(testsEventListener);

        ReturnValues executionReturnValues = testsEventListener.getExecutionReturnValues();

        String errorMessageFlowExecution = testsEventListener.getErrorMessage();

        String message;
        if (BooleanUtils.isTrue(testCase.getThrowsException())) {
            if(StringUtils.isBlank(errorMessageFlowExecution)) {
                message = TEST_CASE_FAILED + testCaseName + " - " + testCase.getDescription() + "\n\tFlow " +
                                compilationArtifact.getExecutionPlan().getName() + " did not throw an exception as expected";
                log.info(message);
                throw new RuntimeException(message);
            }
            log.info(TEST_CASE_PASSED + testCaseName + ". Finished running: " + flowName + " with exception as expected" );
            return executionId;
        }

        if (StringUtils.isNotBlank(errorMessageFlowExecution)){
            // unexpected exception occurred during flow execution
            message = "Error occurred while running test: " + testCaseName + " - " + testCase.getDescription() + "\n\t" + errorMessageFlowExecution;
            log.info(message);
            throw new RuntimeException(message);
        }

        String executionResult = executionReturnValues.getResult();
        if (result != null && !result.equals(executionResult)){
            message = TEST_CASE_FAILED + testCaseName + " - " + testCase.getDescription() + "\n\tExpected result: " + result + "\n\tActual result: " + executionResult;
            log.error(message);
            throw new RuntimeException(message);
        }

        Map<String, Serializable> executionOutputs = executionReturnValues.getOutputs();
        if (MapUtils.isNotEmpty(outputs)){
            for(Map.Entry<String, Serializable> output: outputs.entrySet()) {
                String outputName = output.getKey();
                Serializable outputValue = output.getValue();
                Serializable executionOutputValue = executionOutputs.get(outputName);
                if(!executionOutputs.containsKey(outputName) ||
                        !outputsAreEqual(outputValue, executionOutputValue)){
                    message = TEST_CASE_FAILED + testCaseName + " - " + testCase.getDescription() + "\n\tFor output: " + outputName+ "\n\tExpected value: " + outputValue + "\n\tActual value: " + executionOutputValue;
                    log.error(message);
                    throw new RuntimeException(message);
                }
            }
        }

        log.info(TEST_CASE_PASSED + testCaseName + ". Finished running: " + flowName + " with result: " + executionResult);
        return executionId;
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
