/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.utils;

import java.util.Collection;

public class PythonScriptGeneratorUtils {
    private static String LINE_SEPARATOR = System.lineSeparator();
    private static final String VALID_PYTHON_SCRIPT = "def execute({params}):" + LINE_SEPARATOR +
            "  a = in1" + LINE_SEPARATOR +
            "  return locals()" + LINE_SEPARATOR;

    public static String generateScript(Collection<String> params) {
        return VALID_PYTHON_SCRIPT.replace("{params}", String.join(", ", params));
    }
}
