package com.hp.score.lang.compiler.utils;

import com.hp.score.lang.compiler.domain.SlangFile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
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
        Mockito.when(yaml.loadAs(any(InputStream.class), eq(SlangFile.class))).thenThrow(IOException.class);
        exception.expect(RuntimeException.class);
        exception.expectMessage("syntax");
        yamlParser.loadSlangFile(new File(""));
    }
}