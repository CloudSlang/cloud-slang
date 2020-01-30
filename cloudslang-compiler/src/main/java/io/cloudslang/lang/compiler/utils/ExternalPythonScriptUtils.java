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

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExternalPythonScriptUtils {
    private static final String METHOD_SIGNATURE_REGEX = "^def\\s+execute\\((([a-zA-Z0-9_]+,?\\s*)*)\\):\\s*\\R";
    private static final Pattern methodSignaturePattern = Pattern.compile(METHOD_SIGNATURE_REGEX,
            Pattern.DOTALL | Pattern.MULTILINE);

    public static String[] getScriptParams(String script) {
        Matcher matcher = methodSignaturePattern.matcher(script);
        matcher.find();
        String scriptInputsString = matcher.group(1);
        if (StringUtils.isBlank(scriptInputsString)) {
            return new String[]{};
        }
        return scriptInputsString.replaceAll("\\s", "").split(",");
    }
}
