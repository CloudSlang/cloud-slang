/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.utils;

import io.cloudslang.utils.PythonScriptGeneratorUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class ExternalPythonScriptUtilsTest {

    @Test
    public void testNoArgs() {
        String[] scriptParams = ExternalPythonScriptUtils.getScriptParams(
                PythonScriptGeneratorUtils.generateScript(Collections.emptyList()));

        Assert.assertArrayEquals(new String[]{}, scriptParams);
    }

    @Test
    public void testTwoArgs() {
        String[] scriptParams = ExternalPythonScriptUtils.getScriptParams(
                PythonScriptGeneratorUtils.generateScript(Arrays.asList("a", "b")));

        Assert.assertArrayEquals(new String[]{"a", "b"}, scriptParams);
    }
}
