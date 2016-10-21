/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.tester;


import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.tools.build.tester.parallel.MultiTriggerTestCaseEventListener;
import io.cloudslang.lang.tools.build.tester.parallel.services.TestCaseEventDispatchService;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.BeginSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.FailedSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.PassedSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;

import java.util.Map;

public class SlangTestCaseRunnable implements Runnable {

    private final SlangTestCase testCase;
    private final Map<String, CompilationArtifact> compiledFlows;
    private final String projectPath;
    private final TestCaseEventDispatchService testCaseEventDispatchService;
    private final SlangTestRunner slangTestRunService;
    private final MultiTriggerTestCaseEventListener multiTriggerTestCaseEventListener;

    public SlangTestCaseRunnable(SlangTestCase testCase, Map<String, CompilationArtifact> compiledFlows,
                                 String projectPath, SlangTestRunner slangTestRunService,
                                 TestCaseEventDispatchService testCaseEventDispatchService,
                                 MultiTriggerTestCaseEventListener multiTriggerTestCaseEventListener) {
        this.testCase = testCase;
        this.compiledFlows = compiledFlows;
        this.projectPath = projectPath;
        this.testCaseEventDispatchService = testCaseEventDispatchService;
        this.slangTestRunService = slangTestRunService;
        this.multiTriggerTestCaseEventListener = multiTriggerTestCaseEventListener;
    }

    @Override
    public void run() {
        try {
            testCaseEventDispatchService.notifyListeners(new BeginSlangTestCaseEvent(testCase));

            CompilationArtifact compiledTestFlow = slangTestRunService.getCompiledTestFlow(compiledFlows, testCase);
            slangTestRunService
                    .runTestCaseParallel(testCase, compiledTestFlow, projectPath, multiTriggerTestCaseEventListener);
            testCaseEventDispatchService.notifyListeners(new PassedSlangTestCaseEvent(testCase));
        } catch (RuntimeException failureException) {
            testCaseEventDispatchService.notifyListeners(
                    new FailedSlangTestCaseEvent(testCase, failureException.getMessage(), failureException));
        }
    }
}
