/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller;

import io.cloudslang.lang.compiler.modeller.model.Metadata;
import io.cloudslang.lang.compiler.parser.utils.DescriptionTag;

import java.util.LinkedHashMap;
import java.util.Map;

public class MetadataModellerImpl implements MetadataModeller {

    @Override
    public Metadata createModel(Map<String, String> metadataMap) {
        return transformToMetadata(metadataMap);
    }

    private Metadata transformToMetadata(Map<String, String> fullMap) {
        Metadata metadata = new Metadata();
        String description = fullMap.get(DescriptionTag.DESCRIPTION.getValue());
        metadata.setDescription(description != null ? description : "");
        String prerequisites = fullMap.get(DescriptionTag.PREREQUISITES.getValue());
        metadata.setPrerequisites(prerequisites != null ? prerequisites : "");
        metadata.setInputs(getTagMap(fullMap, DescriptionTag.INPUT));
        metadata.setOutputs(getTagMap(fullMap, DescriptionTag.OUTPUT));
        metadata.setResults(getTagMap(fullMap, DescriptionTag.RESULT));

        return metadata;
    }

    private Map<String, String> getTagMap(Map<String, String> fullMap, DescriptionTag tag) {
        Map<String, String> map = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : fullMap.entrySet()) {
            if (entry.getKey().contains(tag.getValue())) {
                map.put(getName(entry, tag), entry.getValue());
            }
        }
        return map;
    }

    private String getName(Map.Entry<String, String> entry, DescriptionTag tag) {
        return entry.getKey().substring(tag.getValue().length()).trim();
    }

}
