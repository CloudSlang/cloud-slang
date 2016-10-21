/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build;

import io.cloudslang.lang.compiler.SlangSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.Validate;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static java.util.Locale.ENGLISH;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

/**
 * This class provides some utility operation to read values from a java.util.Properties object in a friendlier mode.
 */
public class ArgumentProcessorUtils {

    private static final String NOT_TS = "!";
    private static final String SUITE_LIST_SEPARATOR = ",";
    static final String PROPERTIES_OBJECT_CANNOT_BE_NULL = "Properties object cannot be null";
    static final String PROPERTY_KEY_CANNOT_BE_NULL = "Property key cannot be null";
    private static final String LIST_JOINER = ", ";
    static final String EMPTY = "<empty>";

    private ArgumentProcessorUtils() {
    }

    /**
     * Returns a property value as boolean value, with a default in case of null.
     *
     * @param propertyKey the key inside the properties entries.
     * @param defaultBooleanValue the default boolean value to use in case it is not present
     *     inside the properties entries.
     * @param properties the properties entries.
     * @return
     */
    public static boolean getBooleanFromPropertiesWithDefault(String propertyKey,
                                                              boolean defaultBooleanValue, Properties properties) {
        validateArguments(propertyKey, properties);
        String valueBoolean = properties.getProperty(propertyKey);
        return (equalsIgnoreCase(valueBoolean, TRUE.toString()) || equalsIgnoreCase(valueBoolean, FALSE.toString())) ?
                parseBoolean(valueBoolean) : defaultBooleanValue;
    }

    /**
     * Returns a property value as an integer value, with a default in case the range contract is not respected or
     *     in case of null.
     *
     * @param propertyKey the key inside the properties entries.
     * @param defaultIntValue the default integer value to use in case it is not present inside the properties entries.
     * @param properties the properties entries.
     * @param lowerLimit the lower limit for the integer value against which the comparison is done using >= sign.
     * @param upperLimit the upper limit for the integer value against which the comparison is done using < sign.
     * @return
     */
    public static int getIntFromPropertiesWithDefaultAndRange(String propertyKey, int defaultIntValue,
                                                              Properties properties, Integer lowerLimit,
                                                              Integer upperLimit) {
        validateArguments(propertyKey, properties);
        try {
            int value = parseInt(properties.getProperty(propertyKey, valueOf(defaultIntValue)));
            return ((lowerLimit != null) && (upperLimit != null)) ?
                    (((value >= lowerLimit) && (value < upperLimit)) ? value : defaultIntValue)
                    : value;
        } catch (NumberFormatException nfEx) {
            return defaultIntValue;
        }
    }

    /**
     *
     * @param propertyKey the key inside the properties entries.
     * @param defaultValue the default value of the enum.
     * @param properties the properties entries.
     * @param <T> generic type T of the enum.
     * @return
     */
    public static <T extends Enum<T>> T getEnumInstanceFromPropertiesWithDefault(String propertyKey, T defaultValue,
                                                                                 Properties properties) {
        validateArguments(propertyKey, properties);
        try {
            String propertyValue = properties.getProperty(propertyKey);
            if (isNotEmpty(propertyValue)) {
                @SuppressWarnings("unchecked")
                Class<T> defaultValueClass = (Class<T>) defaultValue.getClass();
                return T.valueOf(defaultValueClass, propertyValue.toUpperCase(ENGLISH));
            }
        } catch (IllegalArgumentException ignore) {
        }
        return defaultValue;
    }

    /**
     * Validates that the properties and key are not null
     * @param propertyKey the key inside the properties entries.
     * @param properties the properties entries.
     */
    private static void validateArguments(String propertyKey, Properties properties) {
        Validate.notNull(properties, PROPERTIES_OBJECT_CANNOT_BE_NULL);
        Validate.notNull(propertyKey, PROPERTY_KEY_CANNOT_BE_NULL);
    }


