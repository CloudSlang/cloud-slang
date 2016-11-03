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
import org.apache.commons.collections4.CollectionUtils;

public class NavigateTransformer implements Transformer<List<Object>, List<Map<String, String>>> {

    @Override
    public TransformModellingResult<List<Map<String, String>>> transform(List<Object> rawData) {
        List<Map<String, String>> transformedData = new ArrayList<>();
        List<RuntimeException> errors = new ArrayList<>();

        if (CollectionUtils.isEmpty(rawData)) {
            return new BasicTransformModellingResult<>(transformedData, errors);
        }

        for (Object elementAsObject : rawData) {
            try {
                if (elementAsObject instanceof Map) {
                    Map elementAsMap = (Map) elementAsObject;
                    if (elementAsMap.size() != 1) {
                        throw new RuntimeException("Each list item in the navigate " +
                                "section should contain exactly one key:value pair.");
                    }
                    // - SUCCESS: some_step
                    Map.Entry navigationEntry = (Map.Entry) elementAsMap.entrySet().iterator().next();
                    Object navigationKey = navigationEntry.getKey();
                    Object navigationValue = navigationEntry.getValue();
                    if (!(navigationKey instanceof String)) {
                        throw new RuntimeException("Each key in the navigate section should be a string.");
                    }
                    if (!(navigationValue instanceof String)) {
                        throw new RuntimeException("Each value in the navigate section should be a string.");
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, String> elementAsStringMap = elementAsMap;
                    transformedData.add(elementAsStringMap);
                } else {
                    throw new RuntimeException(
                            "Navigation rule should be a Map. Actual type is " +
                                    elementAsObject.getClass().getName() + ": " + elementAsObject
                    );
                }
            } catch (RuntimeException rex) {
                errors.add(rex);
            }
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
