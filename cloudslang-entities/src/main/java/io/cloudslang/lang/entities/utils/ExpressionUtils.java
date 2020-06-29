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

import io.cloudslang.lang.entities.bindings.ScriptFunction;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.cloudslang.lang.entities.constants.Regex.CHECK_EMPTY_REGEX;
import static io.cloudslang.lang.entities.constants.Regex.CS_APPEND_REGEX;
import static io.cloudslang.lang.entities.constants.Regex.CS_EXTRACT_NUMBER_REGEX;
import static io.cloudslang.lang.entities.constants.Regex.CS_PREPEND_REGEX;
import static io.cloudslang.lang.entities.constants.Regex.CS_REPLACE_REGEX;
import static io.cloudslang.lang.entities.constants.Regex.CS_ROUND_REGEX;
import static io.cloudslang.lang.entities.constants.Regex.CS_SUBSTRING_REGEX;
import static io.cloudslang.lang.entities.constants.Regex.CS_TO_LOWER_REGEX;
import static io.cloudslang.lang.entities.constants.Regex.CS_TO_UPPER_REGEX;
import static io.cloudslang.lang.entities.constants.Regex.EXPRESSION_REGEX;
import static io.cloudslang.lang.entities.constants.Regex.GET_REGEX;
import static io.cloudslang.lang.entities.constants.Regex.GET_REGEX_WITH_DEFAULT;
import static io.cloudslang.lang.entities.constants.Regex.SYSTEM_PROPERTY_REGEX_DOUBLE_QUOTE;
import static io.cloudslang.lang.entities.constants.Regex.SYSTEM_PROPERTY_REGEX_SINGLE_QUOTE;
import static io.cloudslang.lang.entities.constants.Regex.SYSTEM_PROPERTY_REGEX_WITH_DEFAULT_DOUBLE_QUOTE;
import static io.cloudslang.lang.entities.constants.Regex.SYSTEM_PROPERTY_REGEX_WITH_DEFAULT_SINGLE_QUOTE;
import static java.util.regex.Pattern.compile;

/**
 * @author Bonczidai Levente
 * @since 1/18/2016
 */
public final class ExpressionUtils {

    private ExpressionUtils() {
    }

    private static final Pattern EXPRESSION_PATTERN = compile(EXPRESSION_REGEX,
            Pattern.UNICODE_CHARACTER_CLASS | Pattern.DOTALL);
    private static final Pattern SYSTEM_PROPERTY_PATTERN_SINGLE_QUOTE = compile(SYSTEM_PROPERTY_REGEX_SINGLE_QUOTE,
            Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern SYSTEM_PROPERTY_PATTERN_DOUBLE_QUOTE =
            compile(SYSTEM_PROPERTY_REGEX_DOUBLE_QUOTE, Pattern.UNICODE_CHARACTER_CLASS);

    private static final Pattern SYSTEM_PROPERTY_PATTERN_WITH_DEFAULT_SINGLE_QUOTE =
            compile(SYSTEM_PROPERTY_REGEX_WITH_DEFAULT_SINGLE_QUOTE, Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern SYSTEM_PROPERTY_PATTERN_WITH_DEFAULT_DOUBLE_QUOTE =
            compile(SYSTEM_PROPERTY_REGEX_WITH_DEFAULT_DOUBLE_QUOTE, Pattern.UNICODE_CHARACTER_CLASS);

    private static final Pattern GET_PATTERN = compile(GET_REGEX, Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern GET_PATTERN_WITH_DEFAULT = compile(GET_REGEX_WITH_DEFAULT,
            Pattern.UNICODE_CHARACTER_CLASS);

    private static final Map<ScriptFunction, Pattern> patternsMap = new HashMap<>();

    static {
        addPattern(ScriptFunction.CHECK_EMPTY, CHECK_EMPTY_REGEX);
        addPattern(ScriptFunction.CS_APPEND, CS_APPEND_REGEX);
        addPattern(ScriptFunction.CS_PREPEND, CS_PREPEND_REGEX);
        addPattern(ScriptFunction.CS_EXTRACT_NUMBER, CS_EXTRACT_NUMBER_REGEX);
        addPattern(ScriptFunction.CS_REPLACE, CS_REPLACE_REGEX);
        addPattern(ScriptFunction.CS_ROUND, CS_ROUND_REGEX);
        addPattern(ScriptFunction.CS_SUBSTRING, CS_SUBSTRING_REGEX);
        addPattern(ScriptFunction.CS_TO_LOWER, CS_TO_LOWER_REGEX);
        addPattern(ScriptFunction.CS_TO_UPPER, CS_TO_UPPER_REGEX);
    }

    private static void addPattern(ScriptFunction function, String regex) {
        patternsMap.put(function, compile(regex, Pattern.UNICODE_CHARACTER_CLASS));
    }

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

    private static boolean matchPattern(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }

    private static Set<String> matchFunction(Pattern functionPattern, String text, int parameterGroup) {
        Matcher matcher = functionPattern.matcher(text);
        Set<String> parameters = new HashSet<>();
        while (matcher.find()) {
            parameters.add(matcher.group(parameterGroup));
        }
        return parameters;
    }

    public static boolean matchesFunction(ScriptFunction function, String expression) {
        Pattern pattern = patternsMap.get(function);
        if (pattern != null) {
            return matchPattern(pattern, expression);
        }

        return false;
    }
}
