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

import io.cloudslang.lang.entities.bindings.ScriptFunction;
import java.io.Serializable;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Date: 3/25/2015
 *
 * @author Bonczidai Levente
 */
public abstract class LoopStatement implements Serializable {
    private final String expression;
    private Set<ScriptFunction> functionDependencies;
    private Set<String> systemPropertyDependencies;

    public LoopStatement(String expression, Set<ScriptFunction> functionDependencies,
                         Set<String> systemPropertyDependencies) {

        Validate.notBlank(expression, "loop expression cannot be empty");

        this.expression = expression;
        this.functionDependencies = functionDependencies;
        this.systemPropertyDependencies = systemPropertyDependencies;
    }

    /**
     * only here to satisfy serialization libraries
     */
    protected LoopStatement() {
        expression = null;
        functionDependencies = null;
    }

    public String getExpression() {
        return expression;
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
                .append("expression", expression)
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

        LoopStatement that = (LoopStatement) o;

        return new EqualsBuilder()
                .append(expression, that.expression)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(expression)
                .toHashCode();
    }

}
