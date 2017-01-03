/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
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
import io.cloudslang.lang.logging.LoggingService;
import io.cloudslang.lang.logging.LoggingServiceImpl;
import io.cloudslang.lang.tools.build.commands.ApplicationArgs;
import io.cloudslang.lang.tools.build.tester.IRunTestResults;
import io.cloudslang.lang.tools.build.tester.TestRun;
import io.cloudslang.lang.tools.build.tester.parallel.report.SlangTestCaseRunReportGeneratorService;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import io.cloudslang.lang.tools.build.tester.runconfiguration.TestRunInfoService;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.events.ScoreEventListener;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static io.cloudslang.lang.tools.build.ArgumentProcessorUtils.getBooleanFromPropertiesWithDefault;
import static io.cloudslang.lang.tools.build.ArgumentProcessorUtils.getEnumInstanceFromPropertiesWithDefault;
import static io.cloudslang.lang.tools.build.ArgumentProcessorUtils.getIntFromPropertiesWithDefaultAndRange;
import static io.cloudslang.lang.tools.build.ArgumentProcessorUtils.getListForPrint;
import static io.cloudslang.lang.tools.build.SlangBuildMain.BulkRunMode.ALL_PARALLEL;
import static io.cloudslang.lang.tools.build.SlangBuildMain.BulkRunMode.ALL_SEQUENTIAL;
import static io.cloudslang.lang.tools.build.SlangBuildMain.BulkRunMode.POSSIBLY_MIXED;
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
import static java.util.Locale.ENGLISH;
import static org.apache.commons.collections4.ListUtils.removeAll;
import static org.apache.commons.collections4.ListUtils.union;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isEmpty;


public class SlangBuildMain {
    public static final String DEFAULT_TESTS = "default";

    private static final String TEST_CASE_REPORT_LOCATION = "cloudslang.test.case.report.location";
    private static final String CONTENT_DIR = File.separator + "content";
    private static final String TEST_DIR = File.separator + "test";

    private static final Logger log = Logger.getLogger(SlangBuildMain.class);
    private static final int MAX_THREADS_TEST_RUNNER = 32;
    private static final String MESSAGE_NOT_SCHEDULED_FOR_RUN_RULES = "Rules '%s' defined in '%s' key " +
            "are not scheduled for run.";

    private static final String MESSAGE_TEST_SUITES_WITH_UNSPECIFIED_MAPPING = "Test suites '%s' have " +
            "unspecified mapping. They will run in '%s' mode.";
    private static final String PROPERTIES_FILE_EXTENSION = "properties";

    private static final String DID_NOT_DETECT_RUN_CONFIGURATION_PROPERTIES_FILE = "Did not detect run " +
            "configuration properties file at path '%s'. " +
            "Check that the path you are using is an absolute path. " +
            "Check that the path separator is '\\\\' for Windows, or '/' for Linux.";
    private static final String NEW_LINE = System.lineSeparator();
    private static final String MESSAGE_BOTH_PARALLEL_AND_SEQUENTIAL_EXECUTION = "The '%s' suites are configured for " +
            "both parallel and sequential execution." +
            " Each test suite must have only one execution mode (parallel or sequential).";
    private static final String MESSAGE_ERROR_LOADING_SMART_MODE_CONFIG_FILE = "Error loading smart " +
            "mode configuration file:";
    private static final String LOG4J_CONFIGURATION_KEY = "log4j.configuration";
    private static final String LOG4J_ERROR_PREFIX = "log4j: error loading log4j properties file.";
    private static final String LOG4J_ERROR_SUFFIX = "Using default configuration.";
    private static final String APP_HOME_KEY = "app.home";

    // This class is a used in the interaction with the run configuration property file
    static class RunConfigurationProperties {
        static final String TEST_COVERAGE = "test.coverage";
        static final String TEST_PARALLEL_THREAD_COUNT = "test.parallel.thread.count";
        static final String TEST_SUITES_TO_RUN = "test.suites.active";
        static final String TEST_SUITES_PARALLEL = "test.suites.parallel";
        static final String TEST_SUITES_SEQUENTIAL = "test.suites.sequential";
        static final String TEST_SUITES_RUN_UNSPECIFIED = "test.suites.run.mode.unspecified";
    }

