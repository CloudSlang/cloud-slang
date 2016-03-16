/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.env;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Context implements Serializable{

    private final Map<String, Serializable> variables;
    private final Map<String, Serializable> langVariables;

    public Context(Map<String, Serializable> variables) {
        this.variables = variables;
        langVariables = new HashMap<>();
    }

    public void putVariable(String name, Serializable value) {
        variables.put(name, value);
    }

    public void putVariables(Map<String, ? extends Serializable> newVariables) {
        variables.putAll(newVariables);
    }

    public Map<String, Serializable> getImmutableViewOfLanguageVariables() {
        return Collections.unmodifiableMap(langVariables);
    }

    public void putLanguageVariable(String name, Serializable value) {
        langVariables.put(name, value);
    }

    public Map<String, Serializable> getImmutableViewOfVariables() {
        return Collections.unmodifiableMap(variables);
    }

    public Serializable removeLanguageVariable(String key) {
        return langVariables.remove(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Context that = (Context) o;

        return new EqualsBuilder()
                .append(variables, that.variables)
                .append(langVariables, that.langVariables)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(variables)
                .append(langVariables)
                .toHashCode();
    }
}
