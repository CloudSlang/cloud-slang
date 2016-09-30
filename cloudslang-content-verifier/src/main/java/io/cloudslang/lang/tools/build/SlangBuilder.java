/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package io.cloudslang.lang.tools.build;

import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.tools.build.SlangBuildMain.BulkRunMode;
import io.cloudslang.lang.tools.build.tester.IRunTestResults;
import io.cloudslang.lang.tools.build.tester.RunTestsResults;
import io.cloudslang.lang.tools.build.tester.SlangTestRunner;
import io.cloudslang.lang.tools.build.tester.SlangTestRunner.TestCaseRunState;
import io.cloudslang.lang.tools.build.tester.parallel.report.ThreadSafeRunTestResults;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import io.cloudslang.lang.tools.build.verifier.CompilationResult;
import io.cloudslang.lang.tools.build.verifier.SlangContentVerifier;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.cloudslang.lang.tools.build.SlangBuildMain.BulkRunMode.ALL_PARALLEL;
import static io.cloudslang.lang.tools.build.SlangBuildMain.BulkRunMode.ALL_SEQUENTIAL;
import static io.cloudslang.lang.tools.build.SlangBuildMain.BulkRunMode.POSSIBLY_MIXED;

/*
 * Created by stoneo on 2/9/2015.
 */

/**
 * Verifies all files with extensions: .sl, .sl.yaml or .sl.yml in a given directory are valid
 */
@Component
public class SlangBuilder {

    private static final String UNSUPPORTED_BULK_RUN_MODE = "Unsupported bulk run mode '%s'.";

    @Autowired
    private SlangContentVerifier slangContentVerifier;

    @Autowired
    private SlangTestRunner slangTestRunner;

    private final static Logger log = Logger.getLogger(SlangBuilder.class);

    public SlangBuildResults buildSlangContent(String projectPath, String contentPath, String testsPath, List<String> testSuits, boolean shouldValidateDescription, BulkRunMode bulkRunMode) {

        String projectName = FilenameUtils.getName(projectPath);
        log.info("");
        log.info("------------------------------------------------------------");
        log.info("Building project: " + projectName);
        log.info("------------------------------------------------------------");

        log.info("");
        log.info("--- compiling sources ---");
        CompilationResult compilationResult =
                slangContentVerifier.createModelsAndValidate(contentPath, shouldValidateDescription);
        Map<String, Executable> slangModels = compilationResult.getResults();


        Map<String, CompilationArtifact> compiledSources = compileModels(slangModels);

        IRunTestResults runTestsResults = new RunTestsResults();
        if (StringUtils.isNotBlank(testsPath) && new File(testsPath).isDirectory()) {
            runTestsResults = runTests(slangModels, projectPath, testsPath, testSuits, bulkRunMode);
        }
        List<RuntimeException> exceptions = new ArrayList<>(runTestsResults.getExceptions());
        exceptions.addAll(compilationResult.getExceptions());
        return new SlangBuildResults(compiledSources.size(), runTestsResults, exceptions);
    }

    /**
     * Compiles all CloudSlang models
     *
     * @return the number of valid CloudSlang files in the given directory
     */
    private Map<String, CompilationArtifact> compileModels(Map<String, Executable> slangModels) {
        Map<String, CompilationArtifact> compiledSlangFiles =
                slangContentVerifier.compileSlangModels(slangModels);

        if (compiledSlangFiles.size() != slangModels.size()) {
            throw new RuntimeException("Some Slang files were not compiled.\n" +
                    "Found: " + slangModels.size() + " slang models, but managed to compile only: "
                    + compiledSlangFiles.size());
        }

        log.info("Successfully finished Compilation of: " + compiledSlangFiles.size() + " Slang files");
        return compiledSlangFiles;
    }