    // The possible ways to execute a test case
    public enum TestCaseRunMode {
        PARALLEL,
        SEQUENTIAL
    }

    // The typical configuration on how to configure the run of all tests as a bulk
    public enum BulkRunMode {
        ALL_PARALLEL,
        ALL_SEQUENTIAL,
        POSSIBLY_MIXED
    }

    // The possible ways to run tests: everything or the tests affected by current changelist
    public enum BuildMode {
        BASIC,
        CHANGED
    }

    public static void main(String[] args) {
        loadUserProperties();
        configureLog4j();

        ApplicationArgs appArgs = new ApplicationArgs();
        parseArgs(args, appArgs);
        String projectPath = parseProjectPathArg(appArgs);
        final String contentPath = defaultIfEmpty(appArgs.getContentRoot(), projectPath + CONTENT_DIR);
        final String testsPath = defaultIfEmpty(appArgs.getTestRoot(), projectPath + TEST_DIR);
        List<String> testSuites = parseTestSuites(appArgs);
        boolean shouldPrintCoverageData = appArgs.shouldOutputCoverage();
        boolean runTestsInParallel = appArgs.isParallel();
        int threadCount = parseThreadCountArg(appArgs, runTestsInParallel);
        String testCaseTimeout = parseTestTimeout(appArgs);
        setProperty(TEST_CASE_TIMEOUT_IN_MINUTES_KEY, valueOf(testCaseTimeout));
        final boolean shouldValidateDescription = appArgs.shouldValidateDescription();
        final boolean shouldValidateCheckstyle = appArgs.shouldValidateCheckstyle();
        String runConfigPath = FilenameUtils.normalize(appArgs.getRunConfigPath());

        BuildMode buildMode = null;
        Set<String> changedFiles = null;
        try {
            String smartModePath = appArgs.getChangesOnlyConfigPath();
            if (StringUtils.isEmpty(smartModePath)) {
                buildMode = BuildMode.BASIC;
                changedFiles = new HashSet<>();
                printBuildModeInfo(buildMode);
            } else {
                buildMode = BuildMode.CHANGED;
                changedFiles = readChangedFilesFromSource(smartModePath);
                printBuildModeInfo(buildMode);
            }
        } catch (Exception ex) {
            log.error("Exception: " + ex.getMessage());
            System.exit(1);
        }

        // Override with the values from the file if configured
        List<String> testSuitesParallel = new ArrayList<>();
        List<String> testSuitesSequential = new ArrayList<>();
        BulkRunMode bulkRunMode = runTestsInParallel ? ALL_PARALLEL : ALL_SEQUENTIAL;

        TestCaseRunMode unspecifiedTestSuiteRunMode = runTestsInParallel ?
                TestCaseRunMode.PARALLEL : TestCaseRunMode.SEQUENTIAL;
        if (get(runConfigPath).isAbsolute() && isRegularFile(get(runConfigPath), NOFOLLOW_LINKS) &&
                equalsIgnoreCase(PROPERTIES_FILE_EXTENSION, FilenameUtils.getExtension(runConfigPath))) {
            Properties runConfigurationProperties = ArgumentProcessorUtils.getPropertiesFromFile(runConfigPath);
            shouldPrintCoverageData = getBooleanFromPropertiesWithDefault(TEST_COVERAGE, shouldPrintCoverageData,
                    runConfigurationProperties);
            threadCount = getIntFromPropertiesWithDefaultAndRange(TEST_PARALLEL_THREAD_COUNT,
                    Runtime.getRuntime().availableProcessors(),
                    runConfigurationProperties, 1, MAX_THREADS_TEST_RUNNER + 1);
            testSuites = getTestSuitesForKey(runConfigurationProperties, TEST_SUITES_TO_RUN);
            testSuitesParallel = getTestSuitesForKey(runConfigurationProperties, TEST_SUITES_PARALLEL);
            testSuitesSequential = getTestSuitesForKey(runConfigurationProperties, TEST_SUITES_SEQUENTIAL);
            addErrorIfSameTestSuiteIsInBothParallelOrSequential(testSuitesParallel, testSuitesSequential);
            unspecifiedTestSuiteRunMode = getEnumInstanceFromPropertiesWithDefault(TEST_SUITES_RUN_UNSPECIFIED,
                    unspecifiedTestSuiteRunMode, runConfigurationProperties);
            addWarningsForMisconfiguredTestSuites(unspecifiedTestSuiteRunMode, testSuites,
                    testSuitesSequential, testSuitesParallel);
            bulkRunMode = POSSIBLY_MIXED;
        } else { // Warn when file is misconfigured, relative path, file does not exist or is not a properties file
            log.info(format(DID_NOT_DETECT_RUN_CONFIGURATION_PROPERTIES_FILE, runConfigPath));
        }

        String testCaseReportLocation = getProperty(TEST_CASE_REPORT_LOCATION);
        if (StringUtils.isBlank(testCaseReportLocation)) {
            log.info("Test case report location property [" + TEST_CASE_REPORT_LOCATION +
                    "] is not defined. Report will be skipped.");
        }

        // Setting thread count for visibility in ParallelTestCaseExecutorService
        setProperty(SLANG_TEST_RUNNER_THREAD_COUNT, valueOf(threadCount));

        log.info(NEW_LINE + "------------------------------------------------------------");
        log.info("Building project: " + projectPath);
        log.info("Content root is at: " + contentPath);
        log.info("Test root is at: " + testsPath);
        log.info("Active test suites are: " + getListForPrint(testSuites));
        log.info("Parallel run mode is configured for test suites: " +
                getListForPrint(testSuitesParallel));
        log.info("Sequential run mode is configured for test suites: " +
                getListForPrint(testSuitesSequential));
        log.info("Default run mode '" + unspecifiedTestSuiteRunMode.name().toLowerCase() +
                "' is configured for test suites: " +
                getListForPrint(getDefaultRunModeTestSuites(testSuites, testSuitesParallel, testSuitesSequential)));

        log.info("Bulk run mode for tests: " + getBulkModeForPrint(bulkRunMode));

        log.info("Print coverage data: " + valueOf(shouldPrintCoverageData));
        log.info("Validate description: " + valueOf(shouldValidateDescription));
        log.info("Validate checkstyle: " + valueOf(shouldValidateCheckstyle));
        log.info("Thread count: " + threadCount);
        log.info("Test case timeout in minutes: " + (isEmpty(testCaseTimeout) ?
                valueOf(MAX_TIME_PER_TESTCASE_IN_MINUTES) : testCaseTimeout));

        log.info(NEW_LINE + "Loading...");

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/testRunnerContext.xml");
        context.registerShutdownHook();
        SlangBuilder slangBuilder = context.getBean(SlangBuilder.class);
        LoggingService loggingService = context.getBean(LoggingServiceImpl.class);
        Slang slang = context.getBean(Slang.class);

        try {

            updateTestSuiteMappings(context.getBean(TestRunInfoService.class), testSuitesParallel,
                    testSuitesSequential, testSuites, unspecifiedTestSuiteRunMode);

            registerEventHandlers(slang);

            List<RuntimeException> exceptions = new ArrayList<>();

            SlangBuildResults buildResults =
                    slangBuilder.buildSlangContent(projectPath, contentPath, testsPath, testSuites,
                            shouldValidateDescription, shouldValidateCheckstyle, bulkRunMode, buildMode, changedFiles);
            exceptions.addAll(buildResults.getCompilationExceptions());
            if (exceptions.size() > 0) {
                logErrors(exceptions, projectPath, loggingService);
            }
            IRunTestResults runTestsResults = buildResults.getRunTestsResults();
            Map<String, TestRun> skippedTests = runTestsResults.getSkippedTests();

            if (isNotEmpty(skippedTests)) {
                printSkippedTestsSummary(skippedTests, loggingService);
            }
            printPassedTests(runTestsResults, loggingService);
            if (shouldPrintCoverageData) {
                printTestCoverageData(runTestsResults, loggingService);
            }

            if (isNotEmpty(runTestsResults.getFailedTests())) {
                printBuildFailureSummary(projectPath, runTestsResults, loggingService);
            } else {
                printBuildSuccessSummary(contentPath, buildResults, runTestsResults, loggingService);
            }
            loggingService.waitForAllLogTasksToFinish();

            generateTestCaseReport(
                    context.getBean(SlangTestCaseRunReportGeneratorService.class),
                    runTestsResults,
                    testCaseReportLocation
            );
            System.exit(isNotEmpty(runTestsResults.getFailedTests()) ? 1 : 0);

        } catch (Throwable e) {
            logErrorsPrefix(loggingService);
            loggingService.logEvent(Level.ERROR, "Exception: " + e.getMessage());
            logErrorsSuffix(projectPath, loggingService);
            loggingService.waitForAllLogTasksToFinish();
            System.exit(1);
        }
    }

