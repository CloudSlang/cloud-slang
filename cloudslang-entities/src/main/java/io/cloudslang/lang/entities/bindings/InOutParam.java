/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities.bindings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.cloudslang.lang.entities.bindings.values.Value;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author moradi
 * @version $Id$
 * @since 13/11/204
 */
public abstract class InOutParam implements Serializable {

    private static final long serialVersionUID = -7712676295781864973L;

    private String name;
    private Value value;
    private Set<ScriptFunction> functionDependencies;
    private Set<String> systemPropertyDependencies;

    public InOutParam(
            String name,
            Value value,
            Set<ScriptFunction> functionDependencies,
            Set<String> systemPropertyDependencies) {
        this.name = name;
        this.value = value;
        this.functionDependencies = functionDependencies;
        this.systemPropertyDependencies = systemPropertyDependencies;
    }

    public InOutParam(String name, Value value) {
        this(name, value, new HashSet<ScriptFunction>(), new HashSet<String>());
    }

    /**
     * only here to satisfy serialization libraries
     */
    protected InOutParam() {
    }

    public String getName() {
        return name;
    }

    public Value getValue() {
        return value;
    }

    @JsonIgnore
    public boolean isSensitive() {
        return value != null && value.isSensitive();
    }

    public Set<ScriptFunction> getFunctionDependencies() {
        return functionDependencies;
    }

    public Set<String> getSystemPropertyDependencies() {
        return systemPropertyDependencies;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("value", value)
                .append("functionDependencies", functionDependencies)
                .append("systemPropertyDependencies", systemPropertyDependencies)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InOutParam that = (InOutParam) o;

        return new EqualsBuilder()
                .append(name, that.name)
                .append(value, that.value)
                .append(functionDependencies, that.functionDependencies)
                .append(systemPropertyDependencies, that.systemPropertyDependencies)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(value)
                .append(functionDependencies)
                .append(systemPropertyDependencies)
                .toHashCode();
    }

}
