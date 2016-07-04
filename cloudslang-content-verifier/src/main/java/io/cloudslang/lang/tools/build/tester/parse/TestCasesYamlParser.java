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
import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.compiler.Extension;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static ch.lambdaj.Lambda.convertMap;

@Component
public class TestCasesYamlParser {

    @Autowired
    private Yaml yaml;

    @Autowired
    Slang slang;

    private ObjectMapper objectMapper = new ObjectMapper();

    private final static Logger log = Logger.getLogger(TestCasesYamlParser.class);

    public Map<String, SlangTestCase> parseTestCases(SlangSource source) {

        if(StringUtils.isEmpty(source.getSource())){
            log.info("No tests cases were found in: " + source.getFileName());
            return new HashMap<>();
        }
        Validate.notEmpty(source.getSource(), "Source " + source.getFileName() + " cannot be empty");

        try {
            @SuppressWarnings("unchecked") Map<String, Map> parsedTestCases = yaml.loadAs(source.getSource(), Map.class);
            if (MapUtils.isEmpty(parsedTestCases)){
                log.info("No tests cases were found in: " + source.getFileName());
                return new HashMap<>();
            }
            return convertMap(parsedTestCases, new Converter<Map, SlangTestCase>() {
                @Override
                public SlangTestCase convert(Map from) {
                    return parseTestCase(from);
                }
            });
        } catch (Throwable e) {
            throw new RuntimeException("There was a problem parsing the YAML source: " + source.getFileName() +
                    source.getFileExtension().getValue() + ".\n" + e.getMessage(), e);
        }
    }

    private SlangTestCase parseTestCase(Map map) {
        try {
            String content = objectMapper.writeValueAsString(map);
            SlangTestCase slangTestCase = objectMapper.readValue(content, SlangTestCase.class);
            setInputs(slangTestCase, map);

            return slangTestCase;
        } catch (IOException e) {
            throw new RuntimeException("Error parsing slang test case", e);
        }
    }

    private void setInputs(SlangTestCase slangTestCase, Map map) {
        Object object = map.get(SlangTextualKeys.INPUTS_KEY);
        if (object instanceof List) {
            List<Map> inputs = getInputs((List<Map<String, Serializable>>) object);
            slangTestCase.setInputs(inputs);
        }
    }

    private List<Map> getInputs(List<Map<String, Serializable>> inputsList) {
        List<Map> inputs = new ArrayList<>();
        for (Map<String, Serializable> singleElementInputMap : inputsList) {
            Map.Entry<String, Serializable> entry = singleElementInputMap.entrySet().iterator().next();

            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked") Map<String, ? extends Serializable> valueMap = (Map) entry.getValue();
                validateKeys(valueMap);
                Map<String, Value> map = new HashMap<>();
                map.put(entry.getKey(), ValueFactory.create(valueMap.get(SlangTextualKeys.VALUE_KEY), isSensitiveValue(valueMap)));
                inputs.add(map);
            } else {
                Map<String, Value> map = new HashMap<>();
                map.put(entry.getKey(), ValueFactory.create(entry.getValue(), false));
                inputs.add(map);
            }
        }
        return inputs;
    }

    private void validateKeys(Map<String, ? extends Serializable> valueMap) {
        List<String> knownModifierKeys = Arrays.asList(SlangTextualKeys.SENSITIVE_KEY, SlangTextualKeys.VALUE_KEY);
        for (String modifierKey : valueMap.keySet()) {
            if (!knownModifierKeys.contains(modifierKey)) {
                throw new RuntimeException(
                        "Artifact has unrecognized tag {" + modifierKey + "}" +
                                ". Supported tags are {" + SlangTextualKeys.VALUE_KEY + "} and {" + SlangTextualKeys.SENSITIVE_KEY +"}. " +
                                "Please take a look at the supported features per versions link.");
            }
        }
    }


    private Boolean isSensitiveValue(Map<String, ? extends Serializable> valueMap) {
        Boolean isSensitive = false;
        if (valueMap.get(SlangTextualKeys.SENSITIVE_KEY) instanceof Boolean) {
            isSensitive = (Boolean) valueMap.get(SlangTextualKeys.SENSITIVE_KEY);
        }
        return isSensitive;
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
