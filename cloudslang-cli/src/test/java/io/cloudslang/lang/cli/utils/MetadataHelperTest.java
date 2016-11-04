/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MetadataHelperTest.Config.class})
public class MetadataHelperTest {

    private static final String DESCRIPTION_AND_PREREQUISITES = "description: " + System.lineSeparator() +
            "  Parses the given JSON input to retrieve the" + System.lineSeparator() +
            "  corresponding value addressed by the json_path input." + System.lineSeparator() +
            "prerequisites: jenkinsapi Python module";
    private static final String JSON_INPUT_VALUE =
            "json_input: JSON data input - Example: '{\"k1\": {\"k2\": [\"v1\", \"v2\"]}}'";
    private static final String PREREQUISITES_MISSING = "description: " + System.lineSeparator() +
            "  Adds or replaces a value to the given JSON at the keys or indices represented by the json_path." +
            System.lineSeparator() +
            "  If the last key in the path does not exist, the key is added as well." +
            System.lineSeparator() +
            "inputs: ";
    private static final String RESULTS = "results:";
    public static final String SOME_OTHER_RESULT = "SOME_OTHER_RESULT";

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
        URI flowFilePath = getClass().getResource("/metadata/metadata.sl").toURI();
        String metadataToPrint = metadataHelper.extractMetadata(new File(flowFilePath));
        Assert.assertNotNull(metadataToPrint);
        Assert.assertFalse(metadataToPrint.contains("io.cloudslang.lang.compiler.modeller.model.Metadata"));
        Assert.assertTrue(metadataToPrint.contains(DESCRIPTION_AND_PREREQUISITES));
        Assert.assertTrue(metadataToPrint.contains(SOME_OTHER_RESULT));
        Assert.assertFalse(metadataToPrint.contains(SOME_OTHER_RESULT + ":"));
    }

    @Test
    public void testPrettyPrintSingleQuotes() throws Exception {
        URI flowFilePath = getClass().getResource("/metadata/add_value.sl").toURI();
        String metadataToPrint = metadataHelper.extractMetadata(new File(flowFilePath));
        Assert.assertNotNull(metadataToPrint);
        Assert.assertFalse(metadataToPrint.contains("io.cloudslang.lang.compiler.modeller.model.Metadata"));
        Assert.assertTrue(metadataToPrint.contains(JSON_INPUT_VALUE));
        Assert.assertTrue(metadataToPrint.contains(PREREQUISITES_MISSING));
        Assert.assertFalse(metadataToPrint.contains(RESULTS));
    }

    @Test
    public void testPrettyPrintForEmptyDescription() throws Exception {
        URI flowFilePath = getClass().getResource("/metadata/metadata_full_description_missing.sl").toURI();
        String metadataToPrint = metadataHelper.extractMetadata(new File(flowFilePath));
        Assert.assertNotNull(metadataToPrint);
        Assert.assertTrue(metadataToPrint.contains("No metadata"));
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
            MetadataExtractorImpl metadataExtractor = new MetadataExtractorImpl();
            metadataExtractor.setMetadataModeller(metadataModeller());
            metadataExtractor.setMetadataParser(metadataParser());

            return metadataExtractor;
        }

        @Bean
        public MetadataModeller metadataModeller() {
            return new MetadataModellerImpl();
        }

        @Bean
        public MetadataParser metadataParser() {
            MetadataParser metadataParser = new MetadataParser();
            metadataParser.setParserExceptionHandler(parserExceptionHandler());
            return metadataParser;
        }

        @Bean
        public ParserExceptionHandler parserExceptionHandler() {
            return new ParserExceptionHandler();
        }

    }
}
