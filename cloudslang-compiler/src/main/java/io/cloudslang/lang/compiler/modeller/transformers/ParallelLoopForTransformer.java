/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.transformers;

import io.cloudslang.lang.compiler.CompilerConstants;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.entities.ParallelLoopStatement;
import io.cloudslang.lang.entities.SensitivityLevel;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.cloudslang.lang.compiler.SlangTextualKeys.FOR_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.MAX_THROTTLE_KEY;

public class ParallelLoopForTransformer extends AbstractForTransformer
        implements Transformer<Object, ParallelLoopStatement> {

    @Override
    public TransformModellingResult<ParallelLoopStatement> transform(Object rawData) {
        return transform(rawData, CompilerConstants.DEFAULT_SENSITIVITY_LEVEL);
    }

    @Override
    public TransformModellingResult<ParallelLoopStatement> transform(Object rawData,
                                                                     SensitivityLevel sensitivityLevel) {
        String value = null;
        String throttle = null;

        if (rawData instanceof Map) {
            Map<String, Object> parallelLoopRawData = (Map<String, Object>) rawData;
            value = Objects.toString(parallelLoopRawData.get(FOR_KEY), null);
            throttle = Objects.toString(parallelLoopRawData.get(MAX_THROTTLE_KEY), null);
        }

        // throttle can be null
        return transformToParallelLoopStatement(value, throttle);
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.singletonList(Scope.BEFORE_STEP);
    }

    @Override
    public String keyToTransform() {
        return SlangTextualKeys.PARALLEL_LOOP_KEY;
    }

}
