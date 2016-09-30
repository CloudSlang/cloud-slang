package io.cloudslang.lang.tools.build;


import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

import static io.cloudslang.lang.tools.build.ArgumentProcessorUtils.PROPERTIES_OBJECT_CANNOT_BE_NULL;
import static io.cloudslang.lang.tools.build.ArgumentProcessorUtils.PROPERTY_KEY_CANNOT_BE_NULL;
import static io.cloudslang.lang.tools.build.ArgumentProcessorUtils.getBooleanFromPropertiesWithDefault;
import static io.cloudslang.lang.tools.build.ArgumentProcessorUtils.getEnumInstanceFromPropertiesWithDefault;
import static io.cloudslang.lang.tools.build.ArgumentProcessorUtils.getIntFromPropertiesWithDefaultAndRange;
import static io.cloudslang.lang.tools.build.SlangBuildMain.TestCaseRunMode.PARALLEL;
import static io.cloudslang.lang.tools.build.SlangBuildMain.TestCaseRunMode.SEQUENTIAL;
import static org.junit.Assert.fail;

public class ArgumentProcessorUtilsTest {

    @Test
    public void testGetBooleanFromPropertiesWithDefaultExceptions() {
        testExceptionGetBooleanWithParams("aa", false, null, PROPERTIES_OBJECT_CANNOT_BE_NULL);
        testExceptionGetBooleanWithParams(null, true, new Properties(), PROPERTY_KEY_CANNOT_BE_NULL);
    }

    @Test
    public void testGetBooleanFromPropertiesWithDefaultSuccess() {
        // Missing property, mean default value is returned
        Properties properties = new Properties();
        Assert.assertEquals(true, getBooleanFromPropertiesWithDefault("bbb", true, properties));
        Assert.assertEquals(false, getBooleanFromPropertiesWithDefault("bbb", false, properties));

        // Wrong value means default value is returned
        properties = new Properties();
        properties.setProperty("bbb", "jkasda");
        Assert.assertEquals(true, getBooleanFromPropertiesWithDefault("bbb", true, properties));
        Assert.assertEquals(false, getBooleanFromPropertiesWithDefault("bbb", false, properties));

        // Correct value means default is NOT taken
        properties = new Properties();
        properties.setProperty("bbb", "TruE");
        Assert.assertEquals(true, getBooleanFromPropertiesWithDefault("bbb", false, properties));
        Assert.assertEquals(true, getBooleanFromPropertiesWithDefault("bbb", true, properties));

        // Correct value means default is NOT taken
        properties.setProperty("ccc", "FalSE");
        Assert.assertEquals(false, getBooleanFromPropertiesWithDefault("ccc", false, properties));
        Assert.assertEquals(false, getBooleanFromPropertiesWithDefault("ccc", true, properties));
    }

    @Test
    public void testGetIntFromPropertiesWithDefaultExceptions() {
        testExceptionGetIntWithParams("aa", 3, null, PROPERTIES_OBJECT_CANNOT_BE_NULL, 7, 11);
        testExceptionGetIntWithParams(null, 5, new Properties(), PROPERTY_KEY_CANNOT_BE_NULL, 2, 5);
    }

