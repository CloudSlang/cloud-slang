/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package org.openscore.lang.tools.build;

import org.apache.log4j.Logger;
import org.openscore.lang.compiler.modeller.model.Executable;
import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.lang.tools.build.tester.SlangTestRunner;
import org.openscore.lang.tools.build.tester.parse.SlangTestCase;
import org.openscore.lang.tools.build.verifier.SlangContentVerifier;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Set;

/*
 * Created by stoneo on 2/9/2015.
 */
/**
 * Verifies all files with extensions: .sl, .sl.yaml or .sl.yml in a given directory are valid
 */
public class SlangBuild {

    @Autowired
    private SlangContentVerifier slangContentVerifier;

    @Autowired
    private SlangTestRunner slangTestRunner;


    private final static Logger log = Logger.getLogger(SlangBuild.class);

    public int buildSlangContent(String directoryPath, String testsPath, Set<String> testSuits){

        Map<String, Executable> slangModels =
                slangContentVerifier.createModelsAndValidate(directoryPath);

        Map<String, CompilationArtifact> compiledSources = compileModels(slangModels);

        if(testsPath != null) {
            runTests(compiledSources, testsPath, testSuits);
        }

        return compiledSources.size();
    }

    /**
     * Compiles all Slang models
     * @return the number of valid Slang files in the given directory
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

    private void runTests(Map<String, CompilationArtifact> compiledSources,
                          String testsPath, Set<String> testSuites){
        // Compile all slang test flows under the test directory
        Map<String, Executable> testFlowModels = slangContentVerifier.createModelsAndValidate(testsPath);
        // Compiling all the test flows
        Map<String, CompilationArtifact> compiledFlows = slangContentVerifier.compileSlangModels(testFlowModels);
        // Add also all of the compiled sources
        compiledFlows.putAll(compiledSources);

        Map<String, SlangTestCase> testCases = slangTestRunner.createTestCases(testsPath);
        slangTestRunner.runAllTests(testCases, compiledFlows);
    }


}
