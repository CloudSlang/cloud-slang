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

	private Input(InputBuilder inputBuilder) {
		super(
				inputBuilder.name,
				inputBuilder.value,
				inputBuilder.functionDependencies,
				inputBuilder.systemPropertyDependencies
		);
		this.encrypted = inputBuilder.encrypted;
		this.required = inputBuilder.required;
		this.overridable = inputBuilder.overridable;
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

	public static class InputBuilder {
		private String name;
		private Serializable value;
		private boolean encrypted;
		private boolean required;
		private boolean overridable;
		private List<ScriptFunction> functionDependencies;
		private List<String> systemPropertyDependencies;

		public InputBuilder(String name, Serializable value) {
			this.name = name;
			this.value = value;
			encrypted = false;
			required = true;
			overridable = true;
			functionDependencies = new ArrayList<>();
			systemPropertyDependencies = new ArrayList<>();
		}

		public InputBuilder withEncrypted(boolean encrypted) {
			this.encrypted = encrypted;
			return this;
		}

		public InputBuilder withRequired(boolean required) {
			this.required = required;
			return this;
		}

		public InputBuilder withOverridable(boolean overridable) {
			this.overridable = overridable;
			return this;
		}

		public InputBuilder withFunctionDependencies(List<ScriptFunction> functionDependencies) {
			this.functionDependencies = functionDependencies;
			return this;
		}

		public InputBuilder withSystemPropertyDependencies(List<String> systemPropertyDependencies) {
			this.systemPropertyDependencies = systemPropertyDependencies;
			return this;
		}

		public Input build() {
			return new Input(this);
		}

	}

}
