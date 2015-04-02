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
import io.cloudslang.lang.tools.build.tester.SlangTestRunner;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import io.cloudslang.lang.tools.build.verifier.SlangContentVerifier;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
 * Created by stoneo on 2/9/2015.
 */
/**
 * Verifies all files with extensions: .sl, .sl.yaml or .sl.yml in a given directory are valid
 */
@Component
public class SlangBuilder {

    @Autowired
    private SlangContentVerifier slangContentVerifier;

    @Autowired
    private SlangTestRunner slangTestRunner;


    private final static Logger log = Logger.getLogger(SlangBuilder.class);

    public SlangBuildResults buildSlangContent(String directoryPath, String testsPath, Set<String> testSuits){

        Map<String, Executable> slangModels =
                slangContentVerifier.createModelsAndValidate(directoryPath);

        Map<String, CompilationArtifact> compiledSources = compileModels(slangModels);

        Map<SlangTestCase, String> failedTests = new HashMap<>();
        if(testsPath != null) {
            failedTests = runTests(slangModels, testsPath, testSuits);
        }

        return new SlangBuildResults(compiledSources.size(), failedTests);
    }

    /**
     * Compiles all CloudSlang models
     * @return the number of valid CloudSlang files in the given directory
     */
    private Map<String, CompilationArtifact> compileModels(Map<String, Executable> slangModels){
        Map<String, CompilationArtifact> compiledSlangFiles =
                slangContentVerifier.compileSlangModels(slangModels);

        if(compiledSlangFiles.size() != slangModels.size()){
            throw new RuntimeException("Some Slang files were not compiled.\n" +
                    "Found: " + slangModels.size() + " slang models, but managed to compile only: "
                    + compiledSlangFiles.size());
        }

        log.info("Successfully finished Compilation of: " + compiledSlangFiles.size() + " Slang files");
        return compiledSlangFiles;
    }

    private Map<SlangTestCase, String> runTests(Map<String, Executable> contentSlangModels,
                          String testsPath, Set<String> testSuites){
        // Compile all slang test flows under the test directory
        Map<String, Executable> testFlowModels = slangContentVerifier.createModelsAndValidate(testsPath);
        // Add also all of the slang models of the content in order to allow for compilation of the test flows
        testFlowModels.putAll(contentSlangModels);
        // Compiling all the test flows
        Map<String, CompilationArtifact> compiledFlows = slangContentVerifier.compileSlangModels(testFlowModels);

        Map<String, SlangTestCase> testCases = slangTestRunner.createTestCases(testsPath);
        log.info("Going to run " + testCases.size() + " tests");
        return slangTestRunner.runAllTests(testCases, compiledFlows);
    }


}
