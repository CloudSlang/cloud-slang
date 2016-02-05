package io.cloudslang.lang.compiler.modeller.model;

import java.util.Map;

/**
 * User: bancl
 * Date: 1/11/2016
 */
public class Metadata {

    private String description;
    private String prerequisites;
    private Map<String, String> inputs;
    private Map<String, String> outputs;
    private Map<String, String> results;

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
}
