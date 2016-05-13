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
import java.util.HashSet;
import java.util.Set;

/**
 * @author orius123
 * @since 05/11/14.
 * @version $Id$
 */
public class Input extends InOutParam {

	private static final long serialVersionUID = -2411446962609754342L;

	private boolean encrypted;
	private boolean required;
	private boolean privateInput;

	private Input(InputBuilder inputBuilder) {
		super(
				inputBuilder.name,
				inputBuilder.value,
				inputBuilder.functionDependencies,
				inputBuilder.systemPropertyDependencies
		);
		this.encrypted = inputBuilder.encrypted;
		this.required = inputBuilder.required;
		this.privateInput = inputBuilder.privateInput;
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

	public boolean isPrivateInput() {
		return privateInput;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("encrypted", encrypted)
				.append("required", required)
				.append("privateInput", privateInput)
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
				.append(privateInput, input.privateInput)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(encrypted)
				.append(required)
				.append(privateInput)
				.toHashCode();
	}

	public static class InputBuilder {
		private String name;
		private Serializable value;
		private boolean encrypted;
		private boolean required;
		private boolean privateInput;
		private Set<ScriptFunction> functionDependencies;
		private Set<String> systemPropertyDependencies;

		public InputBuilder(String name, Serializable value) {
			this.name = name;
			this.value = value;
			encrypted = false;
			required = true;
			privateInput = false;
			functionDependencies = new HashSet<>();
			systemPropertyDependencies = new HashSet<>();
		}

		public InputBuilder withEncrypted(boolean encrypted) {
			this.encrypted = encrypted;
			return this;
		}

		public InputBuilder withRequired(boolean required) {
			this.required = required;
			return this;
		}

		public InputBuilder withPrivateInput(boolean privateInput) {
			this.privateInput = privateInput;
			return this;
		}

		public InputBuilder withFunctionDependencies(Set<ScriptFunction> functionDependencies) {
			this.functionDependencies = functionDependencies;
			return this;
		}

		public InputBuilder withSystemPropertyDependencies(Set<String> systemPropertyDependencies) {
			this.systemPropertyDependencies = systemPropertyDependencies;
			return this;
		}

		public Input build() {
			return new Input(this);
		}

	}

}
