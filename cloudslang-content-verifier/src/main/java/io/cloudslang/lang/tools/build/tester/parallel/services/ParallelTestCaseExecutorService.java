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
import org.springframework.beans.factory.DisposableBean;

import javax.annotation.PostConstruct;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static java.lang.System.getProperty;
import static java.util.concurrent.Executors.newFixedThreadPool;

public class ParallelTestCaseExecutorService implements DisposableBean {

    public static final String SLANG_TEST_RUNNER_THREAD_COUNT = "slang.test.runner.thread.count";
    private ExecutorService threadPoolExecutor;

    @PostConstruct
    public void initializeExecutor() {
        threadPoolExecutor = newFixedThreadPool(parseInt(
                getProperty(SLANG_TEST_RUNNER_THREAD_COUNT, valueOf(Runtime.getRuntime().availableProcessors()))
        ));
    }

    public Future<?> submitTestCase(Runnable runnable) {
        return threadPoolExecutor.submit(runnable);
    }

    @Override
    public void destroy() throws Exception {
        threadPoolExecutor.shutdown();
    }

}
