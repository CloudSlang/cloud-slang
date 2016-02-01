package io.cloudslang.lang.compiler.parser;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
}
