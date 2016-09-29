package io.cloudslang.lang.tools.build.tester.runconfiguration.strategy;


import io.cloudslang.lang.tools.build.SlangBuildMain.TestSuiteRunMode;

import static io.cloudslang.lang.tools.build.SlangBuildMain.TestSuiteRunMode.SEQUENTIAL;

public class RunMultipleTestSuiteConflictResolutionStrategy implements ConflictResolutionStrategy<TestSuiteRunMode> {

    @Override
    public TestSuiteRunMode resolve(TestSuiteRunMode entity1, TestSuiteRunMode entity2) {
        if (entity1 == null) {
            return entity2;
        } else if (entity2 == null) {
            return entity1;
        } else {
            return (entity1 != entity2) ? SEQUENTIAL : entity1;
        }
    }
}
