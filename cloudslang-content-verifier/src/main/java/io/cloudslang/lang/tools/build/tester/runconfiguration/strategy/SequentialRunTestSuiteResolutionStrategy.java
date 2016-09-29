package io.cloudslang.lang.tools.build.tester.runconfiguration.strategy;


import io.cloudslang.lang.tools.build.SlangBuildMain.TestCaseRunMode;

import static io.cloudslang.lang.tools.build.SlangBuildMain.TestCaseRunMode.SEQUENTIAL;

public class SequentialRunTestSuiteResolutionStrategy implements DefaultResolutionStrategy<TestCaseRunMode> {

    @Override
    public TestCaseRunMode getDefaultWhenUnspecified() {
        return SEQUENTIAL;
    }
}
