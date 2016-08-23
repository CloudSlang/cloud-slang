package io.cloudslang.lang.tools.build.tester.parallel.services;


import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class ParallelTestCaseExecutorService implements DisposableBean {

    private ExecutorService threadPoolExecutor;

    @PostConstruct
    public void initializeExecutor() {
        threadPoolExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public <T> Future<T> submitTestCase(Callable<T> callable) {
        return threadPoolExecutor.submit(callable);
    }

    public Future<?> submitTestCase(Runnable runnable) {
        return threadPoolExecutor.submit(runnable);
    }

    @Override
    public void destroy() throws Exception {
        threadPoolExecutor.shutdown();
    }
}
