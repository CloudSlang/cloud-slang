package io.cloudslang.lang.cli;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.shell.Bootstrap;

import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * User: bancl
 * Date: 6/30/2016
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SlangBootstrap.class, Bootstrap.class})
public class SlangBootstrapTest {


    @Before
    public void before() throws Exception {
        System.setProperty("app.home", getClass().getResource("/slangbootstrap").getPath());
    }

    @Test
    public void testLoadUserProperties() throws Exception {
        mockStatic(Bootstrap.class);
        doNothing().when(Bootstrap.class);
        SlangBootstrap.main(null);

        Assert.assertTrue(System.getProperty("cslang.encoding").endsWith("utf-8"));
        Assert.assertTrue(System.getProperty("app.home").endsWith("/slangbootstrap"));
        Assert.assertTrue(System.getProperty("maven.home").endsWith("/slangbootstrap/maven/apache-maven-3.3.9"));
        Assert.assertTrue(System.getProperty("maven.multiModuleProjectDirectory").endsWith("/slangbootstrap/maven/apache-maven-3.3.9"));
        Assert.assertTrue(System.getProperty("maven.settings.xml.path").endsWith("/slangbootstrap/maven/conf/settings.xml"));
        Assert.assertTrue(System.getProperty("maven.m2.conf.path").endsWith("/slangbootstrap/maven/conf/m2.conf"));
        Assert.assertTrue(System.getProperty("cloudslang.maven.repo.local").endsWith("/slangbootstrap/maven/repo"));
        Assert.assertTrue(System.getProperty("cloudslang.maven.repo.remote.url").endsWith("http://repo1.maven.org/maven2"));
        Assert.assertTrue(System.getProperty("cloudslang.maven.plugins.remote.url").endsWith("http://repo1.maven.org/maven2"));
    }
}
