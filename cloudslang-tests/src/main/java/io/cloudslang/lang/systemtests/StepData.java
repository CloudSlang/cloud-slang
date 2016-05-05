/******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * *****************************************************************************/

 package io.cloudslang.lang.systemtests;

import io.cloudslang.lang.entities.bindings.values.Value;

import java.util.Map;

public class StepData {

    private final String path;
    private final String name;
    private final Map<String, Value> inputs;
    private final Map<String, Value> outputs;
    private final String executableName;
    private final String result;

    public StepData(String path, String name, Map<String, Value> inputs,
                    Map<String, Value> outputs, String executableName, String result) {
        this.path = path;
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
        this.executableName = executableName;
        this.result = result;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public Map<String, Value> getInputs() {
        return inputs;
    }

    public Map<String, Value> getOutputs() {
        return outputs;
    }

    public String getExecutableName() {
        return executableName;
    }

    public String getResult() {
        return result;
    }

}
