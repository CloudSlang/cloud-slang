package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.modeller.model.Metadata;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;

/**
 * User: bancl
 * Date: 1/12/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class MetadataExtractorTest {

    public static final String OPERATION_DESCRIPTION = "Parses the given JSON input to retrieve the\t \n" +
                                                       "corresponding value addressed by the json_path input.\n";
    public static final String FIRST_INPUT_VALUE = "JSON data input \n" +
                                                   "- Example: '{\"k1\": {\"k2\": [\"v1\", \"v2\"]}}'\n";
    public static final String FIRST_OUTPUT_VALUE = "the corresponding value of the key referred to by json_path";
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private MetadataExtractor metadataExtractor;

    @Test
    public void testExtractMetadata() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata.sl").toURI();
        Metadata metadata = metadataExtractor.extractMetadata(SlangSource.fromFile(operation));
        Assert.assertNotNull("metadata is null", metadata);
        Assert.assertEquals("different description", OPERATION_DESCRIPTION, metadata.getDescription());
        Assert.assertEquals("different number of inputs", 2, metadata.getInputs().size());
        Assert.assertEquals("different number of outputs", 4, metadata.getOutputs().size());
        Assert.assertEquals("different number of results", 2, metadata.getResults().size());
        String key = metadata.getInputs().keySet().iterator().next();
        Assert.assertEquals("different input name", "json_input", key);
        Assert.assertEquals("different input value", FIRST_INPUT_VALUE, metadata.getInputs().get(key));
    }

    @Test
    public void testExtractMetadataNoResults() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata_no_results.sl").toURI();
        Metadata metadata = metadataExtractor.extractMetadata(SlangSource.fromFile(operation));
        Assert.assertNotNull("metadata is null", metadata);
        Assert.assertEquals("different description", OPERATION_DESCRIPTION, metadata.getDescription());
        Assert.assertEquals("different number of inputs", 2, metadata.getInputs().size());
        Assert.assertEquals("different number of outputs", 4, metadata.getOutputs().size());
        Assert.assertEquals("different number of results", 0, metadata.getResults().size());
        String key = metadata.getInputs().keySet().iterator().next();
        Assert.assertEquals("different input name", "json_input", key);
        Assert.assertEquals("different input value", FIRST_INPUT_VALUE, metadata.getInputs().get(key));
    }

    @Test
    public void testExtractMetadataNoOutputs() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata_no_outputs.sl").toURI();
        Metadata metadata = metadataExtractor.extractMetadata(SlangSource.fromFile(operation));
        Assert.assertNotNull("metadata is null", metadata);
        Assert.assertEquals("different description", OPERATION_DESCRIPTION, metadata.getDescription());
        Assert.assertEquals("different number of inputs", 2, metadata.getInputs().size());
        Assert.assertEquals("different number of outputs", 0, metadata.getOutputs().size());
        Assert.assertEquals("different number of results", 2, metadata.getResults().size());
        String key = metadata.getInputs().keySet().iterator().next();
        Assert.assertEquals("different input name", "json_input", key);
        Assert.assertEquals("different input value", FIRST_INPUT_VALUE, metadata.getInputs().get(key));
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
        String key = metadata.getOutputs().keySet().iterator().next();
        Assert.assertEquals("different input name", "value", key);
        Assert.assertEquals("different input value", FIRST_OUTPUT_VALUE, metadata.getOutputs().get(key));
    }

    @Test
    public void testExtractMetadataWrongDescriptionFormat() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata_wrong_desc.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage("could not found expected ':'");
        metadataExtractor.extractMetadata(SlangSource.fromFile(operation));
    }

    @Test
    public void testExtractMetadataWrongDescriptionIndentation() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata_wrong_indentation.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage("expected <block end>, but found BlockSequenceStart");
        metadataExtractor.extractMetadata(SlangSource.fromFile(operation));
    }

    @Test
    public void testExtractMetadataInvalidOutputValue() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata_invalid_output_value.sl").toURI();
        exception.expect(RuntimeException.class);
        exception.expectMessage("expected <block end>, but found Scalar");
        metadataExtractor.extractMetadata(SlangSource.fromFile(operation));
    }
}
