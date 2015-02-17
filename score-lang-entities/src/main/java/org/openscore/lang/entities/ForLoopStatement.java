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

public class ForLoopStatement implements Serializable{

    private final String varName;
    private final String collectionExpression;

    public ForLoopStatement(String varName, String collectionExpression) {
        Validate.notBlank(varName, "for loop var name cannot be empty");
        Validate.notBlank(collectionExpression, "for loop collection expression cannot be empty");

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

        ForLoopStatement that = (ForLoopStatement) o;

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
