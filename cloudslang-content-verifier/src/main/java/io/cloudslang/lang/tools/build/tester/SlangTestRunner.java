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
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import io.cloudslang.score.events.EventConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.tools.build.tester.parse.TestCasesYamlParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by stoneo on 3/15/2015.
 */
@Component
public class SlangTestRunner {

    @Autowired
    private TestCasesYamlParser parser;

    @Autowired
    private Slang slang;

    private String[] TEST_CASE_FILE_EXTENSIONS = {"yaml", "yml"};

    private final static Logger log = Logger.getLogger(SlangTestRunner.class);

    public Map<String, SlangTestCase> createTestCases(String testPath) {
        Validate.notEmpty(testPath, "You must specify a path for tests");
        File testPathDir = new File(testPath);
        Validate.isTrue(testPathDir.isDirectory(),
                "Directory path argument \'" + testPath + "\' does not lead to a directory");
        Collection<File> testCasesFiles = FileUtils.listFiles(testPathDir, TEST_CASE_FILE_EXTENSIONS, true);

        log.info("Start parsing all test cases files under: " + testPath);
        log.info(testCasesFiles.size() + " test cases files were found");

        Map<String, SlangTestCase> testCases = new HashMap<>();
        for (File testCaseFile : testCasesFiles) {
            Validate.isTrue(testCaseFile.isFile(),
                    "file path \'" + testCaseFile.getAbsolutePath() + "\' must lead to a file");

            Map<String, SlangTestCase> testCasesFromCurrentFile = parser.parse(SlangSource.fromFile(testCaseFile));
            for (String currentTestCaseName : testCasesFromCurrentFile.keySet()) {
                SlangTestCase currentTestCase = testCasesFromCurrentFile.get(currentTestCaseName);
                //todo: temporary solution
                currentTestCase.setName(currentTestCaseName);
                if(StringUtils.isBlank(currentTestCase.getResult())){
                    currentTestCase.setResult(getResultFromFileName(currentTestCase.getTestFlowPath()));
                }
                if(currentTestCase.getThrowsException() == null){
                    currentTestCase.setThrowsException(false);
                }
            }
            testCases.putAll(testCasesFromCurrentFile);
        }
        return testCases;
    }

    public Map<SlangTestCase, String> runAllTests(Map<String, SlangTestCase> testCases,
                            Map<String, CompilationArtifact> compiledFlows) {

        Map<SlangTestCase, String> failedTestCases = new HashMap<>();
        for (Map.Entry<String, SlangTestCase> testCaseEntry : testCases.entrySet()) {
            log.info("Start running test: " + testCaseEntry.getKey() + " - " + testCaseEntry.getValue().getDescription());
            SlangTestCase testCase = testCaseEntry.getValue();
            CompilationArtifact compiledTestFlow = getCompiledTestFlow(compiledFlows, testCase);
            try {
                runTest(testCase, compiledTestFlow);
            } catch (RuntimeException e){
                failedTestCases.put(testCase, e.getMessage());
            }
        }
        return failedTestCases;
    }

    private static CompilationArtifact getCompiledTestFlow(Map<String, CompilationArtifact> compiledFlows, SlangTestCase testCase) {
        String testFlowPath = testCase.getTestFlowPath();
        String testFlowPathTransformed = testFlowPath.replace(File.separatorChar, '.');
        CompilationArtifact compiledTestFlow = compiledFlows.get(testFlowPathTransformed);
        Validate.notNull("Test flow: " + testFlowPath + " is missing. Referenced in test case: " + testCase.getName());
        return compiledTestFlow;
    }

    private void runTest(SlangTestCase testCase, CompilationArtifact compiledTestFlow) {

        List<Map> inputs = testCase.getInputs();
        Map<String, Serializable> convertedInputs = new HashMap<>();
        if (CollectionUtils.isNotEmpty(inputs)) {
            for (Map input : inputs) {
                convertedInputs.put(
                        (String) input.keySet().iterator().next(),
                        (Serializable) input.values().iterator().next());
            }
        }
        //todo: add support in sys properties
        trigger(testCase, compiledTestFlow, convertedInputs, null);
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
                        Map<String, ? extends Serializable> systemProperties) {
        String testCaseName = testCase.getName();
        String result = testCase.getResult();

        //add start event
        Set<String> handlerTypes = new HashSet<>();
        handlerTypes.add(ScoreLangConstants.EVENT_EXECUTION_FINISHED);
        handlerTypes.add(ScoreLangConstants.SLANG_EXECUTION_EXCEPTION);
        handlerTypes.add(ScoreLangConstants.EVENT_OUTPUT_END);
        handlerTypes.add(EventConstants.SCORE_ERROR_EVENT);
        handlerTypes.add(EventConstants.SCORE_FAILURE_EVENT);
        handlerTypes.add(EventConstants.SCORE_FINISHED_EVENT);

        TriggerTestCaseEventListener testsEventListener = new TriggerTestCaseEventListener(testCaseName, result);
        slang.subscribeOnEvents(testsEventListener, handlerTypes);

        Long executionId = slang.run(compilationArtifact, inputs, systemProperties);

        while (!testsEventListener.isFlowFinished()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignore) {}
        }
        slang.unSubscribeOnEvents(testsEventListener);

        ReturnValues executionReturnValues = testsEventListener.getExecutionReturnValues();
        String executionResult = executionReturnValues.getResult();

        String errorMessageFlowExecution = testsEventListener.getErrorMessage();

        if(StringUtils.isBlank(errorMessageFlowExecution) && BooleanUtils.isTrue(testCase.getThrowsException())){
            throw new RuntimeException("Failed test: " + testCaseName + " - " + testCase.getDescription() + "\nFlow " + compilationArtifact.getExecutionPlan().getName() +" did not throw an exception as expected");
        }
        if(StringUtils.isNotBlank(errorMessageFlowExecution) && BooleanUtils.isFalse(testCase.getThrowsException())){
            // unexpected exception occurred during flow execution
            throw new RuntimeException("Error occured while running test: " + testCaseName + " - " + testCase.getDescription() + "\n" + errorMessageFlowExecution);
        }

        if (result != null && !executionResult.equals(result)){
            throw new RuntimeException("Failed test: " + testCaseName +" - " + testCase.getDescription() + "\nExpected result: " + result + "\nActual result: " + executionResult);
        }
        return executionId;
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
