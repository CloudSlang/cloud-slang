/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openscore.lang.entities.bindings;

/**
 * @author orius123
 * @since 05/11/14.
 * @version $Id$
 */
public class Input extends InOutParam {

	private static final long serialVersionUID = -2411446962609754342L;

	private final boolean encrypted;
	private final boolean required;
	private final boolean override;
	private String variableName;

	public Input(String name, String expression, boolean encrypted, boolean required, boolean override, String variableName) {
		super(name, expression);
		this.encrypted = encrypted;
		this.required = required;
		this.override = override;
		this.variableName = variableName;
	}

	public Input(String name, String expression) {
		this(name, expression, false, true, false, null);
	}

	public boolean isEncrypted() {
		return encrypted;
	}

	public boolean isRequired() {
		return required;
	}

	public boolean isOverride() {
		return override;
	}

	public String getVariableName() {
		return this.variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

}
