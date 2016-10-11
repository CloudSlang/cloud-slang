package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import org.springframework.stereotype.Component;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Created by bancl on 10/10/2016.
 */
@Component
public class CachedPrecompileService {

    private Cache<String, ExecutableModellingResult> cache;

    @PostConstruct
    public void init() {
        cache = CacheBuilder.newBuilder()
                .maximumSize(500)
                .concurrencyLevel(10)
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
