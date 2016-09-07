package io.cloudslang.lang.tools.build.tester;


import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.SlangTestCaseEvent;

public interface ISlangTestCaseEventListener {

    void onEvent(SlangTestCaseEvent event);

}
