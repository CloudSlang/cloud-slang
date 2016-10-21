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

import java.util.List;

/**
 * Service interface for managing run information for test suite and test cases.
 */
public interface TestRunInfoService {

    TestCaseRunMode getRunModeForTestSuite(String testSuite);

    void setRunModeForTestSuite(String testSuite, TestCaseRunMode runMode);

    void setRunModeForTestSuites(List<String> testSuites, TestCaseRunMode runMode);

    TestCaseRunMode getRunModeForTestCase(SlangTestCase testCase,
                                          ConflictResolutionStrategy<TestCaseRunMode> multipleModeConflictStrategy,
                                          DefaultResolutionStrategy<TestCaseRunMode> defaultTestSuiteStrategy);
}
