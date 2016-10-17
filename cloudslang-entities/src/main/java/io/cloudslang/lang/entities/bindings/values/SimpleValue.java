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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;

/**
 * Simple InOutParam value
 * <p>
 * Created by Ifat Gavish on 19/04/2016
 */
public class SimpleValue implements Value {

    private Serializable content;

    @SuppressWarnings("unused")
    protected SimpleValue() {
    }

    protected SimpleValue(Serializable content) {
        this.content = content;
    }

    public Serializable getContent() {
        return content;
    }

    public void setContent(Serializable content) {
        this.content = content;
    }

    @Override
    public Serializable get() {
        return content;
    }

    @JsonIgnore
    @Override
    public boolean isSensitive() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleValue that = (SimpleValue) o;
        return content != null ? content.equals(that.content) : that.content == null;
    }

    @Override
    public int hashCode() {
        return content != null ? content.hashCode() : 0;
    }

    @Override
    public String toString() {
        return content == null ? "" : content.toString();
    }
}
