/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller;


import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.compiler.modeller.transformers.Transformer;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransformersHandler {

    public static final String CLASS = "class ";

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

    public Map<String, Serializable> runTransformers(Map<String, Object> rawData,
                                                     List<Transformer> scopeTransformers,
                                                     List<RuntimeException> errors) {
        return runTransformers(rawData, scopeTransformers, errors, "");
    }

    public Map<String, Serializable> runTransformers(Map<String, Object> rawData, List<Transformer> scopeTransformers,
                                                     List<RuntimeException> errors, String errorMessagePrefix) {
        Map<String, Serializable> transformedData = new HashMap<>();
        for (Transformer transformer : scopeTransformers) {
            String key = keyToTransform(transformer);
            Object value = rawData.get(key);
            try {
                @SuppressWarnings("unchecked")
                TransformModellingResult transformModellingResult = transformer.transform(value);
                Object data = transformModellingResult.getTransformedData();
                if (data != null) {
                    transformedData.put(key, (Serializable) data);
                }
                if (rawData.containsKey(key)) {
                    for (RuntimeException rex : transformModellingResult.getErrors()) {
                        errors.add(wrapErrorMessage(rex, errorMessagePrefix));
                    }
                }
            } catch (ClassCastException e) {
                Class transformerType = getTransformerFromType(transformer);
                if (value instanceof Map && transformerType.equals(List.class)) {
                    errors.add(new RuntimeException(errorMessagePrefix + "Under property: '" + key +
                            "' there should be a list of values, but instead there is a map.\n" +
                            "By the Yaml spec lists properties are marked with a '- ' (dash followed by a space)"));
                } else if (value instanceof List && transformerType.equals(Map.class)) {
                    errors.add(new RuntimeException(errorMessagePrefix + "Under property: '" + key +
                            "' there should be a map of values, but instead there is a list.\n" +
                            "By the Yaml spec maps properties are NOT marked with a '- ' (dash followed by a space)"));
                } else if (value instanceof String && transformerType.equals(Map.class)) {
                    errors.add(new RuntimeException(errorMessagePrefix + "Under property: '" + key +
                            "' there should be a map of values, but instead there is a string."));
                } else if (value instanceof String && transformerType.equals(List.class)) {
                    errors.add(new RuntimeException(errorMessagePrefix + "Under property: '" + key +
                            "' there should be a list of values, but instead there is a string."));
                } else {
                    String message = "Data for property: " + key + " -> " + rawData.get(key).toString() +
                            " is illegal." + "\n Transformer is: " + transformer.getClass().getSimpleName();
                    errors.add(new RuntimeException(errorMessagePrefix + message, e));
                }
            } catch (RuntimeException e) {
                errors.add(wrapErrorMessage(e, errorMessagePrefix));
            }
        }
        return transformedData;
    }

    private Class getTransformerFromType(Transformer transformer) {
        // Always take the first interface Transformer<F, T> in case of many interfaces
        // Always take the first parameter F of the Transformer interface
        Type interfaceType = transformer.getClass().getGenericInterfaces()[0];
        Type typeF = ((ParameterizedType) interfaceType).getActualTypeArguments()[0];
        if (typeF instanceof ParameterizedType) {
            return (Class) ((ParameterizedType) typeF).getRawType();
        } else if (typeF instanceof Class) {
            return (Class) typeF;
        } else {
            String fullName = typeF.toString();
            try {
                return fullName.startsWith(CLASS) ? Class.forName(fullName.substring(CLASS.length()))
                        : Class.forName(fullName);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }

    private RuntimeException wrapErrorMessage(RuntimeException rex, String errorMessagePrefix) {
        return new RuntimeException(errorMessagePrefix + rex.getMessage(), rex);
    }

}
