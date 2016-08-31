package io.cloudslang.lang.compiler.modeller;
/**
 * ****************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 * <p/>
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * *****************************************************************************
 */

import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.compiler.modeller.transformers.Transformer;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

/*
 * Created by orius123 on 10/12/14.
 */
@Component
public class TransformersHandler {

    public static String keyToTransform(Transformer transformer) {
        String key;
        if (transformer.keyToTransform() != null) {
            key = transformer.keyToTransform();
        } else {
            String simpleClassName = transformer.getClass().getSimpleName();
            key = StringUtils.substringBefore(simpleClassName, Transformer.class.getSimpleName());
        }
        return key.toLowerCase();
    }

    public Map<String, Serializable> runTransformers(Map<String, Object> rawData, List<Transformer> scopeTransformers, List<RuntimeException> errors) {
        return runTransformers(rawData, scopeTransformers, errors, "");
    }

    public Map<String, Serializable> runTransformers(Map<String, Object> rawData, List<Transformer> scopeTransformers,
                                                     List<RuntimeException> errors, String errorMessagePrefix) {
        Map<String, Serializable> transformedData = new HashMap<>();
        for (Transformer transformer : scopeTransformers) {
            String key = keyToTransform(transformer);
            Object value = rawData.get(key);
            try {
                @SuppressWarnings("unchecked") TransformModellingResult transformModellingResult = transformer.transform(value);
                Object data = transformModellingResult.getTransformedData();
                if (data != null) {
                    transformedData.put(key, (Serializable) data);
                }
                for (RuntimeException rex : transformModellingResult.getErrors()) {
                    errors.add(wrapErrorMessage(rex, errorMessagePrefix));
                }
            } catch (ClassCastException e) {
                Class transformerType = getTransformerFromType(transformer);
                if (value instanceof Map && transformerType.equals(List.class)) {
                    errors.add(new RuntimeException(errorMessagePrefix + "Under property: '" + key + "' there should be a list of values, but instead there is a map.\n" +
                            "By the Yaml spec lists properties are marked with a '- ' (dash followed by a space)"));
                } else if (value instanceof List && transformerType.equals(Map.class)) {
                    errors.add(new RuntimeException(errorMessagePrefix + "Under property: '" + key + "' there should be a map of values, but instead there is a list.\n" +
                            "By the Yaml spec maps properties are NOT marked with a '- ' (dash followed by a space)"));
                } else if (value instanceof String && transformerType.equals(Map.class)) {
                    errors.add(new RuntimeException(errorMessagePrefix + "Under property: '" + key + "' there should be a map of values, but instead there is a string."));
                } else if (value instanceof String && transformerType.equals(List.class)) {
                    errors.add(new RuntimeException(errorMessagePrefix + "Under property: '" + key + "' there should be a list of values, but instead there is a string."));
                } else {
                    String message = "Data for property: " + key + " -> " + rawData.get(key).toString() + " is illegal." +
                            "\n Transformer is: " + transformer.getClass().getSimpleName();
                    errors.add(new RuntimeException(errorMessagePrefix + message, e));
                }
            } catch (RuntimeException e) {
                errors.add(wrapErrorMessage(e, errorMessagePrefix));
            }
        }
        return transformedData;
    }

    private Class getTransformerFromType(Transformer transformer) {
        ResolvableType resolvableType = ResolvableType.forClass(Transformer.class, transformer.getClass());
        return resolvableType.getGeneric(0).resolve();
    }

    private RuntimeException wrapErrorMessage(RuntimeException rex, String errorMessagePrefix) {
        return new RuntimeException(errorMessagePrefix + rex.getMessage(), rex);
    }

}
