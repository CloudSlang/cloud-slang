package io.cloudslang.lang.compiler;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * User: bancl
 * Date: 7/4/2016
 */
public class ExtensionTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testValidateSlangFileExtensionInvalidExtension() {
        exception.expect(RuntimeException.class);
        exception.expectMessage("File: slang.sls must have one of the following extensions: sl, sl.yaml, sl.yml.");
        Extension.validateSlangFileExtension("slang.sls");
    }

    @Test
    public void testValidateSlangFileExtensionSL() {
        Extension.validateSlangFileExtension("slang.sl");
    }

    @Test
    public void testValidateSlangFileExtensionSLYAML() {
        Extension.validateSlangFileExtension("slang.sl.yaml");
    }

    @Test
    public void testValidateSlangFileExtensionSLYML() {
        Extension.validateSlangFileExtension("slang.sl.yml");
    }

    @Test
    public void testValidatePropertiesFileExtensionInvalidExtension() {
        exception.expect(RuntimeException.class);
        exception.expectMessage("File: slang.sls must have one of the following extensions: prop.sl.");
        Extension.validatePropertiesFileExtension("slang.sls");
    }

    @Test
    public void testValidatePropertiesFileExtensionPROPSL() {
        Extension.validatePropertiesFileExtension("slang.prop.sl");
    }

    @Test
    public void testRemoveFileExtension() {
        Assert.assertEquals("slang", Extension.removeExtension("slang.prop.sl"));
    }

    @Test
    public void testRemoveFileExtensionInvalidExtension() {
        Assert.assertEquals("slang.prsaop.sasdl", Extension.removeExtension("slang.prsaop.sasdl"));
    }

    @Test
    public void testFindExtension() {
        Assert.assertEquals(Extension.PROP_SL, Extension.findExtension("slang.prop.sl"));
    }

    @Test
    public void testFindExtensionInvalidExtension() {
        Assert.assertEquals(null, Extension.findExtension("slang.prsaop.sasdl"));
    }

    @Test
    public void testGetYAMLFileExtension() {
        String[] extensions = Extension.getYamlFileExtensionValues();
        Assert.assertEquals(2, extensions.length);
        Assert.assertEquals("yaml", extensions[0]);
        Assert.assertEquals("yml", extensions[1]);
    }
}
