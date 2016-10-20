/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
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