    /**
     *
     * @param suitesString commma separated string of test suite names.
     * @return the test suite names as a java.util.List of String type.
     */
    public static List<String> parseTestSuitesToList(final String suitesString) {
        return isNotEmpty(suitesString) ?
                parseTestSuitesToList(asList(suitesString.split(SUITE_LIST_SEPARATOR))) : new ArrayList<String>();
    }

    static List<String> parseTestSuitesToList(List<String> testSuitesArg) {
        List<String> testSuites = new ArrayList<>();
        final String notDefaultTestSuite = NOT_TS + SlangBuildMain.DEFAULT_TESTS;


        boolean containsDefaultTestSuite = false;
        boolean containsNotDefaultTestSuite = false;
        for (String testSuite : testSuitesArg) {
            if (isEmpty(testSuite)) { // Skip empty suites
                continue;
            }

            if (!startsWithIgnoreCase(testSuite, NOT_TS) &&
                    !equalsIgnoreCase(testSuite, SlangBuildMain.DEFAULT_TESTS)) {
                // every normal test suite except default
                if (!isSuitePresent(testSuites, testSuite)) {
                    testSuites.add(testSuite);
                }
            } else if (!containsNotDefaultTestSuite &&
                    equalsIgnoreCase(testSuite, SlangBuildMain.DEFAULT_TESTS)) {   // default test suite
                containsDefaultTestSuite = true;
            } else if (!containsNotDefaultTestSuite &&
                    equalsIgnoreCase(testSuite, notDefaultTestSuite)) { // !default test suite
                containsNotDefaultTestSuite = true;
            }
        }

        // Add the default test suite once
        if (!containsNotDefaultTestSuite && containsDefaultTestSuite) {
            testSuites.add(SlangBuildMain.DEFAULT_TESTS);
        }
        return testSuites;
    }

    /**
     *  Returns the properties entries inside that file as a java.util.Properties object.
     *
     * @param propertiesAbsolutePath the absolute path to the run configuration properties file
     * @return
     */
    public static Properties getPropertiesFromFile(String propertiesAbsolutePath) {
        try (FileInputStream fileInputStream = new FileInputStream(new File(propertiesAbsolutePath))) {
            Properties properties = new Properties();
            properties.load(fileInputStream);
            return properties;
        } catch (IOException ioEx) {
            throw new RuntimeException("Failed to read from properties file '" + propertiesAbsolutePath + "': ", ioEx);
        }
    }

    public static Set<String> loadChangedItems(String filePath) throws IOException {
        Set<String> changedItems = new HashSet<>();
        try (InputStream fileInputStream = new FileInputStream(filePath)) {
            LineIterator lineIterator = IOUtils.lineIterator(fileInputStream, SlangSource.getCloudSlangCharset());
            while (lineIterator.hasNext()) {
                changedItems.add(lineIterator.next());
            }
        }
        return changedItems;
    }

    /**
     *
     * @param stringList the list of strings
     * @param emptyMessage the empty message to use
     * @return A string joining the test suite names using io.cloudslang.lang.tools.build.SlangBuildMain#LIST_JOINER
     */
    public static String getListForPrint(final List<String> stringList, final String emptyMessage) {
        return CollectionUtils.isEmpty(stringList) ?
                ((emptyMessage == null) ? EMPTY : emptyMessage) : join(stringList, LIST_JOINER);
    }

    /**
     *
     * @param stringList the list of strings
     * @return A string joining the test suite names using io.cloudslang.lang.tools.build.SlangBuildMain#LIST_JOINER
     */
    public static String getListForPrint(final List<String> stringList) {
        return CollectionUtils.isEmpty(stringList) ? EMPTY : join(stringList, LIST_JOINER);
    }

    private static boolean isSuitePresent(final List<String> crtList, final String testSuite) {
        for (String crtSuite : crtList) {
            if (equalsIgnoreCase(testSuite, crtSuite)) {
                return true;
            }
        }
        return false;
    }
}
