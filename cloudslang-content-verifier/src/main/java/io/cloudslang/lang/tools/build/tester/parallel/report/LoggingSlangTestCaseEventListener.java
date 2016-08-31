package io.cloudslang.lang.tools.build.tester.parallel.report;


import io.cloudslang.lang.tools.build.tester.ISlangTestCaseEventListener;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.BeginSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.SkippedSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.SlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import org.apache.log4j.Logger;

public class LoggingSlangTestCaseEventListener implements ISlangTestCaseEventListener {

    private static Logger log = Logger.getLogger(LoggingSlangTestCaseEventListener.class);

    @Override
    public synchronized void onEvent(SlangTestCaseEvent event) {
        SlangTestCase slangTestCase = event.getSlangTestCase();
        if (event instanceof BeginSlangTestCaseEvent) {
            log.info("Running test: " + slangTestCase.getName() + " - " + slangTestCase.getDescription());
        } else if (event instanceof SkippedSlangTestCaseEvent) {
            log.info("Skipping test: " + slangTestCase.getName() + " because it is not in active test suites");
        }
    }

}
