/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package com.hp.score.lang.entities.bindings;

import java.io.Serializable;

/**
 * @author orius123
 * @since 05/11/14.
 * @version $Id$
 */
public class Input extends InOutParam {

	private static final long serialVersionUID = -2411446962609754342L;

	private final Serializable defaultValue;
	private final boolean encrypted;
	private final boolean required;
	private final boolean override;

	public Input(String name, String expression) {
		super(name, expression);
		this.defaultValue = null;
		this.encrypted = false;
		this.required = true;
		this.override = false;
	}

	public Input(String name, String expression, Serializable defaultValue, boolean encrypted, boolean required, boolean override) {
		super(name, expression);
		this.defaultValue = defaultValue;
		this.encrypted = encrypted;
		this.required = required;
		this.override = override;
	}

	public Serializable getDefaultValue() {
		return defaultValue;
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

}
