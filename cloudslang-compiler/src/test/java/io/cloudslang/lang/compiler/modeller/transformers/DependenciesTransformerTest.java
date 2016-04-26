package io.cloudslang.lang.compiler.modeller.transformers;

import com.google.common.collect.Lists;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DependenciesTransformerTest {
    @Test
    public void testTransformWithoutDependencies() throws Exception {
        DependenciesTransformer transformer = new DependenciesTransformer();
        assertTrue(transformer.transform(null).isEmpty());
    }

    @Test
    public void testTransformWithEmptyDependencies() throws Exception {
        DependenciesTransformer transformer = new DependenciesTransformer();

        List<String> list = Lists.newArrayList();
        List<String> transform = transformer.transform(list);
        assertTrue(transform.isEmpty());
        assertEquals(list, transform);
    }

    @Test
    public void testTransformer() throws Exception {
        DependenciesTransformer transformer = new DependenciesTransformer();
        List<Transformer.Scope> scopes = transformer.getScopes();
        assertTrue(scopes.size() == 1);
        assertEquals(Transformer.Scope.ACTION, scopes.get(0));
        assertEquals(SlangTextualKeys.ACTION_DEPENDENCIES, transformer.keyToTransform());
    }

}
