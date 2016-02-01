package io.cloudslang.lang.cli.utils;

import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.api.SlangImpl;
import io.cloudslang.lang.compiler.MetadataExtractor;
import io.cloudslang.lang.compiler.MetadataExtractorImpl;
import io.cloudslang.lang.compiler.SlangCompiler;
import io.cloudslang.lang.compiler.modeller.MetadataModeller;
import io.cloudslang.lang.compiler.modeller.MetadataModellerImpl;
import io.cloudslang.lang.compiler.parser.MetadataParser;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import io.cloudslang.score.api.Score;
import io.cloudslang.score.events.EventBus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.net.URI;

import static org.mockito.Mockito.mock;

/**
 * User: bancl
 * Date: 1/15/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MetadataHelperTest.Config.class)
public class MetadataHelperTest {

    public static final String DESCRIPTION_AND_PREREQUISITES = "description: |-\n" +
            "  Parses the given JSON input to retrieve the\n" +
            "  corresponding value addressed by the json_path input.\n" +
            "prerequisites: jenkinsapi Python module";

    @Autowired
    private MetadataHelper metadataHelper;

    @Test(expected = IllegalArgumentException.class)
    public void testFileNull() throws Exception {
        metadataHelper.extractMetadata(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFilePathWrong() throws Exception {
        metadataHelper.extractMetadata(new File("www"));
    }

    @Test
    public void testPrettyPrint() throws Exception {
        URI flowFilePath = getClass().getResource("/metadata.sl").toURI();
        String metadataToPrint = metadataHelper.extractMetadata(new File(flowFilePath));
        Assert.assertNotNull(metadataToPrint);
        Assert.assertFalse(metadataToPrint.contains("io.cloudslang.lang.compiler.modeller.model.Metadata"));
        Assert.assertTrue(metadataToPrint.contains(DESCRIPTION_AND_PREREQUISITES));
    }

    @Configuration
    static class Config {

        @Bean
        public Slang slang() {
            return new SlangImpl();
        }

        @Bean
        public MetadataHelper metadataHelper() {
            return new MetadataHelperImpl();
        }

        @Bean
        public Yaml yaml() {
            return new Yaml();
        }

        @Bean
        public SlangCompiler compiler() {
            return mock(SlangCompiler.class);
        }

        @Bean
        public Score score() {
            return mock(Score.class);
        }

        @Bean
        public EventBus eventBus() {
            return mock(EventBus.class);
        }

        @Bean
        public MetadataExtractor metadataExtractor() {
            return new MetadataExtractorImpl();
        }

        @Bean
        public MetadataModeller metadataModeller() {
            return new MetadataModellerImpl();
        }

        @Bean
        public MetadataParser metadataParser() {
            return new MetadataParser();
        }

        @Bean
        public ParserExceptionHandler parserExceptionHandler() {
            return new ParserExceptionHandler();
        }

    }
}
