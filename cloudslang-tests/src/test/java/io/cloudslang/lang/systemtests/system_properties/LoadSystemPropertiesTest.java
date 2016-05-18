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
package io.cloudslang.lang.systemtests.system_properties;

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangCompilerImpl;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.systemtests.SystemsTestsParent;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

/**
 * @author Bonczidai Levente
 * @since 1/27/2016
 */
public class LoadSystemPropertiesTest extends SystemsTestsParent {

    @SuppressWarnings("unchecked")
    private static final Set<SystemProperty> EMPTY_PROPS = Collections.EMPTY_SET;

    @Test
    public void testValid() throws Exception {
        URI propertiesURI = getClass().getResource("/yaml/properties/a/b/valid.prop.sl").toURI();
        Set<SystemProperty> expectedSystemProperties = getSystemPropertiesValid();
        Set<SystemProperty> actualSystemProperties = loadSystemProperties(SlangSource.fromFile(propertiesURI));
        Assert.assertEquals(expectedSystemProperties, actualSystemProperties);
    }

    @Test
    public void testValidEmptyNamespace() throws Exception {
        URI propertiesURI = getClass().getResource("/yaml/properties/a/b/valid_empty_namespace.prop.sl").toURI();
        Set<SystemProperty> expectedSystemProperties = getSystemPropertiesValidEmptyNamespace();
        Set<SystemProperty> actualSystemProperties = loadSystemProperties(SlangSource.fromFile(propertiesURI));
        Assert.assertEquals(expectedSystemProperties, actualSystemProperties);
    }

    @Test
    public void testValidMissingNamespaceKey() throws Exception {
        URI propertiesURI = getClass().getResource("/yaml/properties/a/b/valid_missing_namespace_key.prop.sl").toURI();
        Set<SystemProperty> expectedSystemProperties = getSystemPropertiesValidEmptyNamespace();
        Set<SystemProperty> actualSystemProperties = loadSystemProperties(SlangSource.fromFile(propertiesURI));
        Assert.assertEquals(expectedSystemProperties, actualSystemProperties);
    }

    @Test
    public void testValidNoProperties() throws Exception {
        URI propertiesURI = getClass().getResource("/yaml/properties/a/b/valid_no_prop.sl").toURI();
        Set<SystemProperty> expectedSystemProperties = EMPTY_PROPS;
        Set<SystemProperty> actualSystemProperties = loadSystemProperties(SlangSource.fromFile(propertiesURI));
        Assert.assertEquals(expectedSystemProperties, actualSystemProperties);
    }

    @Test
    public void testInvalidMissingProperties() throws Exception {
        URI propertiesURI = getClass().getResource("/yaml/properties/a/b/invalid_missing_properties.prop.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage(SlangCompilerImpl.ERROR_LOADING_PROPERTIES_FILE_MESSAGE);
        exception.expectMessage("no content associated");
        loadSystemProperties(SlangSource.fromFile(propertiesURI));
    }

    @Test
    public void testMapUnderProperties() throws Exception {
        URI propertiesURI = getClass().getResource("/yaml/properties/a/b/map_under_properties.prop.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage(SlangCompilerImpl.ERROR_LOADING_PROPERTIES_FILE_MESSAGE);
        exception.expectMessage(SlangTextualKeys.SYSTEM_PROPERTY_KEY);
        exception.expectMessage("list");
        exception.expectMessage("Map");
        loadSystemProperties(SlangSource.fromFile(propertiesURI));
    }

    @Test
    public void testListElementNotMap() throws Exception {
        URI propertiesURI = getClass().getResource("/yaml/properties/a/b/list_element_not_map.prop.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage(SlangCompilerImpl.ERROR_LOADING_PROPERTIES_FILE_MESSAGE);
        exception.expectMessage(SlangCompilerImpl.PROPERTY_LIST_ELEMENT_WRONG_TYPE_ERROR_MESSAGE_PREFIX);
        exception.expectMessage("i_am_string(java.lang.String)");
        loadSystemProperties(SlangSource.fromFile(propertiesURI));
    }

    @Test
    public void testListElementNull() throws Exception {
        URI propertiesURI = getClass().getResource("/yaml/properties/a/b/list_element_null.prop.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage(SlangCompilerImpl.ERROR_LOADING_PROPERTIES_FILE_MESSAGE);
        exception.expectMessage(SlangCompilerImpl.PROPERTY_LIST_ELEMENT_WRONG_TYPE_ERROR_MESSAGE_PREFIX);
        exception.expectMessage("null");
        loadSystemProperties(SlangSource.fromFile(propertiesURI));
    }

    @Test
    public void testListElementMapWithMultipleEntries() throws Exception {
        URI propertiesURI = getClass().getResource("/yaml/properties/a/b/map_with_multiple_entries.prop.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage(SlangCompilerImpl.ERROR_LOADING_PROPERTIES_FILE_MESSAGE);
        exception.expectMessage(SlangCompilerImpl.SIZE_OF_SYSTEM_PROPERTY_ERROR_MESSAGE_PREFIX);
        exception.expectMessage("{key1=val1, key2=val2}");
        exception.expectMessage("2");
        loadSystemProperties(SlangSource.fromFile(propertiesURI));
    }

    @Test
    public void testWrongSystemPropertyKeyType() throws Exception {
        URI propertiesURI = getClass().getResource("/yaml/properties/a/b/wrong_key_type.prop.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage(SlangCompilerImpl.ERROR_LOADING_PROPERTIES_FILE_MESSAGE);
        exception.expectMessage(SlangCompilerImpl.SYSTEM_PROPERTY_KEY_WRONG_TYPE_ERROR_MESSAGE_PREFIX);
        exception.expectMessage("123");
        exception.expectMessage("Integer");
        loadSystemProperties(SlangSource.fromFile(propertiesURI));
    }

    @Test
    public void testDuplicateKey() throws Exception {
        URI propertiesURI = getClass().getResource("/yaml/properties/a/b/duplicate_key.prop.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage(SlangCompilerImpl.ERROR_LOADING_PROPERTIES_FILE_MESSAGE);
        exception.expectMessage(SlangCompilerImpl.DUPLICATE_SYSTEM_PROPERTY_KEY_ERROR_MESSAGE_PREFIX);
        exception.expectMessage("host");
        loadSystemProperties(SlangSource.fromFile(propertiesURI));
    }

    private Set<SystemProperty> getSystemPropertiesValid() {
        return getSystemPropertiesBasic("a.b");
    }

    private Set<SystemProperty> getSystemPropertiesValidEmptyNamespace() {
        return getSystemPropertiesBasic("");
    }

    private Set<SystemProperty> getSystemPropertiesBasic(String namespace) {
        return Sets.newHashSet(
                new SystemProperty(namespace, "host", "localhost"),
                new SystemProperty(namespace, "c.name", "john doe"),
                new SystemProperty(namespace, "restrict.out.port", "8080")
        );
    }
    
}
