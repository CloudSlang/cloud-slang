package io.cloudslang.lang.tools.build.tester.runconfiguration.strategy;


import io.cloudslang.lang.tools.build.SlangBuildMain.TestSuiteRunMode;

import static io.cloudslang.lang.tools.build.SlangBuildMain.TestSuiteRunMode.SEQUENTIAL;

public class SequentialRunTestSuiteResolutionStrategy implements DefaultResolutionStrategy<TestSuiteRunMode>{

    @Override
    public TestSuiteRunMode getDefaultWhenUnspecified() {
        return SEQUENTIAL;
    }
}
