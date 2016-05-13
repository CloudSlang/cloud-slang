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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.tools.build.commands.ApplicationArgs;
import io.cloudslang.lang.tools.build.tester.RunTestsResults;
import io.cloudslang.lang.tools.build.tester.TestRun;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.events.ScoreEventListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Created by stoneo on 1/11/2015.
 */
public class SlangBuildMain {

    private static final String CONTENT_DIR =  File.separator + "content";
    private static final String TEST_DIR = File.separator + "test";
    public static final String DEFAULT_TESTS = "default";

    private final static Logger log = Logger.getLogger(SlangBuildMain.class);
    private final static String NOT_TS = "!";

    private static final String USER_CONFIG_DIR = "configuration";
    private static final String USER_CONFIG_FILENAME = "cslang.properties";
    private static final String USER_CONFIG_FILEPATH = USER_CONFIG_DIR + File.separator + USER_CONFIG_FILENAME;
    private static final String SUBSTITUTION_REGEX = "\\$\\{([^${}]+)\\}"; // ${system.property.name}
    private static final Pattern SUBSTITUTION_PATTERN = Pattern.compile(SUBSTITUTION_REGEX);

    public static void main(String[] args) {
        try {
            loadUserProperties();
        } catch (Exception ex) {
            System.out.println("Error occurred while loading user configuration: " + ex.getMessage());
            ex.printStackTrace();
        }

        ApplicationArgs appArgs = new ApplicationArgs();
        parseArgs(args, appArgs);
        String projectPath = parseProjectPathArg(appArgs);
        String contentPath = StringUtils.defaultIfEmpty(appArgs.getContentRoot(), projectPath + CONTENT_DIR);
        String testsPath = StringUtils.defaultIfEmpty(appArgs.getTestRoot(), projectPath + TEST_DIR);
        List<String> testSuites = parseTestSuites(appArgs);
        Boolean shouldPrintCoverageData = parseCoverageArg(appArgs);

        log.info("");
        log.info("------------------------------------------------------------");
        log.info("Building project: " + projectPath);
        log.info("Content root is at: " + contentPath);
        log.info("Test root is at: " + testsPath);
        log.info("Active test suites are: " + Arrays.toString(testSuites.toArray()));

        log.info("");
        log.info("Loading...");

        //load application context
        ApplicationContext context = new ClassPathXmlApplicationContext("spring/testRunnerContext.xml");
        SlangBuilder slangBuilder = context.getBean(SlangBuilder.class);
        Slang slang = context.getBean(Slang.class);
        registerEventHandlers(slang);

        try {
            SlangBuildResults buildResults = slangBuilder.buildSlangContent(projectPath, contentPath, testsPath, testSuites);
            RunTestsResults runTestsResults = buildResults.getRunTestsResults();
            Map<String, TestRun> skippedTests = runTestsResults.getSkippedTests();

            if(MapUtils.isNotEmpty(skippedTests)){
                printSkippedTestsSummary(skippedTests);
            }
            if(shouldPrintCoverageData) {
                printTestCoverageData(runTestsResults);
            }
            Map<String, TestRun> failedTests = runTestsResults.getFailedTests();
            if(MapUtils.isNotEmpty(failedTests)){
                printBuildFailureSummary(projectPath, failedTests);
                System.exit(1);
            } else {
                printBuildSuccessSummary(contentPath, buildResults, runTestsResults, skippedTests);
                System.exit(0);
            }
        } catch (Throwable e) {
            log.error("");
            log.error("------------------------------------------------------------");
            log.error("Exception: " + e.getMessage() + "\n\nFAILURE: Validation of slang files for project: \""
                    + projectPath + "\" failed.");
            log.error("------------------------------------------------------------");
            log.error("");
            System.exit(1);
        }
    }

    private static void parseArgs(String[] args, ApplicationArgs appArgs) {
        try {
            JCommander jCommander = new JCommander(appArgs, args);
            if (appArgs.isHelp()) {
                jCommander.usage();
                System.exit(0);
            }
        } catch (ParameterException e) {
            System.out.println(e.getMessage());
            System.out.println("You can use '--help' for usage");
            System.exit(1);
        }
    }

    private static List<String> parseTestSuites(ApplicationArgs appArgs) {
        List<String> testSuites = new ArrayList<>();
        boolean runDefaultTests = true;
        List<String> testSuitesArg = ListUtils.defaultIfNull(appArgs.getTestSuites(), new ArrayList<String>());
        for(String testSuite : testSuitesArg){
            if(!testSuite.startsWith(NOT_TS)){
                testSuites.add(testSuite);
            } else if(testSuite.equalsIgnoreCase(NOT_TS + DEFAULT_TESTS)){
                runDefaultTests = false;
            }
        }
        if(runDefaultTests && !testSuitesArg.contains(DEFAULT_TESTS)){
            testSuites.add(DEFAULT_TESTS);
        }
        return testSuites;
    }

    private static Boolean parseCoverageArg(ApplicationArgs appArgs){
        Boolean shouldOutputCoverageData = false;

        if (appArgs.shouldOutputCoverage() != null) {
            shouldOutputCoverageData = appArgs.shouldOutputCoverage();
        }
        return shouldOutputCoverageData;
    }

    private static void printBuildSuccessSummary(String contentPath, SlangBuildResults buildResults, RunTestsResults runTestsResults, Map<String, TestRun> skippedTests) {
        log.info("");
        log.info("------------------------------------------------------------");
        log.info("BUILD SUCCESS");
        log.info("------------------------------------------------------------");
        log.info("Found " + buildResults.getNumberOfCompiledSources()
                + " slang files under directory: \"" + contentPath + "\" and all are valid.");
        log.info(runTestsResults.getPassedTests().size() + " test cases passed");
        if(skippedTests.size() > 0){
            log.info(skippedTests.size() + " test cases skipped");
        }
        log.info("");
    }

