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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class NavigateTransformer implements Transformer<List<Object>, List<Map<String, String>>>  {

    @Override
    public List<Map<String, String>> transform(List<Object> rawData) {
        if (CollectionUtils.isEmpty(rawData)){
            return new ArrayList<>();
        }
        List<Map<String, String>> navigationData = new ArrayList<>();
        for (Object object : rawData) {
            if (object instanceof Map) {
                @SuppressWarnings("unchecked") Map<String, String> map = (Map<String, String>) object;
                if (map.size() > 1) {
                    throw new RuntimeException("Each list item in the navigate section may contain only one key:value pair");
                }
                navigationData.add(map);
            }
        }

        return navigationData;
    }

    @Override
    public List<Scope> getScopes() {
        return Arrays.asList(Scope.AFTER_TASK);
    }

    @Override
    public String keyToTransform() {
        return null;
    }

}

