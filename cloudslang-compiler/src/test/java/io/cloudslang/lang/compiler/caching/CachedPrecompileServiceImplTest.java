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
        SlangSource slangSource_1 = new SlangSource("abc", "one");
        SlangSource slangSource_2 = new SlangSource("def", "two");

        CacheResult cacheResult;

        cacheResult = cachedPrecompileService.getValueFromCache(path, slangSource_1);
        assertEquals(CacheValueState.MISSING, cacheResult.getState());

        cachedPrecompileService.cacheValue(path, executableModellingResult, slangSource_1);
        cacheResult = cachedPrecompileService.getValueFromCache(path, slangSource_1);
        assertEquals(CacheValueState.VALID, cacheResult.getState());

        cacheResult = cachedPrecompileService.getValueFromCache(path, slangSource_2);
        assertEquals(CacheValueState.OUTDATED, cacheResult.getState());

        cachedPrecompileService.cacheValue(path, executableModellingResult, slangSource_2);
        cacheResult = cachedPrecompileService.getValueFromCache(path, slangSource_2);
        assertEquals(CacheValueState.VALID, cacheResult.getState());

        cachedPrecompileService.invalidateAll();
        cacheResult = cachedPrecompileService.getValueFromCache(path, slangSource_2);
        assertEquals(CacheValueState.MISSING, cacheResult.getState());
    }

    public static class Config {
        @Bean
        public CachedPrecompileService cachedPrecompileService() {
            return new CachedPrecompileServiceImpl();
        }
    }

}