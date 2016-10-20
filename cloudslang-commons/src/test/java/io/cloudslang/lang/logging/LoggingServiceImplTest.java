/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.logging;


import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.log4j.Level;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public class LoggingServiceImplTest {

    private static final String SINGLE_THREAD_EXECUTOR = "singleThreadExecutor";
    private static final String LATEST_TASK = "latestTask";
    @InjectMocks
    @Spy
    private LoggingServiceImpl loggingService;

    @Mock
    private ThreadPoolExecutor singleThreadExecutor;

    @Test
    public void testInitialize() throws Exception {
        LoggingServiceImpl localLoggingService = new LoggingServiceImpl();

        // Tested call
        localLoggingService.initialize();

        Class<? extends LoggingServiceImpl> loggingServiceClass = localLoggingService.getClass();
        Field loggingServiceClassDeclaredField = loggingServiceClass.getDeclaredField(SINGLE_THREAD_EXECUTOR);

        loggingServiceClassDeclaredField.setAccessible(true);
        Object singleThreadExecutor = loggingServiceClassDeclaredField.get(localLoggingService);

        assertTrue(singleThreadExecutor instanceof ThreadPoolExecutor);
        assertEquals(1, ((ThreadPoolExecutor) singleThreadExecutor).getMaximumPoolSize());
    }

    @Test
    public void testDestroy() throws Exception {
        LoggingServiceImpl localLoggingService = new LoggingServiceImpl();
        localLoggingService.initialize();

        Class<? extends LoggingServiceImpl> loggingServiceClass = localLoggingService.getClass();
        Field loggingServiceClassDeclaredField = loggingServiceClass.getDeclaredField(SINGLE_THREAD_EXECUTOR);

        loggingServiceClassDeclaredField.setAccessible(true);
        Object singleThreadExecutor = loggingServiceClassDeclaredField.get(localLoggingService);
        assertNotNull(singleThreadExecutor);

        // Tested call
        localLoggingService.destroy();

        singleThreadExecutor = loggingServiceClassDeclaredField.get(localLoggingService);
        assertNull(singleThreadExecutor);
    }

    @Test
    public void testLogEventWithTwoParams() {
        final List<Runnable> runnableList = new ArrayList<>();
        final MutableInt mutableInt = new MutableInt(0);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                mutableInt.increment();
                Object[] arguments = invocationOnMock.getArguments();
                runnableList.add((Runnable) arguments[0]);
                if (mutableInt.getValue() == 1) {
                    return ConcurrentUtils.constantFuture("aaa");
                } else if (mutableInt.getValue() == 2) {
                    return ConcurrentUtils.constantFuture("bbb");
                } else {
                    return null;
                }
            }
        }).when(singleThreadExecutor).submit(Mockito.any(Runnable.class));

        // Tested calls
        loggingService.logEvent(Level.INFO, "aaa");
        loggingService.logEvent(Level.ERROR, "bbb");

        assertEquals(2, runnableList.size());

        assertTrue(runnableList.get(0) instanceof LoggingServiceImpl.LoggingDetailsRunnable);
        assertTrue(runnableList.get(1) instanceof LoggingServiceImpl.LoggingDetailsRunnable);

        assertEquals(new LoggingServiceImpl.LoggingDetailsRunnable(Level.INFO, "aaa"), runnableList.get(0));
        assertEquals(new LoggingServiceImpl.LoggingDetailsRunnable(Level.ERROR, "bbb"), runnableList.get(1));
    }

    @Test
    public void testLogEventWithThreeParams() {
        final List<Runnable> runnableList = new ArrayList<>();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] arguments = invocationOnMock.getArguments();
                runnableList.add((Runnable) arguments[0]);
                return null;
            }
        }).when(singleThreadExecutor).submit(Mockito.any(Runnable.class));

        final RuntimeException ex1 = new RuntimeException("some exception 1");
        final IllegalArgumentException ex2 = new IllegalArgumentException("some value does not respect its contract");
        final IllegalStateException ex3 = new IllegalStateException("state is illegal");
        final IllegalAccessException ex4 = new IllegalAccessException("Access denied");
        final String message1 = "message1";
        final String message2 = "message2";
        final String message3 = "message3";
        final String message4 = "message4";

        // Tested calls
        loggingService.logEvent(Level.DEBUG, message1, ex1);
        loggingService.logEvent(Level.TRACE, message2, ex2);
        loggingService.logEvent(Level.ERROR, message3, ex3);
        loggingService.logEvent(Level.FATAL, message4, ex4);

        assertEquals(4, runnableList.size());
        assertTrue(runnableList.get(0) instanceof LoggingServiceImpl.LoggingDetailsRunnable);
        assertTrue(runnableList.get(1) instanceof LoggingServiceImpl.LoggingDetailsRunnable);
        assertTrue(runnableList.get(2) instanceof LoggingServiceImpl.LoggingDetailsRunnable);
        assertTrue(runnableList.get(3) instanceof LoggingServiceImpl.LoggingDetailsRunnable);

        assertEquals(new LoggingServiceImpl.LoggingDetailsRunnable(Level.DEBUG, message1, ex1), runnableList.get(0));
        assertEquals(new LoggingServiceImpl.LoggingDetailsRunnable(Level.TRACE, message2, ex2), runnableList.get(1));
        assertEquals(new LoggingServiceImpl.LoggingDetailsRunnable(Level.ERROR, message3, ex3), runnableList.get(2));
        assertEquals(new LoggingServiceImpl.LoggingDetailsRunnable(Level.FATAL, message4, ex4), runnableList.get(3));
    }

}
