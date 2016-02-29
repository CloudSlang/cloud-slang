/**
 * ****************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 * <p/>
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * *****************************************************************************
 */
package io.cloudslang.lang.cli;

import org.apache.commons.lang3.StringUtils;
import org.springframework.shell.Bootstrap;

import java.io.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Bonczidai Levente
 * @since 12/16/2015
 */
public class SlangBootstrap {

    private static final String USER_CONFIG_DIR = "configuration";
    private static final String USER_CONFIG_FILENAME = "cslang.properties";
    private static final String USER_CONFIG_FILEPATH = USER_CONFIG_DIR + File.separator + USER_CONFIG_FILENAME;
    private static final String SUBSTITUTION_REGEX = "\\$\\{([^${}]+)\\}"; // ${system.property.name}
    private static final Pattern SUBSTITUTION_PATTERN = Pattern.compile(SUBSTITUTION_REGEX);

    public static void main(String[] args) throws IOException {
        try {
            loadUserProperties();
        } catch (Exception ex) {
            System.out.println("Error occurred while loading user configuration: " + ex.getMessage());
            ex.printStackTrace();
        }
        System.out.println("Loading..");
        Bootstrap.main(args);
    }

    private static void loadUserProperties() throws IOException {
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

    private static String substitutePropertyReferences(String value) {
        Set<String> variableNames = findPropertyReferences(value);
        return replacePropertyReferences(value, variableNames);
    }

    private static Set<String> findPropertyReferences(String value) {
        Matcher mather = SUBSTITUTION_PATTERN.matcher(value);
        Set<String> variableNames = new HashSet<>();
        while (mather.find()) {
            variableNames.add(mather.group(1));
        }
        return variableNames;
    }

    private static String replacePropertyReferences(String value, Set<String> variableNames) {
        for (String variableName : variableNames) {
            String variableValue = System.getProperty(variableName);
            if (StringUtils.isNotEmpty(variableValue)) {
                value = value.replace("${" + variableName + "}", variableValue);
            }
        }
        return value;
    }

}
