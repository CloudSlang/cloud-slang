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

import com.google.common.collect.Sets;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.InOutParam;
import io.cloudslang.lang.entities.bindings.Input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Bonczidai Levente
 * @since 7/14/2016
 */
public class SetUtilsTest {

    private static Set<String> EMPTY_SET;
    private static Set<String> SET_1;
    private static Set<SystemProperty> EMPTY_SET_SP;
    private static Set<SystemProperty> SET_SP_1;
    private static SystemProperty SP_1;
    private static Set<String> SET_1_2;
    private static Collection<Set<String>> COL_SET_1_2;
    private static Collection<Set<String>> COL_SET_1_2_EMPTY;
    private static Collection<InOutParam> EMPTY_IN_OUT_PARAMS;
    private static Collection<InOutParam> IN_OUT_PARAMS_1;
    private static InOutParam inOutParam1;

    @BeforeClass
    public static void setUpClass() {
        EMPTY_SET = Collections.emptySet();
        SET_1 = Sets.newHashSet(
                "1",
                "ElemeNT",
                "last"
        );
        EMPTY_SET_SP = Collections.emptySet();
        SystemProperty sp2 = new SystemProperty("a.b", "host", "value");
        SystemProperty sp3 = new SystemProperty("a.b", "KEY.subKey", "value");
        SystemProperty sp4 = new SystemProperty("a.b", "port", "value");
        SET_SP_1 = Sets.newHashSet(sp2, sp3, sp4);
        SP_1 = new SystemProperty("a.b", "key.subkey", "value");
        Set<String> set2 = Sets.newHashSet(
                "2",
                "ElemeNT",
                "last one"
        );
        SET_1_2 = new HashSet<>(SET_1.size() + set2.size());
        SET_1_2.addAll(SET_1);
        SET_1_2.addAll(set2);
        COL_SET_1_2 = new ArrayList<>(2);
        COL_SET_1_2.add(SET_1);
        COL_SET_1_2.add(set2);
        COL_SET_1_2_EMPTY = new ArrayList<>(COL_SET_1_2.size() + 1);
        COL_SET_1_2_EMPTY.addAll(COL_SET_1_2);
        COL_SET_1_2_EMPTY.add(EMPTY_SET);
        EMPTY_IN_OUT_PARAMS = Collections.emptyList();
        IN_OUT_PARAMS_1 = new ArrayList<>();
        IN_OUT_PARAMS_1.add(new Input.InputBuilder("name1", "value").build());
        IN_OUT_PARAMS_1.add(new Input.InputBuilder("name2", "value").build());
        IN_OUT_PARAMS_1.add(new Input.InputBuilder("name3", "value").build());
        inOutParam1 = new Input.InputBuilder("NAMe2", "value").build();
    }

    @Test
    public void testContainsIgnoreCaseEmpty() throws Exception {
        Assert.assertFalse(SetUtils.containsIgnoreCase(EMPTY_SET, "element"));
    }

    @Test
    public void testContainsIgnoreCase() throws Exception {
        Assert.assertTrue(SetUtils.containsIgnoreCase(SET_1, "element"));
    }

    @Test
    public void testContainsIgnoreCaseBasedOnFqnEmpty() throws Exception {
        Assert.assertFalse(SetUtils.containsIgnoreCaseBasedOnFqn(EMPTY_SET_SP, SP_1));
    }

    @Test
    public void testContainsIgnoreCaseBasedOnFqn() throws Exception {
        Assert.assertTrue(SetUtils.containsIgnoreCaseBasedOnFqn(SET_SP_1, SP_1));
    }

    @Test
    public void testMergeSetsEmpty() throws Exception {
        Assert.assertEquals(SET_1_2, SetUtils.mergeSets(COL_SET_1_2_EMPTY));
    }

    @Test
    public void testMergeSets() throws Exception {
        Assert.assertEquals(SET_1_2, SetUtils.mergeSets(COL_SET_1_2));
    }

    @Test
    public void testContainsIgnoreCaseBasedOnNameEmpty() throws Exception {
        Assert.assertFalse(SetUtils.containsIgnoreCaseBasedOnName(EMPTY_IN_OUT_PARAMS, inOutParam1));
    }

    @Test
    public void testContainsIgnoreCaseBasedOnName() throws Exception {
        Assert.assertTrue(SetUtils.containsIgnoreCaseBasedOnName(IN_OUT_PARAMS_1, inOutParam1));
    }

}