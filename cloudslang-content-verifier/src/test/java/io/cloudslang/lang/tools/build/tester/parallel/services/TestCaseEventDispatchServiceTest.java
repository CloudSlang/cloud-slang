/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.tester.parallel.services;


import io.cloudslang.lang.tools.build.tester.ISlangTestCaseEventListener;
import io.cloudslang.lang.tools.build.tester.parallel.report.LoggingSlangTestCaseEventListener;
import io.cloudslang.lang.tools.build.tester.parallel.report.ThreadSafeRunTestResults;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.BeginSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.FailedSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.PassedSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.SkippedSlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parallel.testcaseevents.SlangTestCaseEvent;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.lang3.event.EventListenerSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
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

        localTestCaseEventDispatchService.notifyListeners(new SlangTestCaseEvent(
                new SlangTestCase("name", null, null, null, null, null, null, null, null)));

        while (mutableBoolean.isFalse()) {
            Thread.sleep(50);
        }
        // Checks that all listeners are called, order is not important
        assertEquals(3, mutableInt.getValue());
    }

    @Test
    public void notifyListenersAllEventTypes() throws InterruptedException {
        final MutableInt mutableInt = new MutableInt(0);
        final MutableBoolean mutableBoolean = new MutableBoolean(false);
        final String failureMessage = "message";
        final RuntimeException ex = new RuntimeException("ex");

        ISlangTestCaseEventListener listenerIncrement = new ISlangTestCaseEventListener() {
            @Override
            public synchronized void onEvent(SlangTestCaseEvent event) {
                mutableInt.increment();
                Object value = mutableInt.getValue();
                if (value.equals(1)) {
                    assertEquals(BeginSlangTestCaseEvent.class, event.getClass());
                } else if (value.equals(2)) {
                    assertEquals(FailedSlangTestCaseEvent.class, event.getClass());
                    assertEquals(failureMessage, ((FailedSlangTestCaseEvent) event).getFailureReason());
                    assertEquals(ex, ((FailedSlangTestCaseEvent) event).getFailureException());
                } else if (value.equals(3)) {
                    assertEquals(PassedSlangTestCaseEvent.class, event.getClass());
                } else if (value.equals(4)) {
                    assertEquals(SkippedSlangTestCaseEvent.class, event.getClass());
                } else if (value.equals(5)) {
                    assertEquals(SlangTestCaseEvent.class, event.getClass());
                    mutableBoolean.setValue(true);
                }
            }
        };


        TestCaseEventDispatchService localTestCaseEventDispatchService = new TestCaseEventDispatchService();
        localTestCaseEventDispatchService.initializeListeners();
        localTestCaseEventDispatchService.registerListener(listenerIncrement);

        SlangTestCase testCase = new SlangTestCase("name", null, null, null, null, null, null, null, null);
        localTestCaseEventDispatchService.notifyListeners(new BeginSlangTestCaseEvent(testCase));
        localTestCaseEventDispatchService.notifyListeners(new FailedSlangTestCaseEvent(testCase, failureMessage, ex));
        localTestCaseEventDispatchService.notifyListeners(new PassedSlangTestCaseEvent(testCase));
        localTestCaseEventDispatchService.notifyListeners(new SkippedSlangTestCaseEvent(testCase));
        localTestCaseEventDispatchService.notifyListeners(new SlangTestCaseEvent(testCase));

        while (mutableBoolean.isFalse()) {
            Thread.sleep(50);
        }
        // Checks that all listeners are called, order is not important
        assertEquals(5, mutableInt.getValue());
    }

}
