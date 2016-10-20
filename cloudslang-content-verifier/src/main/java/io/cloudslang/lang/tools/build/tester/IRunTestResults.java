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

import java.util.Map;
import java.util.Queue;
import java.util.Set;


public interface IRunTestResults {

    Map<String, TestRun> getPassedTests();

    Map<String, TestRun> getFailedTests();

    Map<String, TestRun> getSkippedTests();

    Queue<RuntimeException> getExceptions();

    Set<String> getCoveredExecutables();

    Set<String> getUncoveredExecutables();

    void addPassedTest(String testCaseName, TestRun testRun);

    void addFailedTest(String testCaseName, TestRun testRun);

    void addSkippedTest(String testCaseName, TestRun testRun);

    void addCoveredExecutables(Set<String> coveredExecutables);

    void addUncoveredExecutables(Set<String> uncoveredExecutables);

    void addExceptions(Queue<RuntimeException> exceptions);
}
