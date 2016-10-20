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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Bonczidai Levente
 * @since 1/22/2016
 */
public class MapUtilsTest {

    private static Map<String, Value> EMPTY_MAP = Collections.emptyMap();
    private static Map<String, Value> map1;
    private static Map<String, Value> map2;
    private static Map<String, Value> map_1_2;

    @BeforeClass
    public static void setUpClass() {
        map1 = new HashMap<>();
        map1.put("key1", ValueFactory.create(1));
        map1.put("key2", ValueFactory.create("value2"));

        map2 = new HashMap<>();
        map2.put("key1", ValueFactory.create(2));
        map2.put("key3", ValueFactory.create("value3"));

        map_1_2 = new HashMap<>();
        map_1_2.putAll(map1);
        map_1_2.putAll(map2);
    }


    @Test
    public void testNullParams() throws Exception {
        Assert.assertEquals(EMPTY_MAP, MapUtils.mergeMaps(null, null));
    }

    @Test
    public void testFirstParamNull() throws Exception {
        Assert.assertEquals(map1, MapUtils.mergeMaps(null, map1));
    }

    @Test
    public void testFirstParamEmptyMap() throws Exception {
        Assert.assertEquals(map1, MapUtils.mergeMaps(EMPTY_MAP, map1));
    }

    @Test
    public void testTwoMaps() throws Exception {
        Assert.assertEquals(map_1_2, MapUtils.mergeMaps(map1, map2));
    }

}
