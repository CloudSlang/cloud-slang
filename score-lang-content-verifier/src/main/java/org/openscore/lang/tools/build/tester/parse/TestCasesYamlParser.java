package org.openscore.lang.tools.build.tester.parse;
/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/


/*
 * Created by orius123 on 05/11/14.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.Validate;
import org.openscore.lang.compiler.SlangSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.Map;

@Component
public class TestCasesYamlParser {

    @Autowired
    private Yaml yaml;

    private ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, SlangTestCase> parse(SlangSource source) {

        Validate.notEmpty(source.getSource(), "Source " + source.getName() + " cannot be empty");

        try {
            Map<String, Map> parsedTestCases = yaml.loadAs(source.getSource(), Map.class);
            for(Map.Entry<String, Map> testCaseEntry : parsedTestCases.entrySet()){
//                objectMapper.convertValue(testCase.getValue(), ParsedSlangTestCase.class);
                createTestCase(testCaseEntry);
            }
            if(parsedTestCases == null) {
                throw new RuntimeException("Source " + source.getName() + " does not contain YAML content");
            }
//            parsedSlang.setName(source.getName());
//            return parsedSlang.getTest_cases();
        } catch (Throwable e) {
            throw new RuntimeException("There was a problem parsing the YAML source: " + source.getName() + ".\n" + e.getMessage(), e);
        }
        return new HashMap<>();
    }

    private void createTestCase(Map.Entry<String, Map> testCase) {
        String testCaseName = testCase.getKey();
//        Map testCaseValue =
    }

}
