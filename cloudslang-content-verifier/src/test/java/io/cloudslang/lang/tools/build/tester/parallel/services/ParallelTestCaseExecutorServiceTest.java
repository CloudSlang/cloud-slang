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


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ParallelTestCaseExecutorServiceTest {

    @Spy
    @InjectMocks
    private ParallelTestCaseExecutorService parallelTestCaseExecutorService;

    @Mock
    private ExecutorService threadPoolExecutor;

    @Test
    public void testSubmitSuccess() {
        Runnable mockRunnable = mock(Runnable.class);
        Future mockFuture = mock(Future.class);

        doReturn(mockFuture).when(threadPoolExecutor).submit(any(Runnable.class));

        Future<?> future = parallelTestCaseExecutorService.submitTestCase(mockRunnable);
        verify(threadPoolExecutor).submit(eq(mockRunnable));
        assertTrue(future == mockFuture);
    }

    @Test
    public void testSubmitException() {
        Runnable mockRunnable = mock(Runnable.class);
        RuntimeException mockException = new RuntimeException("message");
        doThrow(mockException).when(threadPoolExecutor).submit(any(Runnable.class));

        try {
            parallelTestCaseExecutorService.submitTestCase(mockRunnable);
            fail("Expecting exception to be thrown");
        } catch (RuntimeException ex) {
            assertTrue(ex == mockException);
            assertEquals(mockException.getMessage(), ex.getMessage());
        }
        verify(threadPoolExecutor).submit(eq(mockRunnable));
    }

    @Test
    public void testDestroySuccess() throws Exception {
        doNothing().when(threadPoolExecutor).shutdown();
        parallelTestCaseExecutorService.destroy();
        verify(threadPoolExecutor).shutdown();
    }

}
