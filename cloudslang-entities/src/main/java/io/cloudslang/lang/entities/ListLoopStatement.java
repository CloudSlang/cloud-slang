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
 * Date: 2/3/2015
 *
 * @author Bonczidai Levente
 */
public class ListLoopStatement extends LoopStatement implements Serializable {

    private static final long serialVersionUID = -540865117927676643L;
    public static final String FOR_LOOP_VAR_NAME_CANNOT_BE_EMPTY = "for loop var name cannot be empty";
    public static final String PARALLEL_LOOP_VAR_NAME_CANNOT_BE_EMPTY = "parallel loop var name cannot be empty";

    private final String varName;

    public ListLoopStatement(String varName, String collectionExpression, Set<ScriptFunction> functionDependencies,
                             Set<String> systemPropertyDependencies, boolean isParallelLoop) {
        super(collectionExpression, functionDependencies, systemPropertyDependencies);
        String message = FOR_LOOP_VAR_NAME_CANNOT_BE_EMPTY;
        if (isParallelLoop) {
            message = PARALLEL_LOOP_VAR_NAME_CANNOT_BE_EMPTY;
        }
        Validate.notBlank(varName, message);

        this.varName = varName;
    }

    /**
     * only here to satisfy serialization libraries
     */
    @SuppressWarnings("unused")
    private ListLoopStatement() {
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

        ListLoopStatement that = (ListLoopStatement) o;

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
