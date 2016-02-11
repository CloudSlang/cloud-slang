package io.cloudslang.lang.compiler.parser;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.util.Map;

/**
 * User: bancl
 * Date: 1/12/2016
 */
@RunWith(MockitoJUnitRunner.class)
public class MetadataParserTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private MetadataParser metadataParser = new MetadataParser();

    @Mock
    private ParserExceptionHandler parserExceptionHandler;

    @Test
    public void throwExceptionWhenSourceIsEmpty() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("empty");
        metadataParser.parse(new SlangSource("", null));
    }

    @Test
    public void noNullsWhenEmptyValues() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata_empty_values.sl").toURI();
        Map<String, String> metadataMap = metadataParser.parse(SlangSource.fromFile(operation));
        boolean containsNulls = false;
        for (Map.Entry<String, String> entry : metadataMap.entrySet()) {
            if (entry.getValue() == null) containsNulls = true;
        }
        Assert.assertFalse("metadata map contains nulls", containsNulls);
    }

    @Test
    public void fullDescriptionMissing() throws Exception {
        URI operation = getClass().getResource("/metadata/metadata_full_description_missing.sl").toURI();
        Map<String, String> metadataMap = metadataParser.parse(SlangSource.fromFile(operation));
        Assert.assertTrue("metadata map should have size 1", metadataMap.size() == 1);
        Assert.assertEquals("metadata map should contain namespace only", "io.cloudslang.base.json",
                metadataMap.get(SlangTextualKeys.NAMESPACE));
    }
}
