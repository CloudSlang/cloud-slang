package io.cloudslang.lang.compiler.parser;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.parser.model.ParsedMetadata;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

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
    private Yaml yaml;

    @Mock
    private ParserExceptionHandler parserExceptionHandler;

    @Test
    public void throwExceptionWhenFileIsNotValid() throws Exception {
        Mockito.when(yaml.loadAs(any(String.class), eq(ParsedMetadata.class))).thenThrow(IOException.class);
        exception.expect(RuntimeException.class);
        exception.expectMessage("parsing");
        metadataParser.parse(new SlangSource("a", "b"));
    }

    @Test
    public void throwExceptionWhenSourceIsEmpty() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("empty");
        metadataParser.parse(new SlangSource("", null));
    }
}
