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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * @author Bonczidai Levente
 * @since 8/11/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class LoadSystemPropertiesTest {

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
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                loadSystemProperties(SlangSource.fromFile(propertiesUri)));
        Assert.assertEquals("Error loading properties source: 'invalid_missing_properties.prop.sl'. " +
                "Nested exception is: Source invalid_missing_properties.prop.sl has no content associated with " +
                "flow/operation/decision/properties property.", exception.getMessage());
    }

    @Test
    public void testInvalidMissingPropertiesTag() throws Exception {
        final URI propertiesUri = getClass()
                .getResource("/properties/a/b/invalid_missing_properties_tag.prop.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                loadSystemProperties(SlangSource.fromFile(propertiesUri)));
        Assert.assertTrue(exception.getMessage().contains("Unable to find property 'wrong_key' on " +
                "class: io.cloudslang.lang.compiler.parser.model.ParsedSlang"));
    }

    @Test
    public void testMapUnderProperties() throws Exception {
        final URI propertiesUri = getClass().getResource("/properties/a/b/map_under_properties.prop.sl").toURI();
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                loadSystemProperties(SlangSource.fromFile(propertiesUri)));
        Assert.assertEquals("Error loading properties source: 'map_under_properties.prop.sl'." +
                " Nested exception is: Under 'properties' key there should be a list. " +
                "Found: java.util.LinkedHashMap.", exception.getMessage());
    }

    @Test
    public void testListElementNotMap() throws Exception {
        final URI propertiesUri = getClass().getResource("/properties/a/b/list_element_not_map.prop.sl").toURI();
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                loadSystemProperties(SlangSource.fromFile(propertiesUri)));
        Assert.assertEquals("Error loading properties source: 'list_element_not_map.prop.sl'. " +
                "Nested exception is: Property list element should be map in 'key: value' format. " +
                "Found: i_am_string(java.lang.String).", exception.getMessage());
    }

    @Test
    public void testListElementNull() throws Exception {
        final URI propertiesUri = getClass().getResource("/properties/a/b/list_element_null.prop.sl").toURI();
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                loadSystemProperties(SlangSource.fromFile(propertiesUri)));
        Assert.assertEquals("Error loading properties source: 'list_element_null.prop.sl'. " +
                "Nested exception is: Property list element should be map in 'key: value' format. " +
                "Found: null.", exception.getMessage());
    }

    @Test
    public void testListElementMapWithMultipleEntries() throws Exception {
        final URI propertiesUri = getClass().getResource("/properties/a/b/map_with_multiple_entries.prop.sl").toURI();
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                loadSystemProperties(SlangSource.fromFile(propertiesUri)));
        Assert.assertEquals("Error loading properties source: 'map_with_multiple_entries.prop.sl'. " +
                "Nested exception is: Size of system property represented as a map should be 1 (key: value). " +
                "For property: '{key1=val1, key2=val2}' size is: 2.", exception.getMessage());
    }

    @Test
    public void testWrongSystemPropertyKeyType() throws Exception {
        final URI propertiesUri = getClass().getResource("/properties/a/b/wrong_key_type.prop.sl").toURI();
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                loadSystemProperties(SlangSource.fromFile(propertiesUri)));
        Assert.assertEquals("Error loading properties source: 'wrong_key_type.prop.sl'. " +
                "Nested exception is: System property key must be string. " +
                "Found: 123(java.lang.Integer).", exception.getMessage());
    }

    @Test(expected = RuntimeException.class)
    public void testWrongSystemPropertyKeyTypeFromSource() throws Exception {
        final URI propertiesUri = getClass().getResource("/properties/a/b/wrong_key_type.prop.sl").toURI();
        SystemPropertyModellingResult result =
                compiler.loadSystemPropertiesFromSource(SlangSource.fromFile(propertiesUri));
        assertTrue(result.getErrors().size() > 0);
        Assert.assertEquals(
                "Error loading properties source: 'wrong_key_type.prop.sl'. " +
                        "Nested exception is: System property key must be string. Found: 123(java.lang.Integer).",
                result.getErrors().get(0).getMessage());
        throw result.getErrors().get(0);
    }

    @Test
    public void testDuplicateKey() throws Exception {
        final URI propertiesUri = getClass().getResource("/properties/a/b/duplicate_key.prop.sl").toURI();
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                loadSystemProperties(SlangSource.fromFile(propertiesUri)));
        Assert.assertEquals("Error loading properties source: 'duplicate_key.prop.sl'. " +
                "Nested exception is: Duplicate system property key: 'host'.", exception.getMessage());
    }

    @Test
    public void testDuplicateIgnoringCaseSimpleKey() throws Exception {
        final URI propertiesUri = getClass()
                .getResource("/properties/a/b/duplicate_ignoring_case_simple_key.prop.sl").toURI();
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                loadSystemProperties(SlangSource.fromFile(propertiesUri)));
        Assert.assertEquals("Error loading properties source: 'duplicate_ignoring_case_simple_key.prop.sl'." +
                " Nested exception is: Duplicate system property key: 'Host'.", exception.getMessage());
    }

    @Test
    public void testDuplicateIgnoringCaseComplexKey() throws Exception {
        final URI propertiesUri = getClass()
                .getResource("/properties/a/b/duplicate_ignoring_case_complex_key.prop.sl").toURI();
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                loadSystemProperties(SlangSource.fromFile(propertiesUri)));
        Assert.assertEquals("Error loading properties source: 'duplicate_ignoring_case_complex_key.prop.sl'. " +
                "Nested exception is: Duplicate system property key: 'restrict.OUT.port'.", exception.getMessage());
    }

    @Test(expected = RuntimeException.class)
    public void testDuplicateIgnoringCaseComplexKeyFromSource() throws Exception {
        final URI propertiesUri = getClass()
                .getResource("/properties/a/b/duplicate_ignoring_case_complex_key.prop.sl").toURI();
        SystemPropertyModellingResult result =
                compiler.loadSystemPropertiesFromSource(SlangSource.fromFile(propertiesUri));
        assertTrue(result.getErrors().size() > 0);
        Assert.assertEquals(
                "Error loading properties source: 'duplicate_ignoring_case_complex_key.prop.sl'. " +
                        "Nested exception is: Duplicate system property key: 'restrict.OUT.port'.",
                result.getErrors().get(0).getMessage());
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
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                loadSystemProperties(SlangSource.fromFile(propertiesUri)));
        Assert.assertEquals("Error loading properties source: 'invalid_1.prop.sl'. " +
                "Nested exception is: Error validating system property namespace." +
                " Nested exception is: Argument[a.!.b] violates character rules.", exception.getMessage());
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidCharsNamespaceFromSource() throws Exception {
        final URI propertiesUri = getClass().getResource("/properties/a/b/invalid_1.prop.sl").toURI();
        SystemPropertyModellingResult result = compiler
                .loadSystemPropertiesFromSource(SlangSource.fromFile(propertiesUri));
        assertTrue(result.getErrors().size() > 0);
        Assert.assertEquals(
                "Error loading properties source: 'invalid_1.prop.sl'. " +
                        "Nested exception is: Error validating system property namespace." +
                        " Nested exception is: Argument[a.!.b] violates character rules.",
                result.getErrors().get(0).getMessage());
        throw result.getErrors().get(0);
    }

    @Test
    public void testInvalidCharsKey() throws Exception {
        final URI propertiesUri = getClass().getResource("/properties/a/b/invalid_2.prop.sl").toURI();
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                loadSystemProperties(SlangSource.fromFile(propertiesUri)));
        Assert.assertEquals("Error loading properties source: 'invalid_2.prop.sl'. Nested exception is:" +
                " Error validating system property key. Nested exception is:" +
                " Argument[c.?.name] violates character rules.", exception.getMessage());
    }

    private Set<SystemProperty> getSystemPropertiesValid() {
        return getSystemPropertiesBasic("a.b");
    }

    private Set<SystemProperty> getSystemPropertiesValidNullValue() {
        return newHashSet(new SystemProperty("a.b", "host", (String) null, null));
    }

    private Set<SystemProperty> loadSystemProperties(SlangSource source) {
        return compiler.loadSystemProperties(source);
    }

    private Set<SystemProperty> getSystemPropertiesValidEmptyNamespace() {
        return getSystemPropertiesBasic("");
    }

    private Set<SystemProperty> getSystemPropertiesBasic(String namespace) {
        return newHashSet(new SystemProperty(namespace, "host", "localhost", null),
                new SystemProperty(namespace, "c.name", "john doe", null),
                new SystemProperty(namespace, "restrict.out.port", "8080", null));
    }

}