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
import io.cloudslang.lang.commons.services.api.UserConfigurationService;
import io.cloudslang.lang.commons.services.impl.UserConfigurationServiceImpl;
import io.cloudslang.lang.tools.build.commands.ApplicationArgs;
import io.cloudslang.lang.tools.build.tester.IRunTestResults;
import io.cloudslang.lang.tools.build.tester.TestRun;
import io.cloudslang.lang.tools.build.tester.parallel.report.SlangTestCaseRunReportGeneratorService;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.events.ScoreEventListener;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static io.cloudslang.lang.tools.build.ArgumentProcessorUtils.getBooleanFromPropertiesWithDefault;
import static io.cloudslang.lang.tools.build.ArgumentProcessorUtils.getEnumInstanceFromPropertiesWithDefault;
import static io.cloudslang.lang.tools.build.ArgumentProcessorUtils.getIntFromPropertiesWithDefaultAndRange;
import static io.cloudslang.lang.tools.build.SlangBuildMain.BulkRunMode.ALL_PARALLEL;
import static io.cloudslang.lang.tools.build.SlangBuildMain.BulkRunMode.ALL_SEQUENTIAL;
import static io.cloudslang.lang.tools.build.SlangBuildMain.BulkRunMode.POSSIBLE_MIXED;
import static io.cloudslang.lang.tools.build.SlangBuildMain.RunConfigurationProperties.TEST_COVERAGE;
import static io.cloudslang.lang.tools.build.SlangBuildMain.RunConfigurationProperties.TEST_PARALLEL_THREAD_COUNT;
import static io.cloudslang.lang.tools.build.SlangBuildMain.RunConfigurationProperties.TEST_SUITES_PARALLEL;
import static io.cloudslang.lang.tools.build.SlangBuildMain.RunConfigurationProperties.TEST_SUITES_RUN_UNSPECIFIED;
import static io.cloudslang.lang.tools.build.SlangBuildMain.RunConfigurationProperties.TEST_SUITES_SEQUENTIAL;
import static io.cloudslang.lang.tools.build.SlangBuildMain.RunConfigurationProperties.TEST_SUITES_TO_RUN;
import static io.cloudslang.lang.tools.build.tester.SlangTestRunner.MAX_TIME_PER_TESTCASE_IN_MINUTES;
import static io.cloudslang.lang.tools.build.tester.SlangTestRunner.TEST_CASE_TIMEOUT_IN_MINUTES_KEY;
import static io.cloudslang.lang.tools.build.tester.parallel.services.ParallelTestCaseExecutorService.SLANG_TEST_RUNNER_THREAD_COUNT;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static java.util.Locale.ENGLISH;
import static org.apache.commons.collections4.ListUtils.removeAll;
import static org.apache.commons.collections4.ListUtils.union;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;


public class SlangBuildMain {

    private static final String TEST_CASE_REPORT_LOCATION = "cloudslang.test.case.report.location";
    private static final String CONTENT_DIR = File.separator + "content";
    private static final String TEST_DIR = File.separator + "test";
    public static final String DEFAULT_TESTS = "default";

    private final static Logger log = Logger.getLogger(SlangBuildMain.class);
    private final static String NOT_TS = "!";
    private static final int MAX_THREADS_TEST_RUNNER = 32;
    private static final String SUITE_LIST_SEPARATOR = ",";
    private static final String MESSAGE_NOT_SCHEDULED_FOR_RUN_RULES = "Rules '%s' defined in '%s' key are not scheduled for run.";
    public static final String MESSAGE_TEST_SUITES_WITH_UNSPECIFIED_MAPPING = "Test suites '%s' have unspecified mapping. They will run in '%s' mode.";
    public static final String LIST_JOINER = ", ";

    static class RunConfigurationProperties {
        static final String TEST_COVERAGE = "test.coverage";
        static final String TEST_PARALLEL_THREAD_COUNT = "test.parallel.thread.count";
        static final String TEST_SUITES_TO_RUN = "test.suites.run";
        static final String TEST_SUITES_PARALLEL = "test.suites.parallel";
        static final String TEST_SUITES_SEQUENTIAL = "test.suites.sequential";
        static final String TEST_SUITES_RUN_UNSPECIFIED = "test.suites.run.unspecified";
    }

    public enum TestCaseRunMode {
        PARALLEL,
        SEQUENTIAL
    }

