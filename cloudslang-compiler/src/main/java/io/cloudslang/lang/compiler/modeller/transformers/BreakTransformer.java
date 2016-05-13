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

import io.cloudslang.lang.entities.ScoreLangConstants;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class BreakTransformer implements Transformer<List<String>, List<String>>{

    @Override
    public List<String> transform(List<String> rawData) {
        if (rawData == null) {
            return Arrays.asList(ScoreLangConstants.FAILURE_RESULT);
        }

        return rawData;
    }

    @Override
    public List<Scope> getScopes() {
        return Arrays.asList(Scope.AFTER_STEP);
    }

    @Override
    public String keyToTransform() {
        return null;
    }
}
