package org.openscore.lang.compiler.model;
/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/


/*
 * Created by orius123 on 05/11/14.
 */

import org.openscore.lang.entities.bindings.Input;
import org.openscore.lang.entities.bindings.Output;
import org.openscore.lang.entities.bindings.Result;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public abstract class Executable {

    protected final Map<String, Serializable> preExecActionData;
    protected final Map<String, Serializable> postExecActionData;
    protected final String namespace;
    protected final String name;
    protected final List<Input> inputs;
    protected final List<Output> outputs;
    protected final List<Result> results;

    protected Executable(Map<String, Serializable> preExecActionData,
                         Map<String, Serializable> postExecActionData,
                         String namespace,
                         String name,
                         List<Input> inputs,
                         List<Output> outputs,
                         List<Result> results) {
        this.preExecActionData = preExecActionData;
        this.postExecActionData = postExecActionData;
        this.namespace = namespace;
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
        this.results = results;
    }

    public Map<String, Serializable> getPreExecActionData() {
        return preExecActionData;
    }

    public Map<String, Serializable> getPostExecActionData() {
        return postExecActionData;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getId() {
        return getNamespace() + "." + getName();
    }

    public String getName() {
        return name;
    }

    public List<Input> getInputs() {
        return inputs;
    }

    public List<Output> getOutputs() {
        return outputs;
    }

    public List<Result> getResults() {
        return results;
    }

    public abstract String getType();
}
