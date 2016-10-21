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

import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.modeller.result.SystemPropertyModellingResult;
import io.cloudslang.lang.entities.SystemProperty;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertTrue;

/**
 * @author Bonczidai Levente
 * @since 8/11/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class LoadSystemPropertiesTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Autowired
    private SlangCompiler compiler;

    @SuppressWarnings("unchecked")
    private static final Set<SystemProperty> EMPTY_PROPS = Collections.EMPTY_SET;

    @Test
    public void testValid() throws Exception {
        URI propertiesUri = getClass().getResource("/properties/a/b/valid.prop.sl").toURI();
        Set<SystemProperty> expectedSystemProperties = getSystemPropertiesValid();
        Set<SystemProperty> actualSystemProperties = loadSystemProperties(SlangSource.fromFile(propertiesUri));
        Assert.assertEquals(expectedSystemProperties, actualSystemProperties);
    }

    @Test
    public void testValidNullValue() throws Exception {
        URI propertiesUri = getClass().getResource("/properties/a/b/valid_null_value.prop.sl").toURI();
        Set<SystemProperty> expectedSystemProperties = getSystemPropertiesValidNullValue();
        Set<SystemProperty> actualSystemProperties = loadSystemProperties(SlangSource.fromFile(propertiesUri));
        Assert.assertEquals(expectedSystemProperties, actualSystemProperties);
    }

    @Test
    public void testValidEmptyNamespace() throws Exception {
        URI propertiesUri = getClass().getResource("/properties/a/b/valid_empty_namespace.prop.sl").toURI();
        Set<SystemProperty> expectedSystemProperties = getSystemPropertiesValidEmptyNamespace();
        Set<SystemProperty> actualSystemProperties = loadSystemProperties(SlangSource.fromFile(propertiesUri));
        Assert.assertEquals(expectedSystemProperties, actualSystemProperties);
    }

    @Test
    public void testValidMissingNamespaceKey() throws Exception {
        URI propertiesUri = getClass().getResource("/properties/a/b/valid_missing_namespace_key.prop.sl").toURI();
        Set<SystemProperty> expectedSystemProperties = getSystemPropertiesValidEmptyNamespace();
        Set<SystemProperty> actualSystemProperties = loadSystemProperties(SlangSource.fromFile(propertiesUri));
        Assert.assertEquals(expectedSystemProperties, actualSystemProperties);
    }

    @Test
    public void testValidNoProperties() throws Exception {
        URI propertiesUri = getClass().getResource("/properties/a/b/valid_no_prop.sl").toURI();
        Set<SystemProperty> actualSystemProperties = loadSystemProperties(SlangSource.fromFile(propertiesUri));
        Assert.assertEquals(EMPTY_PROPS, actualSystemProperties);
    }

    @Test
    public void testInvalidMissingProperties() throws Exception {
        final URI propertiesUri = getClass().getResource("/properties/a/b/invalid_missing_properties.prop.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage(SlangCompilerImpl.ERROR_LOADING_PROPERTIES_FILE_MESSAGE);
        exception.expectMessage("no content associated");
        loadSystemProperties(SlangSource.fromFile(propertiesUri));
    }

    @Test
    public void testInvalidMissingPropertiesTag() throws Exception {
        final URI propertiesUri = getClass()
                .getResource("/properties/a/b/invalid_missing_properties_tag.prop.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage(
                "Unable to find property 'wrong_key' on class: io.cloudslang.lang.compiler.parser.model.ParsedSlang"
        );
        loadSystemProperties(SlangSource.fromFile(propertiesUri));
    }

    @Test
    public void testMapUnderProperties() throws Exception {
        final URI propertiesUri = getClass().getResource("/properties/a/b/map_under_properties.prop.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage(SlangCompilerImpl.ERROR_LOADING_PROPERTIES_FILE_MESSAGE);
        exception.expectMessage(SlangTextualKeys.SYSTEM_PROPERTY_KEY);
        exception.expectMessage("list");
        exception.expectMessage("Map");
        loadSystemProperties(SlangSource.fromFile(propertiesUri));
    }

    @Test
    public void testListElementNotMap() throws Exception {
        final URI propertiesUri = getClass().getResource("/properties/a/b/list_element_not_map.prop.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage(SlangCompilerImpl.ERROR_LOADING_PROPERTIES_FILE_MESSAGE);
        exception.expectMessage(SlangCompilerImpl.PROPERTY_LIST_ELEMENT_WRONG_TYPE_ERROR_MESSAGE_PREFIX);
        exception.expectMessage("i_am_string(java.lang.String)");
        loadSystemProperties(SlangSource.fromFile(propertiesUri));
    }

    @Test
    public void testListElementNull() throws Exception {
        final URI propertiesUri = getClass().getResource("/properties/a/b/list_element_null.prop.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage(SlangCompilerImpl.ERROR_LOADING_PROPERTIES_FILE_MESSAGE);
        exception.expectMessage(SlangCompilerImpl.PROPERTY_LIST_ELEMENT_WRONG_TYPE_ERROR_MESSAGE_PREFIX);
        exception.expectMessage("null");
        loadSystemProperties(SlangSource.fromFile(propertiesUri));
    }

    @Test
    public void testListElementMapWithMultipleEntries() throws Exception {
        final URI propertiesUri = getClass().getResource("/properties/a/b/map_with_multiple_entries.prop.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage(SlangCompilerImpl.ERROR_LOADING_PROPERTIES_FILE_MESSAGE);
        exception.expectMessage(SlangCompilerImpl.SIZE_OF_SYSTEM_PROPERTY_ERROR_MESSAGE_PREFIX);
        exception.expectMessage("{key1=val1, key2=val2}");
        exception.expectMessage("2");
        loadSystemProperties(SlangSource.fromFile(propertiesUri));
    }

    @Test
    public void testWrongSystemPropertyKeyType() throws Exception {
        final URI propertiesUri = getClass().getResource("/properties/a/b/wrong_key_type.prop.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage(SlangCompilerImpl.ERROR_LOADING_PROPERTIES_FILE_MESSAGE);
        exception.expectMessage(SlangCompilerImpl.SYSTEM_PROPERTY_KEY_WRONG_TYPE_ERROR_MESSAGE_PREFIX);
        exception.expectMessage("123");
        exception.expectMessage("Integer");
        loadSystemProperties(SlangSource.fromFile(propertiesUri));
    }

    @Test
    public void testWrongSystemPropertyKeyTypeFromSource() throws Exception {
        final URI propertiesUri = getClass().getResource("/properties/a/b/wrong_key_type.prop.sl").toURI();
        SystemPropertyModellingResult result =
                compiler.loadSystemPropertiesFromSource(SlangSource.fromFile(propertiesUri));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage(SlangCompilerImpl.ERROR_LOADING_PROPERTIES_FILE_MESSAGE);
        exception.expectMessage(SlangCompilerImpl.SYSTEM_PROPERTY_KEY_WRONG_TYPE_ERROR_MESSAGE_PREFIX);
        exception.expectMessage("123");
        exception.expectMessage("Integer");
        throw result.getErrors().get(0);
    }

    @Test
    public void testDuplicateKey() throws Exception {
        final URI propertiesUri = getClass().getResource("/properties/a/b/duplicate_key.prop.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage(SlangCompilerImpl.ERROR_LOADING_PROPERTIES_FILE_MESSAGE);
        exception.expectMessage(SlangCompilerImpl.DUPLICATE_SYSTEM_PROPERTY_KEY_ERROR_MESSAGE_PREFIX);
        exception.expectMessage("host");
        loadSystemProperties(SlangSource.fromFile(propertiesUri));
    }

    @Test
    public void testDuplicateIgnoringCaseSimpleKey() throws Exception {
        final URI propertiesUri = getClass()
                .getResource("/properties/a/b/duplicate_ignoring_case_simple_key.prop.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage(SlangCompilerImpl.ERROR_LOADING_PROPERTIES_FILE_MESSAGE);
        exception.expectMessage(SlangCompilerImpl.DUPLICATE_SYSTEM_PROPERTY_KEY_ERROR_MESSAGE_PREFIX);
        exception.expectMessage("Host");
        loadSystemProperties(SlangSource.fromFile(propertiesUri));
    }

    @Test
    public void testDuplicateIgnoringCaseComplexKey() throws Exception {
        final URI propertiesUri = getClass()
                .getResource("/properties/a/b/duplicate_ignoring_case_complex_key.prop.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage(SlangCompilerImpl.ERROR_LOADING_PROPERTIES_FILE_MESSAGE);
        exception.expectMessage(SlangCompilerImpl.DUPLICATE_SYSTEM_PROPERTY_KEY_ERROR_MESSAGE_PREFIX);
        exception.expectMessage("restrict.OUT.port");
        loadSystemProperties(SlangSource.fromFile(propertiesUri));
    }

    @Test
    public void testDuplicateIgnoringCaseComplexKeyFromSource() throws Exception {
        final URI propertiesUri = getClass()
                .getResource("/properties/a/b/duplicate_ignoring_case_complex_key.prop.sl").toURI();
        SystemPropertyModellingResult result =
                compiler.loadSystemPropertiesFromSource(SlangSource.fromFile(propertiesUri));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage(SlangCompilerImpl.ERROR_LOADING_PROPERTIES_FILE_MESSAGE);
        exception.expectMessage(SlangCompilerImpl.DUPLICATE_SYSTEM_PROPERTY_KEY_ERROR_MESSAGE_PREFIX);
        exception.expectMessage("restrict.OUT.port");
        throw result.getErrors().get(0);
    }

    @Test
    public void testMultipleExceptionsFromSource() throws Exception {
        final URI propertiesUri = getClass().getResource("/properties/a/b/multiple_invalid.prop.sl").toURI();
        SystemPropertyModellingResult result =
                compiler.loadSystemPropertiesFromSource(SlangSource.fromFile(propertiesUri));
        assertTrue(result.getErrors().size() == 3);
        assertTrue(result.getErrors().get(0).getMessage()
                .contains("Error loading properties source: 'multiple_invalid.prop.sl'. " +
                "Nested exception is: Error validating system property namespace." +
                " Nested exception is: Argument[a.!.b] violates character rules."));
        assertTrue(result.getErrors().get(1).getMessage()
                .contains("Error loading properties source: 'multiple_invalid.prop.sl'. " +
                "Nested exception is: Error validating system property key. Nested exception is:" +
                " Argument[c.?.name] violates character rules."));
        assertTrue(result.getErrors().get(2).getMessage()
                .contains(SlangCompilerImpl.ERROR_LOADING_PROPERTIES_FILE_MESSAGE));
        assertTrue(result.getErrors().get(2).getMessage()
                .contains(SlangCompilerImpl.DUPLICATE_SYSTEM_PROPERTY_KEY_ERROR_MESSAGE_PREFIX));
        assertTrue(result.getErrors().get(2).getMessage().contains("restrict.OUT.port"));
    }

    @Test
    public void testInvalidCharsNamespace() throws Exception {
        final URI propertiesUri = getClass().getResource("/properties/a/b/invalid_1.prop.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage(
                "Error loading properties source: 'invalid_1.prop.sl'. " +
                        "Nested exception is: Error validating system property namespace." +
                        " Nested exception is: Argument[a.!.b] violates character rules."
        );
        loadSystemProperties(SlangSource.fromFile(propertiesUri));
    }

    @Test
    public void testInvalidCharsNamespaceFromSource() throws Exception {
        final URI propertiesUri = getClass().getResource("/properties/a/b/invalid_1.prop.sl").toURI();
        SystemPropertyModellingResult result = compiler
                .loadSystemPropertiesFromSource(SlangSource.fromFile(propertiesUri));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage(
                "Error loading properties source: 'invalid_1.prop.sl'. " +
                        "Nested exception is: Error validating system property namespace." +
                        " Nested exception is: Argument[a.!.b] violates character rules."
        );
        throw result.getErrors().get(0);
    }

    @Test
    public void testInvalidCharsKey() throws Exception {
        final URI propertiesUri = getClass().getResource("/properties/a/b/invalid_2.prop.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage(
                "Error loading properties source: 'invalid_2.prop.sl'. Nested exception is:" +
                        " Error validating system property key. Nested exception is:" +
                        " Argument[c.?.name] violates character rules."
        );
        loadSystemProperties(SlangSource.fromFile(propertiesUri));
    }

    private Set<SystemProperty> getSystemPropertiesValid() {
        return getSystemPropertiesBasic("a.b");
    }

    private Set<SystemProperty> getSystemPropertiesValidNullValue() {
        return newHashSet(new SystemProperty("a.b", "host", (String) null));
    }

    private Set<SystemProperty> loadSystemProperties(SlangSource source) {
        return compiler.loadSystemProperties(source);
    }

    private Set<SystemProperty> getSystemPropertiesValidEmptyNamespace() {
        return getSystemPropertiesBasic("");
    }

    private Set<SystemProperty> getSystemPropertiesBasic(String namespace) {
        return newHashSet(new SystemProperty(namespace, "host", "localhost"),
                new SystemProperty(namespace, "c.name", "john doe"),
                new SystemProperty(namespace, "restrict.out.port", "8080"));
    }

}