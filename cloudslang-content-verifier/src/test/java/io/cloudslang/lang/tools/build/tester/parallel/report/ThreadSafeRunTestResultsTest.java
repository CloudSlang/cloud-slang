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


import io.cloudslang.lang.tools.build.tester.TestRun;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.FailedSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.PassedSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.SkippedSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;

import java.util.HashSet;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ThreadSafeRunTestResultsTest {

    private ThreadSafeRunTestResults threadSafeTestResults;

    @Before
    public void setUp() {
        threadSafeTestResults = new ThreadSafeRunTestResults();
    }

    @Test
    public void testFailedTestCaseOnEvent() {
        String nameFailed = "nameFailed";
        SlangTestCase testCase = new SlangTestCase(nameFailed, null, null, null, null, null, null, null, null);
        FailedSlangTestCaseEvent failedSlangTestCaseEvent =
                new FailedSlangTestCaseEvent(testCase, "message", new RuntimeException("ex"));

        threadSafeTestResults.onEvent(failedSlangTestCaseEvent);

        assertEquals(1, threadSafeTestResults.getFailedTests().size());
        assertEquals(0, threadSafeTestResults.getPassedTests().size());
        assertEquals(0, threadSafeTestResults.getSkippedTests().size());
        Assert.assertTrue(threadSafeTestResults.getFailedTests().keySet().contains(nameFailed));
        assertEquals(new TestRun(testCase, failedSlangTestCaseEvent.getFailureReason()),
                threadSafeTestResults.getFailedTests().values().iterator().next());
    }

    @Test
    public void testPassedTestCaseOnEvent() {
        String namePassed = "namePassed";
        SlangTestCase testCase = new SlangTestCase(namePassed, null, null, null, null, null, null, null, null);
        PassedSlangTestCaseEvent passedSlangTestCaseEvent = new PassedSlangTestCaseEvent(testCase);

        threadSafeTestResults.onEvent(passedSlangTestCaseEvent);

        assertEquals(0, threadSafeTestResults.getFailedTests().size());
        assertEquals(1, threadSafeTestResults.getPassedTests().size());
        assertEquals(0, threadSafeTestResults.getSkippedTests().size());
        Assert.assertTrue(threadSafeTestResults.getPassedTests().keySet().contains(namePassed));
        assertEquals(new TestRun(testCase, null), threadSafeTestResults.getPassedTests().values().iterator().next());
    }

    @Test
    public void testSkippedTestCaseOnEvent() {
        String nameSkipped = "nameSkipped";
        SlangTestCase testCase = new SlangTestCase(nameSkipped, null, null, null, null, null, null, null, null);
        SkippedSlangTestCaseEvent skippedSlangTestCaseEvent = new SkippedSlangTestCaseEvent(testCase);

        threadSafeTestResults.onEvent(skippedSlangTestCaseEvent);

        assertEquals(0, threadSafeTestResults.getFailedTests().size());
        assertEquals(0, threadSafeTestResults.getPassedTests().size());
        assertEquals(1, threadSafeTestResults.getSkippedTests().size());
        Assert.assertTrue(threadSafeTestResults.getSkippedTests().keySet().contains(nameSkipped));
        TestRun expectedTestRun =
                new TestRun(
                        testCase,
                        "Skipping test: " + SlangTestCase.generateTestCaseReference(testCase) +
                                " because it is not in active test suites"
                );
        TestRun actualTestRun = threadSafeTestResults.getSkippedTests().values().iterator().next();
        assertEquals(expectedTestRun, actualTestRun);
    }

    @Test
    public void testCoveredExecutable() {
        HashSet<String> coveredExecutables = new HashSet<>();
        String element = "covered";
        coveredExecutables.add(element);
        coveredExecutables.add(element);

        threadSafeTestResults.addCoveredExecutables(coveredExecutables);

        assertEquals(1, threadSafeTestResults.getCoveredExecutables().size());
        assertEquals(element, threadSafeTestResults.getCoveredExecutables().iterator().next());
    }

    @Test
    public void testUnCoveredExecutableSorting() {
        HashSet<String> uncoveredExecutables = new HashSet<>();
        String element1 = "uncoveredB";
        String element2 = "uncoveredA";
        uncoveredExecutables.add(element1);
        uncoveredExecutables.add(element2);

        threadSafeTestResults.addUncoveredExecutables(uncoveredExecutables);

        assertEquals(2, threadSafeTestResults.getUncoveredExecutables().size());
        Iterator<String> iterator = threadSafeTestResults.getUncoveredExecutables().iterator();
        assertEquals(element2, iterator.next());
        assertEquals(element1, iterator.next());
    }

}
