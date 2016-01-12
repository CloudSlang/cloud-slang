/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.modeller.model.Metadata;
import io.cloudslang.lang.compiler.modeller.model.MetadataYAML;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User: bancl
 * Date: 1/11/2016
 */
@Component
public class YAMLMetadataExtractorImpl implements MetadataExtractor {

    @Autowired
    private Yaml yaml;

    @Override
    public Metadata extractMetadata(SlangSource source) {
        Validate.notNull(source, "You must supply a source to extract the metadata from");

        String descriptionYAML = extractDescriptionYAMLString(source);
        MetadataYAML metadataYAML = yaml.loadAs(descriptionYAML, MetadataYAML.class);
        return getMetadata(metadataYAML);
    }

    private Metadata getMetadata(MetadataYAML metadataYAML) {
        Metadata metadata = new Metadata();
        metadata.setDescription(metadataYAML.getDescription());
        metadata.setInputs(convertMapListToMap(metadataYAML.getInputs()));
        metadata.setOutputs(convertMapListToMap(metadataYAML.getOutputs()));
        metadata.setResults(convertMapListToMap(metadataYAML.getResults()));
        return metadata;
    }

    private Map<String, String> convertMapListToMap(List<Map<String, String>> mapList) {
        Map<String, String> inputs = new LinkedHashMap<>();
        for (Map<String,String> inputMap : mapList) {
            String key = inputMap.keySet().iterator().next();
            inputs.put(key, inputMap.get(key));
        }
        return inputs;
    }

    private String extractDescriptionYAMLString(SlangSource source) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new StringReader(source.getSource()))) {
            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith("##")) {
                    line = line.substring(3);
                    if (!line.startsWith("#"))
                        sb.append(line).append(System.getProperty("line.separator"));
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Read issue(should not happen)", e);
        }
        return sb.toString();
    }
}
