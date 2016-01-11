/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.entities;

import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.score.api.ExecutionPlan;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.Map;

/*
 * Created by orius123 on 10/11/14.
 */
public class CompilationArtifact {

    private final ExecutionPlan executionPlan;
    private final Map<String, ExecutionPlan> dependencies;
    private final List<Input> inputs;

    public CompilationArtifact(ExecutionPlan executionPlan, Map<String, ExecutionPlan> dependencies, List<Input> inputs) {
        this.executionPlan = executionPlan;
        this.dependencies = dependencies;
        this.inputs = inputs;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("executionPlan", executionPlan)
                .append("dependencies", dependencies)
                .append("inputs", inputs)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CompilationArtifact that = (CompilationArtifact) o;

        return new EqualsBuilder()
                .append(executionPlan, that.executionPlan)
                .append(dependencies, that.dependencies)
                .append(inputs, that.inputs)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(executionPlan)
                .append(dependencies)
                .append(inputs)
                .toHashCode();
    }
}
