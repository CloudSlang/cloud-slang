package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.modeller.model.Metadata;
import org.junit.Assert;
import org.junit.Test;
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

    public static final String OPERATION_DESCRIPTION = "Parses the given JSON input to retrieve the corresponding value addressed by the json_path input.";
    public static final String FIRST_INPUT_VALUE = "JSON data input\n" +
                                                   "- Example: '{\"k1\": {\"k2\": [\"v1\", \"v2\"]}}'\n";

    @Autowired
    private MetadataExtractor metadataExtractor;

    @Test
    public void testCompileFlowBasic() throws Exception {
        URI operation = getClass().getResource("/get_value.sl").toURI();
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
}
