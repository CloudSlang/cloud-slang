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

import io.cloudslang.lang.entities.bindings.values.SensitiveStringValue;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import java.io.Serializable;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bonczidai Levente
 * @since 1/26/2016
 */
public class SystemProperty implements Serializable {

    private final String namespace;
    private final String fullyQualifiedName;
    private final Value value;

    private SystemProperty(String namespace, String key, Value value) {
        Validate.notNull(namespace, "System property namespace cannot be null");
        Validate.notEmpty(key, "System property key cannot be empty");

        String fullyQualifiedName;
        if (StringUtils.isNotEmpty(namespace)) {
            fullyQualifiedName = namespace + ScoreLangConstants.NAMESPACE_DELIMITER + key;
        } else {
            fullyQualifiedName = key;
        }

        if (value == null) {
            value = ValueFactory.create(null);
        }

        this.namespace = namespace;
        this.fullyQualifiedName = fullyQualifiedName;
        this.value = value;
    }

    public SystemProperty(String namespace, String key, SensitiveStringValue value) {
        this(namespace, key, (Value) value);
    }

    public SystemProperty(String namespace, String key, String value) {
        this(namespace, key, ValueFactory.create(value));
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
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

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
