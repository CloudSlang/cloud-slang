package io.cloudslang.lang.compiler.modeller.transformers;
/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/


/*
 * Created by orius123 on 05/11/14.
 */
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Component
public class NavigateTransformer implements Transformer<List<Object>, List<Map<String, String>>>  {

    @Override
    public List<Map<String, String>> transform(List<Object> rawData) {
        if (CollectionUtils.isEmpty(rawData)){
            return new ArrayList<>();
        }
        List<Map<String, String>> navigationData = new ArrayList<>();
        for (Object elementAsObject : rawData) {
            if (elementAsObject instanceof Map) {
                Map elementAsMap = (Map) elementAsObject;
                if (elementAsMap.size() != 1) {
                    throw new RuntimeException("Each list item in the navigate section should contain exactly one key:value pair.");
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
                navigationData.add(elementAsStringMap);
            } else {
                throw new RuntimeException();
            }
        }

        return navigationData;
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
