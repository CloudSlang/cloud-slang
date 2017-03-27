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

import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Bonczidai Levente
 * @since 3/21/2017
 */
public class StepMetadata {
    private final String stepName;
    private final Map<String, String> inputs;
    private final Map<String, String> outputs;

    public StepMetadata(String stepName, Map<String, String> inputs, Map<String, String> outputs) {
        this.stepName = stepName;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public String getStepName() {
        return stepName;
    }

    public Map<String, String> getInputs() {
        return inputs;
    }

    public Map<String, String> getOutputs() {
        return outputs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StepMetadata that = (StepMetadata) o;

        return new EqualsBuilder()
                .append(stepName, that.stepName)
                .append(inputs, that.inputs)
                .append(outputs, that.outputs)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(stepName)
                .append(inputs)
                .append(outputs)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "StepMetadata{" +
                "stepName='" + stepName + '\'' +
                ", inputs=" + inputs +
                ", outputs=" + outputs +
                '}';
    }
}
