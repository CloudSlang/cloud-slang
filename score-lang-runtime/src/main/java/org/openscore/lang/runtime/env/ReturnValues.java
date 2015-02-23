/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.lang.runtime.env;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class ReturnValues implements Serializable{

	private static final long serialVersionUID = -3647685319127999716L;

	private final HashMap<String, String> outputs;
    private final String result;

    public ReturnValues(Map<String, String> outputs, String result) {
        this.outputs = new HashMap<>(outputs);
        this.result = result;
    }

    public Map<String, String> getOutputs() {
        return outputs;
    }

    public String getResult() {
        return result;
    }

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
