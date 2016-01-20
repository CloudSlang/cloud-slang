/*
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.compiler.modeller;

import io.cloudslang.lang.compiler.modeller.model.Metadata;
import io.cloudslang.lang.compiler.parser.model.ParsedMetadata;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User: bancl
 * Date: 1/12/2016
 */
@Component
public class MetadataModellerImpl implements MetadataModeller {

    @Override
    public Metadata createModel(ParsedMetadata parsedMetadata) {
        return transformToMetadata(parsedMetadata);
    }

    private Metadata transformToMetadata(ParsedMetadata parsedMetadata) {
        Metadata metadata = new Metadata();
        metadata.setDescription(emptyStringIfNull(parsedMetadata.getDescription()));
        metadata.setInputs(convertMapListToMap(parsedMetadata.getInputs()));
        metadata.setOutputs(convertMapListToMap(parsedMetadata.getOutputs()));
        metadata.setResults(convertMapListToMap(parsedMetadata.getResults()));
        return metadata;
    }

    private Map<String, String> convertMapListToMap(List<Map<String, String>> mapList) {
        Map<String, String> linkedHashMap = new LinkedHashMap<>();
        for (Map<String,String> inputMap : emptyListIfNull(mapList) ) {
            Map.Entry<String, String> entry = inputMap.entrySet().iterator().next();
            linkedHashMap.put(entry.getKey(), entry.getValue());
        }
        return linkedHashMap;
    }

    private List<Map<String, String>> emptyListIfNull(List<Map<String, String>> mapList) {
        return mapList == null ? Collections.EMPTY_LIST : mapList;
    }

    private String emptyStringIfNull(String string) {
        return string == null ? "" : string;
    }
}
