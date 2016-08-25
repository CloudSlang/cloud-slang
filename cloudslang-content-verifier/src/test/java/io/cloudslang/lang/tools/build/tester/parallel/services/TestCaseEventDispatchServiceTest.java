package io.cloudslang.lang.tools.build.tester.parallel.services;


import io.cloudslang.lang.tools.build.tester.ISlangTestCaseEventListener;
import io.cloudslang.lang.tools.build.tester.parallel.report.LoggingSlangTestCaseEventListener;
import io.cloudslang.lang.tools.build.tester.parallel.report.ThreadSafeRunTestResults;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.SlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.lang3.event.EventListenerSupport;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TestCaseEventDispatchServiceTest {

    @Spy
    @InjectMocks
    private TestCaseEventDispatchService testCaseEventDispatchService;

    @Mock
    private EventListenerSupport<ISlangTestCaseEventListener> listenerList;


    @Test
    public void testRegisterListenerSuccess() {
        ISlangTestCaseEventListener mockListener = mock(ISlangTestCaseEventListener.class);
        doNothing().when(listenerList).addListener(any(ISlangTestCaseEventListener.class));

        testCaseEventDispatchService.registerListener(mockListener);
        verify(listenerList).addListener(eq(mockListener));
    }

    @Test
    public void testUnregisterListeners() {
        LoggingSlangTestCaseEventListener loggingListener = new LoggingSlangTestCaseEventListener();
        ThreadSafeRunTestResults collector = new ThreadSafeRunTestResults();
        ISlangTestCaseEventListener[] array = new ISlangTestCaseEventListener[]{loggingListener, collector};
        doReturn(array).when(listenerList).getListeners();
        doNothing().when(listenerList).removeListener(any(ISlangTestCaseEventListener.class));

        testCaseEventDispatchService.unregisterAllListeners();
        InOrder inOrder = inOrder(listenerList);
        inOrder.verify(listenerList).removeListener(eq(loggingListener));
        inOrder.verify(listenerList).removeListener(eq(collector));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void notifyListenersSuccess() throws InterruptedException {
        final MutableInt mutableInt = new MutableInt(0);
        final MutableBoolean mutableBoolean = new MutableBoolean(false);

        ISlangTestCaseEventListener listenerIncrement = new ISlangTestCaseEventListener() {
            @Override
            public void onEvent(SlangTestCaseEvent event) {
                mutableInt.increment();
            }
        };

        ISlangTestCaseEventListener listenerIncrementTwice = new ISlangTestCaseEventListener() {
            @Override
            public void onEvent(SlangTestCaseEvent event) {
                mutableInt.add(2);
                mutableBoolean.setValue(true);
            }
        };

        TestCaseEventDispatchService localTestCaseEventDispatchService = new TestCaseEventDispatchService();
        localTestCaseEventDispatchService.initializeListeners();
        localTestCaseEventDispatchService.registerListener(listenerIncrement);
        localTestCaseEventDispatchService.registerListener(listenerIncrementTwice);

        localTestCaseEventDispatchService.notifyListeners(new SlangTestCaseEvent(new SlangTestCase("name", null, null, null, null, null, null, null, null)));

        while (mutableBoolean.isFalse()) {
            Thread.sleep(50);
        }
        // Checks that all listeners are called, order is not important
        Assert.assertEquals(3, mutableInt.getValue());
    }

}
