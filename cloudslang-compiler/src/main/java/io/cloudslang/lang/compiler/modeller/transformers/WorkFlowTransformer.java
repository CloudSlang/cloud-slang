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



/*
 * Created by orius123 on 05/11/14.
 */

import io.cloudslang.lang.compiler.modeller.result.BasicTransformModellingResult;
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WorkFlowTransformer implements Transformer<Map<String, Object>, Map<String, Object>> {

    @Override
    public TransformModellingResult<Map<String, Object>> transform(Map<String, Object> rawData) {
        return new BasicTransformModellingResult<>(rawData, new ArrayList<RuntimeException>());
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.emptyList();
    }

    @Override
    public String keyToTransform() {
        return null;
    }


}
