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

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class NavigateTransformer implements Transformer<Map<String, Object>, Map<String, String>> {

    @Override
    public Map<String, String> transform(Map<String, Object> rawData) {
        if (MapUtils.isEmpty(rawData)){
            return new LinkedHashMap<>();
        }
        Map<String, String> navigationData = new HashMap<>();
        for (Map.Entry<String, Object> entry : rawData.entrySet()) {
            //todo currently we support only string navigation (no nested navigation)
            // - SUCCESS: some_task
            // the value of the navigation is the step to go to
            if (entry.getValue() instanceof String){
                navigationData.put(entry.getKey(), (String) entry.getValue());
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

