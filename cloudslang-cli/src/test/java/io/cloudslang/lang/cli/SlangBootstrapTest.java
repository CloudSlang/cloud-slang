/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.cli;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.shell.Bootstrap;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;

@RunWith(SpringJUnit4ClassRunner.class)
public class SlangBootstrapTest {

    @Before
    public void before() throws Exception {
        System.setProperty("app.home", getClass().getResource("/slangbootstrap").getPath());
    }

    @Test
    public void testLoadUserProperties() throws Exception {
        mockStatic(Bootstrap.class);
        doNothing().when(Bootstrap.class);
        SlangBootstrap.main(new String[0]);

        Assert.assertTrue(System.getProperty("cslang.encoding").endsWith("utf-8"));
        Assert.assertTrue(System.getProperty("app.home").endsWith("/slangbootstrap"));
        Assert.assertTrue(System.getProperty("maven.home").endsWith("/slangbootstrap/maven/apache-maven-3.3.9"));
        Assert.assertTrue(System.getProperty("maven.multiModuleProjectDirectory")
                .endsWith("/slangbootstrap/maven/apache-maven-3.3.9"));
        Assert.assertTrue(System.getProperty("maven.settings.xml.path")
                .endsWith("/slangbootstrap/maven/conf/settings.xml"));
        Assert.assertTrue(System.getProperty("maven.m2.conf.path").endsWith("/slangbootstrap/maven/conf/m2.conf"));
        Assert.assertTrue(System.getProperty("cloudslang.maven.repo.local").endsWith("/slangbootstrap/maven/repo"));
        Assert.assertTrue(System.getProperty("cloudslang.maven.repo.remote.url")
                .endsWith("https://repo1.maven.org/maven2"));
        Assert.assertTrue(System.getProperty("cloudslang.maven.plugins.remote.url")
                .endsWith("https://repo1.maven.org/maven2"));
    }
}
