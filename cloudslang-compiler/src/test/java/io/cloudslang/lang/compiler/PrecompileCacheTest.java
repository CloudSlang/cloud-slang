package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.modeller.SlangModeller;
import io.cloudslang.lang.compiler.parser.YamlParser;
import io.cloudslang.lang.compiler.scorecompiler.ScoreCompiler;
import io.cloudslang.lang.compiler.validator.CompileValidator;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URL;

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

        compiler.enablePrecompileCache();

        compiler.preCompileSource(slangSource);

        InOrder inOrderCache = inOrder(cachedPrecompileService);
        inOrderCache.verify(cachedPrecompileService).cleanUp();
        inOrderCache.verify(cachedPrecompileService).getValueFromCache(slangSource.getFilePath());
        inOrderCache.verify(cachedPrecompileService).cacheResult(slangSource.getFilePath(), null);
        inOrderCache.verifyNoMoreInteractions();

        compiler.disablePrecompileCache();
        inOrderCache.verify(cachedPrecompileService).cleanUp();

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
}
