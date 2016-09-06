/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.cli.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.commons.services.api.SlangSourceService;
import io.cloudslang.lang.commons.services.impl.SlangSourceServiceImpl;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.entities.encryption.DummyEncryptor;
import io.cloudslang.lang.entities.utils.ApplicationContextProvider;
import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CompilerHelperTest.Config.class)
public class CompilerHelperTest {

    private static final String APP_HOME = "app.home";

    @Autowired
    private CompilerHelper compilerHelper;
    @Autowired
    private Slang slang;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test(expected = IllegalArgumentException.class)
    public void testFilePathWrong() throws Exception {
        compilerHelper.compile(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFilePathNotFile() throws Exception {
        compilerHelper.compile("xxx", null);
    }

    @Before
    public void resetMocks() {
        Mockito.reset(slang);
    }

    @Test
    public void testUnsupportedExtension() throws Exception {
        URI flowFilePath = getClass().getResource("/flow.yaml").toURI();
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("must have one of the following extensions");
        compilerHelper.compile(flowFilePath.getPath(), null);

    }

    @Test
    public void testDependenciesFileParentFolder() throws Exception {
        URI flowPath = getClass().getResource("/executables/dir3/flow.sl").toURI();
        URI opPath = getClass().getResource("/executables/dir3/dir3_1/test_op.sl").toURI();
        compilerHelper.compile(flowPath.getPath(), null);
        Mockito.verify(slang).compile(
                SlangSource.fromFile(flowPath),
                Sets.newHashSet(
                        SlangSource.fromFile(opPath),
                        SlangSource.fromFile(flowPath)
                )
        );
    }

    @Test
    public void testFilePathValidWithOtherPathForDependencies() throws Exception {
        URI flowFilePath = getClass().getResource("/flow.sl").toURI();
        URI folderPath = getClass().getResource("/executables/dir1/").toURI();
        URI flow2FilePath = getClass().getResource("/executables/dir1/flow2.sl").toURI();
        compilerHelper.compile(flowFilePath.getPath(), Lists.newArrayList(folderPath.getPath()));
        Mockito.verify(slang).compile(SlangSource.fromFile(flowFilePath),
                Sets.newHashSet(SlangSource.fromFile(flow2FilePath)));
    }

    @Test
    public void testCompileMixedSlangFiles() throws Exception {
        URI flowFilePath = getClass().getResource("/flow.sl").toURI();
        URI folderPath = getClass().getResource("/mixed_sl_files/").toURI();
        URI dependency1 = getClass().getResource("/mixed_sl_files/configuration/properties/executables/test_flow.sl").toURI();
        URI dependency2 = getClass().getResource("/mixed_sl_files/configuration/properties/executables/test_op.sl").toURI();
        compilerHelper.compile(flowFilePath.getPath(), Lists.newArrayList(folderPath.getPath()));
        Mockito.verify(slang).compile(
                SlangSource.fromFile(flowFilePath),
                Sets.newHashSet(
                        SlangSource.fromFile(dependency1),
                        SlangSource.fromFile(dependency2)
                )
        );
    }

    // flowprop.sl is not recognized as properties file
    @Test
    public void testCompileDependencyPropPartOfFileName() throws Exception {
        URI flowFilePath = getClass().getResource("/flow.sl").toURI();
        URI folderPath = getClass().getResource("/executables/dir2/").toURI();
        URI flow2FilePath = getClass().getResource("/executables/dir2/flowprop.sl").toURI();
        compilerHelper.compile(flowFilePath.getPath(), Lists.newArrayList(folderPath.getPath()));
        Mockito.verify(slang).compile(
                SlangSource.fromFile(flowFilePath),
                Sets.newHashSet(SlangSource.fromFile(flow2FilePath))
        );
    }

    @Test
    public void testInvalidDirPathForDependencies() throws Exception {
        String flowFilePath = getClass().getResource("/flow.sl").getPath();
        String currentDirPath = getClass().getResource("").getPath();
        String invalidDirPath = currentDirPath.concat("xxx");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("xxx");
        expectedException.expectMessage(CompilerHelperImpl.INVALID_DIRECTORY_ERROR_MESSAGE_SUFFIX);

        compilerHelper.compile(flowFilePath, Lists.newArrayList(invalidDirPath));
    }

    @Test
    public void testInvalidDirPathForDependencies2() throws Exception {
        String flowFilePath = getClass().getResource("/flow.sl").getPath();

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("flow.sl");
        expectedException.expectMessage(CompilerHelperImpl.INVALID_DIRECTORY_ERROR_MESSAGE_SUFFIX);

        compilerHelper.compile(flowFilePath, Lists.newArrayList(flowFilePath));
    }

    @Test
    public void testLoadSystemProperties() throws Exception {
        Set<SystemProperty> systemProperties = Sets.newHashSet(
                new SystemProperty("user.sys", "props.host", "localhost"),
                new SystemProperty("user.sys", "props.port", "22"),
                new SystemProperty("user.sys", "props.alla", "balla")
        );
        URI systemPropertyURI = getClass().getResource("/properties/system_properties.prop.sl").toURI();
        SlangSource source = SlangSource.fromFile(systemPropertyURI);
        when(slang.loadSystemProperties(eq(source))).thenReturn(systemProperties);

        compilerHelper.loadSystemProperties(Collections.singletonList(systemPropertyURI.getPath()));

        verify(slang).loadSystemProperties(eq(source));
    }

    @Test
    public void testLoadSystemPropertiesInvalidExtension() throws Exception {
        URI props1 = getClass().getResource("/properties/duplicate/props1.prop.sl").toURI();
        URI props2 = getClass().getResource("/properties/duplicate/props2.prop.sl").toURI();
        Set<SystemProperty> systemProperties1 = Sets.newHashSet(
                new SystemProperty("user.sys", "props.host", "localhost")
        );
        Set<SystemProperty> systemProperties2 = Sets.newHashSet(
                new SystemProperty("user.SYS", "props.host", "localhost")
        );
        when(slang.loadSystemProperties(eq(SlangSource.fromFile(props1)))).thenReturn(systemProperties1);
        when(slang.loadSystemProperties(eq(SlangSource.fromFile(props2)))).thenReturn(systemProperties2);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage(containsIgnoreCase("user.SYS.props.host"));
        expectedException.expectMessage("properties" + File.separator + "duplicate" + File.separator + "props1.prop.sl");
        expectedException.expectMessage("properties" + File.separator + "duplicate" + File.separator + "props1.prop.sl");

        compilerHelper.loadSystemProperties(Lists.newArrayList(props1.getPath(), props2.getPath()));
    }

    @Test
    public void testLoadSystemPropertiesDefaultFolder() throws Exception {
        String initialValue = System.getProperty(APP_HOME, "");
        String defaultDirPath = getClass().getResource("/mixed_sl_files/").getPath();
        System.setProperty(APP_HOME, defaultDirPath);

        compilerHelper.loadSystemProperties(Collections.<String>emptyList());

        ArgumentCaptor<SlangSource> sourceCaptor = ArgumentCaptor.forClass(SlangSource.class);
        verify(slang, times((2))).loadSystemProperties(sourceCaptor.capture());

        Set<SlangSource> capturedSources = new HashSet<>(sourceCaptor.getAllValues());
        Set<SlangSource> expectedSources = Sets.newHashSet(
                SlangSource.fromFile(getClass().getResource("/mixed_sl_files/configuration/properties/properties/ubuntu.prop.sl").toURI()),
                SlangSource.fromFile(getClass().getResource("/mixed_sl_files/configuration/properties/properties/windows.prop.sl").toURI())
        );
        Assert.assertEquals(expectedSources, capturedSources);

        System.setProperty(APP_HOME, initialValue);
    }

    @Test
    public void testLoadInputsFromFile() throws Exception {
        Map<String, Serializable> expected = new HashMap<>();
        expected.put("host", ValueFactory.create("localhost", false));
        expected.put("port", ValueFactory.create("22", false));
        expected.put("username", ValueFactory.create("myusername", false));
        expected.put("password", ValueFactory.create("mypassword", true));
        URI inputsFromFile = getClass().getResource("/inputs/inputs.yaml").toURI();
        Map<String, ? extends Serializable> result = compilerHelper.loadInputsFromFile(Collections.singletonList(inputsFromFile.getPath()));
        Assert.assertNotNull(result);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testLoadInputsFromFileBadValueKey() throws Exception {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("inputs_value_missing.yaml} " +
                "has unrecognized tag {bad_value_key}. Please take a look at the supported features per versions link");

        URI inputsFromFile = getClass().getResource("/inputs/inputs_value_missing.yaml").toURI();
        compilerHelper.loadInputsFromFile(Collections.singletonList(inputsFromFile.getPath()));
    }

    @Test
    public void testLoadInputsFromCommentedFile() throws Exception {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Inputs file");

        URI inputsFromFile = getClass().getResource("/inputs/commented_inputs.yaml").toURI();
        compilerHelper.loadInputsFromFile(Collections.singletonList(inputsFromFile.getPath()));
    }

    @Test
    public void testLoadInputsFromEmptyFile() throws Exception {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Inputs file");

        URI inputsFromFile = getClass().getResource("/inputs/empty_inputs.yaml").toURI();
        compilerHelper.loadInputsFromFile(Collections.singletonList(inputsFromFile.getPath()));

    }

    @Ignore("Awaiting CloudSlang/cloud-slang#302 decision")
    @Test
    public void testLoadInputsFromFileImplicit() throws Exception {
        Map<String, ? extends Serializable> result = compilerHelper.loadInputsFromFile(null);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
    }

    @Configuration
    static class Config {

        @Bean
        public Slang slang() {
            return mock(Slang.class);
        }

        @Bean
        public CompilerHelper compilerHelper() {
            return new CompilerHelperImpl();
        }

        @Bean
        public ApplicationContextProvider applicationContextProvider() {
            return new ApplicationContextProvider();
        }

        @Bean
        public DummyEncryptor dummyEncryptor() {
            return new DummyEncryptor();
        }

        @Bean
        public Yaml yaml() {
            Yaml yaml = new Yaml();
            yaml.setBeanAccess(BeanAccess.FIELD);
            return yaml;
        }

        @Bean
        public SlangSourceService slangSourceService() {
            return new SlangSourceServiceImpl();
        }

    }

    private Matcher<String> containsIgnoreCase(final String element) {
        return new BaseMatcher<String>() {
            @Override
            public boolean matches(Object item) {
                String itemAsString = (String) item;
                return itemAsString.toLowerCase().contains(element.toLowerCase());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("should contain (ignoring case) " + element);
            }
        };
    }

}
