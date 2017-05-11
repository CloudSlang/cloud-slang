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

import io.cloudslang.lang.entities.SensitivityLevel;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.entities.LoopStatement;
import java.util.Collections;
import java.util.List;

public class ForTransformer extends AbstractForTransformer implements Transformer<String, LoopStatement> {

    @Override
    public TransformModellingResult<LoopStatement> transform(String rawData) {
        return transformToLoopStatement(rawData, false);
    }

    @Override
    public TransformModellingResult<LoopStatement> transform(String rawData, SensitivityLevel sensitivityLevel) {
        return transform(rawData);
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.singletonList(Scope.BEFORE_STEP);
    }

    @Override
    public String keyToTransform() {
        return SlangTextualKeys.FOR_KEY;
    }

}
