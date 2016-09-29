package io.cloudslang.lang.tools.build.tester.runconfiguration;


import io.cloudslang.lang.tools.build.SlangBuildMain.TestCaseRunMode;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import io.cloudslang.lang.tools.build.tester.runconfiguration.strategy.ConflictResolutionStrategy;
import io.cloudslang.lang.tools.build.tester.runconfiguration.strategy.DefaultResolutionStrategy;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class TestSuitesRunInfoServiceImpl implements TestSuiteRunInfoService {

    private ConcurrentMap<String, TestCaseRunMode> runModeMap;

    @PostConstruct
    public void initialize() {
        runModeMap = new ConcurrentHashMap<>();
    }

    @Override
    public TestCaseRunMode getRunModeForTestSuite(final String testSuite) {
        return doGetTestSuiteRunMode(testSuite);
    }

    @Override
    public void setRunModeForTestSuite(final String testSuite, final TestCaseRunMode runMode) {
        runModeMap.put(testSuite, runMode);
    }

    @Override
    public TestCaseRunMode getRunModeForTestCase(final SlangTestCase testCase, final ConflictResolutionStrategy<TestCaseRunMode> multipleModeConflictStrategy,
                                                 final DefaultResolutionStrategy<TestCaseRunMode> defaultTestSuiteStrategy) {
        List<String> testSuites = testCase.getTestSuites();
        if (testSuites.isEmpty()) {
            return defaultTestSuiteStrategy.getDefaultWhenUnspecified();
        }

        TestCaseRunMode result = null;
        for (String testSuite : testSuites) {
            result = multipleModeConflictStrategy.resolve(result, doGetTestSuiteRunMode(testSuite));
        }
        if (result == null) {
            return defaultTestSuiteStrategy.getDefaultWhenUnspecified();
        }
        return result;
    }

    private TestCaseRunMode doGetTestSuiteRunMode(String testSuite) {
        return runModeMap.get(testSuite);
    }

}
