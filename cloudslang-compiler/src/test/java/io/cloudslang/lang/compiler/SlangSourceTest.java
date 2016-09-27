package io.cloudslang.lang.compiler;

import io.cloudslang.lang.entities.SlangSystemPropertyConstant;
import io.cloudslang.lang.entities.constants.FileConstants;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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

    private final static String content = "file_content";
    private final static Extension extension = Extension.SL;
    private final static String nameWithoutExtension = "file_name";
    private final static String name = nameWithoutExtension + FileConstants.EXTENSION_DELIMITER + extension.getValue();

    private void assertSourceEquals(String nameSuffix, Extension expectedExtension) throws IOException {
        String name = nameWithoutExtension + nameSuffix;
        File file = folder.newFile(name);
        FileUtils.writeStringToFile(file, content);

        SlangSource source = SlangSource.fromFile(file);
        Assert.assertEquals(content, source.getContent());
        Assert.assertEquals(name, source.getName());
        Assert.assertEquals(expectedExtension, source.getFileExtension());
        Assert.assertTrue(source.getFilePath().endsWith(name));
    }

    private void assertSourceEquals(URI fileURI, Extension expectedExtension) throws IOException {
        SlangSource source = SlangSource.fromFile(fileURI);
        Assert.assertEquals(content, source.getContent());
        Assert.assertEquals(name, source.getName());
        Assert.assertEquals(expectedExtension, source.getFileExtension());
        Assert.assertTrue(source.getFilePath().endsWith(name));
    }

    private void assertSourceEquals(byte[] bytes, String name) throws IOException {
        SlangSource source = SlangSource.fromBytes(bytes, name);
        Assert.assertEquals(content, source.getContent());
        Assert.assertEquals(name, source.getName());
    }

    @Test
    public void testFromFileYAML_SLExtension() throws Exception {
        assertSourceEquals(nameWithoutExtension + "." + Extension.YAML.getValue() + "." + Extension.SL.getValue(), Extension.SL);
    }

    @Test
    public void testFromFileYAML_YMLExtension() throws Exception {
        assertSourceEquals(nameWithoutExtension + "." + Extension.YAML.getValue() + "." + Extension.YML.getValue(), Extension.YML);
    }

    @Test
    public void testFromFileYML_SLExtension() throws Exception {
        assertSourceEquals(nameWithoutExtension + "." + Extension.YML.getValue() + "." + Extension.SL.getValue(), Extension.SL);
    }

    @Test
    public void testFromFileYML_SL_YAMLExtension() throws Exception {
        assertSourceEquals(nameWithoutExtension + "." + Extension.YML.getValue() + "." + Extension.SL_YAML.getValue(), Extension.SL_YAML);
    }

    @Test
    public void testFromFileYML_SL_YMLExtension() throws Exception {
        assertSourceEquals(nameWithoutExtension + "." + Extension.YML.getValue() + "." + Extension.SL_YML.getValue(), Extension.SL_YML);
    }

    @Test
    public void testFromFileSL_PROP_SLExtension() throws Exception {
        assertSourceEquals(nameWithoutExtension + "." + Extension.SL.getValue() + "." + Extension.PROP_SL.getValue(), Extension.PROP_SL);
    }

    @Test
    public void testFromFilePROP_SL_SLExtension() throws Exception {
        assertSourceEquals(nameWithoutExtension + "." + Extension.PROP_SL.getValue() + "." + Extension.SL.getValue(), Extension.SL);
    }

    @Test
    public void testFromFileSLExtension() throws Exception {
        assertSourceEquals(nameWithoutExtension + "." + Extension.SL.getValue(), Extension.SL);
    }

    @Test
    public void testFromFileSL_YAMLExtension() throws Exception {
        assertSourceEquals(nameWithoutExtension + "." + Extension.SL_YAML.getValue(), Extension.SL_YAML);
    }

    @Test
    public void testFromFileSL_YMLExtension() throws Exception {
        assertSourceEquals(nameWithoutExtension + "." + Extension.SL_YML.getValue(), Extension.SL_YML);
    }

    @Test
    public void testFromFilePROP_SLExtension() throws Exception {
        assertSourceEquals(nameWithoutExtension + "." + Extension.PROP_SL.getValue(), Extension.PROP_SL);
    }

    @Test
    public void testFromFileYAMLExtension() throws Exception {
        assertSourceEquals(nameWithoutExtension + "." + Extension.YAML.getValue(), Extension.YAML);
    }

    @Test
    public void testFromFileYMLExtension() throws Exception {
        assertSourceEquals(nameWithoutExtension + "." + Extension.YML.getValue(), Extension.YML);
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
        when(file.getName()).thenReturn(name);
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

        SlangSource.fromFile((File) null);
    }

    @Test
    public void testFromFileUri() throws Exception {
        File file = folder.newFile(name);
        FileUtils.writeStringToFile(file, content);

        assertSourceEquals(file.toURI(), extension);

    }

    @Test
    public void testFromFileUnicode() throws Exception {
        // In this scenario, the source file is encoded using the default encoding (UTF-8)
        final String sourceString = "ÈûÜ噂閏없다哱嘰ЕеЖ lœuvre àît André Citroën";
        final String filename = name + "_utf8";
        File file = folder.newFile(filename);
        FileUtils.writeStringToFile(file, sourceString, "UTF-8");

        // Make sure UTF-8 is the default encoding for reading Slang sources
        SlangSource result = SlangSource.fromFile(file);

        Assert.assertEquals(sourceString, result.getContent());
        Assert.assertEquals(filename, result.getName());
    }

    @Test
    public void testFromFileOverrideEncoding() throws Exception {
        // In this scenario, the source file is encoded using a single-byte Western European encoding (instead of UTF-8)
        final String sourceString = "André Citroën";
        final String filename = name + "_fr";
        File file = folder.newFile(filename);
        FileUtils.writeStringToFile(file, sourceString, "ISO-8859-1");

        // Make sure we can override the default encoding (UTF-8) with a system property
        System.setProperty("cslang.encoding", "ISO-8859-1");
        SlangSource result = SlangSource.fromFile(file);
        System.clearProperty("cslang.encoding");

        Assert.assertEquals(sourceString, result.getContent());
        Assert.assertEquals(filename, result.getName());
    }

    @Test
    public void testFromBytes() throws Exception {
        assertSourceEquals(content.getBytes(getCharset()), name);
    }

    @Test
    public void testFromBytesUnicode() throws Exception {
        // In this scenario, the source file is encoded using the default encoding (UTF-8)
        final String sourceString = "ÈûÜ噂閏없다哱嘰ЕеЖ lœuvre àît André Citroën";
        final byte[] sourceFile = sourceString.getBytes("UTF-8");

        // Make sure UTF-8 is the default encoding for reading Slang sources
        SlangSource result = SlangSource.fromBytes(sourceFile, name);

        Assert.assertEquals(sourceString, result.getContent());
        Assert.assertEquals(name, result.getName());
    }

    @Test
    public void testFromBytesOverrideEncoding() throws Exception {
        // In this scenario, the source file is encoded using a single-byte Western European encoding (instead of UTF-8)
        final String sourceString = "André Citroën";
        final byte[] sourceFile = sourceString.getBytes("ISO-8859-1");

        // Make sure we can override the default encoding (UTF-8) with a system property
        System.setProperty("cslang.encoding", "ISO-8859-1");
        SlangSource result = SlangSource.fromBytes(sourceFile, name);
        System.clearProperty("cslang.encoding");

        Assert.assertEquals(sourceString, result.getContent());
        Assert.assertEquals(name, result.getName());
    }

    @Test
    public void testFromBytesWithExtension() throws Exception {
        String name = SlangSourceTest.name + "." + Extension.SL.getValue();
        assertSourceEquals(content.getBytes(getCharset()), name);
    }

    private Charset getCharset() {
        String cslangEncoding = System.getProperty(SlangSystemPropertyConstant.CSLANG_ENCODING.getValue());
        return StringUtils.isEmpty(cslangEncoding) ?
                StandardCharsets.UTF_8 :
                Charset.forName(cslangEncoding);
    }

}
