/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExternalPythonScriptValidatorImpl implements ExternalPythonScriptValidator {
    private static final String METHOD_SIGNATURE_REGEX = "def\\s+execution\\(([a-zA-Z0-9_]+,?\\s*)*\\):\\s*\\R";
    private Pattern methodSignaturePattern = Pattern.compile(METHOD_SIGNATURE_REGEX);

    @Override
    public void validateExecutionMethodSignature(String script) {
        Matcher matcher = methodSignaturePattern.matcher(script);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Method execution is missing or is invalid");
        }

        // multiple declarations of execution method
        if (matcher.find()) {
            throw new IllegalArgumentException("Overload of the execution method is not allowed");
        }
    }
}
