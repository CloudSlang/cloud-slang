/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
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
public class RegexConstants {
    // match ${ expression } pattern
    public final static String EXPRESSION_REGEX =
            "^\\s*" +
                    ScoreLangConstants.EXPRESSION_START_DELIMITER_ESCAPED +
                    "\\s*" +
                    "(.+?)" +
                    "\\s*" +
                    ScoreLangConstants.EXPRESSION_END_DELIMITER_ESCAPED +
                    "\\s*$";
    public static final String SYSTEM_PROPERTY_DELIMITER = ".";
    public static final String SYSTEM_PROPERTY_DELIMITER_ESCAPED = "\\" + SYSTEM_PROPERTY_DELIMITER;
    public static final String SYSTEM_PROPERTY = "([\\w\\-" + SYSTEM_PROPERTY_DELIMITER + "]+)";
    // match get_sp(key) function
    public final static String SYSTEM_PROPERTY_REGEX_SINGLE_QUOTE = "get_sp\\(\\s*'" + SYSTEM_PROPERTY + "'\\s*\\)";
    public final static String SYSTEM_PROPERTY_REGEX_DOUBLE_QUOTE = "get_sp\\(\\s*\"" + SYSTEM_PROPERTY + "\"\\s*\\)";
    // match get_sp(key, default) function
    public final static String SYSTEM_PROPERTY_REGEX_WITH_DEFAULT_SINGLE_QUOTE = "get_sp\\(\\s*'" + SYSTEM_PROPERTY + "'\\s*,\\s*(.+?)\\)";
    public final static String SYSTEM_PROPERTY_REGEX_WITH_DEFAULT_DOUBLE_QUOTE = "get_sp\\(\\s*\"" + SYSTEM_PROPERTY + "\"\\s*,\\s*(.+?)\\)";
    // match get() function
    public final static String GET_REGEX = "get\\((.+)\\)";
    public final static String GET_REGEX_WITH_DEFAULT = "get\\((.+?),(.+?)\\)";
    public final static String CHECK_EMPTY_REGEX = "check_empty\\((.+?),(.+?)\\)";
}
