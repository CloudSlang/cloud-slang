/*
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package io.cloudslang.lang.compiler.parser;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.parser.model.ParsedMetadata;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * User: bancl
 * Date: 1/12/2016
 */
@Component
public class MetadataParser {

    public static final String PREFIX = "##";
    public static final int BEGIN_INDEX = 3;

    @Autowired
    private Yaml yaml = new Yaml();
    @Autowired
    private ParserExceptionHandler parserExceptionHandler;

    public ParsedMetadata parse(SlangSource source) {
        Validate.notEmpty(source.getSource(), "Source " + source.getName() + " cannot be empty");

        String descriptionYAML = extractDescriptionYAMLString(source);
        try {
            return yaml.loadAs(descriptionYAML, ParsedMetadata.class);
        } catch (Throwable e) {
            throw new RuntimeException("There was a problem parsing the description in YAML format: " +
                    source.getName() + ".\n" + parserExceptionHandler.getErrorMessage(e), e);
        }
    }

    private String extractDescriptionYAMLString(SlangSource source) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new StringReader(source.getSource()))) {
            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith(PREFIX) && line.length() > 2) {
                    line = line.substring(BEGIN_INDEX);
                    if (!line.startsWith("#"))
                        sb.append(line).append(System.getProperty("line.separator"));
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing metadata, error extracting metadata from " + source.getName(), e);
        }
        return sb.toString();
    }
}
