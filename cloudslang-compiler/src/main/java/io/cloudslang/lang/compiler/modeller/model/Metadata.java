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

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Metadata {

    private String description;
    private String prerequisites;
    private Map<String, String> inputs;
    private Map<String, String> outputs;
    private Map<String, String> results;

    public Metadata() {
        description = "";
        prerequisites = "";
        inputs = new HashMap<>();
        outputs = new HashMap<>();
        results = new HashMap<>();
    }

    public Metadata(
            String description,
            String prerequisites,
            Map<String, String> inputs,
            Map<String, String> outputs,
            Map<String, String> results) {
        this.description = description;
        this.prerequisites = prerequisites;
        this.inputs = inputs;
        this.outputs = outputs;
        this.results = results;
    }

    public Map<String, String> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, String> inputs) {
        this.inputs = inputs;
    }

    public Map<String, String> getOutputs() {
        return outputs;
    }

    public void setOutputs(Map<String, String> outputs) {
        this.outputs = outputs;
    }

    public Map<String, String> getResults() {
        return results;
    }

    public void setResults(Map<String, String> results) {
        this.results = results;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }

    public String prettyPrint() {
        StringBuilder stringBuilder = new StringBuilder();
        Field[] allFields = this.getClass().getDeclaredFields();
        for (Field field : allFields) {
            appendField(stringBuilder, field);
        }
        return stringBuilder.toString();
    }

    private void appendField(StringBuilder stringBuilder, Field field) {
        try {
            field.setAccessible(true);
            Object fieldValue = field.get(this);
            if (fieldValue instanceof String) {
                appendStringField(stringBuilder, field.getName(), (String) fieldValue);
            } else if (fieldValue instanceof Map) {
                appendMapField(stringBuilder, field.getName(), (Map<String, String>) fieldValue);
            }
        } catch (IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void appendStringField(StringBuilder stringBuilder,
                                   String fieldName, String fieldValue) throws IOException {
        if (StringUtils.isNotEmpty(fieldValue)) {
            stringBuilder.append(fieldName).append(": ");
            appendString(stringBuilder, fieldValue, "  ");
        }
    }

    private void appendMapField(StringBuilder stringBuilder,
                                String fieldName, Map<String, String> fieldMap) throws IOException {
        if (MapUtils.isNotEmpty(fieldMap)) {
            stringBuilder.append(fieldName).append(": ");
            appendMap(stringBuilder, fieldMap);
        }
    }

    private void appendMap(StringBuilder stringBuilder, Map<String, String> map) throws IOException {
        stringBuilder.append(System.lineSeparator());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            stringBuilder.append("  ").append(entry.getKey());
            if (StringUtils.isNotEmpty(entry.getValue())) {
                stringBuilder.append(": ");
            }
            appendString(stringBuilder, entry.getValue(), "      ");
        }
    }

    private static void appendString(StringBuilder stringBuilder,
                                     String fieldValue, String spacing) throws IOException {
        List<String> lines = IOUtils.readLines(new StringReader(fieldValue));
        if (lines.size() > 1) {
            stringBuilder.append(System.lineSeparator());
            for (String line : lines) {
                stringBuilder.append(spacing).append(line).append(System.lineSeparator());
            }
        } else {
            stringBuilder.append(fieldValue).append(System.lineSeparator());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Metadata that = (Metadata) o;

        return new EqualsBuilder()
                .append(description, that.description)
                .append(prerequisites, that.prerequisites)
                .append(inputs, that.inputs)
                .append(outputs, that.outputs)
                .append(results, that.results)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(description)
                .append(prerequisites)
                .append(inputs)
                .append(outputs)
                .append(results)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "description='" + description + '\'' +
                ", prerequisites='" + prerequisites + '\'' +
                ", inputs=" + inputs +
                ", outputs=" + outputs +
                ", results=" + results +
                '}';
    }
}
