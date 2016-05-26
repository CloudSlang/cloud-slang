/**
 * ****************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 * <p/>
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * *****************************************************************************
 */
package io.cloudslang.lang.entities;

import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * @author Bonczidai Levente
 * @since 1/26/2016
 */
public class SystemProperty implements Serializable {

    private final String namespace;
    private final String fullyQualifiedName;
    private final Value value;

    public SystemProperty(String namespace, String key, String value, boolean sensitive) {
        Validate.notNull(namespace, "System property namespace cannot be null");
        Validate.notEmpty(key, "System property key cannot be empty");

        String fullyQualifiedName;
        if (StringUtils.isNotEmpty(namespace)) {
            fullyQualifiedName = namespace + ScoreLangConstants.NAMESPACE_DELIMITER + key;
        } else {
            fullyQualifiedName = key;
        }

        this.namespace = namespace;
        this.fullyQualifiedName = fullyQualifiedName;
        this.value = ValueFactory.create(value, sensitive);
    }

    public SystemProperty(String namespace, String key, String value) {
        this(namespace, key, value, false);
    }

    public SystemProperty(String key, String value) {
        this("", key, value);
    }

    /**
     * only here to satisfy serialization libraries
     */
    @SuppressWarnings("unused")
    private SystemProperty() {
        namespace = null;
        fullyQualifiedName = null;
        value = null;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("namespace", namespace)
                .append("fullyQualifiedName", fullyQualifiedName)
                .append("value", value)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SystemProperty that = (SystemProperty) o;

        return new EqualsBuilder()
                .append(namespace, that.namespace)
                .append(fullyQualifiedName, that.fullyQualifiedName)
                .append(value, that.value)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(namespace)
                .append(fullyQualifiedName)
                .append(value)
                .toHashCode();
    }

}
