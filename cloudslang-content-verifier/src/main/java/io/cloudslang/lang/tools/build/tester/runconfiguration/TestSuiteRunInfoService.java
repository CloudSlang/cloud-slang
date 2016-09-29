package io.cloudslang.lang.tools.build.tester.runconfiguration;


import io.cloudslang.lang.tools.build.SlangBuildMain.TestSuiteRunMode;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import io.cloudslang.lang.tools.build.tester.runconfiguration.strategy.ConflictResolutionStrategy;
import io.cloudslang.lang.tools.build.tester.runconfiguration.strategy.DefaultResolutionStrategy;

public interface TestSuiteRunInfoService {

    TestSuiteRunMode getRunModeForTestSuite(String testSuite);

    void setRunModeForTestSuite(String testSuite, TestSuiteRunMode runMode);

    TestSuiteRunMode getRunModeForTestCase(SlangTestCase testCase, ConflictResolutionStrategy<TestSuiteRunMode> multipleModeConflictStrategy,
                                           DefaultResolutionStrategy<TestSuiteRunMode> defaultTestSuiteStrategy);
}
