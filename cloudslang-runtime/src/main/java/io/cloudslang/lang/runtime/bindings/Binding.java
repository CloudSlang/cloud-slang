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
package io.cloudslang.lang.runtime.bindings;

import io.cloudslang.lang.entities.ScoreLangConstants;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Bonczidai Levente
 * @since 11/6/2015
 */
public abstract class Binding {

    // match ${ expression } pattern
    private final static String EXPRESSION_REGEX =
            "^\\s*" +
                    ScoreLangConstants.EXPRESSION_START_DELIMITER_ESCAPED +
                    "(.+)" +
                    ScoreLangConstants.EXPRESSION_END_DELIMITER_ESCAPED +
                    "\\s*$";
    private final static Pattern EXPRESSION_PATTERN = Pattern.compile(EXPRESSION_REGEX, Pattern.DOTALL);

    protected String extractExpression(Serializable value) {
        String expression = null;
        if (value instanceof String) {
            String valueAsString = ((String) value);
            Matcher expressionMatcher = EXPRESSION_PATTERN.matcher(valueAsString);
            if (expressionMatcher.find()) {
                expression = expressionMatcher.group(1);
            }
        }
        return expression;
    }

}