    private static void configureLog4j() {
        String configFilename = System.getProperty(LOG4J_CONFIGURATION_KEY);
        String errorMessage = null;

        try {
            if (StringUtils.isEmpty(configFilename)) {
                errorMessage = "Config file name is empty.";
            } else {
                String normalizedPath = FilenameUtils.normalize(configFilename);
                if (normalizedPath == null) {
                    errorMessage = "Normalized config file path is null.";
                } else if (!isUnderAppHome(normalizedPath, getNormalizedApplicationHome())) {
                    errorMessage = "Normalized config file path[" + normalizedPath + "] " +
                            "is not under application home directory";
                } else {
                    if (!isRegularFile(get(normalizedPath), NOFOLLOW_LINKS)) {
                        errorMessage = "Normalized config file path[" + normalizedPath + "]" +
                                " does not lead to a regular file.";
                    } else {
                        Properties log4jProperties = new Properties();
                        try (InputStream log4jInputStream = SlangBuildMain.class.getResourceAsStream(normalizedPath)) {
                            log4jProperties.load(log4jInputStream);
                            PropertyConfigurator.configure(log4jProperties);
                        }
                    }
                }
            }
        } catch (IOException | RuntimeException ex) {
            errorMessage = ex.getMessage();
        }

        if (StringUtils.isNotEmpty(errorMessage)) {
            System.out.printf("%s%n\t%s%n\t%s%n", LOG4J_ERROR_PREFIX, errorMessage, LOG4J_ERROR_SUFFIX);
        }
    }

