/*
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.compiler.parser.model;

import java.util.List;
import java.util.Map;

/**
 * User: bancl
 * Date: 1/11/2016
 */
public class ParsedMetadata {

    private List<Map<String, String>> inputs;
    private List<Map<String, String>> outputs;
    private List<Map<String, String>> results;
    private String description;

    public List<Map<String, String>> getInputs() {
        return inputs;
    }

    public void setInputs(List<Map<String, String>> inputs) {
        this.inputs = inputs;
    }

    public List<Map<String, String>> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<Map<String, String>> outputs) {
        this.outputs = outputs;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Map<String, String>> getResults() {
        return results;
    }

    public void setResults(List<Map<String, String>> results) {
        this.results = results;
    }
}
