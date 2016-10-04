package io.cloudslang.lang.tools.build;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

/**
 * This class provides some utility operation to read values from a java.util.Properties object in a friendlier mode.
 */
public class ArgumentProcessorUtils {

    private final static String NOT_TS = "!";
    private static final String SUITE_LIST_SEPARATOR = ",";
    public static final String PROPERTIES_OBJECT_CANNOT_BE_NULL = "Properties object cannot be null";
    public static final String PROPERTY_KEY_CANNOT_BE_NULL = "Property key cannot be null";

    private ArgumentProcessorUtils() {
    }

    /**
     * Returns a property value as boolean value, with a default in case of null.
     *
     * @param propertyKey the key inside the properties entries.
     * @param defaultBooleanValue the default boolean value to use in case it is not present inside the properties entries.
     * @param properties the properties entries.
     * @return
     */
    public static boolean getBooleanFromPropertiesWithDefault(String propertyKey, boolean defaultBooleanValue, Properties properties) {
        validateArguments(propertyKey, properties);
        String valueBoolean = properties.getProperty(propertyKey);
        return (equalsIgnoreCase(valueBoolean, TRUE.toString()) || equalsIgnoreCase(valueBoolean, FALSE.toString())) ? parseBoolean(valueBoolean) : defaultBooleanValue;
    }

    /**
     * Returns a property value as an integer value, with a default in case the range contract is not respected or in case of null.
     *
     * @param propertyKey the key inside the properties entries.
     * @param defaultIntValue the default integer value to use in case it is not present inside the properties entries.
     * @param properties the properties entries.
     * @param lowerLimit the lower limit for the integer value against which the comparison is done using >= sign.
     * @param upperLimit the upper limit for the integer value against which the comparison is done using < sign.
     * @return
     */
    public static int getIntFromPropertiesWithDefaultAndRange(String propertyKey, int defaultIntValue, Properties properties, Integer lowerLimit, Integer upperLimit) {
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
    public static <T extends Enum<T>> T getEnumInstanceFromPropertiesWithDefault(String propertyKey, T defaultValue, Properties properties) {
        validateArguments(propertyKey, properties);
        try {
            String propertyValue = properties.getProperty(propertyKey);
            if (isNotEmpty(propertyValue)) {
                @SuppressWarnings("unchecked")
                Class<T> aClass = (Class<T>) defaultValue.getClass();
                return T.valueOf(aClass, propertyValue.toUpperCase(ENGLISH));
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
     * @return the test suite names as a java.util.List<String>
     */
    public static List<String> parseTestSuitesToList(final String suitesString) {
        return isNotEmpty(suitesString) ? parseTestSuitesToList(asList(suitesString.split(SUITE_LIST_SEPARATOR))) : new ArrayList<String>();
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

            if (!startsWithIgnoreCase(testSuite, NOT_TS) && !equalsIgnoreCase(testSuite, SlangBuildMain.DEFAULT_TESTS)) { // every normal test suite except default
                if (!isSuitePresent(testSuites, testSuite)) {
                    testSuites.add(testSuite);
                }
            } else if (!containsNotDefaultTestSuite && equalsIgnoreCase(testSuite, SlangBuildMain.DEFAULT_TESTS)) {   // default test suite
                containsDefaultTestSuite = true;
            } else if (!containsNotDefaultTestSuite && equalsIgnoreCase(testSuite, notDefaultTestSuite)) { // !default test suite
                containsNotDefaultTestSuite = true;
            }
        }

        // Add the default test suite once
        if (!containsNotDefaultTestSuite && containsDefaultTestSuite) {
            testSuites.add(SlangBuildMain.DEFAULT_TESTS);
        }
        return testSuites;
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