    private static boolean isUnderAppHome(String normalizedFilePath, String normalizedAppHome) {
        return normalizedFilePath.startsWith(normalizedAppHome);
    }

    private static String getNormalizedApplicationHome() {
        String appHome = System.getProperty(APP_HOME_KEY);
        if (StringUtils.isEmpty(appHome)) {
            throw new RuntimeException(APP_HOME_KEY + " system property is empty");
        }
        String normalizedAppHome = FilenameUtils.normalize(appHome);
        if (normalizedAppHome == null) {
            throw new RuntimeException("Normalized app home path is null.");
        }
        return normalizedAppHome;
    }

    private static void printBuildModeInfo(BuildMode buildMode) {
        log.info("Build mode set to: " + buildMode);
    }

    private static Set<String> readChangedFilesFromSource(String filePath) throws IOException {
        String normalizedPath = FilenameUtils.normalize(filePath);
        if (!get(normalizedPath).isAbsolute()) {
            throw new RuntimeException(MESSAGE_ERROR_LOADING_SMART_MODE_CONFIG_FILE +
                    " Path[" + normalizedPath + "] is not an absolute path.");
        }
        if (!isRegularFile(get(normalizedPath), NOFOLLOW_LINKS)) {
            throw new RuntimeException(MESSAGE_ERROR_LOADING_SMART_MODE_CONFIG_FILE +
                    " Path[" + normalizedPath + "] does not lead to a regular file.");
        }
        return ArgumentProcessorUtils.loadChangedItems(normalizedPath);
    }

    private static void addErrorIfSameTestSuiteIsInBothParallelOrSequential(List<String> testSuitesParallel,
                                                                            List<String> testSuitesSequential) {
        final List<String> intersection = ListUtils.intersection(testSuitesParallel, testSuitesSequential);
        if (!intersection.isEmpty()) {
            final String message = String.format(MESSAGE_BOTH_PARALLEL_AND_SEQUENTIAL_EXECUTION,
                    getListForPrint(intersection));
            log.error(message);
            throw new IllegalStateException();
        }
    }


    /**
     * @param bulkRunMode the mode to configure the run of all tests
     * @return String friendly version for print to the log or console
     */
    private static String getBulkModeForPrint(final BulkRunMode bulkRunMode) {
        return bulkRunMode.toString().replace("_", " ").toLowerCase(ENGLISH);
    }

