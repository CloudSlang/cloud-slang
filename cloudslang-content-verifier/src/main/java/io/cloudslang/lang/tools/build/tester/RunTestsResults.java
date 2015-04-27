/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package io.cloudslang.lang.tools.build.tester;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by stoneo on 4/27/2015.
 **/

/**
 * Holds the results of running the content test cases
 */
public class RunTestsResults {

    private Map<String, TestRun> passedTests;

    private Map<String, TestRun> failedTests;

    private Map<String, TestRun> skippedTests;

    public RunTestsResults(){
        this.passedTests = new HashMap<>();
        this.failedTests = new HashMap<>();
        this.skippedTests = new HashMap<>();
    }

    public RunTestsResults(Map<String, TestRun> passedTests, Map<String, TestRun> failedTests, Map<String, TestRun> skippedTests) {
        this.passedTests = passedTests;
        this.failedTests = failedTests;
        this.skippedTests = skippedTests;
    }


    public Map<String, TestRun> getPassedTests() {
        return passedTests;
    }

    public Map<String, TestRun> getFailedTests() {
        return failedTests;
    }

    public Map<String, TestRun> getSkippedTests() {
        return skippedTests;
    }

    public void addPassedTest(String testCaseName, TestRun testRun){
        passedTests.put(testCaseName, testRun);
    }

    public void addFailedTest(String testCaseName, TestRun testRun){
        failedTests.put(testCaseName, testRun);
    }

    public void addSkippedTest(String testCaseName, TestRun testRun){
        skippedTests.put(testCaseName, testRun);
    }
}
