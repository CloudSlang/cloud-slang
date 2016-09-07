package io.cloudslang.lang.tools.build.tester;

import java.util.Map;
import java.util.Set;


public interface IRunTestResults {

    Map<String, TestRun> getPassedTests();

    Map<String, TestRun> getFailedTests();

    Map<String, TestRun> getSkippedTests();

    Set<String> getCoveredExecutables();

    Set<String> getUncoveredExecutables();

    void addPassedTest(String testCaseName, TestRun testRun);

    void addFailedTest(String testCaseName, TestRun testRun);

    void addSkippedTest(String testCaseName, TestRun testRun);

    void addCoveredExecutables(Set<String> coveredExecutables);

    void addUncoveredExecutables(Set<String> uncoveredExecutables);

}
