/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities.utils;

import io.cloudslang.lang.entities.bindings.values.Value;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bonczidai Levente
 * @since 9/9/2016
 */
public class ValueUtils {
    public static boolean isEmpty(Value value) {
        return value == null || value.get() == null || StringUtils.EMPTY.equals(value.get());
    }

    public static Map<String, Serializable> flatten(Map<String, Value> valueMap) {
        Map<String, Serializable> result = null;
        if (valueMap != null) {
            result = new LinkedHashMap<>();
            flattenMap(valueMap, result);
        }
        return result;
    }

    public static Map<String, Serializable> flatten(List<Map<String, Value>> valueMaps) {
        Map<String, Serializable> result = new LinkedHashMap<>();
        for (Map<String, Value> valueMap : valueMaps) {
            if (valueMap != null) {
                flattenMap(valueMap, result);
            }
        }
        return result;
    }

    private static void flattenMap(Map<String, Value> valueMap, Map<String, Serializable> result) {
        for (Map.Entry<String, Value> entry : valueMap.entrySet()) {
            result.put(entry.getKey(), entry.getValue()== null ? null:entry.getValue().toString());
        }
    }

}