    public enum BulkRunMode {
        ALL_PARALLEL,
        ALL_SEQUENTIAL,
        POSSIBLE_MIXED
    }

    public static void main(String[] args) {
        loadUserProperties();

        ApplicationArgs appArgs = new ApplicationArgs();
        parseArgs(args, appArgs);
        String projectPath = parseProjectPathArg(appArgs);
        String contentPath = defaultIfEmpty(appArgs.getContentRoot(), projectPath + CONTENT_DIR);
        String testsPath = defaultIfEmpty(appArgs.getTestRoot(), projectPath + TEST_DIR);
        List<String> testSuites = parseTestSuites(appArgs);
        boolean shouldPrintCoverageData = appArgs.shouldOutputCoverage();
        boolean runTestsInParallel = appArgs.isParallel();
        int threadCount = parseThreadCountArg(appArgs, runTestsInParallel);
        String testCaseTimeout = parseTestTimeout(appArgs);
        setProperty(SLANG_TEST_RUNNER_THREAD_COUNT, valueOf(threadCount));
        setProperty(TEST_CASE_TIMEOUT_IN_MINUTES_KEY, valueOf(testCaseTimeout));
        boolean shouldValidateDescription = appArgs.shouldValidateDescription();
        String runConfigPath = FilenameUtils.normalize(appArgs.getRunConfigPath());

        // Override with the values from the file if configured
        List<String> testSuitesParallel = new ArrayList<>();
        List<String> testSuitesSequential = new ArrayList<>();
        BulkRunMode bulkRunMode = runTestsInParallel ? ALL_PARALLEL : ALL_SEQUENTIAL;

        TestCaseRunMode testSuiteRunMode = TestCaseRunMode.SEQUENTIAL;
        if (get(runConfigPath).isAbsolute() && isRegularFile(get(runConfigPath), NOFOLLOW_LINKS)) {
            Properties runConfigurationProperties = getRunConfigurationProperties(runConfigPath);
            shouldPrintCoverageData = getBooleanFromPropertiesWithDefault(TEST_COVERAGE, shouldPrintCoverageData, runConfigurationProperties);
            threadCount = getIntFromPropertiesWithDefaultAndRange(TEST_PARALLEL_THREAD_COUNT, threadCount, runConfigurationProperties, 1, MAX_THREADS_TEST_RUNNER + 1);
            testSuites = getTestSuitesForKey(runConfigurationProperties, TEST_SUITES_TO_RUN);
            testSuitesParallel = getTestSuitesForKey(runConfigurationProperties, TEST_SUITES_PARALLEL);
            testSuitesSequential = getTestSuitesForKey(runConfigurationProperties, TEST_SUITES_SEQUENTIAL);
            testSuiteRunMode = getEnumInstanceFromPropertiesWithDefault(TEST_SUITES_RUN_UNSPECIFIED, TestCaseRunMode.PARALLEL, runConfigurationProperties);
            addWarningsForMisconfiguredTestSuites(testSuiteRunMode, testSuites, testSuitesSequential, testSuitesParallel);
            bulkRunMode = POSSIBLE_MIXED;
        }

        log.info("");
        log.info("------------------------------------------------------------");
        log.info("Building project: " + projectPath);
        log.info("Content root is at: " + contentPath);
        log.info("Test root is at: " + testsPath);
        log.info("Active test suites are: " +  join(testSuites, LIST_JOINER));
        log.info("Parallel run mode is configured for test suites: " + join(testSuitesParallel, LIST_JOINER));
        log.info("Sequential run mode is configured for test suites: " + join(testSuitesSequential, LIST_JOINER));
        log.info("Default run mode is configured for test suites: " + join(getDefaultRunModeTestSuites(testSuites, testSuitesParallel, testSuitesSequential), LIST_JOINER));
        log.info("Default run for test suites is: " + testSuiteRunMode.name().toLowerCase());
        log.info("Run mode for tests: " + bulkRunMode.toString().toLowerCase(ENGLISH));

        log.info("Print coverage data: " + valueOf(shouldPrintCoverageData));
        log.info("Validate description: " + valueOf(shouldValidateDescription));
        log.info("Thread count: " + threadCount);
        log.info("Test case timeout in minutes: " + (isEmpty(testCaseTimeout) ? valueOf(MAX_TIME_PER_TESTCASE_IN_MINUTES) : testCaseTimeout));

        log.info("");
        log.info("Loading...");

        //load application context
        ApplicationContext context = new ClassPathXmlApplicationContext("spring/testRunnerContext.xml");
        SlangBuilder slangBuilder = context.getBean(SlangBuilder.class);
        Slang slang = context.getBean(Slang.class);
        SlangTestCaseRunReportGeneratorService reportGeneratorService = context.getBean(SlangTestCaseRunReportGeneratorService.class);
        registerEventHandlers(slang);

        List<RuntimeException> exceptions = new ArrayList<>();
        try {
            SlangBuildResults buildResults = slangBuilder.buildSlangContent(projectPath, contentPath, testsPath,
                    testSuites, shouldValidateDescription, bulkRunMode);
            exceptions.addAll(buildResults.getCompilationExceptions());
            if (exceptions.size() > 0) {
                logErrors(exceptions, projectPath);
            }
            IRunTestResults runTestsResults = buildResults.getRunTestsResults();
            Map<String, TestRun> skippedTests = runTestsResults.getSkippedTests();

            if (isNotEmpty(skippedTests)) {
                printSkippedTestsSummary(skippedTests);
            }
            printPassedTests(runTestsResults);
            if (shouldPrintCoverageData) {
                printTestCoverageData(runTestsResults);
            }

            if (isNotEmpty(runTestsResults.getFailedTests())) {
                printBuildFailureSummary(projectPath, runTestsResults);
            } else {
                printBuildSuccessSummary(contentPath, buildResults, runTestsResults);
            }

            generateTestCaseReport(reportGeneratorService, runTestsResults);
            System.exit(isNotEmpty(runTestsResults.getFailedTests()) ? 1 : 0);

        } catch (Throwable e) {
            logErrorsPrefix();
            log.error("Exception: " + e.getMessage());
            logErrorsSuffix(projectPath);
            System.exit(1);
        }
    }

