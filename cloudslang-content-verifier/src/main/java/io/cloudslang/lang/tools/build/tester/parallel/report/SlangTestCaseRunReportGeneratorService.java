/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.tester.parallel.report;


import io.cloudslang.lang.tools.build.tester.IRunTestResults;
import io.cloudslang.lang.tools.build.tester.TestRun;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import org.apache.commons.io.FileUtils;
import org.rendersnake.HtmlAttributes;
import org.rendersnake.HtmlCanvas;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Joiner.on;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ENGLISH;

/**
 * Simple Html Report for Slang Test Cases
 */
public class SlangTestCaseRunReportGeneratorService {

    // Test case table column name
    static final String TEST_NAME = "Test Case Name";
    static final String TEST_SUITE = "Test Case Suites";
    static final String TEST_STATUS = "Test Case Status";
    static final String TEST_FLOW_PATH = "Test Case Flow Path";
    static final String TEST_DESCRIPTION = "Test Case Description";
    static final String OUTPUTS = "Test Case Outputs";
    static final String EXCEPTION_OR_MESSAGE = "Test Case Exception/Message";

    private static final String SEPARATOR = ",";
    private static final String SLASH = "/";
    private static final String FORMATTER_STRING = "_%s";

    // Resources
    private static final String CURRENT_DIRECTORY = "./";
    private static final String CLASSPATH = "classpath:";
    private static final String TEST_CASE_REPORT_NAME = "test-case-report";
    private static final String RES = "res";
    private static final String REPORT = "report";
    private static final String IMAGES = "imgs";
    private static final String CLOUD_SLANG_LOGO_PNG = "CloudSlang_logo.png";
    static final String BASIC_REPORT_CSS = "basic_report.css";
    static final String PIECHART_JS = "piechart.js";
    private static final String REPORT_EXTENSION = ".html";

    // Links or script href
    private static final String HTTP_CLOUD_SLANG_IO = "http://cloudslang.io/";
    static final String GOOGLE_CHARTS_URL = "https://www.gstatic.com/charts/loader.js";

    // Css classes and element ids
    private static final String PIECHART_ID = "piechart";
    public static final String HEADER_BAR_ID = "header-bar";
    private static final String HIDDEN_CLASS = "hidden";
    private static final String TEST_SUMMARY_CLASS = "test-summary";
    static final String TEST_CASE_REPORT = "Test Case Report";
    private static final String STATUS_CSS_CLASS = "status";
    private static final String TEST_SUITES_CSS_CLASS = "test-suite";
    private static final String TABLE_CLASS = "table";
    public static final String REPORT_TITLE_CLASS = "report-title";

    private static final String CLOUD_SLANG_LOGO_ALT = "CloudSlang Logo";
    private static final String BLANK = "_blank";

    public static final String PASSED_TESTS = "%d passed test cases";
    public static final String FAILED_TESTS = "%d failed test cases";
    public static final String SKIPPED_TESTS = "%d skipped test cases";
    static final String PASSED = "Passed";
    static final String FAILED = "Failed";
    static final String SKIPPED = "Skipped";


