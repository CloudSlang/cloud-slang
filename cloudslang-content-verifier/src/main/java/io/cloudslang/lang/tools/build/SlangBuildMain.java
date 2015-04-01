/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build;

import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/*
 * Created by stoneo on 1/11/2015.
 */
public class SlangBuildMain {

    public static void main(String[] args) {
        String repositoryPath = parseRepositoryPathArg(args);
        Set<String> testSuites = getTestSuitesProperty();
        String testsPath = System.getProperty("testPath");

        //load application context
        ApplicationContext context = new ClassPathXmlApplicationContext("spring/testRunnerContext.xml");
        SlangBuilder slangBuilder = context.getBean(SlangBuilder.class);

        try {
            SlangBuildResults buildResults = slangBuilder.buildSlangContent(repositoryPath, testsPath, testSuites);
            Map<SlangTestCase, String> failedTests = buildResults.getFailedTests();
            if(MapUtils.isNotEmpty(failedTests)){
                System.out.println("FAILURE: CloudSlang build for repository: \"" + repositoryPath + "\" failed due to failed tests.");
                System.out.println("Following tests failed:");
                for(Map.Entry<SlangTestCase, String> failedTest : failedTests.entrySet()){
                    SlangTestCase testCase = failedTest.getKey();
                    System.out.println("  - Test: " + testCase.getName() + " - " + testCase.getDescription() + ". Error: " + failedTest.getValue());
                }
                System.exit(1);
            } else {
                System.out.println("SUCCESS: Found " + buildResults.getNumberOfCompiledSources()
                        + " slang files under directory: \"" + repositoryPath + "\" and all are valid.");
                System.exit(0);
            }
        } catch (Throwable e) {
            System.out.println(e.getMessage() + "\n\nFAILURE: Validation of slang files under directory: \""
                    + repositoryPath + "\" failed.");
            System.exit(1);
        }
    }

    private static String parseRepositoryPathArg(String[] args) {
        Validate.notEmpty(args, "You must pass a path to your repository");
        String repositoryPath = args[0];
        Validate.notNull(repositoryPath, "You must pass a path to your repository");
        repositoryPath = FilenameUtils.separatorsToSystem(repositoryPath);
        Validate.isTrue(new File(repositoryPath).isDirectory(),
                "Directory path argument \'" + repositoryPath + "\' does not lead to a directory");
        return repositoryPath;
    }

    private static Set<String> getTestSuitesProperty() {
        String testSuitesArg = System.getProperty("testSuites");
        Set<String> testSuites = null;
        if (testSuitesArg != null) {
            String[] testSuitesArray = testSuitesArg.split(Pattern.quote(","));
            testSuites = new HashSet<>(Arrays.asList(testSuitesArray));
        }
        return testSuites;
    }

}