    /**
     * @param testRunInfoService          the service responsible for managing run information
     * @param parallelSuites              the suite names to be executed in parallel
     * @param sequentialSuites            the suite names to be executed in sequential manner
     * @param activeSuites                the suite names that are active
     * @param unspecifiedTestSuiteRunMode the default run mode for suites that don't explicitly mention a run mode.
     */
    private static void updateTestSuiteMappings(final TestRunInfoService testRunInfoService,
                                                final List<String> parallelSuites,
                                                final List<String> sequentialSuites, final List<String> activeSuites,
                                                final TestCaseRunMode unspecifiedTestSuiteRunMode) {
        testRunInfoService.setRunModeForTestSuites(parallelSuites, TestCaseRunMode.PARALLEL);
        testRunInfoService.setRunModeForTestSuites(sequentialSuites, TestCaseRunMode.SEQUENTIAL);
        testRunInfoService.setRunModeForTestSuites(
                getDefaultRunModeTestSuites(activeSuites, parallelSuites, sequentialSuites),
                unspecifiedTestSuiteRunMode);
    }

    /**
     * @param activeSuites     the suite names that are active
     * @param parallelSuites   the suite names to be executed in parallel
     * @param sequentialSuites the suite names to be executed in sequential manner
     * @return
     */
    private static List<String> getDefaultRunModeTestSuites(final List<String> activeSuites,
                                                            final List<String> parallelSuites,
                                                            final List<String> sequentialSuites) {
        return removeAll(new ArrayList<>(activeSuites), union(parallelSuites, sequentialSuites));
    }

    /**
     * @param unspecifiedTestSuiteRunMode the default run mode for suites that don't explicitly mention a run mode.
     * @param activeSuites                the suite names that are active
     * @param sequentialSuites            the suite names to be executed in sequential manner
     * @param parallelSuites              the suite names to be executed in parallel
     */
    private static void addWarningsForMisconfiguredTestSuites(final TestCaseRunMode unspecifiedTestSuiteRunMode,
                                                              final List<String> activeSuites,
                                                              final List<String> sequentialSuites,
                                                              final List<String> parallelSuites) {
        addWarningForSubsetOfRules(activeSuites, sequentialSuites, TEST_SUITES_SEQUENTIAL);
        addWarningForSubsetOfRules(activeSuites, parallelSuites, TEST_SUITES_PARALLEL);
        addInformativeNoteForUnspecifiedRules(unspecifiedTestSuiteRunMode, activeSuites,
                sequentialSuites, parallelSuites);
    }

    /**
     * Displays an informative message in case there is at least one test suite left for default run mode.
     *
     * @param unspecifiedTestSuiteRunMode the default run mode for suites that don't explicitly mention a run mode.
     * @param activeSuites                the suite names that are active
     * @param sequentialSuites            the suite names to be executed in sequential manner
     * @param parallelSuites              the suite names to be executed in parallel
     */
    private static void addInformativeNoteForUnspecifiedRules(final TestCaseRunMode unspecifiedTestSuiteRunMode,
                                                              final List<String> activeSuites,
                                                              final List<String> sequentialSuites,
                                                              final List<String> parallelSuites) {
        List<String> union = union(sequentialSuites, parallelSuites);
        if (!union.containsAll(activeSuites)) {
            List<String> copy = new ArrayList<>(activeSuites);
            copy.removeAll(union);

            log.info(format(MESSAGE_TEST_SUITES_WITH_UNSPECIFIED_MAPPING,
                    getListForPrint(copy), unspecifiedTestSuiteRunMode.name()));
        }
    }

    /**
     * Displays a warning message for test suites that have rules defined for sequential or parallel execution
     * but are not in active test suites.
     *
     * @param testSuites          suite names contained in 'container' suites
     * @param testSuitesContained suite names contained in 'contained' suites
     * @param key                 run configuration property key
     */
    private static void addWarningForSubsetOfRules(List<String> testSuites, List<String> testSuitesContained,
                                                   String key) {
        List<String> intersectWithContained = ListUtils.intersection(testSuites, testSuitesContained);
        if (intersectWithContained.size() != testSuitesContained.size()) {
            List<String> notScheduledForRun = new ArrayList<>(testSuitesContained);
            notScheduledForRun.removeAll(intersectWithContained);
            log.warn(format(MESSAGE_NOT_SCHEDULED_FOR_RUN_RULES, getListForPrint(notScheduledForRun), key));
        }
    }

