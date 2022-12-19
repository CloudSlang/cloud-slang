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

public class MapParallelLoopStatement extends ParallelLoopStatement implements Serializable {

    private static final long serialVersionUID = -2140474603664654274L;

    private final String keyName;
    private final String valueName;

    public MapParallelLoopStatement(String keyName,
                                    String valueName,
                                    String collectionExpression,
                                    String throttleExpression,
                                    Set<ScriptFunction> functionDependencies,
                                    Set<String> systemPropertyDependencies) {
        super(collectionExpression, throttleExpression, functionDependencies, systemPropertyDependencies);
        Validate.notBlank(keyName, "key name cannot be empty");
        Validate.notBlank(valueName, "value name cannot be empty");

        this.keyName = keyName;
        this.valueName = valueName;
    }

    /**
     * only here to satisfy serialization libraries
     */
    @SuppressWarnings("unused")
    private MapParallelLoopStatement() {
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
        if (!super.equals(o)) {
            return false;
        }
        MapParallelLoopStatement that = (MapParallelLoopStatement) o;
        return Objects.equals(keyName, that.keyName) && Objects.equals(valueName, that.valueName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), keyName, valueName);
    }
}
