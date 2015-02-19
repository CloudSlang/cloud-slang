/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openscore.lang.entities.bindings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author orius123
 * @since 05/11/14.
 * @version $Id$
 */
public class Input extends InOutParam {

	private static final long serialVersionUID = -2411446962609754342L;

	private final boolean encrypted;
	private final boolean required;
	private final boolean overridable;
	private String systemPropertyName;

    @JsonCreator
	public Input(
            @JsonProperty("name") String name,
            @JsonProperty("expression") String expression,
            @JsonProperty("encrypted") boolean encrypted,
            @JsonProperty("required") boolean required,
            @JsonProperty("overridable") boolean overridable,
            @JsonProperty("systemPropertyName") String systemPropertyName) {
		super(name, expression);
		this.encrypted = encrypted;
		this.required = required;
		this.overridable = overridable;
		this.systemPropertyName = systemPropertyName;
	}

	public Input(String name, String expression) {
		this(name, expression, false, true, true, null);
	}

	public boolean isEncrypted() {
		return encrypted;
	}

	public boolean isRequired() {
		return required;
	}

	public boolean isOverridable() {
		return overridable;
	}

	public String getSystemPropertyName() {
		return this.systemPropertyName;
	}

	public void setSystemPropertyName(String systemPropertyName) {
		this.systemPropertyName = systemPropertyName;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Input that = (Input) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(that))
                .append(this.encrypted, that.encrypted)
                .append(this.required, that.required)
                .append(this.overridable, that.overridable)
                .append(this.systemPropertyName, that.systemPropertyName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(encrypted)
                .append(required)
                .append(overridable)
                .append(systemPropertyName)
                .toHashCode();
    }
}
