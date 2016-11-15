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

import io.cloudslang.lang.compiler.caching.CachedPrecompileService;
import io.cloudslang.lang.compiler.modeller.SlangModeller;
import io.cloudslang.lang.compiler.parser.YamlParser;
import io.cloudslang.lang.compiler.scorecompiler.ScoreCompiler;
import io.cloudslang.lang.compiler.validator.CompileValidator;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidator;
import java.net.URL;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.inOrder;

@RunWith(MockitoJUnitRunner.class)
public class PrecompileCacheTest {

    @Spy
    @InjectMocks
    private SlangCompilerImpl compiler;

    @Mock
    private YamlParser yamlParser;

    @Mock
    private SlangModeller slangModeller;

    @Mock
    private ScoreCompiler scoreCompiler;

    @Mock
    private CompileValidator compileValidator;

    @Mock
    private SystemPropertyValidator systemPropertyValidator;

    @Mock
    private CachedPrecompileService cachedPrecompileService;

    @Test
    public void testPrecompileCacheEnabled() throws Exception {
        URL resource = getClass().getResource("/corrupted/op_without_namespace.sl");
        SlangSource slangSource = SlangSource.fromFile(resource.toURI());

        compiler.preCompileSource(slangSource, PrecompileStrategy.WITH_CACHE);

        InOrder inOrderCache = inOrder(cachedPrecompileService);
        inOrderCache.verify(cachedPrecompileService).getValueFromCache(slangSource.getFilePath(), slangSource);
        inOrderCache.verify(cachedPrecompileService).cacheValue(slangSource.getFilePath(), null, slangSource);
        inOrderCache.verifyNoMoreInteractions();

        compiler.preCompileSource(slangSource);

        inOrderCache.verifyNoMoreInteractions();
    }

    @Test
    public void testPrecompileCacheDisabled() throws Exception {
        URL resource = getClass().getResource("/corrupted/op_without_namespace.sl");
        SlangSource slangSource = SlangSource.fromFile(resource.toURI());

        compiler.preCompileSource(slangSource);

        InOrder inOrderCache = inOrder(cachedPrecompileService);
        inOrderCache.verifyNoMoreInteractions();
    }

    @Test
    public void testCacheCleanupIsInvoked() throws Exception {
        compiler.invalidateAllInPreCompileCache();

        InOrder inOrderCache = inOrder(cachedPrecompileService);
        inOrderCache.verify(cachedPrecompileService).invalidateAll();
        inOrderCache.verifyNoMoreInteractions();
    }
}