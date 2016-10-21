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
 * This class provides the resolution in case of test case run mode conflicts.
 *  E.g. test case t1 that is included in test suites aa, bb,
 *  with rules aa - sequential, bb - parallel, will be executed sequentially.
 */

public class RunMultipleTestSuiteConflictResolutionStrategy implements ConflictResolutionStrategy<TestCaseRunMode> {

    @Override
    public TestCaseRunMode resolve(TestCaseRunMode entity1, TestCaseRunMode entity2) {
        if (entity1 == null) {
            return entity2;
        } else if (entity2 == null) {
            return entity1;
        } else {
            return (entity1 != entity2) ? SEQUENTIAL : entity1;
        }
    }
}
