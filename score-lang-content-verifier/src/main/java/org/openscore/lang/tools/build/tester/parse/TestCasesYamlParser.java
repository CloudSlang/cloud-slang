/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.lang.tools.build.tester.parse;

/*
 * Created by orius123 on 05/11/14.
 */

import ch.lambdaj.function.convert.Converter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.Validate;
import org.openscore.lang.compiler.SlangSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.Map;

import static ch.lambdaj.Lambda.convertMap;

@Component
public class TestCasesYamlParser {

    @Autowired
    private Yaml yaml;

    private ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, SlangTestCase> parse(SlangSource source) {

        Validate.notEmpty(source.getSource(), "Source " + source.getName() + " cannot be empty");

        try {
            @SuppressWarnings("unchecked") Map<String, Map> parsedTestCases = yaml.loadAs(source.getSource(), Map.class);
            return convertMap(parsedTestCases, new Converter<Map, SlangTestCase>() {
                @Override
                public SlangTestCase convert(Map from) {
                    return parseTestCase(from);
                }
            });
        } catch (Throwable e) {
            throw new RuntimeException("There was a problem parsing the YAML source: " + source.getName() + ".\n" + e.getMessage(), e);
        }
    }

    private SlangTestCase parseTestCase(Map map) {
        try {
            String content = objectMapper.writeValueAsString(map);
            return objectMapper.readValue(content, SlangTestCase.class);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing slang test case", e);
        }
    }
}
