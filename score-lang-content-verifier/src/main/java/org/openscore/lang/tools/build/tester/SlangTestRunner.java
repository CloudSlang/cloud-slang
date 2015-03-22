/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package org.openscore.lang.tools.build.tester;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.openscore.lang.api.Slang;
import org.openscore.lang.compiler.SlangSource;
import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.lang.runtime.env.ReturnValues;
import org.openscore.lang.tools.build.tester.parse.SlangTestCase;
import org.openscore.lang.tools.build.tester.parse.TestCasesYamlParser;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openscore.lang.entities.ScoreLangConstants.*;

/**
 * Created by stoneo on 3/15/2015.
 */
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
            testCases.putAll(parser.parse(SlangSource.fromFile(testCaseFile)));
        }
        return testCases;
    }

    public void runAllTests(Map<String, SlangTestCase> testCases,
                            Map<String, CompilationArtifact> compiledTestFlows) {
        for (Map.Entry<String, SlangTestCase> testCaseEntry : testCases.entrySet()) {
            log.info("Start running test: " + testCaseEntry.getKey());
            SlangTestCase testCase = testCaseEntry.getValue();
            String testFlowPath = testCase.getTestFlowPath();
            String testFlowPathTransformed = testFlowPath.replace(File.separatorChar, '.');
            CompilationArtifact compiledTestFlow = compiledTestFlows.get(testFlowPathTransformed);
            Validate.notNull("Test flow: " + testFlowPath + " is missing. Referenced in test case: " + testCase.getName());
            runTest(testCase, compiledTestFlow);
        }
    }

    private void runTest(SlangTestCase testCase, CompilationArtifact compiledTestFlow) {

        List<Map> inputs = testCase.getInputs();
        Map<String, Serializable> convertedInputs = new HashMap<>();
        for(Map input : inputs){
            convertedInputs.put((String)input.keySet().iterator().next(), (Serializable)input.values().iterator().next());
        }
        //todo: add support in sys properties
        trigger(testCase.getName(), compiledTestFlow, convertedInputs, null, testCase.getResult());
    }

    /**
     * This method will trigger the flow in a synchronize matter, meaning only one flow can run at a time.
     * @param compilationArtifact the artifact to trigger
     * @param inputs : flow inputs
     * @return executionId
     */
    public Long trigger(String testCaseName, CompilationArtifact compilationArtifact,
                        Map<String, ? extends Serializable> inputs,
                        Map<String, ? extends Serializable> systemProperties,
                        String result){
        //add start event
        Set<String> handlerTypes = new HashSet<>();
        handlerTypes.add(EVENT_EXECUTION_FINISHED);
        handlerTypes.add(SLANG_EXECUTION_EXCEPTION);
        handlerTypes.add(EVENT_OUTPUT_END);

        TriggerTestCaseEventListener testsEventListener = new TriggerTestCaseEventListener(testCaseName);
        slang.subscribeOnEvents(testsEventListener, handlerTypes);

        Long executionId = slang.run(compilationArtifact, inputs, systemProperties);

        while(!testsEventListener.isFlowFinished()){
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignore) {}
        }
        slang.unSubscribeOnEvents(testsEventListener);

        ReturnValues executionReturnValues = testsEventListener.getExecutionReturnValues();
        String executionResult = executionReturnValues.getResult();

        String errorMessageFlowExecution = testsEventListener.getErrorMessage();
        if (StringUtils.isNotEmpty(errorMessageFlowExecution) || !executionResult.equals(result)) {
            // exception occurred during flow execution
            throw new RuntimeException(errorMessageFlowExecution);
        }

        return executionId;
    }
}
