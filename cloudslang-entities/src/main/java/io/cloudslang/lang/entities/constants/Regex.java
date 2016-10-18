/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities.constants;

import io.cloudslang.lang.entities.ScoreLangConstants;

/**
 * @author Bonczidai Levente
 * @since 8/10/2016
 */
public class Regex {
    // match ${ expression } pattern
    public static final String EXPRESSION_REGEX =
            "^\\s*" +
                    ScoreLangConstants.EXPRESSION_START_DELIMITER_ESCAPED +
                    "\\s*" +
                    "(.+?)" +
                    "\\s*" +
                    ScoreLangConstants.EXPRESSION_END_DELIMITER_ESCAPED +
                    "\\s*$";
    public static final String NAMESPACE_PROPERTY_DELIMITER = ".";
    public static final String NAMESPACE_DELIMITER_ESCAPED = "\\" + NAMESPACE_PROPERTY_DELIMITER;
    public static final String NAMESPACE_CHARS = "([\\w\\-" + NAMESPACE_PROPERTY_DELIMITER + "]+)";
    public static final String SIMPLE_NAME_CHARS = "([\\w]+)";
    public static final String RESULT_NAME_CHARS = "([\\w]+)";
    public static final String VARIABLE_NAME_CHARS = "(([A-Za-z][\\w]*)|([_][\\w]+))";
    // match get_sp(key) function
    public static final String SYSTEM_PROPERTY_REGEX_SINGLE_QUOTE = "get_sp\\(\\s*'" + NAMESPACE_CHARS + "'\\s*\\)";
    public static final String SYSTEM_PROPERTY_REGEX_DOUBLE_QUOTE = "get_sp\\(\\s*\"" + NAMESPACE_CHARS + "\"\\s*\\)";
    // match get_sp(key, default) function
    public static final String SYSTEM_PROPERTY_REGEX_WITH_DEFAULT_SINGLE_QUOTE = "get_sp\\(\\s*'" + NAMESPACE_CHARS +
            "'\\s*,\\s*(.+?)\\)";
    public static final String SYSTEM_PROPERTY_REGEX_WITH_DEFAULT_DOUBLE_QUOTE = "get_sp\\(\\s*\"" + NAMESPACE_CHARS +
            "\"\\s*,\\s*(.+?)\\)";
    // match get() function
    public static final String GET_REGEX = "get\\((.+)\\)";
    public static final String GET_REGEX_WITH_DEFAULT = "get\\((.+?),(.+?)\\)";
    public static final String CHECK_EMPTY_REGEX = "check_empty\\((.+?),(.+?)\\)";
}
