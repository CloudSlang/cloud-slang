/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.validator.matcher;

import io.cloudslang.lang.entities.constants.Regex;
import java.util.regex.Pattern;

/**
 * @author Bonczidai Levente
 * @since 8/30/2016
 */
public class VariableNamePatternMatcher extends PatternMatcher {
    public VariableNamePatternMatcher() {
        super(Pattern.compile(Regex.VARIABLE_NAME_CHARS));
    }
}
