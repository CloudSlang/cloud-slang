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
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bonczidai Levente
 * @since 3/30/2016
 */
public final class MapUtils {

    private MapUtils() {
    }

    public static Map<String, Value> mergeMaps(
            Map<String, Value> ... maps) {
        Map<String, Value> result = new HashMap<>();
        for (Map<String, Value> map:maps
        ) {
            putAllIfNotEmpty(result, map);
        }
        return result;
    }

    public static Map<String, Value> convertMapNonSensitiveValues(Map<String, ? extends Serializable> source) {
        Map<String, Value> target = new HashMap<>(source.size());
        for (Map.Entry<String, ? extends Serializable> entry : source.entrySet()) {
            target.put(entry.getKey(), ValueFactory.create(entry.getValue()));
        }
        return target;
    }

    private static void putAllIfNotEmpty(
            Map<String, Value> target,
            Map<String, Value> source) {
        if (org.apache.commons.collections4.MapUtils.isNotEmpty(source)) {
            target.putAll(source);
        }
    }

}
