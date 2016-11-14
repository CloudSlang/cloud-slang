/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.caching;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;

/**
 * @author Bonczidai Levente
 * @since 11/11/2016
 */
public interface CachedPrecompileService {
    /**
     * Cache a value.
     *
     * @param path            the key for caching: path from source
     * @param modellingResult actual value to cache
     * @param source          used to detect changes since last cached
     */
    void cacheValue(String path, ExecutableModellingResult modellingResult, SlangSource source);

    /**
     * Get a value from the cache.
     *
     * @param path          the key for caching: path from source
     * @param currentSource used to detect changes since last cached
     * @return {@link CacheResult} or null if key cannot be retrieved
     */
    CacheResult getValueFromCache(String path, SlangSource currentSource);

    /**
     * Remove a value associated with this key from the cache.
     *
     * @param path the key for caching: path from source
     */
    void invalidateEntry(String path);

    /**
     * Remove all cached values.
     */
    void invalidateAll();
}