    IRunTestResults runTests(Map<String, Executable> contentSlangModels,
                             String projectPath, String testsPath, List<String> testSuites, BulkRunMode bulkRunMode) {
        log.info("");
        log.info("--- compiling tests sources ---");
        // Compile all slang test flows under the test directory
        CompilationResult compilationResult = slangContentVerifier.createModelsAndValidate(testsPath, false);
        Map<String, Executable> testFlowModels = compilationResult.getResults();
        // Add also all of the slang models of the content in order to allow for compilation of the test flows
        Map<String, Executable> allTestedFlowModels = new HashMap<>(testFlowModels);
        allTestedFlowModels.putAll(contentSlangModels);

        // Compiling all the test flows
        Map<String, CompilationArtifact> compiledFlows = slangContentVerifier.compileSlangModels(allTestedFlowModels);

        Set<String> allTestedFlowsFQN = mapExecutablesToFullyQualifiedName(allTestedFlowModels.values());
        Map<String, SlangTestCase> testCases = slangTestRunner.createTestCases(testsPath, allTestedFlowsFQN);
        log.info("");
        log.info("--- running tests ---");
        log.info("Found " + testCases.size() + " tests");
        IRunTestResults runTestsResults;

        if (bulkRunMode == ALL_PARALLEL) { // Run All tests in parallel
            ThreadSafeRunTestResults parallelRunTestResults = new ThreadSafeRunTestResults();
            Map<TestCaseRunState, Map<String, SlangTestCase>> testCaseRunState = slangTestRunner.splitTestCasesByRunState(bulkRunMode, testCases, testSuites, parallelRunTestResults);
            slangTestRunner.runTestsParallel(projectPath, testCaseRunState.get(TestCaseRunState.PARALLEL), compiledFlows, parallelRunTestResults);
            runTestsResults = parallelRunTestResults;

        } else if (bulkRunMode == ALL_SEQUENTIAL) { // Run all tests sequentially
            RunTestsResults sequentialRunTestResults = new RunTestsResults();
            Map<TestCaseRunState, Map<String, SlangTestCase>> testCaseRunState = slangTestRunner.splitTestCasesByRunState(bulkRunMode, testCases, testSuites, sequentialRunTestResults);
            slangTestRunner.runTestsSequential(projectPath, testCaseRunState.get(TestCaseRunState.SEQUENTIAL), compiledFlows, sequentialRunTestResults);
            runTestsResults = sequentialRunTestResults;

        } else if (bulkRunMode == POSSIBLY_MIXED) { // Run some tests in parallel and rest of tests sequentially
            ThreadSafeRunTestResults mixedTestResults = new ThreadSafeRunTestResults();
            Map<TestCaseRunState, Map<String, SlangTestCase>> testCaseRunState = slangTestRunner.splitTestCasesByRunState(bulkRunMode, testCases, testSuites, mixedTestResults);
            slangTestRunner.runTestsSequential(projectPath, testCaseRunState.get(TestCaseRunState.SEQUENTIAL), compiledFlows, mixedTestResults);
            slangTestRunner.runTestsParallel(projectPath, testCaseRunState.get(TestCaseRunState.PARALLEL), compiledFlows, mixedTestResults);
            runTestsResults = mixedTestResults;

        } else {
            throw new IllegalStateException(String.format(UNSUPPORTED_BULK_RUN_MODE, (bulkRunMode == null) ? null : bulkRunMode.toString()));
        }

        runTestsResults.addExceptions(compilationResult.getExceptions());
        addCoverageDataToRunTestsResults(contentSlangModels, testFlowModels, testCases, runTestsResults);
        return runTestsResults;
    }

    private Set<String> mapExecutablesToFullyQualifiedName(Collection<Executable> executables) {
        Set<String> fullyQualifiedNames = new HashSet<>();
        for (Executable executable : executables) {
            fullyQualifiedNames.add(executable.getId());
        }
        return fullyQualifiedNames;
    }

    void addCoverageDataToRunTestsResults(Map<String, Executable> contentSlangModels, Map<String, Executable> testFlowModels,
                                          Map<String, SlangTestCase> testCases, IRunTestResults runTestsResults) {
        Set<String> coveredContent = new HashSet<>();
        Set<String> uncoveredContent = new HashSet<>();
        // Add to the covered content set all the dependencies of the test flows
        for (SlangTestCase testCase : testCases.values()) {
            String testFlowPath = testCase.getTestFlowPath();
            Executable testFlowModel;
            if (testFlowModels.containsKey(testFlowPath)) {
                testFlowModel = testFlowModels.get(testFlowPath);
            } else {
                testFlowModel = contentSlangModels.get(testFlowPath);
            }
            if (testFlowModel == null) {
                continue;
            }
            addAllDependenciesToCoveredContent(coveredContent, testFlowModel.getExecutableDependencies(), contentSlangModels);
        }
        Set<String> contentExecutablesNames = contentSlangModels.keySet();
        // Add to the covered content set also all the direct test case's test flows, which are part of the tested content
        for (SlangTestCase testCase : testCases.values()) {
            String testFlowPath = testCase.getTestFlowPath();
            // Add the test flow only if it part of the content, and not of the test flows
            if (contentExecutablesNames.contains(testFlowPath)) {
                coveredContent.add(testFlowPath);
            }
        }
        // Create the uncovered content set from the content which does not appear in the covered set
        for (String contentModelName : contentExecutablesNames) {
            if (!coveredContent.contains(contentModelName)) {
                uncoveredContent.add(contentModelName);
            }
        }

        runTestsResults.addCoveredExecutables(coveredContent);
        runTestsResults.addUncoveredExecutables(uncoveredContent);
    }

    /**
     * Collect recursively all the dependencies of an executable
     *
     * @param allDependencies
     * @param directDependencies
     * @param contentSlangModels
     */
    private void addAllDependenciesToCoveredContent(Set<String> allDependencies, Set<String> directDependencies, Map<String, Executable> contentSlangModels) {
        for (String dependency : directDependencies) {
            if (allDependencies.contains(dependency)) {
                continue;
            }
            allDependencies.add(dependency);
            Executable executable = contentSlangModels.get(dependency);
            // Executable will be null in case of a dependecy which is a test flow (and not patr of the content)
            if (executable != null) {
                addAllDependenciesToCoveredContent(allDependencies, executable.getExecutableDependencies(), contentSlangModels);
            }
        }
    }

}
