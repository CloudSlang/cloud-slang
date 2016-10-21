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
import io.cloudslang.lang.tools.build.tester.runconfiguration.strategy.ConflictResolutionStrategy;
import io.cloudslang.lang.tools.build.tester.runconfiguration.strategy.DefaultResolutionStrategy;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for managing test and test suite run information
 */
public class TestRunInfoServiceImpl implements TestRunInfoService {

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
        doSetRunModeForTestSuite(runMode, testSuite);
    }

    @Override
    public void setRunModeForTestSuites(final List<String> testSuites, final TestCaseRunMode runMode) {
        for (String testSuite : testSuites) {
            doSetRunModeForTestSuite(runMode, testSuite);
        }
    }

    @Override
    public TestCaseRunMode getRunModeForTestCase(final SlangTestCase testCase,
                                                 final ConflictResolutionStrategy<TestCaseRunMode> conflictStrategy,
                                                 final DefaultResolutionStrategy<TestCaseRunMode> defaultStrategy) {
        List<String> testSuites = testCase.getTestSuites();
        if (testSuites.isEmpty()) {
            return defaultStrategy.getDefaultWhenUnspecified();
        }

        TestCaseRunMode result = null;
        for (String testSuite : testSuites) {
            result = conflictStrategy.resolve(result, doGetTestSuiteRunMode(testSuite));
        }
        if (result == null) {
            return defaultStrategy.getDefaultWhenUnspecified();
        }
        return result;
    }

    private TestCaseRunMode doGetTestSuiteRunMode(String testSuite) {
        return runModeMap.get(testSuite);
    }

    private void doSetRunModeForTestSuite(TestCaseRunMode runMode, String testSuite) {
        runModeMap.put(testSuite, runMode);
    }

}
