package io.cloudslang.lang.tools.build.tester;

import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import io.cloudslang.lang.tools.build.tester.parse.TestCasesYamlParser;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yaml.snakeyaml.Yaml;

import java.net.URI;
import java.util.Map;

import static org.mockito.Mockito.mock;

/**
 * Created by stoneo on 4/15/2015.
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangTestRunnerTest.Config.class)
public class SlangTestRunnerTest {


    @Autowired
    private SlangTestRunner slangTestRunner;

    @Autowired
    private TestCasesYamlParser parser;

    @Autowired
    private Slang slang;

    @Rule
    public ExpectedException exception = ExpectedException.none();


    @Before
    public void resetMocks() {
        Mockito.reset(parser);
        Mockito.reset(slang);
    }

    @Test
    public void testNullTestPath() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("path");
        slangTestRunner.createTestCases(null);
    }

    @Test
    public void testEmptyTestPath() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("path");
        slangTestRunner.createTestCases("");
    }

    @Test
    public void testInvalidTestPath() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("directory");
        slangTestRunner.createTestCases("aaa");
    }

    @Test
    public void testPathWithNoTests() throws Exception {
        URI resource = getClass().getResource("/dependencies").toURI();
        Map<String, SlangTestCase> testCases = slangTestRunner.createTestCases(resource.getPath());
        Assert.assertEquals(0, testCases.size());
    }


    @Configuration
    static class Config {

        @Bean
        public SlangTestRunner slangTestRunner() {
            return new SlangTestRunner();
        }

        @Bean
        public TestCasesYamlParser parser(){
            return mock(TestCasesYamlParser.class);
        }

        @Bean
        public Yaml yaml(){
            return mock(Yaml.class);
        }

        @Bean
        public Slang slang(){
            return mock(Slang.class);
        }
    }}
