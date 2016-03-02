package io.cloudslang.lang.compiler;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * User: bancl
 * Date: 3/1/2016
 */
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
        for(int i = 0; i < extensions.length; i++)
            extensionValues[i] = extensions[i].getValue();
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

    public static String[] getPropertiesFileExtensionValues() {
        return Arrays.copyOfRange(extensionValues, 3, 4);
    }

    public static String[] getYamlFileExtensionValues() {
        return Arrays.copyOfRange(extensionValues, 4, extensionValues.length);
    }

    public static String removeExtension(String fileName) {
        Extension extension = findExtension(fileName);
        if (extension != null) return StringUtils.removeEnd(fileName, "." + extension.getValue());
        return fileName;
    }

    private static Extension findExtension(String fileName) {
        String fileNameTail = fileName.substring(fileName.length() - getLargestExtensionLength() - 1,
                fileName.length());
        Extension foundExtension = null;
        for (Extension extension : values()) {
            if (fileNameTail.endsWith("." + extension.getValue()) &&
                    (foundExtension == null || extension.getValue().length() > foundExtension.getValue().length())) {
                foundExtension = extension;
            }
        }
        return foundExtension;
    }

    private static int getLargestExtensionLength() {
        int length = 0;
        for (Extension extension : values()) {
            if (extension.getValue().length() > length) length = extension.getValue().length();
        }
        return length;
    }
}