/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.compiler.modeller.model;

import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Created by orius123 on 05/11/14.
 */
public abstract class Executable {

    protected final Map<String, Serializable> preExecActionData;
    protected final Map<String, Serializable> postExecActionData;
    protected final String namespace;
    protected final String name;
    protected final List<Input> inputs;
    protected final List<Output> outputs;
    protected final List<Result> results;
    protected final Set<String> dependencies;
    protected final String description;

    protected Executable(Map<String, Serializable> preExecActionData,
                         Map<String, Serializable> postExecActionData,
                         String namespace,
                         String name,
                         List<Input> inputs,
                         List<Output> outputs,
                         List<Result> results,
                         Set<String> dependencies) {
        this(
                preExecActionData,
                postExecActionData,
                namespace,
                name,
                inputs,
                outputs,
                results,
                dependencies,
                ""
        );
    }

    protected Executable(Map<String, Serializable> preExecActionData,
                         Map<String, Serializable> postExecActionData,
                         String namespace,
                         String name,
                         List<Input> inputs,
                         List<Output> outputs,
                         List<Result> results,
                         Set<String> dependencies,
                         String description) {
        this.preExecActionData = preExecActionData;
        this.postExecActionData = postExecActionData;
        this.namespace = namespace;
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
        this.results = results;
        this.dependencies = dependencies;
        this.description = description;
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

    public Set<String> getDependencies() {
        return dependencies;
    }

    public abstract String getType();

    public String getDescription() {
        return description;
    }

}
