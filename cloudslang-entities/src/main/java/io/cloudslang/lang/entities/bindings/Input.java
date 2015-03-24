/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.entities.bindings;

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

	public Input(String name, String expression, boolean encrypted, boolean required, boolean overridable, String systemPropertyName) {
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

	public String getSystemPropertyName() {
		return this.systemPropertyName;
	}

}
