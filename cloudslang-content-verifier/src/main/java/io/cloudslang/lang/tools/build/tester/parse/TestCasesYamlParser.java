/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.commons.services.api.SlangSourceService;
import io.cloudslang.lang.compiler.Extension;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.logging.LoggingService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class TestCasesYamlParser {

    @Autowired
    private Yaml yaml;

    @Autowired
    private Slang slang;

    @Autowired
    private SlangSourceService slangSourceService;

    @Autowired
    private LoggingService loggingService;

    // // TODO: 10/10/2016 this needs to be fixed and have proper autowiring
    // object mapper is thread safe if not re-configured , alternatively we can use With 2.0 and above,
    // above can be augmented by noting that there is an even better way: use ObjectWriter and ObjectReader objects,
    // which are full thread safe
    private ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, SlangTestCase> parseTestCases(SlangSource source) {

        if (StringUtils.isEmpty(source.getContent())) {
            loggingService.logEvent(Level.INFO, "No tests cases were found in: " + source.getName());
            return new HashMap<>();
        }
        Validate.notEmpty(source.getContent(), "Source " + source.getName() + " cannot be empty");

        try {
            @SuppressWarnings("unchecked")
            Map<String, Map> parsedTestCases = yaml.loadAs(source.getContent(), Map.class);
            if (MapUtils.isEmpty(parsedTestCases)) {
                loggingService.logEvent(Level.INFO, "No tests cases were found in: " + source.getName());
                return new HashMap<>();
            }
            return parseTestCases(parsedTestCases, source.getFilePath());
        } catch (Throwable e) {
            throw new RuntimeException(
                    "There was a problem parsing the YAML source: " + source.getName() + ".\n" + e.getMessage(), e
            );
        }
    }

    private Map<String, SlangTestCase> parseTestCases(Map<String, Map> parsedTestCases, String filePath) {
        Map<String, SlangTestCase> convertedTestCases = new HashMap<>();
        for (Map.Entry<String, Map> entry : parsedTestCases.entrySet()) {
            convertedTestCases.put(entry.getKey(), parseTestCase(entry.getValue(), entry.getKey(), filePath));
        }
        return convertedTestCases;
    }

    private SlangTestCase parseTestCase(Map map, String artifact, String filePath) {
        try {
            String content = objectMapper.writeValueAsString(map);
            SlangTestCase slangTestCase = objectMapper.readValue(content, SlangTestCase.class);
            setInputs(slangTestCase, map, artifact);
            slangTestCase.setFilePath(filePath);

            return slangTestCase;
        } catch (IOException e) {
            throw new RuntimeException("Error parsing slang test case: " + e.getMessage(), e);
        }
    }

    private void setInputs(SlangTestCase slangTestCase, Map map, String artifact) {
        Object object = map.get(SlangTextualKeys.INPUTS_KEY);
        if (object instanceof List) {
            List<Map> inputs = getInputs((List<Map<String, Serializable>>) object, artifact);
            slangTestCase.setInputs(inputs);
        }
    }

    private List<Map> getInputs(List<Map<String, Serializable>> inputsList, String artifact) {
        List<Map> inputs = new ArrayList<>();
        for (Map<String, Serializable> singleElementInputMap : inputsList) {
            inputs.add(slangSourceService.convertInputFromMap(singleElementInputMap, artifact));
        }
        return inputs;
    }

    public Set<SystemProperty> parseProperties(String fileName) {
        Set<SystemProperty> result = new HashSet<>();
        File file = new File(fileName);
        Extension.validatePropertiesFileExtension(file.getName());
        SlangSource source = SlangSource.fromFile(new File(fileName));
        result.addAll(slang.loadSystemProperties(source));
        return result;
    }
}
