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
import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.rendersnake.CanvasMacros;
import org.rendersnake.HtmlAttributes;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.internal.CharactersWriteable;

import java.io.IOException;
import java.util.Map;

import static io.cloudslang.lang.tools.build.tester.parallel.report.SlangTestCaseRunReportGeneratorService.BASIC_REPORT_CSS;
import static io.cloudslang.lang.tools.build.tester.parallel.report.SlangTestCaseRunReportGeneratorService.EXCEPTION_OR_MESSAGE;
import static io.cloudslang.lang.tools.build.tester.parallel.report.SlangTestCaseRunReportGeneratorService.FAILED;
import static io.cloudslang.lang.tools.build.tester.parallel.report.SlangTestCaseRunReportGeneratorService.GOOGLE_CHARTS_URL;
import static io.cloudslang.lang.tools.build.tester.parallel.report.SlangTestCaseRunReportGeneratorService.OUTPUTS;
import static io.cloudslang.lang.tools.build.tester.parallel.report.SlangTestCaseRunReportGeneratorService.PASSED;
import static io.cloudslang.lang.tools.build.tester.parallel.report.SlangTestCaseRunReportGeneratorService.PIECHART_JS;
import static io.cloudslang.lang.tools.build.tester.parallel.report.SlangTestCaseRunReportGeneratorService.SKIPPED;
import static io.cloudslang.lang.tools.build.tester.parallel.report.SlangTestCaseRunReportGeneratorService.TEST_CASE_REPORT;
import static io.cloudslang.lang.tools.build.tester.parallel.report.SlangTestCaseRunReportGeneratorService.TEST_DESCRIPTION;
import static io.cloudslang.lang.tools.build.tester.parallel.report.SlangTestCaseRunReportGeneratorService.TEST_FLOW_PATH;
import static io.cloudslang.lang.tools.build.tester.parallel.report.SlangTestCaseRunReportGeneratorService.TEST_NAME;
import static io.cloudslang.lang.tools.build.tester.parallel.report.SlangTestCaseRunReportGeneratorService.TEST_STATUS;
import static io.cloudslang.lang.tools.build.tester.parallel.report.SlangTestCaseRunReportGeneratorService.TEST_SUITE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SlangTestCaseRunReportGeneratorServiceTest {

    @Spy
    @InjectMocks
    private SlangTestCaseRunReportGeneratorService reportGeneratorService;

    @Test
    public void testAppReportPageHead() throws IOException {
        HtmlCanvas canvas = mock(HtmlCanvas.class);
        CanvasMacros<?> macros = mock(CanvasMacros.class);

        HtmlCanvas mockHead = mock(HtmlCanvas.class);
        doReturn(mockHead).when(canvas).head();
        doReturn(macros).when(mockHead).macros();
        doReturn(new HtmlCanvas())
                .doReturn(new HtmlCanvas())
                .when(macros).javascript(anyString());

        doReturn(new HtmlCanvas()).when(macros).stylesheet(anyString());
        doReturn(new HtmlCanvas()).when(mockHead)._head();

        reportGeneratorService.appendReportPageHead(canvas);

        verify(canvas).head();

        InOrder canvasHead = inOrder(mockHead);
        canvasHead.verify(mockHead, times(3)).macros();
        canvasHead.verify(mockHead)._head();
        canvasHead.verifyNoMoreInteractions();

        verify(macros).javascript(eq(GOOGLE_CHARTS_URL));
        verify(macros).javascript(endsWith("/" + PIECHART_JS));
        verify(macros).stylesheet(endsWith("/" + BASIC_REPORT_CSS));
    }

    @Test
    public void testGenerateHeader() throws IOException {
        HtmlCanvas canvas = mock(HtmlCanvas.class);
        final String csLogo = "csLogo";

        HtmlCanvas mockHeaderDiv = mock(HtmlCanvas.class);
        HtmlCanvas mockAnchor = mock(HtmlCanvas.class);
        doReturn(mockHeaderDiv).when(canvas).div(any(HtmlAttributes.class));
        doReturn(mockAnchor).when(mockHeaderDiv).a(any(HtmlAttributes.class));
        doReturn(new HtmlCanvas()).when(mockAnchor).content(anyString(), anyBoolean());

        doReturn(new HtmlCanvas()).when(mockHeaderDiv)._div();

        doReturn(canvas).when(canvas).h1(any(HtmlAttributes.class));
        doReturn(new HtmlCanvas()).when(canvas).content(anyString());

        reportGeneratorService.generateHeader(canvas, csLogo);
        verify(canvas).div(any(HtmlAttributes.class));

        InOrder mockHeaderDivInOrder = inOrder(mockHeaderDiv);
        mockHeaderDivInOrder.verify(mockHeaderDiv).a(any(HtmlAttributes.class));
        mockHeaderDivInOrder.verify(mockHeaderDiv)._div();
        mockHeaderDivInOrder.verifyNoMoreInteractions();

        verify(canvas).h1(any(HtmlAttributes.class));
        verify(canvas).content(eq(TEST_CASE_REPORT));
    }

    @Test
    public void generateTestCaseReportTable() throws IOException {
        HtmlCanvas canvas = mock(HtmlCanvas.class);
        IRunTestResults runTestResults = mock(IRunTestResults.class);

        final HtmlCanvas mockTable = mock(HtmlCanvas.class);
        HtmlCanvas mockTr = mock(HtmlCanvas.class);
        final MutablePair<Integer, Object> pair = new MutablePair<>();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                pair.setLeft(0);
                pair.setRight(invocationOnMock.getArguments()[0]);
                return mockTable;
            }
        }).when(canvas).table(any(HtmlAttributes.class));
        doReturn(mockTr).when(mockTable).tr();
        doReturn(mockTr).when(mockTr).th();
        doReturn(mockTr).when(mockTr).content(anyString());

        doReturn(mockTr).when(mockTr).th(any(CharactersWriteable.class));
        doReturn(new HtmlCanvas()).when(mockTr)._tr();

        doNothing().when(reportGeneratorService).appendTestRuns(any(HtmlCanvas.class), anyMap(), anyString());
        Map<String, TestRun> success = mock(Map.class);
        Map<String, TestRun> failed = mock(Map.class);
        Map<String, TestRun> skipped = mock(Map.class);

        doReturn(success).when(runTestResults).getPassedTests();
        doReturn(failed).when(runTestResults).getFailedTests();
        doReturn(skipped).when(runTestResults).getSkippedTests();

        reportGeneratorService.generateTestCaseReportTable(canvas, runTestResults);
        verify(canvas).table(eq((CharactersWriteable) pair.getRight()));

        verify(mockTable).tr();
        InOrder mockTableInOrder = inOrder(mockTr);

        mockTableInOrder.verify(mockTr).th();
        mockTableInOrder.verify(mockTr).content(eq(TEST_NAME));
        mockTableInOrder.verify(mockTr).th(any(HtmlAttributes.class));
        mockTableInOrder.verify(mockTr).content(TEST_SUITE);

        mockTableInOrder.verify(mockTr).th(any(HtmlAttributes.class));
        mockTableInOrder.verify(mockTr).content(TEST_STATUS);
        mockTableInOrder.verify(mockTr).th();
        mockTableInOrder.verify(mockTr).content(TEST_FLOW_PATH);
        mockTableInOrder.verify(mockTr).th();
        mockTableInOrder.verify(mockTr).content(TEST_DESCRIPTION);
        mockTableInOrder.verify(mockTr).th();
        mockTableInOrder.verify(mockTr).content(OUTPUTS);
        mockTableInOrder.verify(mockTr).th();
        mockTableInOrder.verify(mockTr).content(EXCEPTION_OR_MESSAGE);

        mockTableInOrder.verify(mockTr)._tr();
        mockTableInOrder.verifyNoMoreInteractions();

        verify(reportGeneratorService).appendTestRuns(eq(mockTable), eq(success), eq(PASSED));
        verify(reportGeneratorService).appendTestRuns(eq(mockTable), eq(failed), eq(FAILED));
        verify(reportGeneratorService).appendTestRuns(eq(mockTable), eq(skipped), eq(SKIPPED));
    }

}
