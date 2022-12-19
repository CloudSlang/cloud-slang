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
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

public class ListParallelLoopStatement extends ParallelLoopStatement implements Serializable {

    private static final long serialVersionUID = -8939322708339882016L;
    public static final String PARALLEL_LOOP_VAR_NAME_CANNOT_BE_EMPTY = "parallel loop var name cannot be empty";

    private final String varName;

    public ListParallelLoopStatement(String varName,
                                     String collectionExpression,
                                     String throttleExpression,
                                     Set<ScriptFunction> functionDependencies,
                                     Set<String> systemPropertyDependencies) {
        super(collectionExpression, throttleExpression, functionDependencies, systemPropertyDependencies);

        Validate.notBlank(varName, PARALLEL_LOOP_VAR_NAME_CANNOT_BE_EMPTY);
        this.varName = varName;
    }

    /**
     * only here to satisfy serialization libraries
     */
    @SuppressWarnings("unused")
    private ListParallelLoopStatement() {
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ListParallelLoopStatement that = (ListParallelLoopStatement) o;
        return Objects.equals(varName, that.varName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), varName);
    }
}
