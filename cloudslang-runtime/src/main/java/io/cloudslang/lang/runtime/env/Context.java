/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.env;

import io.cloudslang.lang.entities.bindings.values.Value;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

public class Context implements Serializable {

    private final Map<String, Value> variables;
    private final Map<String, Value> langVariables;
    private final Map<String, Value> magicVariables;

    public Context(Map<String, Value> variables, Map<String, Value> magicVariables) {
        this.variables = variables;
        this.magicVariables = magicVariables;
        this.langVariables = new HashMap<>();
    }

    public Value getVariable(String name) {
        return variables.get(name);
    }

    public void putVariable(String name, Value value) {
        variables.put(name, value);
    }

    public void putVariables(Map<String, ? extends Value> newVariables) {
        variables.putAll(newVariables);
    }

    public Map<String, Value> getImmutableViewOfLanguageVariables() {
        return unmodifiableMap(langVariables);
    }

    public Map<String, Value> getImmutableViewOfMagicVariables() {
        return unmodifiableMap(magicVariables);
    }

    public Value getLanguageVariable(String name) {
        return langVariables.get(name);
    }

    public void putLanguageVariable(String name, Value value) {
        langVariables.put(name, value);
    }

    public Map<String, Value> getImmutableViewOfVariables() {
        return unmodifiableMap(variables);
    }

    public Value removeLanguageVariable(String key) {
        return langVariables.remove(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Context that = (Context) o;

        return new EqualsBuilder()
                .append(variables, that.variables)
                .append(langVariables, that.langVariables)
                .append(magicVariables,that.magicVariables)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(variables)
                .append(langVariables)
                .append(magicVariables)
                .toHashCode();
    }

}