    private static List<String> getDefaultRunModeTestSuites(List<String> testSuites, List<String> testSuitesParallel, List<String> testSuitesSequential) {
        return removeAll(new ArrayList<>(testSuites), union(testSuitesParallel, testSuitesSequential));
    }

    private static void addWarningsForMisconfiguredTestSuites(final TestCaseRunMode testSuiteRunMode, final List<String> testSuites, final List<String> testSuitesSequential,
                                                              final List<String> testSuitesParallel) {
        addWarningForSubsetOfRules(testSuites, testSuitesSequential, TEST_SUITES_SEQUENTIAL);
        addWarningForSubsetOfRules(testSuites, testSuitesParallel, TEST_SUITES_PARALLEL);
        addWarningForUnspecifiedRules(testSuiteRunMode, testSuites, testSuitesSequential, testSuitesParallel);
    }

    private static void addWarningForUnspecifiedRules(final TestCaseRunMode testSuiteRunMode, final List<String> testSuites, final List<String> testSuitesSequential,
                                                      final List<String> testSuitesParallel) {
        List<String> union = union(testSuitesSequential, testSuitesParallel);
        if (!union.containsAll(testSuites)) {
            List<String> copy = new ArrayList<>(testSuites);
            copy.removeAll(union);

            log.info(format(MESSAGE_TEST_SUITES_WITH_UNSPECIFIED_MAPPING, join(copy, LIST_JOINER), testSuiteRunMode.name()));
        }
    }

    private static void addWarningForSubsetOfRules(List<String> testSuites, List<String> testSuitesContained, String key) {
        List<String> intersectWithContained = ListUtils.intersection(testSuites, testSuitesContained);
        if (intersectWithContained.size() != testSuitesContained.size()) {
            List<String> notScheduledForRun = new ArrayList<>(testSuitesContained);
            notScheduledForRun.removeAll(intersectWithContained);
            log.warn(format(MESSAGE_NOT_SCHEDULED_FOR_RUN_RULES, join(notScheduledForRun, LIST_JOINER), key));
        }
    }

    private static List<String> getTestSuitesForKey(Properties runConfigurationProperties, String key) {
        final String valueList = runConfigurationProperties.getProperty(key);
        return StringUtils.isNotEmpty(valueList) ? parseTestSuitesToList(asList(valueList.split(SUITE_LIST_SEPARATOR))) : new ArrayList<String>();

    }

