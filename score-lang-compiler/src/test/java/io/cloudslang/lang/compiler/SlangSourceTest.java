package io.cloudslang.lang.compiler;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SlangSourceTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private final static String sourceName = "someName";
    private final static String sourceString = "someString";

    private void assertSourceEquals(SlangSource source) {
        Assert.assertEquals(source.getSource(), sourceString);
        Assert.assertEquals(source.getName(), sourceName);
    }

    @Test
    public void testFromFile() throws Exception {
        File file = folder.newFile(sourceName);
        FileUtils.writeStringToFile(file, sourceString);

        assertSourceEquals(SlangSource.fromFile(file));
    }

    @Test
    public void testWhenPassingDirectoryShouldThrowException() throws Exception {
        File tempFolder = folder.newFolder();

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("directories");
        exception.expectMessage(tempFolder.getName());
        SlangSource.fromFile(tempFolder);
    }

    @Test
    public void testWhenThereIsProblemReadingTheFileThrowException() throws Exception {
        File file = mock(File.class);
        when(file.getName()).thenReturn(sourceName);
        when(file.isFile()).thenReturn(true);

        when(file.getPath()).thenThrow(IOException.class);

        exception.expect(RuntimeException.class);
        exception.expectMessage("reading");
        exception.expectMessage(file.getName());

        SlangSource.fromFile(file);
    }

    @Test
    public void testWhenPassingNullFileThrowException() throws Exception {
        File file = null;

        exception.expect(NullPointerException.class);
        exception.expectMessage("null");

        assertSourceEquals(SlangSource.fromFile(file));
    }

    @Test
    public void testFromFileUri() throws Exception {
        File file = folder.newFile(sourceName);
        FileUtils.writeStringToFile(file, sourceString);

        assertSourceEquals(SlangSource.fromFile(file.toURI()));

    }

    @Test
    public void testFromBytes() throws Exception {
        assertSourceEquals(SlangSource.fromBytes(sourceString.getBytes(), sourceName));
    }
}