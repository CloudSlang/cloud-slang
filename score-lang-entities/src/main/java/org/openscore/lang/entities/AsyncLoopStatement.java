/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.openscore.lang.entities;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Date: 3/17/2015
 *
 * @author Bonczidai Levente
 */
public class AsyncLoopStatement extends LoopStatement implements Serializable {

    private final String varName;

    public AsyncLoopStatement(String varName, String expression) {
        super(expression);

        Validate.notBlank(varName, "async loop var name cannot be empty");
        this.varName = varName;
    }

    public String getVarName() {
        return varName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        AsyncLoopStatement that = (AsyncLoopStatement) o;

        return new EqualsBuilder()
                .append(varName, that.varName)
                .append(getExpression(), that.getExpression())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(varName)
                .append(getExpression())
                .toHashCode();
    }

}
