/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.modeller.model.Metadata;
import io.cloudslang.lang.compiler.modeller.result.MetadataModellingResult;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class MetadataExtractorTest {

    private static final String NEWLINE = System.lineSeparator();

    private static final String DESCRIPTION_AND_PREREQUISITES = "description: " + System.lineSeparator() +
            "  Parses the given JSON input to retrieve the" + System.lineSeparator() +
            "  corresponding value addressed by the json_path input." + System.lineSeparator() +
            "prerequisites: jenkinsapi Python module";
    private static final String OPERATION_DESCRIPTION = "Parses the given JSON input to retrieve the" + NEWLINE +
            "corresponding value addressed by the json_path input.";
    private static final String FIRST_INPUT_VALUE = "JSON data input" + NEWLINE +
            "Example: '{\"k1\": {\"k2\": [\"v1\", \"v2\"]}}'";
    private static final String FIRST_OUTPUT_VALUE = "the corresponding value of the key referred to by json_path";
    private static final String SECOND_OUTPUT_VALUE =
            "path from which to retrieve value represented as a list of keys and/or indices." + NEWLINE +
            "Passing an empty list ([]) will retrieve the entire json_input. - Example: [\"k1\", \"k2\", 1]" + NEWLINE +
            "More information after newline";
    private static final String PREREQUISITES = "jenkinsapi Python module";
    private static final String SOME_OTHER_RESULT = "SOME_OTHER_RESULT";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private MetadataExtractor metadataExtractor;

    @Test
    public void testExtractMetadata() throws Exception {
        URI executable = getClass().getResource("/metadata/metadata.sl").toURI();
        validateMetadata(executable);
    }

    @Test
    public void testExtractMetadataDecision() throws Exception {
        URI executable = getClass().getResource("/metadata/metadata_decision.sl").toURI();
        validateMetadata(executable);
    }

    @Test
    public void testMetadataPrettyPrint() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata.sl").toURI();
        Metadata metadata = metadataExtractor.extractMetadata(SlangSource.fromFile(operation));
        String metadataToPrint = metadata.prettyPrint();
        Assert.assertNotNull(metadataToPrint);
        Assert.assertFalse(metadataToPrint.contains("io.cloudslang.lang.compiler.modeller.model.Metadata"));
        Assert.assertTrue(metadataToPrint.contains(DESCRIPTION_AND_PREREQUISITES));
        Assert.assertTrue(metadataToPrint.contains(SOME_OTHER_RESULT));
        Assert.assertFalse(metadataToPrint.contains(SOME_OTHER_RESULT + ":"));
    }

    @Test
    public void testExtractMetadataNoPrerequisites() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata_no_prerequisites.sl").toURI();
        Metadata metadata = metadataExtractor.extractMetadata(SlangSource.fromFile(operation));
        Assert.assertNotNull("metadata is null", metadata);
        Assert.assertEquals("different description", OPERATION_DESCRIPTION, metadata.getDescription());
        Assert.assertEquals("different prerequisites", "", metadata.getPrerequisites());
        Assert.assertEquals("different number of inputs", 2, metadata.getInputs().size());
        Assert.assertEquals("different number of outputs", 4, metadata.getOutputs().size());
        Assert.assertEquals("different number of results", 2, metadata.getResults().size());
        Iterator<Map.Entry<String, String>> it = metadata.getInputs().entrySet().iterator();
        Map.Entry<String, String> entry = it.next();
        Assert.assertEquals("different input name", "json_input", entry.getKey());
        Assert.assertEquals("different input value", FIRST_INPUT_VALUE, entry.getValue());
        Map.Entry<String, String> entry2 = it.next();
        Assert.assertEquals("different input name", "json_path", entry2.getKey());
        Assert.assertEquals("different input value", SECOND_OUTPUT_VALUE, entry2.getValue());
    }

    @Test
    public void testExtractMetadataEmptyDescriptionSection() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata_empty_description.sl").toURI();
        Metadata metadata = metadataExtractor.extractMetadata(SlangSource.fromFile(operation));
        Assert.assertNotNull("metadata is null", metadata);
        Assert.assertEquals("different description", "", metadata.getDescription());
    }

    @Test
    public void testExtractMetadataBadInput() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata_bad_inputs.sl").toURI();
        MetadataModellingResult result = metadataExtractor
                .extractMetadataModellingResult(SlangSource.fromFile(operation));

        Metadata metadata = result.getMetadata();

        Assert.assertEquals("@input json_input_#1: JSON data input" + System.lineSeparator() +
                "Example: '{\"k1\": {\"k2\": [\"v1\", \"v2\"]}}'",
                metadata.getInputs().get("json_input_#1: JSON data input"));
        Assert.assertEquals(2, result.getErrors().size());
        exception.expect(RuntimeException.class);
        exception.expectMessage("does not contain colon between the tag name and the description of the " +
                "tag for metadata_bad_inputs.sl");
        throw result.getErrors().get(0);
    }

    @Test
    public void testExtractMetadataWrongOrder() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata_wrong_order.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Order is not preserved for metadata_wrong_order.sl");
        metadataExtractor.extractMetadata(SlangSource.fromFile(operation));
    }

    @Test
    public void startingTagMissing() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata_starting_tag_missing.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Starting tag missing");
        metadataExtractor.extractMetadata(SlangSource.fromFile(operation));
    }

    @Test
    public void colonMissingSingleTag() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata_colon_missing_after_tag_name1.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage("does not contain colon between the tag name and the description of the " +
                "tag for metadata_colon_missing_after_tag_name1.sl");
        metadataExtractor.extractMetadata(SlangSource.fromFile(operation));
    }

    @Test
    public void colonMissingRegularTag() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata_colon_missing_after_tag_name2.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage("does not contain colon between the tag name and the description of the " +
                "tag for metadata_colon_missing_after_tag_name2.sl");
        metadataExtractor.extractMetadata(SlangSource.fromFile(operation));
    }

    @Test
    public void colonMissingOkScenario() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata_colon_missing_after_tag_name_ok.sl").toURI();
        metadataExtractor.extractMetadata(SlangSource.fromFile(operation));
    }

    @Test
    public void descriptionAfterStartingTag() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata_description_after_starting_tag.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Description is not accepted on the same line as the starting " +
                "tag for metadata_description_after_starting_tag.sl");
        metadataExtractor.extractMetadata(SlangSource.fromFile(operation));
    }

    @Test
    public void testExtractMetadataNoResults() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata_no_results.sl").toURI();
        Metadata metadata = metadataExtractor.extractMetadata(SlangSource.fromFile(operation));
        Assert.assertNotNull("metadata is null", metadata);
        Assert.assertEquals("different description", OPERATION_DESCRIPTION, metadata.getDescription());
        Assert.assertEquals("different prerequisites", PREREQUISITES, metadata.getPrerequisites());
        Assert.assertEquals("different number of inputs", 2, metadata.getInputs().size());
        Assert.assertEquals("different number of outputs", 4, metadata.getOutputs().size());
        Assert.assertEquals("different number of results", 0, metadata.getResults().size());
        Map.Entry<String, String> entry = metadata.getInputs().entrySet().iterator().next();
        Assert.assertEquals("different input name", "json_input", entry.getKey());
        Assert.assertEquals("different input value", FIRST_INPUT_VALUE, entry.getValue());
    }

    @Test
    public void testExtractMetadataNoOutputs() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata_no_outputs.sl").toURI();
        Metadata metadata = metadataExtractor.extractMetadata(SlangSource.fromFile(operation));
        Assert.assertNotNull("metadata is null", metadata);
        Assert.assertEquals("different description", OPERATION_DESCRIPTION, metadata.getDescription());
        Assert.assertEquals("different prerequisites", PREREQUISITES, metadata.getPrerequisites());
        Assert.assertEquals("different number of inputs", 2, metadata.getInputs().size());
        Assert.assertEquals("different number of outputs", 0, metadata.getOutputs().size());
        Assert.assertEquals("different number of results", 2, metadata.getResults().size());
        Map.Entry<String, String> entry = metadata.getInputs().entrySet().iterator().next();
        Assert.assertEquals("different input name", "json_input", entry.getKey());
        Assert.assertEquals("different input value", FIRST_INPUT_VALUE, entry.getValue());
    }

    @Test
    public void testExtractMetadataNoInputs() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata_no_inputs.sl").toURI();
        Metadata metadata = metadataExtractor.extractMetadata(SlangSource.fromFile(operation));
        Assert.assertNotNull("metadata is null", metadata);
        Assert.assertEquals("different description", OPERATION_DESCRIPTION, metadata.getDescription());
        Assert.assertEquals("different number of inputs", 0, metadata.getInputs().size());
        Assert.assertEquals("different number of outputs", 4, metadata.getOutputs().size());
        Assert.assertEquals("different number of results", 2, metadata.getResults().size());
        Map.Entry<String, String> entry = metadata.getOutputs().entrySet().iterator().next();
        Assert.assertEquals("different input name", "value", entry.getKey());
        Assert.assertEquals("different input value", FIRST_OUTPUT_VALUE, entry.getValue());
    }

    @Test
    public void testExtractMetadataSingleHashForResults() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata_single_hash_for_results.sl").toURI();
        Metadata metadata = metadataExtractor.extractMetadata(SlangSource.fromFile(operation));
        Assert.assertNotNull("metadata is null", metadata);
        Assert.assertEquals("different description", OPERATION_DESCRIPTION, metadata.getDescription());
        Assert.assertEquals("different number of inputs", 2, metadata.getInputs().size());
        Assert.assertEquals("different number of outputs", 4, metadata.getOutputs().size());
        Assert.assertEquals("different number of results", 0, metadata.getResults().size());
        Map.Entry<String, String> entry = metadata.getInputs().entrySet().iterator().next();
        Assert.assertEquals("different input name", "json_input", entry.getKey());
        Assert.assertEquals("different input value", FIRST_INPUT_VALUE, entry.getValue());
    }

    @Test
    public void testExtractMetadataSingleHashForAResult() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata_single_hash_for_a_result.sl").toURI();
        Metadata metadata = metadataExtractor.extractMetadata(SlangSource.fromFile(operation));
        Assert.assertNotNull("metadata is null", metadata);
        Assert.assertEquals("different description", OPERATION_DESCRIPTION, metadata.getDescription());
        Assert.assertEquals("different number of inputs", 2, metadata.getInputs().size());
        Assert.assertEquals("different number of outputs", 4, metadata.getOutputs().size());
        Assert.assertEquals("different number of results", 1, metadata.getResults().size());
        Map.Entry<String, String> entry = metadata.getInputs().entrySet().iterator().next();
        Assert.assertEquals("different input name", "json_input", entry.getKey());
        Assert.assertEquals("different input value", FIRST_INPUT_VALUE, entry.getValue());
    }

    private void validateMetadata(URI operation) {
        Metadata metadata = metadataExtractor.extractMetadata(SlangSource.fromFile(operation));
        Assert.assertNotNull("metadata is null", metadata);
        Assert.assertEquals("different description", OPERATION_DESCRIPTION, metadata.getDescription());
        Assert.assertEquals("different prerequisites", PREREQUISITES, metadata.getPrerequisites());
        Assert.assertEquals("different number of inputs", 2, metadata.getInputs().size());
        Assert.assertEquals("different number of outputs", 4, metadata.getOutputs().size());
        Assert.assertEquals("different number of results", 3, metadata.getResults().size());
        Iterator<Map.Entry<String, String>> it = metadata.getInputs().entrySet().iterator();
        Map.Entry<String, String> entry = it.next();
        Assert.assertEquals("different input name", "json_input", entry.getKey());
        Assert.assertEquals("different input value", FIRST_INPUT_VALUE, entry.getValue());
        Map.Entry<String, String> entry2 = it.next();
        Assert.assertEquals("different input name", "json_path", entry2.getKey());
        Assert.assertEquals("different input value", SECOND_OUTPUT_VALUE, entry2.getValue());
        Assert.assertEquals("different result value", "", metadata.getResults().get(SOME_OTHER_RESULT));
    }

}