    private static Properties getRunConfigurationProperties(String runConfigPath) {
        try (FileInputStream fileInputStream = new FileInputStream(new File(runConfigPath))) {
            Properties properties = new Properties();
            properties.load(fileInputStream);
            return properties;
        } catch (IOException ioEx) {
            throw new RuntimeException(ioEx);
        }
    }

    private static void logErrors(List<RuntimeException> exceptions, String projectPath) {
        logErrorsPrefix();
        for (RuntimeException runtimeException : exceptions) {
            log.error("Exception: " + runtimeException.getMessage());
        }
        logErrorsSuffix(projectPath);
        System.exit(1);
    }

    private static void logErrorsSuffix(String projectPath) {
        log.error("FAILURE: Validation of slang files for project: \""
                + projectPath + "\" failed.");
        log.error("------------------------------------------------------------");
        log.error("");
    }

    private static void logErrorsPrefix() {
        log.error("");
        log.error("------------------------------------------------------------");
    }

    private static void generateTestCaseReport(SlangTestCaseRunReportGeneratorService reportGeneratorService, IRunTestResults runTestsResults) throws IOException {
        Path reportDirectoryPath = get(getProperty(TEST_CASE_REPORT_LOCATION));
        if (!exists(reportDirectoryPath)) {
            createDirectories(reportDirectoryPath);
        }
        reportGeneratorService.generateReport(runTestsResults, reportDirectoryPath.toString());
    }

