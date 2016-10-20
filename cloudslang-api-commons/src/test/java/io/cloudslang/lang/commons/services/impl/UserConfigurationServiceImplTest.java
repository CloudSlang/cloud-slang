/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.commons.services.impl;

import io.cloudslang.lang.commons.services.api.UserConfigurationService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Bonczidai Levente
 * @since 8/24/2016
 */
public class UserConfigurationServiceImplTest {
    private static final String SP_APP_HOME = "app.home";
    private static final String SP_SINGLE_KEY = "single_key";
    private static final String SP_MAVEN_HOME = "maven.home";
    private static final String SP_REMOTE_URL = "cloudslang.maven.plugins.remote.url";

    private static final String APP_HOME_1 = "/app_home";
    private static final String APP_HOME_2 = "/app_home2";
    private static final String APP_HOME_3 = "/app_home3";

    private UserConfigurationService userConfigurationService;

    @Before
    public void setUp() throws Exception {
        userConfigurationService = new UserConfigurationServiceImpl();
    }

    @Test
    public void loadUserPropertiesSimple() throws Exception {
        System.setProperty(SP_APP_HOME, getPathForResource(APP_HOME_1));
        userConfigurationService.loadUserProperties();
        assertEquals("single_value", System.getProperty(SP_SINGLE_KEY));
    }

    @Test
    public void loadUserPropertiesSubstitution() throws Exception {
        System.setProperty(SP_APP_HOME, getPathForResource(APP_HOME_2));
        System.setProperty("custom.home", "root");
        userConfigurationService.loadUserProperties();
        assertEquals("root/maven/apache-maven-3.3.9", System.getProperty(SP_MAVEN_HOME));
        assertEquals("http://repo1.maven.org/maven2", System.getProperty(SP_REMOTE_URL));
    }

    @Test
    public void loadUserPropertiesMissingFolderValid() throws Exception {
        System.setProperty(SP_APP_HOME, getPathForResource(APP_HOME_3));
        userConfigurationService.loadUserProperties();
    }

    private String getPathForResource(String resourceRelativePath) {
        return getClass().getResource(resourceRelativePath).getPath();
    }

}