    public void generateReport(IRunTestResults iRunTestResults, String reportDirectory) throws IOException {
        String reportFileName = String.format(TEST_CASE_REPORT_NAME + FORMATTER_STRING +
                REPORT_EXTENSION, valueOf(currentTimeMillis()));
        Path path = Paths.get(reportDirectory, reportFileName);

        try (Writer writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(path.toFile(), false), UTF_8)))) {
            HtmlCanvas reportPage = new HtmlCanvas(writer);

            HtmlCanvas reportPageHtml = reportPage.html();
            appendReportPageHead(reportPageHtml);

            HtmlCanvas reportPageBody = reportPageHtml.body();
            createResourcesFolder(reportDirectory);
            copyResources(reportDirectory);

            generateHeader(reportPageBody, CURRENT_DIRECTORY + RES + SLASH + CLOUD_SLANG_LOGO_PNG);
            generatePiechart(reportPageBody, iRunTestResults);
            generateTestCaseReportTable(reportPageBody, iRunTestResults);
        }
    }

    void appendReportPageHead(HtmlCanvas reportPageHtml) throws IOException {
        HtmlCanvas reportPageHead = reportPageHtml.head();
        reportPageHead.macros().javascript(GOOGLE_CHARTS_URL);
        reportPageHead.macros().javascript(CURRENT_DIRECTORY + RES + SLASH + PIECHART_JS);
        reportPageHead.macros().stylesheet(CURRENT_DIRECTORY + RES + SLASH + BASIC_REPORT_CSS);
        reportPageHead._head();
    }

    private void generatePiechart(HtmlCanvas reportPageBody, IRunTestResults iRunTestResults) throws IOException {
        HtmlCanvas divSummary = reportPageBody.div();
        divSummary.div(new HtmlAttributes().id(PIECHART_ID))._div();
        HtmlCanvas divTestSummary = divSummary.div(new HtmlAttributes().class_(TEST_SUMMARY_CLASS));

        int passedCount = iRunTestResults.getPassedTests().size();
        int skippedCount = iRunTestResults.getSkippedTests().size();
        int failedCount = iRunTestResults.getFailedTests().size();

        divTestSummary.h2().content(format(PASSED_TESTS, passedCount));
        divTestSummary.h2().content(format(FAILED_TESTS, failedCount));
        divTestSummary.h2().content(format(SKIPPED_TESTS, skippedCount));
        divTestSummary._div();
        divSummary._div();

        // Hidden divs used to be able to link data to piechart.js
        reportPageBody.div(new HtmlAttributes().id(PASSED.toLowerCase(ENGLISH))
                .class_(HIDDEN_CLASS)).content(passedCount);
        reportPageBody.div(new HtmlAttributes().id(FAILED.toLowerCase(ENGLISH))
                .class_(HIDDEN_CLASS)).content(failedCount);
        reportPageBody.div(new HtmlAttributes().id(SKIPPED.toLowerCase(ENGLISH))
                .class_(HIDDEN_CLASS)).content(skippedCount);
    }

    public void createResourcesFolder(String reportDirectory) throws IOException {
        Path resDirectory = Paths.get(reportDirectory, RES);
        if (!Files.exists(resDirectory)) {
            Files.createDirectories(resDirectory);
        }
    }

    public void copyResources(String reportDirectory) throws IOException {
        copyResourceFromClasspath(reportDirectory, Paths.get(IMAGES, CLOUD_SLANG_LOGO_PNG),
                Paths.get(RES, CLOUD_SLANG_LOGO_PNG));
        copyResourceFromClasspath(reportDirectory, Paths.get(REPORT, BASIC_REPORT_CSS),
                Paths.get(RES, BASIC_REPORT_CSS));
        copyResourceFromClasspath(reportDirectory, Paths.get(REPORT, PIECHART_JS),
                Paths.get(RES, PIECHART_JS));
    }

    private void copyResourceFromClasspath(String reportDirectory, Path relativePathSource,
                                           Path relativePathDest) throws IOException {
        File destination = Paths.get(reportDirectory).resolve(relativePathDest).toFile();
        InputStream sourceStream = new DefaultResourceLoader().getResource(CLASSPATH +
                relativePathSource.toString()).getInputStream();
        // overwrite if exists
        FileUtils.copyInputStreamToFile(sourceStream, destination);
    }

    void generateHeader(HtmlCanvas reportPageBody, String cloudSlangImage) throws IOException {
        HtmlCanvas headerDiv = reportPageBody.div(new HtmlAttributes().id(HEADER_BAR_ID));
        HtmlCanvas anchor = headerDiv.a(new HtmlAttributes().href(HTTP_CLOUD_SLANG_IO).target(BLANK));
        HtmlCanvas img = new HtmlCanvas().img(new HtmlAttributes().src(cloudSlangImage).alt(CLOUD_SLANG_LOGO_ALT));
        anchor.content(img.toHtml(), false);
        headerDiv._div();

        reportPageBody.h1(new HtmlAttributes().class_(REPORT_TITLE_CLASS)).content(TEST_CASE_REPORT);
    }

    public void generateTestCaseReportTable(HtmlCanvas htmlCanvas, IRunTestResults iRunTestResults) throws IOException {
        HtmlCanvas table = htmlCanvas.table(new HtmlAttributes().class_(TABLE_CLASS));
        table.tr().th().content(TEST_NAME)
                .th(getClass(TEST_SUITES_CSS_CLASS)).content(TEST_SUITE)
                .th(getClass(STATUS_CSS_CLASS)).content(TEST_STATUS)
                .th().content(TEST_FLOW_PATH)
                .th().content(TEST_DESCRIPTION)
                .th().content(OUTPUTS)
                .th().content(EXCEPTION_OR_MESSAGE)
                ._tr();

        appendTestRuns(table, iRunTestResults.getPassedTests(), PASSED);
        appendTestRuns(table, iRunTestResults.getFailedTests(), FAILED);
        appendTestRuns(table, iRunTestResults.getSkippedTests(), SKIPPED);
    }

    void appendTestRuns(HtmlCanvas table, Map<String, TestRun> tests, String result) throws IOException {
        for (Map.Entry<String, TestRun> stringTestRunEntry : tests.entrySet()) {
            TestRun testRun = stringTestRunEntry.getValue();
            SlangTestCase testCase = testRun.getTestCase();
            appendTestRunRowToTable(table, testRun, testCase, result);
        }
    }

    private void appendTestRunRowToTable(HtmlCanvas table, TestRun testRun,
                                         SlangTestCase testCase, String result) throws IOException {
        table.tr().td().content(testCase.getName())
                .td(getClass(TEST_SUITES_CSS_CLASS)).content(getTestSuites(testCase))
                .td(getClass(STATUS_CSS_CLASS)).content(result)
                .td().content(testCase.getTestFlowPath())
                .td().content(testCase.getDescription())
                .td().content(getOutputs(testCase))
                .td().content(testRun.getMessage())
                ._tr();
    }

    private HtmlAttributes getClass(String cssClassName) {
        return new HtmlAttributes().class_(cssClassName);
    }

    private String getOutputs(SlangTestCase testCase) {
        List<Map> outputs = testCase.getOutputs();
        if (outputs != null) {
            return on(SEPARATOR).join(outputs);
        }
        return "";
    }

    private String getTestSuites(SlangTestCase testCase) {
        return on(SEPARATOR).join(testCase.getTestSuites());
    }

}
