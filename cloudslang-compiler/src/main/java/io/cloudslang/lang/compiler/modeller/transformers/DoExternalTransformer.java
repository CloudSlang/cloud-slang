package io.cloudslang.lang.compiler.modeller.transformers;

import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.entities.bindings.Argument;

import java.util.List;
import java.util.Map;

public class DoExternalTransformer extends DoTransformer implements Transformer<Map<String, Object>, List<Argument>> {

    @Override
    public String keyToTransform() {
        return SlangTextualKeys.DO_EXTERNAL_KEY;
    }
}
