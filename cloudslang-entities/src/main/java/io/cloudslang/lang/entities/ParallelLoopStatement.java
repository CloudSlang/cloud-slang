/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities;

import io.cloudslang.lang.entities.bindings.ScriptFunction;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Set;

/**
 * Date: 3/25/2015
 *
 * @author Bonczidai Levente
 */
public class ParallelLoopStatement extends LoopStatement implements Serializable {

    private final String varName;

    public ParallelLoopStatement(String varName, String expression, Set<ScriptFunction> functionDependencies,
                                 Set<String> systemPropertyDependencies) {
        super(expression, functionDependencies, systemPropertyDependencies);

        Validate.notBlank(varName, "parallel loop var name cannot be empty");
        this.varName = varName;
    }

    /**
     * only here to satisfy serialization libraries
     */
    @SuppressWarnings("unused")
    private ParallelLoopStatement() {
        varName = null;
    }

    public String getVarName() {
        return varName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("varName", varName)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ParallelLoopStatement that = (ParallelLoopStatement) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(varName, that.varName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(varName)
                .toHashCode();
    }

}
