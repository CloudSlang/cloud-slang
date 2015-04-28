package io.cloudslang.lang.tools.build;

import org.aspectj.lang.annotation.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by stoneo on 4/22/2015.
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangBuildMainTest.Config.class)
public class SlangBuildMainTest {


    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void runMainWithArgs() throws URISyntaxException {
        URI projectResource = getClass().getResource("/").toURI();
        URI contentResource = getClass().getResource("/content").toURI();
        URI testResource = getClass().getResource("/test/valid").toURI();
        System.setProperty("contentPath", contentResource.getPath());
        System.setProperty("testPath", testResource.getPath());
        SlangBuildMain.main(new String[]{projectResource.getPath()});
        System.clearProperty("contentPath");
        System.clearProperty("testPath");
    }

    @Test
    public void runMainWithOnlyProjectDir() throws URISyntaxException {
        URI projectResource = getClass().getResource("/").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage("FAILURE");
        SlangBuildMain.main(new String[]{projectResource.getPath()});
    }

    @Test
    public void runMainWithoutArgs() throws URISyntaxException {
        exception.expect(RuntimeException.class);
        exception.expectMessage("FAILURE");
        SlangBuildMain.main(null);
    }

    @Configuration
    static class Config {

    }
}
