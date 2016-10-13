/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.env;

import io.cloudslang.lang.entities.bindings.values.Value;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ReturnValues implements Serializable {

    private final Map<String, Value> outputs;

    private final String result;

    public ReturnValues(Map<String, Value> outputs, String result) {
        this.outputs = new HashMap<>(outputs);
        this.result = result;
    }

    public Map<String, Value> getOutputs() {
        return outputs;
    }

    public String getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "ReturnValues{" +
                "result='" + result + "', " +
                "outputs=" + outputs +
                '}';
    }
}
