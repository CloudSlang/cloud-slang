/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.lang.tools.build.tester.parse;

/*
 * Created by orius123 on 05/11/14.
 */

import ch.lambdaj.function.convert.Converter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudslang.lang.compiler.SlangSource;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static ch.lambdaj.Lambda.convertMap;

@Component
public class TestCasesYamlParser {

    @Autowired
    private Yaml yaml;

    private ObjectMapper objectMapper = new ObjectMapper();

    private final static Logger log = Logger.getLogger(TestCasesYamlParser.class);

    public Map<String, SlangTestCase> parseTestCases(SlangSource source) {

        if(StringUtils.isEmpty(source.getSource())){
            log.info("No tests cases were found in: " + source.getName());
            return new HashMap<>();
        }
        Validate.notEmpty(source.getSource(), "Source " + source.getName() + " cannot be empty");

        try {
            @SuppressWarnings("unchecked") Map<String, Map> parsedTestCases = yaml.loadAs(source.getSource(), Map.class);
            if (MapUtils.isEmpty(parsedTestCases)){
                log.info("No tests cases were found in: " + source.getName());
                return new HashMap<>();
            }
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

    public Map<String, Serializable> parseProperties(String fileName) {
        Map<String, Serializable> result = new HashMap<>();
        if(StringUtils.isNotEmpty(fileName)) {
            try {
                result.putAll((Map<String, Serializable>) yaml.load(FileUtils.readFileToString(new File(fileName))));
            } catch (IOException ex) {
                log.error("Error loading file: " + fileName, ex);
                throw new RuntimeException("Error loading file: " + fileName, ex);
            }
        }
        return result;
    }
}
