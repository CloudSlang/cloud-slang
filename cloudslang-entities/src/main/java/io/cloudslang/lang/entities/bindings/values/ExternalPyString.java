/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities.bindings.values;

import java.io.Serializable;

public class ExternalPyString implements Serializable {
    private static final long serialVersionUID = 9171324321685259174L;

    private String value;

    public ExternalPyString(String value) {
        this.value = value;
    }

    public ExternalPyString() {
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getValue();
    }
}
