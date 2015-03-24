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

import io.cloudslang.lang.entities.ScoreLangConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Component
public class Java_ActionTransformer implements Transformer<Map<String, String>, Map<String, String>> {

    @Override
    public Map<String, String> transform(Map<String, String> rawData) {
        Set<String> expectedKeySet = new HashSet<>(Arrays.asList(ScoreLangConstants.ACTION_CLASS_KEY, ScoreLangConstants.ACTION_METHOD_KEY));

        if (rawData != null) {
            // validation case: there is at least one key under java_action property
            Set<String> actualKeySet = rawData.keySet();

            for (String key : actualKeySet) {
                if (!expectedKeySet.remove(key)) {
                    throw new RuntimeException("Invalid key for java action: " + key);
                }
            }

            if (!expectedKeySet.isEmpty()) {
                throw new RuntimeException("The following keys for java action are missing: " + StringUtils.join(expectedKeySet, ","));
            }
        }

        return rawData;
    }

    @Override
    public List<Scope> getScopes() {
        return Arrays.asList(Scope.ACTION);
    }

    @Override
    public String keyToTransform() {
        return null;
    }

}

