package io.cloudslang.lang.tools.build.tester.runconfiguration;


import io.cloudslang.lang.tools.build.SlangBuildMain.TestCaseRunMode;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import io.cloudslang.lang.tools.build.tester.runconfiguration.strategy.ConflictResolutionStrategy;
import io.cloudslang.lang.tools.build.tester.runconfiguration.strategy.DefaultResolutionStrategy;

import java.util.List;

public interface TestSuiteRunInfoService {

    TestCaseRunMode getRunModeForTestSuite(String testSuite);

    void setRunModeForTestSuite(String testSuite, TestCaseRunMode runMode);

    void setRunModeForTestSuites(List<String> testSuites, TestCaseRunMode runMode);

    TestCaseRunMode getRunModeForTestCase(SlangTestCase testCase, ConflictResolutionStrategy<TestCaseRunMode> multipleModeConflictStrategy,
                                          DefaultResolutionStrategy<TestCaseRunMode> defaultTestSuiteStrategy);
}