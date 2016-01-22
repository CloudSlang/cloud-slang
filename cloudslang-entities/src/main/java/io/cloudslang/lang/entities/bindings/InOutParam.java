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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author moradi
 * @since 13/11/204
 * @version $Id$
 */
public abstract class InOutParam implements Serializable {

	private static final long serialVersionUID = -7712676295781864973L;

	private String name;
	private Serializable value;
	private Set<ScriptFunction> functionDependencies;
	private Set<String> systemPropertyDependencies;

	public InOutParam(
			String name,
			Serializable value,
			Set<ScriptFunction> functionDependencies,
			Set<String> systemPropertyDependencies) {
		this.name = name;
		this.value = value;
		this.functionDependencies = functionDependencies;
		this.systemPropertyDependencies = systemPropertyDependencies;
	}

	public InOutParam(String name, Serializable value) {
		this(name, value, new HashSet<ScriptFunction>(), new HashSet<String>());
	}

	/**
	 * only here to satisfy serialization libraries
	 */
	protected InOutParam() {}

	public String getName() {
		return name;
	}

	public Serializable getValue() {
		return value;
	}

	public Set<ScriptFunction> getFunctionDependencies() {
		return functionDependencies;
	}

	public Set<String> getSystemPropertyDependencies() {
		return systemPropertyDependencies;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("name", name)
				.append("value", value)
				.append("functionDependencies", functionDependencies)
				.append("systemPropertyDependencies", systemPropertyDependencies)
				.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		InOutParam that = (InOutParam) o;

		return new EqualsBuilder()
				.append(name, that.name)
				.append(value, that.value)
				.append(functionDependencies, that.functionDependencies)
				.append(systemPropertyDependencies, that.systemPropertyDependencies)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(name)
				.append(value)
				.append(functionDependencies)
				.append(systemPropertyDependencies)
				.toHashCode();
	}

}
