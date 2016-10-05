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


import org.junit.Assert;
import org.junit.Test;

import static io.cloudslang.lang.tools.build.SlangBuildMain.TestCaseRunMode.PARALLEL;
import static io.cloudslang.lang.tools.build.SlangBuildMain.TestCaseRunMode.SEQUENTIAL;

public class RunMultipleTestSuiteConflictResolutionStrategyTest {

    @Test
    public void testResolve() {
        RunMultipleTestSuiteConflictResolutionStrategy strategy = new RunMultipleTestSuiteConflictResolutionStrategy();
        // Both null
        Assert.assertEquals(null, strategy.resolve(null, null));

        // One null and one sequential
        Assert.assertEquals(SEQUENTIAL, strategy.resolve(null, SEQUENTIAL));
        Assert.assertEquals(SEQUENTIAL, strategy.resolve(SEQUENTIAL, null));

        // One null and one sequential
        Assert.assertEquals(PARALLEL, strategy.resolve(null, PARALLEL));
        Assert.assertEquals(PARALLEL, strategy.resolve(PARALLEL, null));

        // Two not nulls
        Assert.assertEquals(PARALLEL, strategy.resolve(PARALLEL, PARALLEL));
        Assert.assertEquals(SEQUENTIAL, strategy.resolve(SEQUENTIAL, SEQUENTIAL));

        Assert.assertEquals(SEQUENTIAL, strategy.resolve(PARALLEL, SEQUENTIAL));
        Assert.assertEquals(SEQUENTIAL, strategy.resolve(SEQUENTIAL, PARALLEL));
    }
}
