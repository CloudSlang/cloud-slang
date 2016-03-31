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
package io.cloudslang.lang.entities.utils;

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

    public static Map<String, Serializable> mergeMaps(
            Map<String, Serializable> map1,
            Map<String, Serializable> map2) {
        Map<String, Serializable> result = new HashMap<>();
        putAllIfNotEmpty(result, map1);
        putAllIfNotEmpty(result, map2);
        return result;
    }

    private static void putAllIfNotEmpty(
            Map<String, Serializable> target,
            Map<String, Serializable> source) {
        if (org.apache.commons.collections4.MapUtils.isNotEmpty(source)) {
            target.putAll(source);
        }
    }

}
