/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.model;

import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.cloudslang.lang.entities.constants.Regex;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
    protected final Set<String> executableDependencies;
    protected final Set<String> systemPropertyDependencies;
    private transient String id;

    protected Executable(Map<String, Serializable> preExecActionData,
                         Map<String, Serializable> postExecActionData,
                         String namespace,
                         String name,
                         List<Input> inputs,
                         List<Output> outputs,
                         List<Result> results,
                         Set<String> executableDependencies,
                         Set<String> systemPropertyDependencies) {
        this.preExecActionData = preExecActionData;
        this.postExecActionData = postExecActionData;
        this.namespace = namespace;
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
        this.results = results;
        this.executableDependencies = executableDependencies;
        this.systemPropertyDependencies = systemPropertyDependencies;
        this.id = namespace + Regex.NAMESPACE_PROPERTY_DELIMITER + name;
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
        return id;
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

    public Set<String> getExecutableDependencies() {
        return executableDependencies;
    }

    public Set<String> getSystemPropertyDependencies() {
        return systemPropertyDependencies;
    }

    public abstract String getType();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Executable that = (Executable) o;

        return new EqualsBuilder()
                .append(preExecActionData, that.preExecActionData)
                .append(postExecActionData, that.postExecActionData)
                .append(namespace, that.namespace)
                .append(name, that.name)
                .append(inputs, that.inputs)
                .append(outputs, that.outputs)
                .append(results, that.results)
                .append(executableDependencies, that.executableDependencies)
                .append(systemPropertyDependencies, that.systemPropertyDependencies)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(preExecActionData)
                .append(postExecActionData)
                .append(namespace)
                .append(name)
                .append(inputs)
                .append(outputs)
                .append(results)
                .append(executableDependencies)
                .append(systemPropertyDependencies)
                .toHashCode();
    }
}
