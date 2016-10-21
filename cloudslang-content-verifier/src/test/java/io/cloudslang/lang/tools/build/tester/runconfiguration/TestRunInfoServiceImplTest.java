/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.tester.runconfiguration;


import io.cloudslang.lang.tools.build.SlangBuildMain.TestCaseRunMode;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import io.cloudslang.lang.tools.build.tester.runconfiguration.strategy.RunMultipleTestSuiteConflictResolutionStrategy;
import io.cloudslang.lang.tools.build.tester.runconfiguration.strategy.SequentialRunTestSuiteResolutionStrategy;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.beust.jcommander.internal.Lists.newArrayList;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class TestRunInfoServiceImplTest {

    private static final String SUITE_GET = "suite_get";
    private static final String SUITE_PUT = "suite_put";

    @Spy
    @InjectMocks
    private TestRunInfoServiceImpl testRunInfoService;

    @Mock
    private ConcurrentMap<String, TestCaseRunMode> runModeMap;

    @Test
    public void testGetRunModeForTestSuite() {
        TestCaseRunMode runModeMock = TestCaseRunMode.SEQUENTIAL;
        doReturn(runModeMock).when(runModeMap).get(anyString());

        // Tested call
        TestCaseRunMode runModeResult = testRunInfoService.getRunModeForTestSuite(SUITE_GET);

        verify(runModeMap).get(eq(SUITE_GET));
        Assert.assertSame(runModeMock, runModeResult);
    }

    @Test
    public void testSetRunModeForTestSuite() {
        TestCaseRunMode runModeMock = TestCaseRunMode.PARALLEL;
        doReturn(null).when(runModeMap).put(anyString(), any(TestCaseRunMode.class));

        // Tested call
        testRunInfoService.setRunModeForTestSuite(SUITE_PUT, runModeMock);

        verify(runModeMap).put(eq(SUITE_PUT), eq(runModeMock));
    }

    @Test
    public void testSetRunModeForTestSuites() {
        TestCaseRunMode runModeMock = TestCaseRunMode.SEQUENTIAL;
        doReturn(null)
                .doReturn(null)
                .doReturn(null)
                .when(runModeMap).put(anyString(), any(TestCaseRunMode.class));

        // Tested call
        List<String> list = newArrayList("aaa", "bbb", "ccc");
        testRunInfoService.setRunModeForTestSuites(list, runModeMock);

        verify(runModeMap).put(eq("aaa"), eq(runModeMock));
        verify(runModeMap).put(eq("bbb"), eq(runModeMock));
        verify(runModeMap).put(eq("ccc"), eq(runModeMock));
    }

    @Test
    public void testGetRunModeForTestCaseEmptySuites() {
        SlangTestCase testCase = mock(SlangTestCase.class);
        RunMultipleTestSuiteConflictResolutionStrategy multipleTestSuiteConflictResolutionStrategy =
                mock(RunMultipleTestSuiteConflictResolutionStrategy.class);
        SequentialRunTestSuiteResolutionStrategy sequentialResolutionStrategy =
                mock(SequentialRunTestSuiteResolutionStrategy.class);

        doReturn(newArrayList()).when(testCase).getTestSuites();
        doCallRealMethod().when(sequentialResolutionStrategy).getDefaultWhenUnspecified();

        // Tested call
        TestCaseRunMode runModeForTestCaseResult = testRunInfoService.getRunModeForTestCase(testCase,
                multipleTestSuiteConflictResolutionStrategy, sequentialResolutionStrategy);
        assertEquals(TestCaseRunMode.SEQUENTIAL, runModeForTestCaseResult);
        verify(sequentialResolutionStrategy).getDefaultWhenUnspecified();
        verify(multipleTestSuiteConflictResolutionStrategy, never()).resolve(any(TestCaseRunMode.class),
                any(TestCaseRunMode.class));
    }

    @Test
    public void testGetRunModeForTestCaseSomeSuites() {
        SlangTestCase testCase = mock(SlangTestCase.class);
        RunMultipleTestSuiteConflictResolutionStrategy multipleTestSuiteConflictResolutionStrategy =
                mock(RunMultipleTestSuiteConflictResolutionStrategy.class);
        SequentialRunTestSuiteResolutionStrategy sequentialResolutionStrategy =
                mock(SequentialRunTestSuiteResolutionStrategy.class);

        doReturn(newArrayList("aaa", "bbb", "ccc")).when(testCase).getTestSuites();
        doCallRealMethod().when(sequentialResolutionStrategy).getDefaultWhenUnspecified();
        doCallRealMethod()
                .doCallRealMethod()
                .doCallRealMethod().when(multipleTestSuiteConflictResolutionStrategy)
                .resolve(any(TestCaseRunMode.class), any(TestCaseRunMode.class));
        doReturn(TestCaseRunMode.SEQUENTIAL)
                .doReturn(TestCaseRunMode.PARALLEL)
                .doReturn(TestCaseRunMode.SEQUENTIAL).when(runModeMap).get(anyString());

        // Tested call
        TestCaseRunMode runModeForTestCaseResult = testRunInfoService
                .getRunModeForTestCase(testCase, multipleTestSuiteConflictResolutionStrategy,
                        sequentialResolutionStrategy);

        assertEquals(TestCaseRunMode.SEQUENTIAL, runModeForTestCaseResult);
        verify(sequentialResolutionStrategy, never()).getDefaultWhenUnspecified();

        verify(multipleTestSuiteConflictResolutionStrategy)
                .resolve(eq((TestCaseRunMode) null), eq(TestCaseRunMode.SEQUENTIAL));
        verify(multipleTestSuiteConflictResolutionStrategy)
                .resolve(eq(TestCaseRunMode.SEQUENTIAL), eq(TestCaseRunMode.PARALLEL));
        verify(multipleTestSuiteConflictResolutionStrategy)
                .resolve(eq(TestCaseRunMode.SEQUENTIAL), eq(TestCaseRunMode.SEQUENTIAL));
        verifyNoMoreInteractions(multipleTestSuiteConflictResolutionStrategy);
    }

    @Test
    public void testInitialize() throws IllegalAccessException, NoSuchFieldException {
        TestRunInfoServiceImpl local = new TestRunInfoServiceImpl();
        local.initialize();
        Class<? extends TestRunInfoServiceImpl> localClass = local.getClass();
        Field fieldRunModeMap = localClass.getDeclaredField("runModeMap");
        fieldRunModeMap.setAccessible(true);
        Object value = fieldRunModeMap.get(local);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof ConcurrentHashMap);
    }

}