    /**
     * Returns the names of the suites from the run configuration java.util.Properties object at a certain key.
     *
     * @param runConfigurationProperties
     * @param key
     * @return
     */
    private static List<String> getTestSuitesForKey(Properties runConfigurationProperties, String key) {
        final String valueList = runConfigurationProperties.getProperty(key);
        return ArgumentProcessorUtils.parseTestSuitesToList(valueList);
    }

    private static void logErrors(List<RuntimeException> exceptions, String projectPath,
                                  final LoggingService loggingService) {
        logErrorsPrefix(loggingService);
        for (RuntimeException runtimeException : exceptions) {
            loggingService.logEvent(Level.ERROR, "Exception: " + runtimeException.getMessage());
        }
        logErrorsSuffix(projectPath, loggingService);
        loggingService.waitForAllLogTasksToFinish();
        System.exit(1);
    }

    private static void logErrorsSuffix(String projectPath, final LoggingService loggingService) {
        loggingService.logEvent(Level.ERROR, "FAILURE: Validation of slang files for project: \"" +
                projectPath + "\" failed.");
        loggingService.logEvent(Level.ERROR, "------------------------------------------------------------");
        loggingService.logEvent(Level.ERROR, "");
    }

    private static void logErrorsPrefix(final LoggingService loggingService) {
        loggingService.logEvent(Level.ERROR, "");
        loggingService.logEvent(Level.ERROR, "------------------------------------------------------------");
    }

