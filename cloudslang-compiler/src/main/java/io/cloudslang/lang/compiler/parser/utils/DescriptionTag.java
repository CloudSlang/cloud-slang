package io.cloudslang.lang.compiler.parser.utils;

/**
 * User: bancl
 * Date: 1/27/2016
 */
public enum DescriptionTag {
    DESCRIPTION("@description"),
    PREREQUISITES("@prerequisites"),
    INPUT("@input"),
    OUTPUT("@output"),
    RESULT("@result");

    private final String value;
    DescriptionTag(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
