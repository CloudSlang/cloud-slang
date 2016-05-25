/**
 * ****************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 * <p/>
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * *****************************************************************************
 */
package io.cloudslang.lang.entities.bindings.values;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang.SerializationUtils;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Sensitive InOutParam value
 *
 * Created by Ifat Gavish on 19/04/2016
 */
public class SensitiveValue implements Value {

    public static final String SENSITIVE_VALUE_MASK = "********";

    private byte[] content;

    @SuppressWarnings("unused")
    protected SensitiveValue() {
    }

    protected SensitiveValue(Serializable content) {
        this.content = SerializationUtils.serialize(content);
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public Serializable get() {
        return (Serializable)SerializationUtils.deserialize(content);
    }

    @JsonIgnore
    @Override
    public boolean isSensitive() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SensitiveValue that = (SensitiveValue) o;
        return Arrays.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(content);
    }

    @Override
    public String toString() {
        return SENSITIVE_VALUE_MASK;
    }
}
