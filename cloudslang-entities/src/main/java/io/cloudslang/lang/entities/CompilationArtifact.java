/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.entities;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.score.api.ExecutionPlan;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/*
 * Created by orius123 on 10/11/14.
 */
public class CompilationArtifact {

    private final ExecutionPlan executionPlan;
    private final Map<String, ExecutionPlan> dependencies;
    private final List<Input> inputs;
    private final Collection<Input> systemProperties;

    public CompilationArtifact(ExecutionPlan executionPlan, Map<String, ExecutionPlan> dependencies, List<Input> inputs, Collection<Input> systemProperties) {
        this.executionPlan = executionPlan;
        this.dependencies = dependencies;
        this.inputs = inputs;
        this.systemProperties = systemProperties;
    }

    public ExecutionPlan getExecutionPlan() {
        return executionPlan;
    }

    public Map<String, ExecutionPlan> getDependencies() {
        return dependencies;
    }

    public List<Input> getInputs() {
        return inputs;
    }

    public Collection<Input> getSystemProperties() {
        return systemProperties;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
