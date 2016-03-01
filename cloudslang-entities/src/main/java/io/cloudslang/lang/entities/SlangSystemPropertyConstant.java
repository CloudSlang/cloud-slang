package io.cloudslang.lang.entities;

/**
 * User: bancl
 * Date: 2/29/2016
 */
public enum SlangSystemPropertyConstant {
    CSLANG_ENCODING("cslang.encoding"),
    LOG4J_CONFIGURATION("log4j.configuration");

    private final String value;

    SlangSystemPropertyConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
