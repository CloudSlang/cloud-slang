package io.cloudslang.lang.tools.build.tester.parallel.services;

import io.cloudslang.lang.tools.build.tester.ISlangTestCaseEventListener;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.SlangTestCaseEvent;
import org.apache.commons.lang3.event.EventListenerSupport;

import javax.annotation.PostConstruct;

public class TestCaseEventDispatchService {

    private EventListenerSupport<ISlangTestCaseEventListener> listenerList;

    @PostConstruct
    void initializeListeners() {
        this.listenerList = EventListenerSupport.create(ISlangTestCaseEventListener.class);
    }

    public void registerListener(ISlangTestCaseEventListener listener) {
        listenerList.addListener(listener);
    }

    public void unregisterListener(ISlangTestCaseEventListener listener) {
        listenerList.removeListener(listener);
    }

    public void unregisterAllListeners() {
        ISlangTestCaseEventListener[] listeners = listenerList.getListeners();
        if (listeners != null) {
            for (ISlangTestCaseEventListener testCaseEventListener : listeners) {
                listenerList.removeListener(testCaseEventListener);
            }
        }
    }

    public void notifyListeners(SlangTestCaseEvent slangTestCaseEvent) {
        listenerList.fire().onEvent(slangTestCaseEvent);
    }

}
