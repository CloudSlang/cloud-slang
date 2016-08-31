package io.cloudslang.lang.compiler;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

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
        Assert.assertEquals(sourceString, source.getSource());
        Assert.assertEquals(sourceName, source.getFileName());
    }

    @Test
    public void testFromFile() throws Exception {
        File file = folder.newFile(sourceName);
        FileUtils.writeStringToFile(file, sourceString);

        assertSourceEquals(SlangSource.fromFile(file));
    }

    @Test
    public void testFromFileYAML_SLExtension() throws Exception {
        File file = folder.newFile(sourceName + "." + Extension.YAML.getValue() + "." + Extension.SL.getValue());
        FileUtils.writeStringToFile(file, sourceString);

        SlangSource source = SlangSource.fromFile(file);
        Assert.assertEquals(sourceString, source.getSource());
        Assert.assertEquals(sourceName + "." + Extension.YAML.getValue(), source.getFileName());
    }

    @Test
    public void testFromFileYAML_YMLExtension() throws Exception {
        File file = folder.newFile(sourceName + "." + Extension.YAML.getValue() + "." + Extension.YML.getValue());
        FileUtils.writeStringToFile(file, sourceString);

        SlangSource source = SlangSource.fromFile(file);
        Assert.assertEquals(sourceString, source.getSource());
        Assert.assertEquals(sourceName + "." + Extension.YAML.getValue(), source.getFileName());
    }

    @Test
    public void testFromFileYML_SLExtension() throws Exception {
        File file = folder.newFile(sourceName + "." + Extension.YML.getValue() + "." + Extension.SL.getValue());
        FileUtils.writeStringToFile(file, sourceString);

        SlangSource source = SlangSource.fromFile(file);
        Assert.assertEquals(sourceString, source.getSource());
        Assert.assertEquals(sourceName + "." + Extension.YML.getValue(), source.getFileName());
    }

    @Test
    public void testFromFileYML_SL_YAMLExtension() throws Exception {
        File file = folder.newFile(sourceName + "." + Extension.YML.getValue() + "." + Extension.SL_YAML.getValue());
        FileUtils.writeStringToFile(file, sourceString);

        SlangSource source = SlangSource.fromFile(file);
        Assert.assertEquals(sourceString, source.getSource());
        Assert.assertEquals(sourceName + "." + Extension.YML.getValue(), source.getFileName());
    }

    @Test
    public void testFromFileYML_SL_YMLExtension() throws Exception {
        File file = folder.newFile(sourceName + "." + Extension.YML.getValue() + "." + Extension.SL_YML.getValue());
        FileUtils.writeStringToFile(file, sourceString);

        SlangSource source = SlangSource.fromFile(file);
        Assert.assertEquals(sourceString, source.getSource());
        Assert.assertEquals(sourceName + "." + Extension.YML.getValue(), source.getFileName());
    }

    @Test
    public void testFromFileSL_PROP_SLExtension() throws Exception {
        File file = folder.newFile(sourceName + "." + Extension.SL.getValue() + "." + Extension.PROP_SL.getValue());
        FileUtils.writeStringToFile(file, sourceString);

        SlangSource source = SlangSource.fromFile(file);
        Assert.assertEquals(sourceString, source.getSource());
        Assert.assertEquals(sourceName + "." + Extension.SL.getValue(), source.getFileName());
    }

    @Test
    public void testFromFilePROP_SL_SLExtension() throws Exception {
        File file = folder.newFile(sourceName + "." + Extension.PROP_SL.getValue() + "." + Extension.SL.getValue());
        FileUtils.writeStringToFile(file, sourceString);

        SlangSource source = SlangSource.fromFile(file);
        Assert.assertEquals(sourceString, source.getSource());
        Assert.assertEquals(sourceName + "." + Extension.PROP_SL.getValue(), source.getFileName());
    }

    @Test
    public void testFromFileSLExtension() throws Exception {
        File file = folder.newFile(sourceName + "." + Extension.SL.getValue());
        FileUtils.writeStringToFile(file, sourceString);

        assertSourceEquals(SlangSource.fromFile(file));
    }

    @Test
    public void testFromFileSL_YAMLExtension() throws Exception {
        File file = folder.newFile(sourceName + "." + Extension.SL_YAML.getValue());
        FileUtils.writeStringToFile(file, sourceString);

        assertSourceEquals(SlangSource.fromFile(file));
    }

    @Test
    public void testFromFileSL_YMLExtension() throws Exception {
        File file = folder.newFile(sourceName + "." + Extension.SL_YML.getValue());
        FileUtils.writeStringToFile(file, sourceString);

        assertSourceEquals(SlangSource.fromFile(file));
    }

    @Test
    public void testFromFilePROP_SLExtension() throws Exception {
        File file = folder.newFile(sourceName + "." + Extension.PROP_SL.getValue());
        FileUtils.writeStringToFile(file, sourceString);

        assertSourceEquals(SlangSource.fromFile(file));
    }

    @Test
    public void testFromFileYAMLExtension() throws Exception {
        File file = folder.newFile(sourceName + "." + Extension.YAML.getValue());
        FileUtils.writeStringToFile(file, sourceString);

        assertSourceEquals(SlangSource.fromFile(file));
    }

    @Test
    public void testFromFileYMLExtension() throws Exception {
        File file = folder.newFile(sourceName + "." + Extension.YML.getValue());
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

        when(file.getPath()).thenThrow((Class) IOException.class);

        exception.expect(RuntimeException.class);
        exception.expectMessage("reading");
        exception.expectMessage(file.getName());

        SlangSource.fromFile(file);
    }

    @Test
    public void testWhenPassingNullFileThrowException() throws Exception {

        exception.expect(NullPointerException.class);
        exception.expectMessage("null");

        assertSourceEquals(SlangSource.fromFile((File) null));
    }

    @Test
    public void testFromFileUri() throws Exception {
        File file = folder.newFile(sourceName);
        FileUtils.writeStringToFile(file, sourceString);

        assertSourceEquals(SlangSource.fromFile(file.toURI()));

    }

    @Test
    public void testFromFileUnicode() throws Exception {
        // In this scenario, the source file is encoded using the default encoding (UTF-8)
        final String sourceString = "ÈûÜ噂閏없다哱嘰ЕеЖ lœuvre àît André Citroën";
        final String filename = sourceName + "_utf8";
        File file = folder.newFile(filename);
        FileUtils.writeStringToFile(file, sourceString, "UTF-8");

        // Make sure UTF-8 is the default encoding for reading Slang sources
        SlangSource result = SlangSource.fromFile(file);

        Assert.assertEquals(sourceString, result.getSource());
        Assert.assertEquals(filename, result.getFileName());
    }

    @Test
    public void testFromFileOverrideEncoding() throws Exception {
        // In this scenario, the source file is encoded using a single-byte Western European encoding (instead of UTF-8)
        final String sourceString = "André Citroën";
        final String filename = sourceName + "_fr";
        File file = folder.newFile(filename);
        FileUtils.writeStringToFile(file, sourceString, "ISO-8859-1");

        // Make sure we can override the default encoding (UTF-8) with a system property
        System.setProperty("cslang.encoding", "ISO-8859-1");
        SlangSource result = SlangSource.fromFile(file);
        System.clearProperty("cslang.encoding");

        Assert.assertEquals(sourceString, result.getSource());
        Assert.assertEquals(filename, result.getFileName());
    }

    @Test
    public void testFromBytes() throws Exception {
        assertSourceEquals(SlangSource.fromBytes(sourceString.getBytes(), sourceName));
    }

    @Test
    public void testFromBytesUnicode() throws Exception {
        // In this scenario, the source file is encoded using the default encoding (UTF-8)
        final String sourceString = "ÈûÜ噂閏없다哱嘰ЕеЖ lœuvre àît André Citroën";
        final byte[] sourceFile = sourceString.getBytes("UTF-8");

        // Make sure UTF-8 is the default encoding for reading Slang sources
        SlangSource result = SlangSource.fromBytes(sourceFile, sourceName);

        Assert.assertEquals(sourceString, result.getSource());
        Assert.assertEquals(sourceName, result.getFileName());
    }

    @Test
    public void testFromBytesOverrideEncoding() throws Exception {
        // In this scenario, the source file is encoded using a single-byte Western European encoding (instead of UTF-8)
        final String sourceString = "André Citroën";
        final byte[] sourceFile = sourceString.getBytes("ISO-8859-1");

        // Make sure we can override the default encoding (UTF-8) with a system property
        System.setProperty("cslang.encoding", "ISO-8859-1");
        SlangSource result = SlangSource.fromBytes(sourceFile, sourceName);
        System.clearProperty("cslang.encoding");

        Assert.assertEquals(sourceString, result.getSource());
        Assert.assertEquals(sourceName, result.getFileName());
    }

    @Test
    public void testFromBytesWithExtension() throws Exception {
        SlangSource source = SlangSource.fromBytes(
                sourceString.getBytes(),
                sourceName + "." + Extension.SL.getValue()
        );
        Assert.assertEquals(sourceString, source.getSource());
        Assert.assertEquals(sourceName, source.getFileName());
    }

}
