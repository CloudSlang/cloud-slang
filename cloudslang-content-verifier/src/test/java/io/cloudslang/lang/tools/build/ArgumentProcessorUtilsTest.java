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


import com.beust.jcommander.internal.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static com.google.common.io.Resources.getResource;
import static io.cloudslang.lang.tools.build.ArgumentProcessorUtils.PROPERTIES_OBJECT_CANNOT_BE_NULL;
import static io.cloudslang.lang.tools.build.ArgumentProcessorUtils.PROPERTY_KEY_CANNOT_BE_NULL;
import static io.cloudslang.lang.tools.build.ArgumentProcessorUtils.getBooleanFromPropertiesWithDefault;
import static io.cloudslang.lang.tools.build.ArgumentProcessorUtils.getEnumInstanceFromPropertiesWithDefault;
import static io.cloudslang.lang.tools.build.ArgumentProcessorUtils.getIntFromPropertiesWithDefaultAndRange;
import static io.cloudslang.lang.tools.build.ArgumentProcessorUtils.getListForPrint;
import static io.cloudslang.lang.tools.build.ArgumentProcessorUtils.getPropertiesFromFile;
import static io.cloudslang.lang.tools.build.ArgumentProcessorUtils.parseTestSuitesToList;
import static io.cloudslang.lang.tools.build.SlangBuildMain.RunConfigurationProperties.TEST_COVERAGE;
import static io.cloudslang.lang.tools.build.SlangBuildMain.RunConfigurationProperties.TEST_PARALLEL_THREAD_COUNT;
import static io.cloudslang.lang.tools.build.SlangBuildMain.RunConfigurationProperties.TEST_SUITES_PARALLEL;
import static io.cloudslang.lang.tools.build.SlangBuildMain.RunConfigurationProperties.TEST_SUITES_RUN_UNSPECIFIED;
import static io.cloudslang.lang.tools.build.SlangBuildMain.RunConfigurationProperties.TEST_SUITES_SEQUENTIAL;
import static io.cloudslang.lang.tools.build.SlangBuildMain.RunConfigurationProperties.TEST_SUITES_TO_RUN;
import static io.cloudslang.lang.tools.build.SlangBuildMain.TestCaseRunMode.PARALLEL;
import static io.cloudslang.lang.tools.build.SlangBuildMain.TestCaseRunMode.SEQUENTIAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
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
        assertEquals(true, getBooleanFromPropertiesWithDefault("bbb", true, properties));
        assertEquals(false, getBooleanFromPropertiesWithDefault("bbb", false, properties));

        // Wrong value means default value is returned
        properties = new Properties();
        properties.setProperty("bbb", "jkasda");
        assertEquals(true, getBooleanFromPropertiesWithDefault("bbb", true, properties));
        assertEquals(false, getBooleanFromPropertiesWithDefault("bbb", false, properties));

        // Correct value means default is NOT taken
        properties = new Properties();
        properties.setProperty("bbb", "TruE");
        assertEquals(true, getBooleanFromPropertiesWithDefault("bbb", false, properties));
        assertEquals(true, getBooleanFromPropertiesWithDefault("bbb", true, properties));

        // Correct value means default is NOT taken
        properties.setProperty("ccc", "FalSE");
        assertEquals(false, getBooleanFromPropertiesWithDefault("ccc", false, properties));
        assertEquals(false, getBooleanFromPropertiesWithDefault("ccc", true, properties));
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
        assertEquals(11, getIntFromPropertiesWithDefaultAndRange("aaa", 11, properties, null, null));
        assertEquals(21, getIntFromPropertiesWithDefaultAndRange("aaa", 21, properties, 2, 3));

        // Wrong value means default value is returned
        properties = new Properties();
        properties.setProperty("bbb", "56aava"); // string
        assertEquals(20, getIntFromPropertiesWithDefaultAndRange("bbb", 20, properties, null, null));
        properties.setProperty("bbb", "11111111111111111111111"); //too large
        assertEquals(40, getIntFromPropertiesWithDefaultAndRange("bbb", 40, properties, 6, 12));

        // Correct value means default is NOT taken
        properties = new Properties();
        properties.setProperty("ccc", "5");
        assertEquals(5, getIntFromPropertiesWithDefaultAndRange("ccc", 10, properties, null, null));
        assertEquals(5, getIntFromPropertiesWithDefaultAndRange("ccc", 11, properties, 1, 20));
        assertEquals(5, getIntFromPropertiesWithDefaultAndRange("ccc", 12, properties, 1, null));

        // Check that range is checked
        properties.setProperty("ddd", "234");
        assertEquals(90, getIntFromPropertiesWithDefaultAndRange("ddd", 90, properties, 1, 100));
        assertEquals(234, getIntFromPropertiesWithDefaultAndRange("ddd", 150, properties, 1, 300));
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
        assertSame(PARALLEL, getEnumInstanceFromPropertiesWithDefault("bbb", PARALLEL, properties));
        assertSame(SEQUENTIAL, getEnumInstanceFromPropertiesWithDefault("bbb", SEQUENTIAL, properties));

        // Wrong value means default value is returned
        properties = new Properties();
        properties.setProperty("ccc", "parall454");
        assertSame(SEQUENTIAL, getEnumInstanceFromPropertiesWithDefault("ccc", SEQUENTIAL, properties));
        assertSame(PARALLEL, getEnumInstanceFromPropertiesWithDefault("ccc", PARALLEL, properties));

        // Correct value means default is NOT taken
        properties = new Properties();
        properties.setProperty("bbb", "PaRaLLeL");
        assertSame(PARALLEL, getEnumInstanceFromPropertiesWithDefault("bbb", PARALLEL, properties));
        assertSame(PARALLEL, getEnumInstanceFromPropertiesWithDefault("bbb", SEQUENTIAL, properties));

        properties.setProperty("eee", "SeqUENTial");
        assertSame(SEQUENTIAL, getEnumInstanceFromPropertiesWithDefault("eee", PARALLEL, properties));
        assertSame(SEQUENTIAL, getEnumInstanceFromPropertiesWithDefault("eee", SEQUENTIAL, properties));
    }

    @Test
    public void testParseTestSuitesToList() {
        // Case 1
        String testSuitesString = "!default,abc";

        // Tested call
        List<String> suites = parseTestSuitesToList(testSuitesString);
        assertEquals(1, suites.size());
        assertEquals("abc", suites.get(0));

        // Case 2
        testSuitesString = "default,abcd";

        // Tested call
        suites = parseTestSuitesToList(testSuitesString);
        assertEquals(2, suites.size());
        assertEquals("abcd", suites.get(0));
        assertEquals("default", suites.get(1));

        // Case 3
        testSuitesString = "!ab,abcd,!ef,defg,!cd";

        // Tested call
        suites = parseTestSuitesToList(testSuitesString);
        assertEquals(2, suites.size());
        assertEquals("abcd", suites.get(0));
        assertEquals("defg", suites.get(1));

        // Case 4
        testSuitesString = "!default,default,ef";

        // Tested call
        suites = parseTestSuitesToList(testSuitesString);
        assertEquals(1, suites.size());
        assertEquals("ef", suites.get(0));

        // Case 5
        testSuitesString = "default,!default,gh";

        // Tested call
        suites = parseTestSuitesToList(testSuitesString);
        assertEquals(1, suites.size());
        assertEquals("gh", suites.get(0));


        // Case 6
        testSuitesString = "abc,ef,gh,ef";

        // Tested call
        suites = parseTestSuitesToList(testSuitesString);
        assertEquals(3, suites.size());
        assertEquals("abc", suites.get(0));
        assertEquals("ef", suites.get(1));
        assertEquals("gh", suites.get(2));
    }

    @Test
    public void testGetPropertiesFromFileThrowsException() throws URISyntaxException, IOException {
        InputStream fis = null;
        Writer outputWriter = null;
        File tempRunConfigFile = null;
        try {
            fis = new FileInputStream(new File(
                    getResource("lang/tools/build/builder_run_configuration.properties").toURI()));
            Path tempRunConfig = Files.createTempFile("temp_run_config", ".properties");

            tempRunConfigFile = tempRunConfig.toFile();
            outputWriter = new PrintWriter(new FileWriter(tempRunConfigFile));
            IOUtils.copy(fis, outputWriter);
            outputWriter.flush();

            String absolutePath = tempRunConfigFile.getAbsolutePath();
            Properties propertiesFromFile = getPropertiesFromFile(absolutePath);
            assertEquals("false", propertiesFromFile.get(TEST_COVERAGE));
            assertEquals("sequential", propertiesFromFile.get(TEST_SUITES_RUN_UNSPECIFIED));
            assertEquals("!default,vmware-local,xml-local,images", propertiesFromFile.get(TEST_SUITES_TO_RUN));
            assertEquals("images", propertiesFromFile.get(TEST_SUITES_SEQUENTIAL));
            assertEquals("xml-local,vmware-local", propertiesFromFile.get(TEST_SUITES_PARALLEL));
            assertEquals("8", propertiesFromFile.get(TEST_PARALLEL_THREAD_COUNT));
        } finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(outputWriter);
            FileUtils.deleteQuietly(tempRunConfigFile);
        }
    }

    @Test
    public void testGetListForPrint() {
        List<String> testSuites = Lists.newArrayList("aaa", "bbb", "ccc", "ddd");
        assertEquals("aaa, bbb, ccc, ddd", getListForPrint(testSuites));

        testSuites = Lists.newArrayList("AA1");
        assertEquals("AA1", getListForPrint(testSuites));

        testSuites = Lists.newArrayList();
        assertEquals(ArgumentProcessorUtils.EMPTY, getListForPrint(testSuites));

        testSuites = Lists.newArrayList();
        assertEquals("empty list", getListForPrint(testSuites, "empty list"));
    }

    private void testExceptionGetBooleanWithParams(final String key, final boolean defaultValue,
                                                   final Properties properties, final String expectedMessage) {
        try {
            getBooleanFromPropertiesWithDefault(key, defaultValue, properties);
            fail("Expecting exception");
        } catch (Exception ex) {
            assertTrue(ex instanceof NullPointerException);
            assertEquals(expectedMessage, ex.getMessage());
        }
    }

    private void testExceptionGetIntWithParams(final String key, final int defaultValue,
                                               final Properties properties, final String expectedMessage,
                                               final Integer lower, final Integer upper) {
        try {
            getIntFromPropertiesWithDefaultAndRange(key, defaultValue, properties, lower, upper);
            fail("Expecting exception");
        } catch (Exception ex) {
            assertTrue(ex instanceof NullPointerException);
            assertEquals(expectedMessage, ex.getMessage());
        }
    }

    private <T extends Enum<T>> void testExceptionGetEnumWithParams(final String key, T defaultValue,
                                                                    final Properties properties,
                                                                    final String expectedMessage) {
        try {
            getEnumInstanceFromPropertiesWithDefault(key, defaultValue, properties);
            fail("Expecting exception");
        } catch (Exception ex) {
            assertTrue(ex instanceof NullPointerException);
            assertEquals(expectedMessage, ex.getMessage());
        }
    }
}
