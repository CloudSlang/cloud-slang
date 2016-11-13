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
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Bonczidai Levente
 * @since 11/13/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CachedPrecompileServiceImplTest.Config.class)
public class CachedPrecompileServiceImplTest {

    @Autowired
    private CachedPrecompileService cachedPrecompileService;

    @Test
    public void testBasicSanity() throws Exception {
        String path = "path_1";
        Executable executableMock = mock(Executable.class);
        ExecutableModellingResult executableModellingResult =
                new ExecutableModellingResult(executableMock, new ArrayList<RuntimeException>());
        SlangSource slangSource1 = new SlangSource("abc", "one");

        CacheResult cacheResult;

        cacheResult = cachedPrecompileService.getValueFromCache(path, slangSource1);
        assertEquals(CacheValueState.MISSING, cacheResult.getState());

        cachedPrecompileService.cacheValue(path, executableModellingResult, slangSource1);
        cacheResult = cachedPrecompileService.getValueFromCache(path, slangSource1);
        assertEquals(CacheValueState.VALID, cacheResult.getState());

        SlangSource slangSource2 = new SlangSource("def", "two");

        cacheResult = cachedPrecompileService.getValueFromCache(path, slangSource2);
        assertEquals(CacheValueState.OUTDATED, cacheResult.getState());

        cachedPrecompileService.cacheValue(path, executableModellingResult, slangSource2);
        cacheResult = cachedPrecompileService.getValueFromCache(path, slangSource2);
        assertEquals(CacheValueState.VALID, cacheResult.getState());

        cachedPrecompileService.invalidateAll();
        cacheResult = cachedPrecompileService.getValueFromCache(path, slangSource2);
        assertEquals(CacheValueState.MISSING, cacheResult.getState());
    }

    public static class Config {
        @Bean
        public CachedPrecompileService cachedPrecompileService() {
            return new CachedPrecompileServiceImpl();
        }
    }

}