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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.digest.DigestUtils;

import javax.annotation.PostConstruct;

public class CachedPrecompileServiceImpl implements CachedPrecompileService {

    private Cache<String, CacheValue> cache;

    @PostConstruct
    public void init() {
        cache = CacheBuilder.newBuilder()
                .maximumSize(500)
                .concurrencyLevel(2 * Runtime.getRuntime().availableProcessors())
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public void cacheValue(String path, ExecutableModellingResult modellingResult, SlangSource source) {
        if (path != null) {
            CacheValue cacheValue = new CacheValue(source, modellingResult);
            cache.put(path, cacheValue);
        }
    }

    @Override
    public CacheResult getValueFromCache(String path, SlangSource currentSource) {
        if (path == null) {
            return null;
        }
        CacheValue cachedValue = cache.getIfPresent(path);
        CacheValueState state;
        ExecutableModellingResult executableModellingResult = null;

        if (cachedValue == null) {
            state = CacheValueState.MISSING;
        } else {
            if (hasChangedSinceCached(currentSource, cachedValue.getSource())) {
                state = CacheValueState.OUTDATED;
            } else {
                state = CacheValueState.VALID;
                executableModellingResult = cachedValue.getExecutableModellingResult();
            }
        }
        return new CacheResult(state, executableModellingResult);
    }

    @Override
    public void invalidateEntry(String path) {
        if (path != null) {
            cache.invalidate(path);
        }
    }

    @Override
    public void invalidateAll() {
        cache.invalidateAll();
    }

    boolean hasChangedSinceCached(SlangSource source1, SlangSource source2) {
        String source1AsStr = source1.toString();
        String source2AsStr = source2.toString();
        return (source1AsStr.length() != source2AsStr.length()) ||
                (!DigestUtils.md5Hex(source1AsStr).equals(DigestUtils.md5Hex(source2AsStr))) ||
                (!DigestUtils.sha256Hex(source1AsStr).equals(DigestUtils.sha256Hex(source2AsStr)));
    }

}
