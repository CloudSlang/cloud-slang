/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.tester;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Queue;
import java.util.ArrayDeque;


/**
 * Holds the results of running the content test cases
 */
public class RunTestsResults implements IRunTestResults {

    private Map<String, TestRun> passedTests;

    private Map<String, TestRun> failedTests;

    private Map<String, TestRun> skippedTests;

    private TreeSet<String> coveredExecutables;

    private TreeSet<String> uncoveredExecutables;

    private Queue<RuntimeException> exceptions;

    public RunTestsResults() {
        this.passedTests = new HashMap<>();
        this.failedTests = new HashMap<>();
        this.skippedTests = new HashMap<>();
        this.coveredExecutables = new TreeSet<>();
        this.uncoveredExecutables = new TreeSet<>();
        this.exceptions = new ArrayDeque<>();
    }

    @Override
    public Map<String, TestRun> getPassedTests() {
        return passedTests;
    }

    @Override
    public Map<String, TestRun> getFailedTests() {
        return failedTests;
    }

    @Override
    public Map<String, TestRun> getSkippedTests() {
        return skippedTests;
    }

    @Override
    public Set<String> getCoveredExecutables() {
        return coveredExecutables;
    }

    @Override
    public Set<String> getUncoveredExecutables() {
        return uncoveredExecutables;
    }

    @Override
    public Queue<RuntimeException> getExceptions() {
        return exceptions;
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
        this.coveredExecutables.addAll(coveredExecutables);
    }

    @Override
    public void addUncoveredExecutables(Set<String> uncoveredExecutables) {
        this.uncoveredExecutables.addAll(uncoveredExecutables);
    }

    @Override
    public void addExceptions(Queue<RuntimeException> exceptions) {
        this.exceptions.addAll(exceptions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RunTestsResults that = (RunTestsResults) o;

        if (passedTests != null ? !passedTests.equals(that.passedTests) : that.passedTests != null) {
            return false;
        }
        if (failedTests != null ? !failedTests.equals(that.failedTests) : that.failedTests != null) {
            return false;
        }
        return !(skippedTests != null ? !skippedTests.equals(that.skippedTests) : that.skippedTests != null);

    }

    @Override
    public int hashCode() {
        int result = passedTests != null ? passedTests.hashCode() : 0;
        result = 31 * result + (failedTests != null ? failedTests.hashCode() : 0);
        result = 31 * result + (skippedTests != null ? skippedTests.hashCode() : 0);
        return result;
    }
}