    @Test
    public void testGetIntFromPropertiesWithDefaultSuccess() {
        // Missing property, mean default value is returned
        Properties properties = new Properties();
        Assert.assertEquals(11, getIntFromPropertiesWithDefaultAndRange("aaa", 11, properties, null, null));
        Assert.assertEquals(21, getIntFromPropertiesWithDefaultAndRange("aaa", 21, properties, 2, 3));

        // Wrong value means default value is returned
        properties = new Properties();
        properties.setProperty("bbb", "56aava"); // string
        Assert.assertEquals(20, getIntFromPropertiesWithDefaultAndRange("bbb", 20, properties, null, null));
        properties.setProperty("bbb", "11111111111111111111111"); //too large
        Assert.assertEquals(40, getIntFromPropertiesWithDefaultAndRange("bbb", 40, properties, 6, 12));

        // Correct value means default is NOT taken
        properties = new Properties();
        properties.setProperty("ccc", "5");
        Assert.assertEquals(5, getIntFromPropertiesWithDefaultAndRange("ccc", 10, properties, null, null));
        Assert.assertEquals(5, getIntFromPropertiesWithDefaultAndRange("ccc", 11, properties, 1, 20));
        Assert.assertEquals(5, getIntFromPropertiesWithDefaultAndRange("ccc", 12, properties, 1, null));

        // Check that range is checked
        properties.setProperty("ddd", "234");
        Assert.assertEquals(90, getIntFromPropertiesWithDefaultAndRange("ddd", 90, properties, 1, 100));
        Assert.assertEquals(234, getIntFromPropertiesWithDefaultAndRange("ddd", 150, properties, 1, 300));
    }

    @Test
    public void testGetEnumFromPropertiesWithDefaultExceptions() {
        testExceptionGetEnumWithParams("aa", PARALLEL, null, PROPERTIES_OBJECT_CANNOT_BE_NULL);
        testExceptionGetEnumWithParams(null, SEQUENTIAL, new Properties(), PROPERTY_KEY_CANNOT_BE_NULL);
    }

    @Test
    public void testGetEnumFromPropertiesWithDefaultSuccess() {
        // Missing property, mean default value is returned
        Properties properties = new Properties();
        Assert.assertSame(PARALLEL, getEnumInstanceFromPropertiesWithDefault("bbb", PARALLEL, properties));
        Assert.assertSame(SEQUENTIAL, getEnumInstanceFromPropertiesWithDefault("bbb", SEQUENTIAL, properties));

        // Wrong value means default value is returned
        properties = new Properties();
        properties.setProperty("ccc", "parall454");
        Assert.assertSame(SEQUENTIAL, getEnumInstanceFromPropertiesWithDefault("ccc", SEQUENTIAL, properties));
        Assert.assertSame(PARALLEL, getEnumInstanceFromPropertiesWithDefault("ccc", PARALLEL, properties));

        // Correct value means default is NOT taken
        properties = new Properties();
        properties.setProperty("bbb", "PaRaLLeL");
        Assert.assertSame(PARALLEL, getEnumInstanceFromPropertiesWithDefault("bbb", PARALLEL, properties));
        Assert.assertSame(PARALLEL, getEnumInstanceFromPropertiesWithDefault("bbb", SEQUENTIAL, properties));

        properties.setProperty("eee", "SeqUENTial");
        Assert.assertSame(SEQUENTIAL, getEnumInstanceFromPropertiesWithDefault("eee", PARALLEL, properties));
        Assert.assertSame(SEQUENTIAL, getEnumInstanceFromPropertiesWithDefault("eee", SEQUENTIAL, properties));
    }

    private void testExceptionGetBooleanWithParams(final String key, final boolean defaultValue, final Properties properties, final String expectedMessage) {
        try {
            getBooleanFromPropertiesWithDefault(key, defaultValue, properties);
            fail("Expecting exception");
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof NullPointerException);
            Assert.assertEquals(expectedMessage, ex.getMessage());
        }
    }

    private void testExceptionGetIntWithParams(final String key, final int defaultValue, final Properties properties, final String expectedMessage,
                                               final Integer lower, final Integer upper) {
        try {
            getIntFromPropertiesWithDefaultAndRange(key, defaultValue, properties, lower, upper);
            fail("Expecting exception");
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof NullPointerException);
            Assert.assertEquals(expectedMessage, ex.getMessage());
        }
    }

    private <T extends Enum<T>> void testExceptionGetEnumWithParams(final String key, T defaultValue, final Properties properties, final String expectedMessage) {
        try {
            getEnumInstanceFromPropertiesWithDefault(key, defaultValue, properties);
            fail("Expecting exception");
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof NullPointerException);
            Assert.assertEquals(expectedMessage, ex.getMessage());
        }
    }
}
