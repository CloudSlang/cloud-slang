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
import java.util.Objects;
import java.util.Set;

public abstract class ParallelLoopStatement extends LoopStatement implements Serializable {

    private static final long serialVersionUID = -2601483775652385662L;

    private final String throttleExpression;


    public ParallelLoopStatement(String expression,
                                 String throttleExpression,
                                 Set<ScriptFunction> functionDependencies,
                                 Set<String> systemPropertyDependencies) {

        super(expression, functionDependencies, systemPropertyDependencies);

        this.throttleExpression = throttleExpression;
    }

    /**
     * only here to satisfy serialization libraries
     */
    protected ParallelLoopStatement() {
        throttleExpression = null;
    }

    public String getThrottleExpression() {
        return throttleExpression;
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
        ParallelLoopStatement that = (ParallelLoopStatement) o;
        return Objects.equals(throttleExpression, that.throttleExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), throttleExpression);
    }
}
