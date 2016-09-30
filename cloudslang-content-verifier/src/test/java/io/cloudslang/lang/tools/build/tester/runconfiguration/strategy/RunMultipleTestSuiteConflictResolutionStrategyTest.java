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
