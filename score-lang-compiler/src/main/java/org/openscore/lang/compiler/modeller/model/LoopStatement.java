/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.openscore.lang.compiler.modeller.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class LoopStatement implements Serializable{

    private final String varName;
    private final String collectionExpression;

    public LoopStatement(String varName, String collectionExpression) {
        this.varName = varName;
        this.collectionExpression = collectionExpression;
    }

    public String getVarName() {
        return varName;
    }

    public String getCollectionExpression() {
        return collectionExpression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        LoopStatement that = (LoopStatement) o;

        return new EqualsBuilder()
                .append(varName, that.varName)
                .append(collectionExpression, that.collectionExpression)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(varName)
                .append(collectionExpression)
                .toHashCode();
    }
}
