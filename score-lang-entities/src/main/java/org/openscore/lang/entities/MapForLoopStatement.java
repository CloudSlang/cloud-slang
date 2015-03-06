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
 * Date: 2/3/2015
 *
 * @author Bonczidai Levente
 */
public class MapForLoopStatement extends ForLoopStatement implements Serializable{

    private final String keyName;
    private final String valueName;

    public MapForLoopStatement(String keyName, String valueName, String collectionExpression) {
        super(collectionExpression);
        Validate.notBlank(keyName, "key name cannot be empty");
        Validate.notBlank(valueName, "value name cannot be empty");

        this.keyName = keyName;
        this.valueName = valueName;
    }

    public String getKeyName() {
        return keyName;
    }

    public String getValueName() {
        return valueName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MapForLoopStatement that = (MapForLoopStatement) o;

        return new EqualsBuilder()
                .append(keyName, that.keyName)
                .append(valueName, that.valueName)
                .append(getCollectionExpression(), that.getCollectionExpression())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(keyName)
                .append(valueName)
                .append(getCollectionExpression())
                .toHashCode();
    }

}