    @SuppressWarnings("Duplicates")
    private static void loadUserProperties() {
        try {
            UserConfigurationService userConfigurationService = new UserConfigurationServiceImpl();
            userConfigurationService.loadUserProperties();
        } catch (Exception ex) {
            System.out.println("Error occurred while loading user configuration: " + ex.getMessage());
            ex.printStackTrace();
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
        final List<String> testSuitesArg = ListUtils.defaultIfNull(appArgs.getTestSuites(), new ArrayList<String>());
        return parseTestSuitesToList(testSuitesArg);
    }

    private static List<String> parseTestSuitesToList(List<String> testSuitesArg) {
        List<String> testSuites = new ArrayList<>();
        final String notDefaultTestSuite = NOT_TS + DEFAULT_TESTS;


        boolean containsDefaultTestSuite = false;
        boolean containsNotDefaultTestSuite = false;
        for (String testSuite : testSuitesArg) {
            if (isEmpty(testSuite)) { // Skip empty suites
                continue;
            }

            if (!startsWithIgnoreCase(testSuite, NOT_TS) && !equalsIgnoreCase(testSuite, DEFAULT_TESTS)) { // every normal test suite except default
                if (!isSuitePresent(testSuites, testSuite)) {
                    testSuites.add(testSuite);
                }
            } else if (!containsNotDefaultTestSuite && equalsIgnoreCase(testSuite, DEFAULT_TESTS)) {   // !default test suite
                containsDefaultTestSuite = true;
            } else if (!containsNotDefaultTestSuite && equalsIgnoreCase(testSuite, notDefaultTestSuite)) { // default test suite
                containsNotDefaultTestSuite = true;
            }
        }

        // Add the default test suite once
        if (!containsNotDefaultTestSuite && containsDefaultTestSuite) {
            testSuites.add(DEFAULT_TESTS);
        }
        return testSuites;
    }

    private static boolean isSuitePresent(final List<String> crtList, final String testSuite) {
        for (String crtSuite : crtList) {
            if (equalsIgnoreCase(testSuite, crtSuite)) {
                return true;
            }
        }
        return false;
    }

    private static String parseTestTimeout(ApplicationArgs appArgs) {
        Map<String, String> dynamicArgs = appArgs.getDynamicParams();
        return dynamicArgs.get(TEST_CASE_TIMEOUT_IN_MINUTES_KEY);
    }

    private static int parseThreadCountArg(ApplicationArgs appArgs, boolean isParallel) {
        if (!isParallel) {
            return 1;
        } else {
            int defaultThreadCount = Runtime.getRuntime().availableProcessors();
            String threadCountErrorMessage = format("Thread count is misconfigured. The thread count value must be a positive integer less than or equal to %d. Using %d threads.", MAX_THREADS_TEST_RUNNER, defaultThreadCount);
            try {
                String sThreadCount = appArgs.getThreadCount();
                if (sThreadCount != null) {
                    int threadCount = parseInt(sThreadCount);
                    if ((threadCount > 0) && (threadCount <= MAX_THREADS_TEST_RUNNER)) {
                        return threadCount;
                    } else {
                        log.warn(threadCountErrorMessage);
                    }
                }
            } catch (NumberFormatException nfEx) {
                log.warn(threadCountErrorMessage);
            }
            return defaultThreadCount;
        }
    }

    private static void printBuildSuccessSummary(String contentPath, SlangBuildResults buildResults, IRunTestResults runTestsResults) {
        log.info("");
        log.info("------------------------------------------------------------");
        log.info("BUILD SUCCESS");
        log.info("------------------------------------------------------------");
        log.info("Found " + buildResults.getNumberOfCompiledSources()
                + " slang files under directory: \"" + contentPath + "\" and all are valid.");
        printNumberOfPassedAndSkippedTests(runTestsResults);
        log.info("");
    }

    private static void printNumberOfPassedAndSkippedTests(IRunTestResults runTestsResults) {
        log.info(runTestsResults.getPassedTests().size() + " test cases passed");
        Map<String, TestRun> skippedTests = runTestsResults.getSkippedTests();
        if (skippedTests.size() > 0) {
            log.info(skippedTests.size() + " test cases skipped");
        }
    }

    private static void printPassedTests(IRunTestResults runTestsResults) {
        if (runTestsResults.getPassedTests().size() > 0) {
            log.info("------------------------------------------------------------");
            log.info("Following " + runTestsResults.getPassedTests().size() + " test cases passed:");
            for (Map.Entry<String, TestRun> passedTest : runTestsResults.getPassedTests().entrySet()) {
                String testCaseName = passedTest.getValue().getTestCase().getName();
                log.info("- " + testCaseName.replaceAll("\n", "\n\t"));
            }
        }
    }

    private static void printBuildFailureSummary(String projectPath, IRunTestResults runTestsResults) {
        printNumberOfPassedAndSkippedTests(runTestsResults);
        Map<String, TestRun> failedTests = runTestsResults.getFailedTests();
        logErrorsPrefix();
        log.error("BUILD FAILURE");
        log.error("------------------------------------------------------------");
        log.error("CloudSlang build for repository: \"" + projectPath + "\" failed due to failed tests.");
        log.error("Following " + failedTests.size() + " tests failed:");
        for (Map.Entry<String, TestRun> failedTest : failedTests.entrySet()) {
            String failureMessage = failedTest.getValue().getMessage();
            log.error("- " + failureMessage.replaceAll("\n", "\n\t"));
        }
        log.error("");
    }

    private static void printSkippedTestsSummary(Map<String, TestRun> skippedTests) {
        log.info("");
        log.info("------------------------------------------------------------");
        log.info("Following " + skippedTests.size() + " tests were skipped:");
        for (Map.Entry<String, TestRun> skippedTest : skippedTests.entrySet()) {
            String message = skippedTest.getValue().getMessage();
            log.info("- " + message.replaceAll("\n", "\n\t"));
        }
    }

    private static void printTestCoverageData(IRunTestResults runTestsResults) {
        printCoveredExecutables(runTestsResults.getCoveredExecutables());
        printUncoveredExecutables(runTestsResults.getUncoveredExecutables());
        int coveredExecutablesSize = runTestsResults.getCoveredExecutables().size();
        int uncoveredExecutablesSize = runTestsResults.getUncoveredExecutables().size();
        int totalNumberOfExecutables = coveredExecutablesSize + uncoveredExecutablesSize;
        double coveragePercentage = (double) coveredExecutablesSize / (double) totalNumberOfExecutables * 100;
        log.info("");
        log.info("------------------------------------------------------------");
        log.info(((int) coveragePercentage) + "% of the content has tests");
        log.info("Out of " + totalNumberOfExecutables + " executables, " + coveredExecutablesSize + " executables have tests");
    }

    private static void printCoveredExecutables(Set<String> coveredExecutables) {
        log.info("");
        log.info("------------------------------------------------------------");
        log.info("Following " + coveredExecutables.size() + " executables have tests:");
        for (String executable : coveredExecutables) {
            log.info("- " + executable);
        }
    }

    private static void printUncoveredExecutables(Set<String> uncoveredExecutables) {
        log.info("");
        log.info("------------------------------------------------------------");
        log.info("Following " + uncoveredExecutables.size() + " executables do not have tests:");
        for (String executable : uncoveredExecutables) {
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

}
