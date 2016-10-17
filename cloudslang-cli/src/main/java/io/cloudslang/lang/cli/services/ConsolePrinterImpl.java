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

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * Created by bancl on 10/17/2016.
 */
@Service
public class ConsolePrinterImpl implements ConsolePrinter, DisposableBean {

    private ThreadPoolExecutor singleThreadExecutor;

    private List<Future<?>> tasks;

    @PostConstruct
    public void initialize() {
        singleThreadExecutor = (ThreadPoolExecutor) newFixedThreadPool(1); // one thread only
        tasks = new ArrayList<>();
    }

    @Override
    public void destroy() throws Exception {
        singleThreadExecutor.shutdown();
        singleThreadExecutor = null;
    }

    @Override
    public synchronized void waitForAllPrintTasksToFinish() {
        for (Future<?> future : tasks) {
            try {
                future.get(1, TimeUnit.MINUTES);
            } catch (InterruptedException | ExecutionException | TimeoutException ignore) {
            }
        }
    }

    @Override
    public synchronized Future<?> printWithColor(final Ansi.Color color, final String message) {
        try {
            Future<?> future = singleThreadExecutor.submit(new ConsolePrinterRunnable(color, message));
            tasks.add(future);
            return future;
        } catch (RejectedExecutionException ignore) {
        }
        return null;
    }

    private static class ConsolePrinterRunnable implements Runnable {

        private final Ansi.Color color;
        private final String message;

        ConsolePrinterRunnable(Ansi.Color color, String message) {
            this.color = color;
            this.message = message;
        }

        @Override
        public void run() {
            try {
                AnsiConsole.out().print(ansi().fg(color).a(message).newline());
                AnsiConsole.out().print(ansi().fg(Ansi.Color.WHITE));
            } catch (Exception ignore) {
                // so that this thread does not die
            }
        }
    }

}
