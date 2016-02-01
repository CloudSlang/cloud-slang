/*
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.cli.utils;

import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.model.Metadata;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.representer.Representer;

import java.beans.IntrospectionException;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: bancl
 * Date: 1/11/2016
 */
@Component
public class MetadataHelperImpl implements MetadataHelper {

    @Autowired
    private Slang slang;

    @Override
    public String extractMetadata(File file) {
        Validate.notNull(file, "File can not be null");
        Validate.notNull(file.getAbsolutePath(), "File path can not be null");
        Validate.isTrue(file.isFile(), "File: " + file.getName() + " was not found");

        Metadata metadata = slang.extractMetadata(SlangSource.fromFile(file));

        return prettyPrint(metadata);
    }

    private String prettyPrint(Metadata metadata) {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setAllowReadOnlyProperties(true);
        dumperOptions.setPrettyFlow(true);
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(new CustomOrderMetadataRepresenter(), dumperOptions);
        String lineBreak = dumperOptions.getLineBreak().getString();
        String result = yaml.dump(cleanWhiteSpaces(metadata, lineBreak));
        result = result.substring(result.indexOf(lineBreak) + 1);
        return result;
    }

    private Metadata cleanWhiteSpaces(Metadata metadata, String lineBreak) {
        metadata.setDescription(metadata.getDescription().trim().replaceAll("[\\s]+" + lineBreak, lineBreak));
        metadata.setPrerequisites(metadata.getPrerequisites().trim().replaceAll("[\\s]+" + lineBreak, lineBreak));
        cleanMapWhiteSpaces(metadata.getInputs(), lineBreak);
        cleanMapWhiteSpaces(metadata.getOutputs(), lineBreak);
        cleanMapWhiteSpaces(metadata.getResults(), lineBreak);
        return metadata;
    }

    private void cleanMapWhiteSpaces(Map<String, String> map, String lineBreak) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            map.put(entry.getKey(), entry.getValue().trim().replaceAll("[\\s]+" + lineBreak, lineBreak));
        }
    }

    private static class CustomOrderMetadataRepresenter extends Representer {
        @Override
        protected Set<Property> getProperties(Class<? extends Object> type)
                throws IntrospectionException {
            Set<Property> result = new LinkedHashSet<>();
            Set<Property> set = super.getProperties(type);
            for (Property property : set) {
                if (property.getType().equals(String.class)) {
                    result.add(property);
                }
            }
            for (Property property : set) {
                if (property.getType().equals(Map.class)) {
                    result.add(property);
                }
            }
            return result;
        }
    }
}
