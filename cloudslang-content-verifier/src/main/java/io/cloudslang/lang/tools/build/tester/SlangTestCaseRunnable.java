package io.cloudslang.lang.tools.build.tester;


import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.tools.build.tester.parallel.services.TestCaseEventDispatchService;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.BeginSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.FailedSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.PassedSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.SkippedSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;

import java.util.List;
import java.util.Map;

public class SlangTestCaseRunnable implements Runnable {

    private final SlangTestCase testCase;
    private final Map<String, CompilationArtifact> compiledFlows;
    private final String projectPath;
    private final List<String> testSuites;
    private final TestCaseEventDispatchService testCaseEventDispatchService;
    private final SlangTestRunner slangTestRunService;

    public SlangTestCaseRunnable(SlangTestCase testCase, Map<String, CompilationArtifact> compiledFlows, String projectPath, List<String> testSuites, SlangTestRunner slangTestRunService, TestCaseEventDispatchService testCaseEventDispatchService) {
        this.testCase = testCase;
        this.compiledFlows = compiledFlows;
        this.projectPath = projectPath;
        this.testSuites = testSuites;
        this.testCaseEventDispatchService = testCaseEventDispatchService;
        this.slangTestRunService = slangTestRunService;
    }

    @Override
    public void run() {
        try {
            if (slangTestRunService.isTestCaseInActiveSuite(testCase, testSuites)) {
                testCaseEventDispatchService.notifyListeners(new BeginSlangTestCaseEvent(testCase));

                CompilationArtifact compiledTestFlow = slangTestRunService.getCompiledTestFlow(compiledFlows, testCase);
                slangTestRunService.runTestParallel(testCase, compiledTestFlow, projectPath);
                testCaseEventDispatchService.notifyListeners(new PassedSlangTestCaseEvent(testCase));
            } else {
                testCaseEventDispatchService.notifyListeners(new SkippedSlangTestCaseEvent(testCase));
            }
        } catch (RuntimeException failureException) {
            testCaseEventDispatchService.notifyListeners(new FailedSlangTestCaseEvent(testCase, failureException.getMessage(), failureException));
        }
    }
}
