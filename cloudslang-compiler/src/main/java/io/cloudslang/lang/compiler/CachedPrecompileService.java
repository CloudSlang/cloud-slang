/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Created by bancl on 10/10/2016.
 */
public class CachedPrecompileService {

    private Cache<String, ExecutableModellingResult> cache;

    @PostConstruct
    public void init() {
        cache = CacheBuilder.newBuilder()
                .maximumSize(500)
                .concurrencyLevel(2 * Runtime.getRuntime().availableProcessors())
                .expireAfterWrite(60, TimeUnit.MINUTES).build();
    }

    public void cacheResult(String key, ExecutableModellingResult value) {
        if (key != null) {
            cache.put(key, value);
        }
    }

    public ExecutableModellingResult getValueFromCache(String key) {
        if (key == null) {
            return null;
        }
        return cache.getIfPresent(key);
    }

    public void cleanUp() {
        cache.cleanUp();
    }
}
