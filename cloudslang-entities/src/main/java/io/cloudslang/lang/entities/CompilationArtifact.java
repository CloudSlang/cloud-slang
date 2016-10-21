/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities;

import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.score.api.ExecutionPlan;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/*
 * Created by orius123 on 10/11/14.
 */
public class CompilationArtifact {

    private final ExecutionPlan executionPlan;
    private final Map<String, ExecutionPlan> dependencies;
    private final List<Input> inputs;
    private final Set<String> systemProperties;

    public CompilationArtifact(ExecutionPlan executionPlan, Map<String, ExecutionPlan> dependencies, List<Input> inputs,
                               Set<String> systemProperties) {
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

    public Set<String> getSystemProperties() {
        return systemProperties;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("executionPlan", executionPlan)
                .append("dependencies", dependencies)
                .append("inputs", inputs)
                .append("systemProperties", systemProperties)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CompilationArtifact that = (CompilationArtifact) o;

        return new EqualsBuilder()
                .append(executionPlan, that.executionPlan)
                .append(dependencies, that.dependencies)
                .append(inputs, that.inputs)
                .append(systemProperties, that.systemProperties)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(executionPlan)
                .append(dependencies)
                .append(inputs)
                .append(systemProperties)
                .toHashCode();
    }

}
