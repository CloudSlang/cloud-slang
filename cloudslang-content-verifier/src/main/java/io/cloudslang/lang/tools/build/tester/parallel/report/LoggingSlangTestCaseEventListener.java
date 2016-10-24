/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.tester.parallel.report;


import io.cloudslang.lang.logging.LoggingService;
import io.cloudslang.lang.tools.build.tester.ISlangTestCaseEventListener;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.BeginSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.SkippedSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.SlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;

public class LoggingSlangTestCaseEventListener implements ISlangTestCaseEventListener {

    @Autowired
    private LoggingService loggingService;

    @Override
    public synchronized void onEvent(SlangTestCaseEvent event) {
        SlangTestCase slangTestCase = event.getSlangTestCase();
        if (event instanceof BeginSlangTestCaseEvent) {
            loggingService.logEvent(Level.INFO, "Running test: " +
                    SlangTestCase.generateTestCaseReference(slangTestCase) + " - " + slangTestCase.getDescription());
        } else if (event instanceof SkippedSlangTestCaseEvent) {
            loggingService.logEvent(Level.INFO, "Skipping test: " +
                    SlangTestCase.generateTestCaseReference(slangTestCase) +
                    " because it is not in active test suites");
        }
    }

}
