package io.cloudslang.lang.compiler.modeller.transformers;

import com.google.common.collect.Lists;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DependenciesTransformerTest {
    @Test
    public void testTransformWithoutDependencies() throws Exception {
        DependenciesTransformer transformer = new DependenciesTransformer();
        List<String> transform = transformer.transform(null);
        assertNotNull(transform);
        assertTrue(transform.isEmpty());
    }

    @Test
    public void testTransformWithEmptyDependencies() throws Exception {
        DependenciesTransformer transformer = new DependenciesTransformer();

        List<String> list = Lists.newArrayList();
        List<String> transform = transformer.transform(list);
        assertNotNull(transform);
        assertTrue(transform.isEmpty());
        assertEquals(list, transform);
    }

    @Test
    public void testTransformWithNotEmptyDependencies() throws Exception {
        DependenciesTransformer transformer = new DependenciesTransformer();

        String dep1 = "g1:a2:v3";
        String dep2 = "g2:a3:v4";
        List<String> list = Lists.newArrayList(dep1, dep2);
        List<String> transform = transformer.transform(list);
        assertNotNull(transform);
        assertTrue(transform.size() == 2);
        assertEquals(dep1, transform.get(0));
        assertEquals(dep2, transform.get(1));
        assertEquals(list, transform);
    }

    @Test
    public void testTransformer() throws Exception {
        DependenciesTransformer transformer = new DependenciesTransformer();
        List<Transformer.Scope> scopes = transformer.getScopes();
        assertTrue(scopes.size() == 1);
        assertEquals(Transformer.Scope.ACTION, scopes.get(0));
        assertEquals(SlangTextualKeys.PYTHON_ACTION_DEPENDENCIES_KEY, transformer.keyToTransform());
    }

}
