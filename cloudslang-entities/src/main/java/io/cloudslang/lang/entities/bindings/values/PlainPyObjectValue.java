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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;

@JsonSerialize(using = PlainPyObjectValueSerializer.class)
public class PlainPyObjectValue implements PyObjectValue {
    private static final long serialVersionUID = -2981575572912695201L;
    private final String value;
    private final boolean sensitive;

    public PlainPyObjectValue(String value, boolean sensitive) {
        this.value = value;
        this.sensitive = sensitive;
    }

    @Override
    public boolean isAccessed() {
        return true;
    }

    @Override
    public Serializable get() {
        return value;
    }

    @Override
    public boolean isSensitive() {
        return sensitive;
    }

    @Override
    public String toString() {
        return value;
    }
}
