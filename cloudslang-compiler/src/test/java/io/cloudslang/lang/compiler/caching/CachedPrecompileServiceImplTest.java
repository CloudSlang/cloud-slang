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
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import java.lang.reflect.Field;
import junit.framework.Assert;
import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static io.cloudslang.lang.compiler.caching.CacheValueState.OUTDATED;
import static io.cloudslang.lang.compiler.caching.CacheValueState.VALID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author Bonczidai Levente
 * @since 11/13/2016
 */

@RunWith(MockitoJUnitRunner.class)
public class CachedPrecompileServiceImplTest {

    public static final String CACHE = "cache";
    @Spy
    @InjectMocks
    private CachedPrecompileServiceImpl cachedPrecompileServiceImpl;

    @Mock
    private Cache<String, CacheValue> cache;

    @Test
    public void testCacheValueSuccess() {
        String myPath = "aaa";
        ExecutableModellingResult executableModellingResult = mock(ExecutableModellingResult.class);
        SlangSource slangSource = mock(SlangSource.class);

        final MutablePair<CacheValue, Boolean> pair = new MutablePair<>();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                pair.setLeft((CacheValue) invocation.getArguments()[1]);
                return null;
            }
        }).when(cache).put(anyString(), any(CacheValue.class));

        // Tested call
        cachedPrecompileServiceImpl.cacheValue(myPath, executableModellingResult, slangSource);
        verify(cache).put(eq(myPath), same(pair.getLeft()));
    }

    @Test
    public void testCacheValueDoesNothingForNull() {
        String myPath = null;
        ExecutableModellingResult executableModellingResult = mock(ExecutableModellingResult.class);
        SlangSource slangSource = mock(SlangSource.class);

        final MutablePair<CacheValue, Boolean> pair = new MutablePair<>();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                pair.setLeft((CacheValue) invocation.getArguments()[1]);
                return null;
            }
        }).when(cache).put(anyString(), any(CacheValue.class));

        // Tested call
        cachedPrecompileServiceImpl.cacheValue(myPath, executableModellingResult, slangSource);
        verify(cache, never()).put(anyString(), any(CacheValue.class));
    }

    @Test
    public void testGetValueSuccessWithOutdated() {
        final String myPath = "bb";
        final SlangSource slangSource = mock(SlangSource.class);
        SlangSource cachedSlangSource = mock(SlangSource.class);

        CacheValue mockCacheValue = mock(CacheValue.class);

        doReturn(mockCacheValue).when(cache).getIfPresent(anyObject());
        doReturn(cachedSlangSource).when(mockCacheValue).getSource();
        doReturn(true).when(cachedPrecompileServiceImpl)
                .hasChangedSinceCached(any(SlangSource.class), any(SlangSource.class));

        // Tested call
        final CacheResult valueFromCache = cachedPrecompileServiceImpl.getValueFromCache(myPath, slangSource);

        InOrder inOrder = Mockito.inOrder(cache, cachedPrecompileServiceImpl);
        inOrder.verify(cache).getIfPresent(eq(myPath));
        inOrder.verify(cachedPrecompileServiceImpl)
                .hasChangedSinceCached(eq(slangSource), eq(cachedSlangSource));
        inOrder.verifyNoMoreInteractions();

        assertEquals(OUTDATED, valueFromCache.getState());
        assertNull(valueFromCache.getExecutableModellingResult());

    }

    @Test
    public void testGetValueSuccessWithValid() {
        final String myPath = "cc";
        final SlangSource slangSource = mock(SlangSource.class);
        SlangSource cachedSlangSource = mock(SlangSource.class);

        CacheValue mockCacheValue = mock(CacheValue.class);

        doReturn(mockCacheValue).when(cache).getIfPresent(anyObject());
        doReturn(cachedSlangSource).when(mockCacheValue).getSource();
        doReturn(false).when(cachedPrecompileServiceImpl)
                .hasChangedSinceCached(any(SlangSource.class), any(SlangSource.class));
        ExecutableModellingResult mockExecutionModellingResult = mock(ExecutableModellingResult.class);
        doReturn(mockExecutionModellingResult).when(mockCacheValue).getExecutableModellingResult();

        // Tested call
        final CacheResult valueFromCache = cachedPrecompileServiceImpl.getValueFromCache(myPath, slangSource);

        InOrder inOrder = Mockito.inOrder(cache, cachedPrecompileServiceImpl);
        inOrder.verify(cache).getIfPresent(eq(myPath));
        inOrder.verify(cachedPrecompileServiceImpl)
                .hasChangedSinceCached(eq(slangSource), eq(cachedSlangSource));
        inOrder.verifyNoMoreInteractions();

        assertEquals(VALID, valueFromCache.getState());
        assertSame(mockExecutionModellingResult, valueFromCache.getExecutableModellingResult());
    }

    @Test
    public void testInvalidateEntryWithRealPath() {
        String myPath = "invalidatepath";
        doNothing().when(cache).invalidate(anyString());

        // Tested call
        cachedPrecompileServiceImpl.invalidateEntry(myPath);

        verify(cache).invalidate(eq(myPath));
    }

    @Test
    public void testInvalidateEntryWithNullPath() {
        doNothing().when(cache).invalidate(anyString());

        // Tested call
        cachedPrecompileServiceImpl.invalidateEntry(null);

        verify(cache, never()).invalidate(anyString());
    }

    @Test
    public void testInvalidateAll() {
        doNothing().when(cache).invalidateAll();

        // Tested call
        cachedPrecompileServiceImpl.invalidateAll();

        verify(cache).invalidateAll();
    }

    @Test
    public void testInvalidateAllThrowsException() {
        RuntimeException whatever = new RuntimeException("whatever");
        doThrow(whatever).when(cache).invalidateAll();

        // Tested call
        try {
            cachedPrecompileServiceImpl.invalidateAll();
            fail("Expecting to throw exception");
        } catch (Exception exc) {
            assertSame(whatever, exc);
        }

    }

    @Test
    public void testInit() throws Exception {
        CachedPrecompileServiceImpl cachedPrecompileService = new CachedPrecompileServiceImpl();
        cachedPrecompileService.init();

        Class<? extends CachedPrecompileServiceImpl> cachePrecompileClass = cachedPrecompileService.getClass();
        Field cachePrecompileClassField = cachePrecompileClass.getDeclaredField(CACHE);

        cachePrecompileClassField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Cache<String, CacheValue> internalCache = (Cache<String, CacheValue>) cachePrecompileClassField
                .get(cachedPrecompileService);
        Assert.assertNotNull(internalCache);
    }

}