    private static void generateTestCaseReport(
            SlangTestCaseRunReportGeneratorService reportGeneratorService,
            IRunTestResults runTestsResults,
            String testCaseReportLocation) throws IOException {
        if (StringUtils.isNotBlank(testCaseReportLocation)) {
            Path reportDirectoryPath = get(testCaseReportLocation);
            if (!exists(reportDirectoryPath)) {
                createDirectories(reportDirectoryPath);
            }
            reportGeneratorService.generateReport(runTestsResults, reportDirectoryPath.toString());
        }
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
            JCommander commander = new JCommander(appArgs, args);
            if (appArgs.isHelp()) {
                commander.usage();
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
        return ArgumentProcessorUtils.parseTestSuitesToList(testSuitesArg);
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
            String threadCountErrorMessage = format("Thread count is misconfigured. The thread count value must be a " +
                            "positive integer less than or equal to %d. Using %d threads.",
                    MAX_THREADS_TEST_RUNNER, defaultThreadCount);
            try {
                String stringThreadCount = appArgs.getThreadCount();
                if (stringThreadCount != null) {
                    int threadCount = parseInt(stringThreadCount);
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

    private static void printBuildSuccessSummary(String contentPath, SlangBuildResults buildResults,
                                                 IRunTestResults runTestsResults,
                                                 final LoggingService loggingService) {
        loggingService.logEvent(Level.INFO, "");
        loggingService.logEvent(Level.INFO, "------------------------------------------------------------");
        loggingService.logEvent(Level.INFO, "BUILD SUCCESS");
        loggingService.logEvent(Level.INFO, "------------------------------------------------------------");
        loggingService.logEvent(Level.INFO, "Found " + buildResults.getNumberOfCompiledSources() +
                " slang files under directory: \"" + contentPath + "\" and all are valid.");
        printNumberOfPassedAndSkippedTests(runTestsResults, loggingService);
        loggingService.logEvent(Level.INFO, "");
    }

    private static void printNumberOfPassedAndSkippedTests(IRunTestResults runTestsResults,
                                                           final LoggingService loggingService) {
        loggingService.logEvent(Level.INFO, runTestsResults.getPassedTests().size() + " test cases passed");
        Map<String, TestRun> skippedTests = runTestsResults.getSkippedTests();
        if (skippedTests.size() > 0) {
            loggingService.logEvent(Level.INFO, skippedTests.size() + " test cases skipped");
        }
    }

    private static void printPassedTests(IRunTestResults runTestsResults, final LoggingService loggingService) {
        if (runTestsResults.getPassedTests().size() > 0) {
            loggingService.logEvent(Level.INFO, "------------------------------------------------------------");
            loggingService.logEvent(Level.INFO, "Following " + runTestsResults.getPassedTests().size() +
                    " test cases passed:");
            for (Map.Entry<String, TestRun> passedTest : runTestsResults.getPassedTests().entrySet()) {
                String testCaseReference = SlangTestCase.generateTestCaseReference(passedTest.getValue().getTestCase());
                loggingService.logEvent(Level.INFO, "- " + testCaseReference.replaceAll("\n", "\n\t"));
            }
        }
    }

    private static void printBuildFailureSummary(String projectPath, IRunTestResults runTestsResults,
                                                 final LoggingService loggingService) {
        printNumberOfPassedAndSkippedTests(runTestsResults, loggingService);
        final Map<String, TestRun> failedTests = runTestsResults.getFailedTests();
        logErrorsPrefix(loggingService);
        loggingService.logEvent(Level.ERROR, "BUILD FAILURE");
        loggingService.logEvent(Level.ERROR, "------------------------------------------------------------");
        loggingService.logEvent(Level.ERROR, "CloudSlang build for repository: \"" + projectPath +
                "\" failed due to failed tests.");
        loggingService.logEvent(Level.ERROR, "Following " + failedTests.size() + " tests failed:");
        for (Map.Entry<String, TestRun> failedTest : failedTests.entrySet()) {
            String failureMessage = failedTest.getValue().getMessage();
            loggingService.logEvent(Level.ERROR, "- " + failureMessage.replaceAll("\n", "\n\t"));
        }
        loggingService.logEvent(Level.ERROR, "");
    }

    private static void printSkippedTestsSummary(Map<String, TestRun> skippedTests,
                                                 final LoggingService loggingService) {
        loggingService.logEvent(Level.INFO, "");
        loggingService.logEvent(Level.INFO, "------------------------------------------------------------");
        loggingService.logEvent(Level.INFO, "Following " + skippedTests.size() + " tests were skipped:");
        for (Map.Entry<String, TestRun> skippedTest : skippedTests.entrySet()) {
            String message = skippedTest.getValue().getMessage();
            loggingService.logEvent(Level.INFO, "- " + message.replaceAll("\n", "\n\t"));
        }
    }

    private static void printTestCoverageData(IRunTestResults runTestsResults, final LoggingService loggingService) {
        printCoveredExecutables(runTestsResults.getCoveredExecutables(), loggingService);
        printUncoveredExecutables(runTestsResults.getUncoveredExecutables(), loggingService);
        int coveredExecutablesSize = runTestsResults.getCoveredExecutables().size();
        int uncoveredExecutablesSize = runTestsResults.getUncoveredExecutables().size();
        int totalNumberOfExecutables = coveredExecutablesSize + uncoveredExecutablesSize;
        double coveragePercentage = (double) coveredExecutablesSize / (double) totalNumberOfExecutables * 100;
        loggingService.logEvent(Level.INFO, "");
        loggingService.logEvent(Level.INFO, "------------------------------------------------------------");
        loggingService.logEvent(Level.INFO, ((int) coveragePercentage) + "% of the content has tests");
        loggingService.logEvent(Level.INFO, "Out of " + totalNumberOfExecutables + " executables, " +
                coveredExecutablesSize + " executables have tests");
    }

    private static void printCoveredExecutables(Set<String> coveredExecutables, final LoggingService loggingService) {
        loggingService.logEvent(Level.INFO, "");
        loggingService.logEvent(Level.INFO, "------------------------------------------------------------");
        loggingService.logEvent(Level.INFO, "Following " + coveredExecutables.size() + " executables have tests:");
        for (String executable : coveredExecutables) {
            loggingService.logEvent(Level.INFO, "- " + executable);
        }
    }

    private static void printUncoveredExecutables(Set<String> uncoveredExecutables,
                                                  final LoggingService loggingService) {
        loggingService.logEvent(Level.INFO, "");
        loggingService.logEvent(Level.INFO, "------------------------------------------------------------");
        loggingService.logEvent(Level.INFO, "Following " + uncoveredExecutables.size() +
                " executables do not have tests:");
        for (String executable : uncoveredExecutables) {
            loggingService.logEvent(Level.INFO, "- " + executable);
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
