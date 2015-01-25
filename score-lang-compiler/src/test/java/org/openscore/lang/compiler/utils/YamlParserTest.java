package org.openscore.lang.compiler.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.openscore.lang.compiler.SlangSource;
import org.openscore.lang.compiler.model.ParsedSlang;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;

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