/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.tester.runconfiguration.strategy;


import io.cloudslang.lang.tools.build.SlangBuildMain.TestCaseRunMode;

import static io.cloudslang.lang.tools.build.SlangBuildMain.TestCaseRunMode.SEQUENTIAL;

/**
 * This class provides a resolution for the cases when a run mode for a test case is unspecified.
 */
public class SequentialRunTestSuiteResolutionStrategy implements DefaultResolutionStrategy<TestCaseRunMode> {

    @Override
    public TestCaseRunMode getDefaultWhenUnspecified() {
        return SEQUENTIAL;
    }
}
