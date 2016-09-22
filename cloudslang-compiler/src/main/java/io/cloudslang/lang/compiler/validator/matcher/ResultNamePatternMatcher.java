/*
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.compiler.validator.matcher;

import io.cloudslang.lang.entities.constants.RegexConstants;
import java.util.regex.Pattern;

/**
 * @author Bonczidai Levente
 * @since 8/30/2016
 */
public class ResultNamePatternMatcher extends PatternMatcher {
    public ResultNamePatternMatcher() {
        super(Pattern.compile(RegexConstants.RESULT_NAME_CHARS));
    }
}
