/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.newyaml.impl;


import io.cloudslang.lang.compiler.newyaml.ViburYamlFactory;
import io.cloudslang.lang.compiler.newyaml.YamlFactoryService;
import io.cloudslang.lang.compiler.newyaml.YamlPoolService;
import jakarta.annotation.PreDestroy;
import org.vibur.objectpool.ConcurrentPool;
import org.vibur.objectpool.util.ConcurrentLinkedQueueCollection;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static java.lang.Integer.getInteger;
import static java.lang.Long.getLong;
import static java.time.Duration.ofMinutes;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class YamlPoolServiceImpl implements YamlPoolService {

    // This state is intentional
    private final ConcurrentPool<Yaml> yamlConcurrentPool;
    private final long checkoutTimeoutMillis;

    public YamlPoolServiceImpl(YamlFactoryService yamlFactoryService) {
        this.yamlConcurrentPool = doCreateAndGetPool(getInteger("yaml.poolSize", 5),
                yamlFactoryService::createYamlForParsing
        );
        this.checkoutTimeoutMillis = getLong("yaml.checkoutTimeoutSeconds", ofMinutes(10).toMillis());
    }

    @Override
    public Yaml tryTakeYamlWithTimeout(long timeout) {
        return yamlConcurrentPool.tryTake(timeout, MILLISECONDS);
    }

    @Override
    public Yaml tryTakeYamlWithDefaultTimeout() {
        return yamlConcurrentPool.tryTake(checkoutTimeoutMillis, MILLISECONDS);
    }

    @Override
    public void restoreYaml(Yaml poolable) {
        yamlConcurrentPool.restore(poolable);
    }

    @PreDestroy
    public void close() {
        doClosePool(yamlConcurrentPool);
    }

    private ConcurrentPool<Yaml> doCreateAndGetPool(final int poolSize,
                                                    final Supplier<Yaml> supplier) {
        ConcurrentLinkedQueueCollection<Yaml> collection = new ConcurrentLinkedQueueCollection<>();

        List<CompletableFuture<Void>> completableFutureList = new ArrayList<>(poolSize);
        for (int counter = 0; counter < poolSize; counter++) {
            CompletableFuture<Void> completableFuture = runAsync(() -> collection.offerLast(supplier.get()));
            completableFutureList.add(completableFuture);
        }
        CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[0]))
                .join();

        return new ConcurrentPool<>(collection,
                new ViburYamlFactory(supplier),
                poolSize,
                poolSize,
                false
        );
    }

    private void doClosePool(ConcurrentPool<Yaml> pool) {
        try {
            pool.close();
        } catch (RuntimeException ignored) {
        }
    }

}
