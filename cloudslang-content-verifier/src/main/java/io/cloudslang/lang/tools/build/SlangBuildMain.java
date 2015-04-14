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
import org.apache.log4j.Logger;
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

    private static final String CONTENT_DIR = "content";

    private final static Logger log = Logger.getLogger(SlangBuildMain.class);

    public static void main(String[] args) {
        String repositoryPath = parseRepositoryPathArg(args);
        Set<String> testSuites = getTestSuitesProperty();
        String testsPath = System.getProperty("testPath", repositoryPath.replaceAll(CONTENT_DIR + "(\\\\+|\\/+)?$", "test"));

        log.info("Loading...");
        //load application context
        ApplicationContext context = new ClassPathXmlApplicationContext("spring/testRunnerContext.xml");
        SlangBuilder slangBuilder = context.getBean(SlangBuilder.class);

        try {
            SlangBuildResults buildResults = slangBuilder.buildSlangContent(repositoryPath, testsPath, testSuites);
            Map<SlangTestCase, String> failedTests = buildResults.getFailedTests();
            if(MapUtils.isNotEmpty(failedTests)){
                log.error("");
                log.error("------------------------------------------------------------");
                log.error("BUILD FAILURE");
                log.error("------------------------------------------------------------");
                log.error("CloudSlang build for repository: \"" + repositoryPath + "\" failed due to failed tests.");
                log.error("Following " + failedTests.size() + " tests failed:");
                for(Map.Entry<SlangTestCase, String> failedTest : failedTests.entrySet()){
                    log.error("- " + failedTest.getValue().replaceAll("\n", "\n\t"));
                }
                log.error("");
                System.exit(1);
            } else {
                //todo: add printing of how many tests actually ran
                log.info("");
                log.info("------------------------------------------------------------");
                log.info("BUILD SUCCESS");
                log.info("------------------------------------------------------------");
                log.info("Found " + buildResults.getNumberOfCompiledSources()
                        + " slang files under directory: \"" + repositoryPath + "\" and all are valid.");
                log.info("");
                System.exit(0);
            }
        } catch (Throwable e) {
            log.error("");
            log.error("------------------------------------------------------------");
            log.error("Exception: " + e.getMessage() + "\n\nFAILURE: Validation of slang files under directory: \""
                    + repositoryPath + "\" failed.");
            log.error("------------------------------------------------------------");
            log.error("");
            System.exit(1);
        }
    }

    private static String parseRepositoryPathArg(String[] args) {
        String repositoryPath;
        if(args == null || args.length == 0){
            repositoryPath = System.getProperty("user.dir") + File.separator + CONTENT_DIR;
        } else {
            repositoryPath = FilenameUtils.separatorsToSystem(args[0]);
        }
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
