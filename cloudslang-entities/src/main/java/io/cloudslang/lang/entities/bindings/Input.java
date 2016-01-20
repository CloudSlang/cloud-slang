/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.entities.bindings;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

	public Input(
			String name,
			Serializable value,
			boolean encrypted,
			boolean required,
			boolean overridable,
			List<ScriptFunction> scriptFunctions,
			List<String> systemPropertyDependencies) {
		super(name, value, scriptFunctions, systemPropertyDependencies);
		this.encrypted = encrypted;
		this.required = required;
		this.overridable = overridable;
	}

	public Input(String name, Serializable expression) {
		this(name, expression, false, true, true, new ArrayList<ScriptFunction>(), new ArrayList<String>());
	}

	/**
	 * only here to satisfy serialization libraries
	 */
	@SuppressWarnings("unused")
	private Input() {}

	public boolean isEncrypted() {
		return encrypted;
	}

	public boolean isRequired() {
		return required;
	}

	public boolean isOverridable() {
		return overridable;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("encrypted", encrypted)
				.append("required", required)
				.append("overridable", overridable)
				.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		Input input = (Input) o;

		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(encrypted, input.encrypted)
				.append(required, input.required)
				.append(overridable, input.overridable)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(encrypted)
				.append(required)
				.append(overridable)
				.toHashCode();
	}

}
