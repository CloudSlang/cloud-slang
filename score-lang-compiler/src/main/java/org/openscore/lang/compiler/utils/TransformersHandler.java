package org.openscore.lang.compiler.utils;/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/


import org.openscore.lang.compiler.transformers.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;

import static ch.lambdaj.Lambda.exists;
import static org.hamcrest.Matchers.equalToIgnoringCase;

/*
 * Created by orius123 on 10/12/14.
 */
@Component
public class TransformersHandler {

    public Map<String, Serializable> runTransformers(Map<String, Object> rawData, List<Transformer> scopeTransformers) {
        Map<String, Serializable> transformedData = new HashMap<>();
        for (Transformer transformer : scopeTransformers) {
            String key = keyToTransform(transformer);
            Object value = rawData.get(key);
            try {
                @SuppressWarnings("unchecked") Object transformedValue = transformer.transform(value);
                transformedData.put(key, (Serializable) transformedValue);
            } catch (ClassCastException e) {
                Class transformerType = getTransformerFromType(transformer);
                if (value instanceof Map && transformerType.equals(List.class)) {
                    throw new RuntimeException("Key: '" + key + "' expected a list but got a map.\n" +
                            "By the Yaml spec lists properties are marked with a '- ' (dash followed by a space)");
                }
                if (value instanceof List && transformerType.equals(Map.class)) {
                    throw new RuntimeException("Key: '" + key + "' expected a map but got a list.\n" +
                            "By the Yaml spec maps properties are NOT marked with a '- ' (dash followed by a space)");
                }
                String message = "\nFailed casting for key: " + key +
                        ". Raw data is: " + key + ": " +rawData.get(key).toString() +
                        "\n Transformer is: " + transformer.getClass().getName();
                throw new RuntimeException(message, e);
            }
        }
        return transformedData;
    }

    private Class getTransformerFromType(Transformer transformer){
        ResolvableType resolvableType = ResolvableType.forClass(Transformer.class, transformer.getClass());
        return resolvableType.getGeneric(0).resolve();
    }

    public void validateKeyWords(String dataLogicalName, Map<String, Object> rawData, List<Transformer> allRelevantTransformers, List<String> additionalValidKeyWords) {
        Set<String> validKeywords = new HashSet<>();

        if (additionalValidKeyWords != null) {
            validKeywords.addAll(additionalValidKeyWords);
        }

        for (Transformer transformer : allRelevantTransformers) {
            validKeywords.add(keyToTransform(transformer));
        }

        for (String key : rawData.keySet()) {
            if (!(exists(validKeywords, equalToIgnoringCase(key)))) {
                throw new RuntimeException("Property: " + key + " at: " + dataLogicalName + " is illegal");
            }
        }
    }

    private String keyToTransform(Transformer transformer) {
        String key;
        if (transformer.keyToTransform() != null) {
            key = transformer.keyToTransform();
        } else {
            String simpleClassName = transformer.getClass().getSimpleName();
            key = StringUtils.substringBefore(simpleClassName, Transformer.class.getSimpleName());
        }
        return key.toLowerCase();
    }
}
