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
import io.cloudslang.lang.tools.build.tester.ISlangTestCaseEventListener;
import io.cloudslang.lang.tools.build.tester.TestRun;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.FailedSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.PassedSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.SkippedSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.SlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ThreadSafeRunTestResults implements IRunTestResults, ISlangTestCaseEventListener {

    private ConcurrentMap<String, TestRun> passedTests;
    private ConcurrentMap<String, TestRun> failedTests;
    private ConcurrentMap<String, TestRun> skippedTests;
    private TreeSet<String> coveredExecutables;
    private TreeSet<String> uncoveredExecutables;
    private Queue<RuntimeException> exceptions;

    private final Object lockCoveredExecutables;
    private final Object lockUncoveredExecutables;

    public ThreadSafeRunTestResults() {
        this.passedTests = new ConcurrentHashMap<>();
        this.failedTests = new ConcurrentHashMap<>();
        this.skippedTests = new ConcurrentHashMap<>();
        this.coveredExecutables = new TreeSet<>();
        this.uncoveredExecutables = new TreeSet<>();
        this.exceptions = new ConcurrentLinkedDeque<>();

        this.lockCoveredExecutables = new Object();
        this.lockUncoveredExecutables = new Object();
    }

    @Override
    public Map<String, TestRun> getPassedTests() {
        return new HashMap<>(passedTests);
    }

    @Override
    public Map<String, TestRun> getFailedTests() {
        return new HashMap<>(failedTests);
    }

    @Override
    public Map<String, TestRun> getSkippedTests() {
        return new HashMap<>(skippedTests);
    }

    @Override
    public Queue<RuntimeException> getExceptions() {
        return exceptions;
    }

    @Override
    public Set<String> getCoveredExecutables() {
        synchronized (lockCoveredExecutables) {
            return new TreeSet<>(coveredExecutables);
        }
    }

    @Override
    public Set<String> getUncoveredExecutables() {
        synchronized (lockUncoveredExecutables) {
            return new TreeSet<>(uncoveredExecutables);
        }
    }

    @Override
    public void addPassedTest(String testCaseName, TestRun testRun) {
        passedTests.put(testCaseName, testRun);
    }

    @Override
    public void addFailedTest(String testCaseName, TestRun testRun) {
        failedTests.put(testCaseName, testRun);
    }

    @Override
    public void addSkippedTest(String testCaseName, TestRun testRun) {
        skippedTests.put(testCaseName, testRun);
    }

    @Override
    public void addCoveredExecutables(Set<String> coveredExecutables) {
        synchronized (lockCoveredExecutables) {
            this.coveredExecutables.addAll(coveredExecutables);
        }
    }

    @Override
    public void addUncoveredExecutables(Set<String> uncoveredExecutables) {
        synchronized (lockUncoveredExecutables) {
            this.uncoveredExecutables.addAll(uncoveredExecutables);
        }
    }

    @Override
    public void addExceptions(Queue<RuntimeException> exceptions) {
        this.exceptions.addAll(exceptions);
    }

    @Override
    public synchronized void onEvent(SlangTestCaseEvent event) {
        SlangTestCase slangTestCase = event.getSlangTestCase();
        if (event instanceof FailedSlangTestCaseEvent) {
            addFailedTest(slangTestCase.getName(),
                    new TestRun(slangTestCase, ((FailedSlangTestCaseEvent) event).getFailureReason()));
        } else if (event instanceof PassedSlangTestCaseEvent) {
            addPassedTest(slangTestCase.getName(), new TestRun(slangTestCase, null));
        } else if (event instanceof SkippedSlangTestCaseEvent) {
            addSkippedTest(
                    slangTestCase.getName(),
                    new TestRun(
                            slangTestCase,
                            "Skipping test: " + SlangTestCase.generateTestCaseReference(slangTestCase) +
                                    " because it is not in active test suites"
                    )
            );
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        ThreadSafeRunTestResults rhs = (ThreadSafeRunTestResults) obj;
        return new EqualsBuilder()
                .append(this.passedTests, rhs.passedTests)
                .append(this.failedTests, rhs.failedTests)
                .append(this.skippedTests, rhs.skippedTests)
                .append(this.coveredExecutables, rhs.coveredExecutables)
                .append(this.uncoveredExecutables, rhs.uncoveredExecutables)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(passedTests)
                .append(failedTests)
                .append(skippedTests)
                .append(coveredExecutables)
                .append(uncoveredExecutables)
                .toHashCode();
    }

}