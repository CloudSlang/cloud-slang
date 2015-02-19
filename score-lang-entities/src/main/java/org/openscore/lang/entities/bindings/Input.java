/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openscore.lang.entities.bindings;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author orius123
 * @since 05/11/14.
 * @version $Id$
 */
public class Input extends InOutParam {

	private static final long serialVersionUID = -2411446962609754342L;

	private boolean encrypted;
	private boolean required;
	private boolean overridable;
	private String systemPropertyName;

	public Input(String name, String expression, boolean encrypted,
            boolean required, boolean overridable, String systemPropertyName) {
		super(name, expression);
		this.encrypted = encrypted;
		this.required = required;
		this.overridable = overridable;
		this.systemPropertyName = systemPropertyName;
	}

	public Input(String name, String expression) {
		this(name, expression, false, true, true, null);
	}

    /**
     * only here to satisfy serialization libraries
     */
    private Input(){}

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
