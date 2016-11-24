/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.cli.services;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.apache.commons.lang3.mutable.MutableInt;
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
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsolePrinterImplTest {

    private static final String SINGLE_THREAD_EXECUTOR = "singleThreadExecutor";
    public static final String LAST_TASK = "lastTask";

    @InjectMocks
    @Spy
    private ConsolePrinterImpl consolePrinter;

    @Mock
    private ThreadPoolExecutor singleThreadExecutor;

    @Mock
    private Future<?> lastTask;

    @Test
    public void testInitialize() throws Exception {
        ConsolePrinterImpl consolePrinter = new ConsolePrinterImpl();

        consolePrinter.initialize();

        Class<? extends ConsolePrinterImpl> consolePrinterClass = consolePrinter.getClass();
        Field consolePrinterClassExecutorDeclaredField = consolePrinterClass.getDeclaredField(SINGLE_THREAD_EXECUTOR);

        consolePrinterClassExecutorDeclaredField.setAccessible(true);
        Object singleThreadExecutor = consolePrinterClassExecutorDeclaredField.get(consolePrinter);

        assertTrue(singleThreadExecutor instanceof ThreadPoolExecutor);
        assertEquals(1, ((ThreadPoolExecutor) singleThreadExecutor).getMaximumPoolSize());
    }

    @Test
    public void testDestroy() throws Exception {
        ConsolePrinterImpl consolePrinter = new ConsolePrinterImpl();
        consolePrinter.initialize();

        Class<? extends ConsolePrinterImpl> consolePrinterClass = consolePrinter.getClass();
        Field consolePrinterClassDeclaredField = consolePrinterClass.getDeclaredField(SINGLE_THREAD_EXECUTOR);

        consolePrinterClassDeclaredField.setAccessible(true);
        Object singleThreadExecutor = consolePrinterClassDeclaredField.get(consolePrinter);
        assertNotNull(singleThreadExecutor);

        consolePrinter.destroy();

        singleThreadExecutor = consolePrinterClassDeclaredField.get(consolePrinter);
        assertNull(singleThreadExecutor);
    }

    @Test
    public void testWaitForAllPrintTasksToFinish() throws Exception {
        when(lastTask.get(1, TimeUnit.MINUTES)).thenReturn(null);

        consolePrinter.waitForAllPrintTasksToFinish();

        verify(lastTask, times(1)).get(1, TimeUnit.MINUTES);
    }

    @Test
    public void testNullPointerExceptionNotThrown() throws NoSuchFieldException, IllegalAccessException {
        ConsolePrinterImpl consolePrinter = new ConsolePrinterImpl();
        consolePrinter.initialize();

        Class<? extends ConsolePrinterImpl> consolePrinterClass = consolePrinter.getClass();
        Field consolePrinterClassDeclaredField = consolePrinterClass.getDeclaredField(LAST_TASK);

        consolePrinterClassDeclaredField.setAccessible(true);
        Object lastTask = consolePrinterClassDeclaredField.get(consolePrinter);
        assertNull(lastTask);

        consolePrinter.waitForAllPrintTasksToFinish();
    }

    @Test
    public void testConsolePrint() throws Exception {
        final List<Runnable> runnableList = new ArrayList<>();
        final MutableInt mutableInt = new MutableInt(0);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                mutableInt.increment();
                Object[] arguments = invocationOnMock.getArguments();
                runnableList.add((Runnable) arguments[0]);
                if (mutableInt.getValue() == 1) {
                    return ConcurrentUtils.constantFuture("firstMessage");
                } else if (mutableInt.getValue() == 2) {
                    return ConcurrentUtils.constantFuture("secondMessage");
                } else {
                    return null;
                }
            }
        }).when(singleThreadExecutor).submit(Mockito.any(Runnable.class));

        consolePrinter.printWithColor(GREEN, "firstMessage");
        Future lastFuture = consolePrinter.printWithColor(GREEN, "secondMessage");

        assertEquals("secondMessage", lastFuture.get());

        assertEquals(2, runnableList.size());

        assertTrue(runnableList.get(0) instanceof ConsolePrinterImpl.ConsolePrinterRunnable);
        assertTrue(runnableList.get(1) instanceof ConsolePrinterImpl.ConsolePrinterRunnable);

        assertEquals(new ConsolePrinterImpl.ConsolePrinterRunnable(GREEN, "firstMessage"), runnableList.get(0));
        assertEquals(new ConsolePrinterImpl.ConsolePrinterRunnable(GREEN, "secondMessage"), runnableList.get(1));
    }
}
