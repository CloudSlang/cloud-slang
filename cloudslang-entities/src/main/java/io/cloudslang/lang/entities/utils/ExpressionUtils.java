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
package io.cloudslang.lang.entities.utils;

import io.cloudslang.lang.entities.ScoreLangConstants;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Bonczidai Levente
 * @since 1/18/2016
 */
public final class ExpressionUtils {

    private ExpressionUtils() {
    }

    // match ${ expression } pattern
    private final static String EXPRESSION_REGEX =
            "^\\s*" +
                    ScoreLangConstants.EXPRESSION_START_DELIMITER_ESCAPED +
                    "\\s*" +
                    "(.+?)" +
                    "\\s*" +
                    ScoreLangConstants.EXPRESSION_END_DELIMITER_ESCAPED +
                    "\\s*$";
    // match get_sp(key) function
    private final static String SYSTEM_PROPERTY_REGEX_SINGLE_QUOTE = "get_sp\\(\\s*'([\\w\\-.]+)'\\s*\\)";
    private final static String SYSTEM_PROPERTY_REGEX_DOUBLE_QUOTE = "get_sp\\(\\s*\"([\\w\\-.]+)\"\\s*\\)";
    // match get_sp(key, default) function
    private final static String SYSTEM_PROPERTY_REGEX_WITH_DEFAULT_SINGLE_QUOTE = "get_sp\\(\\s*'([\\w\\-.]+)'\\s*,\\s*(.+?)\\)";
    private final static String SYSTEM_PROPERTY_REGEX_WITH_DEFAULT_DOUBLE_QUOTE = "get_sp\\(\\s*\"([\\w\\-.]+)\"\\s*,\\s*(.+?)\\)";
    // match get() function
    private final static String GET_REGEX = "get\\((.+)\\)";
    private final static String GET_REGEX_WITH_DEFAULT = "get\\((.+?),(.+?)\\)";
    private final static String CHECK_EMPTY_REGEX = "check_empty\\((.+?),(.+?)\\)";

    private final static Pattern EXPRESSION_PATTERN = Pattern.compile(EXPRESSION_REGEX, Pattern.DOTALL);
    private final static Pattern SYSTEM_PROPERTY_PATTERN_SINGLE_QUOTE = Pattern.compile(SYSTEM_PROPERTY_REGEX_SINGLE_QUOTE);
    private final static Pattern SYSTEM_PROPERTY_PATTERN_DOUBLE_QUOTE = Pattern.compile(SYSTEM_PROPERTY_REGEX_DOUBLE_QUOTE);
    private final static Pattern SYSTEM_PROPERTY_PATTERN_WITH_DEFAULT_SINGLE_QUOTE = Pattern.compile(SYSTEM_PROPERTY_REGEX_WITH_DEFAULT_SINGLE_QUOTE);
    private final static Pattern SYSTEM_PROPERTY_PATTERN_WITH_DEFAULT_DOUBLE_QUOTE = Pattern.compile(SYSTEM_PROPERTY_REGEX_WITH_DEFAULT_DOUBLE_QUOTE);
    private final static Pattern GET_PATTERN = Pattern.compile(GET_REGEX);
    private final static Pattern GET_PATTERN_WITH_DEFAULT = Pattern.compile(GET_REGEX_WITH_DEFAULT);
    private final static Pattern CHECK_EMPTY_PATTERN = Pattern.compile(CHECK_EMPTY_REGEX);

    public static String extractExpression(Serializable value) {
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

    public static Set<String> extractSystemProperties(String expression) {
        Set<String> properties = matchFunction(SYSTEM_PROPERTY_PATTERN_SINGLE_QUOTE, expression, 1);
        properties.addAll(matchFunction(SYSTEM_PROPERTY_PATTERN_DOUBLE_QUOTE, expression, 1));
        properties.addAll(matchFunction(SYSTEM_PROPERTY_PATTERN_WITH_DEFAULT_SINGLE_QUOTE, expression, 1));
        properties.addAll(matchFunction(SYSTEM_PROPERTY_PATTERN_WITH_DEFAULT_DOUBLE_QUOTE, expression, 1));
        return properties;
    }

    public static boolean matchGetFunction(String text) {
        return matchPattern(GET_PATTERN_WITH_DEFAULT, text) || matchPattern(GET_PATTERN, text);
    }

    public static boolean matchCheckEmptyFunction(String text) {
        return matchPattern(CHECK_EMPTY_PATTERN, text);
    }

    private static boolean matchPattern(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }

    private static Set<String> matchFunction(Pattern functionPattern, String text, int parameterGroup) {
        Matcher matcher = functionPattern.matcher(text);
        Set<String> parameters = new HashSet<>();
        while(matcher.find()) {
            parameters.add(matcher.group(parameterGroup));
        }
        return parameters;
    }

}
