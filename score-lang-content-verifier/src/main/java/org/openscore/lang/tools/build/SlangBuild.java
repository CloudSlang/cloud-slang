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

import static org.openscore.lang.compiler.SlangSource.fromFile;

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


    Map<String, CompilationArtifact> compiledSlangFiles;

    private final static Logger log = Logger.getLogger(SlangBuild.class);

    public int buildSlangContent(String directoryPath, String testsPath, String[] testSuits){
        verifyAllSlangFilesInDirAreValid(directoryPath);
        int numOfCompiledSlangFiles = compiledSlangFiles.size();

        if(testsPath != null) {
            verifyAllSlangFilesInDirAreValid(testsPath);
            runTests(testsPath, testSuits);
        }

        return numOfCompiledSlangFiles;
    }

    /**
     * Transform all Slang files in given directory to Slang models, and store them
     * @param directoryPath given directory containing all Slang files
     * @return the number of valid Slang files in the given directory
     */
    private void verifyAllSlangFilesInDirAreValid(String directoryPath){
        Map<String, Executable> slangModels = slangContentVerifier.transformSlangFilesInDirToModelsAndValidate(directoryPath);
        compiledSlangFiles = slangContentVerifier.compileSlangModels(slangModels);
        if(compiledSlangFiles.size() != slangModels.size()){
            throw new RuntimeException("Some Slang files were not compiled.\n" +
                    "Found: " + slangModels.size() + " slang models, but managed to compile only: " + compiledSlangFiles.size());
        }
        String successMessage = "Successfully finished Compilation of: " + compiledSlangFiles.size() + " Slang files";
        log.info(successMessage);
    }

    public void runTests(String testsPath, String[] testSuits){
        // Compile all slang test flows under the test directory
        Map<String, Executable> testFlowModels = slangContentVerifier.transformSlangFilesInDirToModelsAndValidate(testsPath);
        Map<String, CompilationArtifact> compiledTestFlows = slangContentVerifier.compileSlangModels(testFlowModels);

        Map<String, SlangTestCase> testCases = slangTestRunner.createTestCases(testsPath);
        slangTestRunner.runAllTests(testCases, compiledSlangFiles, compiledTestFlows);
    }


}
