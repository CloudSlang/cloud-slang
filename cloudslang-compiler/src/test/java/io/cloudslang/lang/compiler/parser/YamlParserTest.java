package io.cloudslang.lang.compiler.parser;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.yaml.snakeyaml.Yaml;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class YamlParserTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private YamlParser yamlParser = new YamlParser();

    @Mock
    private Yaml yaml;

    @Mock
    private ParserExceptionHandler parserExceptionHandler;

    @Test
    public void throwExceptionWhenFileIsNotValid() throws Exception {
        Mockito.when(yaml.loadAs(any(InputStream.class), eq(ParsedSlang.class))).thenThrow(IOException.class);
        exception.expect(RuntimeException.class);
        exception.expectMessage("parsing");
        yamlParser.parse(new SlangSource("a", "b"));
    }

    @Test
    public void throwExceptionWhenSourceIsEmpty() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("empty");
        yamlParser.parse(new SlangSource("", null));
    }
}