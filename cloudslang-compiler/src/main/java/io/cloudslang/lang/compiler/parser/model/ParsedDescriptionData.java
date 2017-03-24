/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.parser.model;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Bonczidai Levente
 * @since 3/20/2017
 */
public class ParsedDescriptionData {
    private final List<ParsedDescriptionSection> topLevelDescriptions;
    private final Map<String, ParsedDescriptionSection> stepDescriptions;
    private final List<RuntimeException> errors;

    public ParsedDescriptionData(
            List<ParsedDescriptionSection> topLevelDescriptions,
            Map<String, ParsedDescriptionSection> stepDescriptions,
            List<RuntimeException> errors) {
        this.topLevelDescriptions = topLevelDescriptions;
        this.stepDescriptions = stepDescriptions;
        this.errors = errors;
    }

    public List<ParsedDescriptionSection> getTopLevelDescriptions() {
        return topLevelDescriptions;
    }

    public Map<String, ParsedDescriptionSection> getStepDescriptions() {
        return stepDescriptions;
    }

    public List<RuntimeException> getErrors() {
        return errors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ParsedDescriptionData that = (ParsedDescriptionData) o;

        return new EqualsBuilder()
                .append(topLevelDescriptions, that.topLevelDescriptions)
                .append(stepDescriptions, that.stepDescriptions)
                .append(errors, that.errors)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(topLevelDescriptions)
                .append(stepDescriptions)
                .append(errors)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "ParsedDescriptionData{" +
                "topLevelDescriptions=" + topLevelDescriptions +
                ", stepDescriptions=" + stepDescriptions +
                ", errors=" + errors +
                '}';
    }
}