    private static void printBuildFailureSummary(String projectPath, Map<String, TestRun> failedTests) {
        log.error("");
        log.error("------------------------------------------------------------");
        log.error("BUILD FAILURE");
        log.error("------------------------------------------------------------");
        log.error("CloudSlang build for repository: \"" + projectPath + "\" failed due to failed tests.");
        log.error("Following " + failedTests.size() + " tests failed:");
        for(Map.Entry<String, TestRun> failedTest : failedTests.entrySet()){
            String failureMessage = failedTest.getValue().getMessage();
            log.error("- " + failureMessage.replaceAll("\n", "\n\t"));
        }
        log.error("");
    }

    private static void printSkippedTestsSummary(Map<String, TestRun> skippedTests) {
        log.info("");
        log.info("------------------------------------------------------------");
        log.info("Following " + skippedTests.size() + " tests were skipped:");
        for(Map.Entry<String, TestRun> skippedTest : skippedTests.entrySet()){
            String message = skippedTest.getValue().getMessage();
            log.info("- " + message.replaceAll("\n", "\n\t"));
        }
    }

    private static void printTestCoverageData(RunTestsResults runTestsResults){
        printCoveredExecutables(runTestsResults.getCoveredExecutables());
        printUncoveredExecutables(runTestsResults.getUncoveredExecutables());
        int coveredExecutablesSize = runTestsResults.getCoveredExecutables().size();
        int uncoveredExecutablesSize = runTestsResults.getUncoveredExecutables().size();
        int totalNumberOfExecutables = coveredExecutablesSize + uncoveredExecutablesSize;
        Double coveragePercentage = new Double(coveredExecutablesSize)/new Double(totalNumberOfExecutables)*100;
        log.info("");
        log.info("------------------------------------------------------------");
        log.info(coveragePercentage.intValue() + "% of the content has tests");
        log.info("Out of " + totalNumberOfExecutables + " executables, " + coveredExecutablesSize + " executables have tests");
    }

    private static void printCoveredExecutables(Set<String> coveredExecutables) {
        log.info("");
        log.info("------------------------------------------------------------");
        log.info("Following " + coveredExecutables.size() + " executables have tests:");
        for(String executable : coveredExecutables){
            log.info("- " + executable);
        }
    }

    private static void printUncoveredExecutables(Set<String> uncoveredExecutables) {
        log.info("");
        log.info("------------------------------------------------------------");
        log.info("Following " + uncoveredExecutables.size() + " executables do not have tests:");
        for(String executable : uncoveredExecutables){
            log.info("- " + executable);
        }
    }

    private static String parseProjectPathArg(ApplicationArgs args) {
        String repositoryPath;

        if (args.getProjectRoot() != null) {
            repositoryPath = args.getProjectRoot();
        // if only one parameter was passed, we treat it as the project root
        // i.e. './cslang-builder some/path/to/project'
        } else if (args.getParameters().size() == 1) {
            repositoryPath = args.getParameters().get(0);
        } else {
            repositoryPath = System.getProperty("user.dir");
        }

        repositoryPath = FilenameUtils.separatorsToSystem(repositoryPath);

        Validate.isTrue(new File(repositoryPath).isDirectory(),
                "Directory path argument \'" + repositoryPath + "\' does not lead to a directory");

        return repositoryPath;
    }

    private static void registerEventHandlers(Slang slang) {
        slang.subscribeOnAllEvents(new ScoreEventListener() {
            @Override
            public synchronized void onEvent(ScoreEvent event) {
                logEvent(event);
            }
        });
    }

    private static void logEvent(ScoreEvent event) {
        log.debug(("Event received: " + event.getEventType() + " Data is: " + event.getData()));
    }

    private static void loadUserProperties() throws IOException {
        String appHome = System.getProperty("app.home");
        String propertyFilePath = appHome + File.separator + USER_CONFIG_FILEPATH;
        File propertyFile = new File(propertyFilePath);
        Properties rawProperties = new Properties();
        if (propertyFile.isFile()) {
            try (InputStream propertiesStream = new FileInputStream(propertyFilePath)) {
                rawProperties.load(propertiesStream);
            }
        }
        Set<Map.Entry<Object, Object>> propertyEntries = rawProperties.entrySet();
        for (Map.Entry<Object, Object> property : propertyEntries) {
            String key = (String) property.getKey();
            String value = (String) property.getValue();
            value = substitutePropertyReferences(value);
            System.setProperty(key, value);
        }
    }

    private static String substitutePropertyReferences(String value) {
        Set<String> variableNames = findPropertyReferences(value);
        return replacePropertyReferences(value, variableNames);
    }

    private static Set<String> findPropertyReferences(String value) {
        Matcher mather = SUBSTITUTION_PATTERN.matcher(value);
        Set<String> variableNames = new HashSet<>();
        while (mather.find()) {
            variableNames.add(mather.group(1));
        }
        return variableNames;
    }

    private static String replacePropertyReferences(String value, Set<String> variableNames) {
        for (String variableName : variableNames) {
            String variableValue = System.getProperty(variableName);
            if (StringUtils.isNotEmpty(variableValue)) {
                value = value.replace("${" + variableName + "}", variableValue);
            }
        }
        return value;
    }

}
