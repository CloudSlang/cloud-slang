package io.cloudslang.lang.entities.utils;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bonczidai Levente
 * @since 1/22/2016
 */
public class MapUtilsTest {

    private static Map<String, Serializable> EMPTY_MAP =  Collections.emptyMap();
    private static Map<String, Serializable> map1;
    private static Map<String, Serializable> map2;
    private static Map<String, Serializable> map_1_2;

    @BeforeClass
    public static void setUpClass() {
        map1 = new HashMap<>();
        map1.put("key1", 1);
        map1.put("key2", "value2");

        map2 = new HashMap<>();
        map2.put("key1", 2);
        map2.put("key3", "value3");

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
