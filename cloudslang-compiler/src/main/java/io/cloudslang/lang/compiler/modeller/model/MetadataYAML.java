package io.cloudslang.lang.compiler.modeller.model;

import java.util.List;
import java.util.Map;

/**
 * User: bancl
 * Date: 1/11/2016
 */
public class MetadataYAML {

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
