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
import io.cloudslang.lang.compiler.SlangSource;
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
        exception.expectMessage("loading properties file");
        exception.expectMessage("no content associated");
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
                SystemProperty.createSystemProperty(namespace, "host", "localhost"),
                SystemProperty.createSystemProperty(namespace, "c.name", "john doe"),
                SystemProperty.createSystemProperty(namespace, "restrict.out.port", "8080")
        );
    }
    
}
