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

public enum ExecutableType {

    OPERATION("operation"),
    FLOW("flow"),
    DECISION("decision"),
    EXTERNAL("external");

    private final String value;

    ExecutableType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
