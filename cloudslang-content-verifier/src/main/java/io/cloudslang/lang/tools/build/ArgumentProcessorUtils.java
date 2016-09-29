package io.cloudslang.lang.tools.build;

import java.util.Properties;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static java.util.Locale.ENGLISH;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class ArgumentProcessorUtils {

    private ArgumentProcessorUtils() {
    }

    public static boolean getBooleanFromPropertiesWithDefault(String propertyKey, boolean defaultBooleanValue, Properties properties) {
        String valueBoolean = properties.getProperty(propertyKey);
        return (equalsIgnoreCase(valueBoolean, TRUE.toString()) || equalsIgnoreCase(valueBoolean, FALSE.toString())) ? parseBoolean(valueBoolean) : defaultBooleanValue;
    }

    public static int getIntFromPropertiesWithDefaultAndRange(String propertyKey, int defaultIntValue, Properties properties, Integer lowerLimit, Integer upperLimit) {
        try {
            int value = parseInt(properties.getProperty(propertyKey, valueOf(defaultIntValue)));
            return ((lowerLimit != null) && (upperLimit != null)) ?
                    (((value >= lowerLimit) && (value < upperLimit)) ? value : defaultIntValue)
                    : value;
        } catch (NumberFormatException nfEx) {
            return defaultIntValue;
        }
    }

    public static <T extends Enum<T>> T getEnumInstanceFromPropertiesWithDefault(String propertyKey, T defaultValue, Properties properties) {
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

}
