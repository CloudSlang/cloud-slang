/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.transformers;

import io.cloudslang.lang.compiler.modeller.result.BasicTransformModellingResult;
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.entities.ScoreLangConstants;
import java.util.ArrayList;
import java.util.Collections;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BreakTransformer implements Transformer<List<String>, List<String>>{

    @Override
    public TransformModellingResult<List<String>> transform(List<String> rawData) {
        List<String> transformedData = new ArrayList<>();
        List<RuntimeException> errors = new ArrayList<>();

        if (rawData == null) {
            transformedData.add(ScoreLangConstants.FAILURE_RESULT);
        } else {
            transformedData = rawData;
        }

        return new BasicTransformModellingResult<>(transformedData, errors);
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.singletonList(Scope.AFTER_STEP);
    }

    @Override
    public String keyToTransform() {
        return null;
    }
}
