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
public class MapLoopStatement extends LoopStatement implements Serializable {

    private static final long serialVersionUID = -667210580560286978L;

    private final String keyName;
    private final String valueName;

    public MapLoopStatement(String keyName, String valueName, String collectionExpression,
                            Set<ScriptFunction> functionDependencies, Set<String> systemPropertyDependencies) {
        super(collectionExpression, functionDependencies, systemPropertyDependencies);
        Validate.notBlank(keyName, "key name cannot be empty");
        Validate.notBlank(valueName, "value name cannot be empty");

        this.keyName = keyName;
        this.valueName = valueName;
    }

    /**
     * only here to satisfy serialization libraries
     */
    @SuppressWarnings("unused")
    private MapLoopStatement() {
        keyName = null;
        valueName = null;
    }

    public String getKeyName() {
        return keyName;
    }

    public String getValueName() {
        return valueName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("keyName", keyName)
                .append("valueName", valueName)
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

        MapLoopStatement that = (MapLoopStatement) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(keyName, that.keyName)
                .append(valueName, that.valueName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(keyName)
                .append(valueName)
                .toHashCode();
    }

}
