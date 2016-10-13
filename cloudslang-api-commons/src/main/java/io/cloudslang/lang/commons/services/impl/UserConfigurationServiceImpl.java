/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.commons.services.impl;

import io.cloudslang.lang.commons.services.api.UserConfigurationService;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Bonczidai Levente
 * @since 8/22/2016
 */
public class UserConfigurationServiceImpl implements UserConfigurationService {
    private static final String USER_CONFIG_DIR = "configuration";
    private static final String USER_CONFIG_FILENAME = "cslang.properties";
    private static final String USER_CONFIG_FILEPATH = USER_CONFIG_DIR + File.separator + USER_CONFIG_FILENAME;
    private static final String SUBSTITUTION_REGEX = "\\$\\{([^${}]+)\\}"; // ${system.property.name}
    private static final Pattern SUBSTITUTION_PATTERN = Pattern.compile(SUBSTITUTION_REGEX);

    @Override
    public void loadUserProperties() throws IOException {
        String appHome = System.getProperty("app.home");
        String propertyFilePath = appHome + File.separator + USER_CONFIG_FILEPATH;
        File propertyFile = new File(propertyFilePath);
        Properties rawProperties = new Properties();
        if (propertyFile.isFile()) {
            try (InputStream propertiesStream = new FileInputStream(propertyFilePath)) {
                rawProperties.load(propertiesStream);
            }
        }
        Set<Map.Entry<Object, Object>> propertyEntries = rawProperties.entrySet();
        for (Map.Entry<Object, Object> property : propertyEntries) {
            String key = (String) property.getKey();
            String value = (String) property.getValue();
            value = substitutePropertyReferences(value);
            System.setProperty(key, value);
        }
    }

    private String substitutePropertyReferences(String value) {
        Set<String> variableNames = findPropertyReferences(value);
        return replacePropertyReferences(value, variableNames);
    }

    private Set<String> findPropertyReferences(String value) {
        Matcher mather = SUBSTITUTION_PATTERN.matcher(value);
        Set<String> variableNames = new HashSet<>();
        while (mather.find()) {
            variableNames.add(mather.group(1));
        }
        return variableNames;
    }

    private String replacePropertyReferences(String value, Set<String> variableNames) {
        for (String variableName : variableNames) {
            String variableValue = System.getProperty(variableName);
            if (StringUtils.isNotEmpty(variableValue)) {
                value = value.replace("${" + variableName + "}", variableValue);
            }
        }
        return value;
    }

}
