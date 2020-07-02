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

public class RobotGroupStatement implements Serializable {

    private final String expression;
    private final Set<ScriptFunction> functionDependencies;
    private final Set<String> systemPropertyDependencies;

    public RobotGroupStatement(String expression,
            Set<ScriptFunction> functionDependencies,
            Set<String> systemPropertyDependencies) {

        Validate.notBlank(expression, "robot group expression cannot be empty");

        this.expression = expression;
        this.functionDependencies = functionDependencies;
        this.systemPropertyDependencies = systemPropertyDependencies;
    }

    protected RobotGroupStatement() {
        expression = null;
        functionDependencies = null;
        systemPropertyDependencies = null;
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

        RobotGroupStatement that = (RobotGroupStatement) o;

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
