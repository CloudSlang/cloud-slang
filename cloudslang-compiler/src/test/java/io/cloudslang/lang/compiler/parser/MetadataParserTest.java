/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.parser;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.parser.model.ParsedDescriptionData;
import java.net.URI;
import java.util.LinkedHashMap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MetadataParserTest {

    @InjectMocks
    private MetadataParser metadataParser = new MetadataParser();

    @Test
    public void testParse() throws Exception {
        URI executable = getClass().getResource("/metadata/step/step_description_01.sl").toURI();

        // flow description
        LinkedHashMap<String, String> flowDescription = new LinkedHashMap<>(); // preserver order
        flowDescription.put("@description", "Generated flow description");
        flowDescription.put(
                "@input input_1",
                "Generated description flow input 1 line 1" +
                        System.lineSeparator() +
                        "Generated description flow input 1 line 2"
        );
        flowDescription.put("@input input_2", "Generated description flow input 2");
        flowDescription.put("@output output_1", "Generated description flow output 1");
        flowDescription.put("@result SUCCESS", "Flow completed successfully.");
        flowDescription.put("@result FAILURE", "Failure occurred during execution.");

        ParsedDescriptionData parseResult = metadataParser.parse(SlangSource.fromFile(executable));
        Assert.assertTrue(parseResult.getTopLevelDescriptions().size() == 1);
        Assert.assertEquals(flowDescription, parseResult.getTopLevelDescriptions().get(0).getData());

        // step description
        LinkedHashMap<String, String> stepDescription = new LinkedHashMap<>();
        stepDescription.put("@input step_input_1", "description step input 1");
        stepDescription.put(
                "@input step_input_2",
                "description step input 2 line 1" +
                        System.lineSeparator() +
                        "description step input 2 line 2"
        );
        stepDescription.put("@output step_output_1", "description step output 1");
        stepDescription.put("@output step_output_2", "description step output 2");

        Assert.assertTrue(parseResult.getStepDescriptions().size() == 1);
        Assert.assertTrue(parseResult.getStepDescriptions().containsKey("step_1"));
        Assert.assertEquals(stepDescription, parseResult.getStepDescriptions().get("step_1").getData());

        Assert.assertTrue(parseResult.getErrors().size() == 0);
    }

    @Test
    public void testParseNoDescription() throws Exception {
        URI executable = getClass().getResource("/metadata/step/step_description_03.sl").toURI();
        ParsedDescriptionData parseResult = metadataParser.parse(SlangSource.fromFile(executable));

        Assert.assertTrue(parseResult.getTopLevelDescriptions().size() == 0);
        Assert.assertTrue(parseResult.getStepDescriptions().size() == 0);
        Assert.assertTrue(parseResult.getErrors().size() == 0);
    }

    @Test
    public void testParseEmptySource() throws Exception {
        URI executable = getClass().getResource("/metadata/step/step_description_04.sl").toURI();
        ParsedDescriptionData parseResult = metadataParser.parse(SlangSource.fromFile(executable));

        Assert.assertTrue(parseResult.getTopLevelDescriptions().size() == 0);
        Assert.assertTrue(parseResult.getStepDescriptions().size() == 0);
        Assert.assertTrue(parseResult.getErrors().size() == 0);
    }
}
