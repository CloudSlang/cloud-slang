/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum Extension {
    SL("sl"),
    SL_YAML("sl.yaml"),
    SL_YML("sl.yml"),
    PROP_SL("prop.sl"),
    YAML("yaml"),
    YML("yml");

    private final String value;
    private static final String[] extensionValues = new String[values().length];

    static {
        initExtensionValues();
    }

    private static void initExtensionValues() {
        Extension[] extensions = values();
        for (int i = 0; i < extensions.length; i++) {
            extensionValues[i] = extensions[i].getValue();
        }
    }

    Extension(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static String[] getSlangFileExtensionValues() {
        return Arrays.copyOfRange(extensionValues, 0, 3);
    }

    public static Extension[] getSlangFileExtensions() {
        return Arrays.copyOfRange(values(), 0, 3);
    }

    public static String[] getPropertiesFileExtensionValues() {
        return Arrays.copyOfRange(extensionValues, 3, 4);
    }

    public static Extension[] getPropertiesFileExtensions() {
        return Arrays.copyOfRange(values(), 3, 4);
    }

    public static String[] getYamlFileExtensionValues() {
        return Arrays.copyOfRange(extensionValues, 4, extensionValues.length);
    }

    public static String getExtensionValuesAsString() {
        return StringUtils.join(extensionValues, ", ");
    }

    public static void validateSlangFileExtension(String fileName) {
        Extension fileExtension = Extension.findExtension(fileName);
        validateFileExtension(fileName, fileExtension, getSlangFileExtensions(), getSlangFileExtensionValues());
    }

    public static void validatePropertiesFileExtension(String fileName) {
        Extension fileExtension = Extension.findExtension(fileName);
        validateFileExtension(fileName, fileExtension,
                getPropertiesFileExtensions(), getPropertiesFileExtensionValues());
    }

    private static void validateFileExtension(String fileName, Extension fileExtension,
                                              Extension[] extensions, String[] extensionValues) {
        boolean validFileExtension = false;
        for (Extension extension : extensions) {
            if (extension.equals(fileExtension)) {
                validFileExtension = true;
            }
        }
        String extensionsAsString = Arrays.toString(extensionValues);
        Validate.isTrue(validFileExtension,
                "File: " + fileName + " must have one of the following extensions: " +
                        extensionsAsString.substring(1, extensionsAsString.length() - 1) + "."
        );
    }

    public static String removeExtension(String fileName) {
        Extension extension = findExtension(fileName);
        if (extension != null) {
            return StringUtils.removeEnd(fileName, "." + extension.getValue());
        }
        return fileName;
    }

    public static Extension findExtension(String fileName) {
        Extension foundExtension = null;
        if (fileName != null) {
            for (Extension extension : values()) {
                final String extensionValue = extension.getValue();
                if (fileName.endsWith("." + extensionValue) &&
                        (foundExtension == null || extensionValue.length() > foundExtension.getValue().length())) {
                    foundExtension = extension;
                }
            }
        }
        return foundExtension;
    }
}