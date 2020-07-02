/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities.bindings;

/**
 * @author Bonczidai Levente
 * @since 1/18/2016
 */
public enum ScriptFunction {

    GET("get"),

    GET_SYSTEM_PROPERTY("get_system_property"),

    CHECK_EMPTY("check_empty"),

    CS_APPEND("cs_append"),

    CS_PREPEND("cs_prepend"),

    CS_EXTRACT_NUMBER("cs_extract_number"),

    CS_REGEX("cs_regex"),

    CS_REPLACE("cs_replace"),

    CS_ROUND("cs_round"),

    CS_SUBSTRING("cs_substring"),

    CS_TO_UPPER("cs_to_upper"),

    CS_TO_LOWER("cs_to_lower"),

    CS_XPATH_QUERY("cs_xpath_query"),

    CS_JSON_QUERY("cs_json_query");

    private final String value;

    ScriptFunction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
