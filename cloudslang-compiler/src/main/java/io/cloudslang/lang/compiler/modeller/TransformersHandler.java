package io.cloudslang.lang.compiler.modeller;
/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

import io.cloudslang.lang.compiler.modeller.transformers.Transformer;
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
                if (transformedValue != null) {
                    transformedData.put(key, (Serializable) transformedValue);
                }
            } catch (ClassCastException e) {
                Class transformerType = getTransformerFromType(transformer);
                if (value instanceof Map && transformerType.equals(List.class)) {
                    throw new RuntimeException("Under property: '" + key + "' there should be a list of values, but instead there is a map.\n" +
                            "By the Yaml spec lists properties are marked with a '- ' (dash followed by a space)");
                }
                if (value instanceof List && transformerType.equals(Map.class)) {
                    throw new RuntimeException("Under property: '" + key + "' there should be a map of values, but instead there is a list.\n" +
                            "By the Yaml spec maps properties are NOT marked with a '- ' (dash followed by a space)");
                }
                if (value instanceof String && transformerType.equals(Map.class)) {
                    throw new RuntimeException("Under property: '" + key + "' there should be a map of values, but instead there is a string.");
                }
                if (value instanceof String && transformerType.equals(List.class)) {
                    throw new RuntimeException("Under property: '" + key + "' there should be a list of values, but instead there is a string.");
                }
                String message = "Data for property: " + key + " -> " + rawData.get(key).toString() + " is illegal."+
                        "\n Transformer is: " + transformer.getClass().getSimpleName();
                throw new RuntimeException(message, e);
            }
        }
        return transformedData;
    }

    private Class getTransformerFromType(Transformer transformer){
        ResolvableType resolvableType = ResolvableType.forClass(Transformer.class, transformer.getClass());
        return resolvableType.getGeneric(0).resolve();
    }

    public void validateKeyWords(
            String dataLogicalName,
            Map<String, Object> rawData,
            List<Transformer> allRelevantTransformers,
            List<String> additionalValidKeyWords,
            List<List<String>> constraintGroups) {
        Set<String> validKeywords = new HashSet<>();

        if (additionalValidKeyWords != null) {
            validKeywords.addAll(additionalValidKeyWords);
        }

        for (Transformer transformer : allRelevantTransformers) {
            validKeywords.add(keyToTransform(transformer));
        }

        Set<String> rawDataKeySet = rawData.keySet();
        for (String key : rawDataKeySet) {
            if (!(exists(validKeywords, equalToIgnoringCase(key)))) {
                throw new RuntimeException("Property: \'" + key + "\' at: \'" + dataLogicalName + "\' is illegal");
            }
        }

        if (constraintGroups != null) {
            for (List<String> group : constraintGroups) {
                boolean found = false;
                for (String key : group) {
                    if (rawDataKeySet.contains(key)) {
                        if (found) {
                            // one key from this group was already found in action data
                            throw new RuntimeException("Conflicting keys at: " + dataLogicalName);
                        } else {
                            found = true;
                        }
                    }
                }
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
