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
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.SystemProperty;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CompilerHelperTest.Config.class)
public class CompilerHelperTest {

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
	public void testFilePathValid() throws Exception {
		URI flowFilePath = getClass().getResource("/flow.sl").toURI();
		URI opFilePath = getClass().getResource("/test_op.sl").toURI();
		URI flow2FilePath = getClass().getResource("/flowsdir/flow2.sl").toURI();
		URI spFlow = getClass().getResource("/sp/flow.sl").toURI();
		URI spOp = getClass().getResource("/sp/operation.sl").toURI();
        URI metadata = getClass().getResource("/metadata/metadata.sl").toURI();
        URI descriptionMissingMetadata = getClass().getResource("/metadata/metadata_full_description_missing.sl").toURI();
		compilerHelper.compile(flowFilePath.getPath(), null);
		Mockito.verify(slang).compile(SlangSource.fromFile(flowFilePath), Sets.newHashSet(SlangSource.fromFile(flowFilePath), SlangSource.fromFile(flow2FilePath),
			SlangSource.fromFile(opFilePath), SlangSource.fromFile(spFlow), SlangSource.fromFile(spOp), SlangSource.fromFile(metadata), SlangSource.fromFile(descriptionMissingMetadata)));
	}

    @Test
    public void testFilePathValidWithOtherPathForDependencies() throws Exception {
        URI flowFilePath = getClass().getResource("/flow.sl").toURI();
        URI folderPath = getClass().getResource("/flowsdir/").toURI();
        URI flow2FilePath = getClass().getResource("/flowsdir/flow2.sl").toURI();
        compilerHelper.compile(flowFilePath.getPath(), Lists.newArrayList(folderPath.getPath()));
        Mockito.verify(slang).compile(SlangSource.fromFile(flowFilePath),
                Sets.newHashSet(SlangSource.fromFile(flow2FilePath)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDirPathForDependencies() throws Exception {
        String flowFilePath = getClass().getResource("/flow.sl").getPath();
        String invalidDirPath = getClass().getResource("").getPath().concat("xxx");
        compilerHelper.compile(flowFilePath, Lists.newArrayList(invalidDirPath));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDirPathForDependencies2() throws Exception {
        String flowFilePath = getClass().getResource("/flow.sl").getPath();
        compilerHelper.compile(flowFilePath, Lists.newArrayList(flowFilePath));
    }

	@Test
	public void testLoadSystemProperties() throws Exception {
		Set<SystemProperty> systemProperties = Sets.newHashSet(
                SystemProperty.createSystemProperty("user.sys", "props.host", "localhost"),
                SystemProperty.createSystemProperty("user.sys", "props.port", "22"),
                SystemProperty.createSystemProperty("user.sys", "props.alla", "balla")
        );
		URI systemPropertyURI = getClass().getResource("/properties/system_properties.sl").toURI();
        SlangSource source = SlangSource.fromFile(systemPropertyURI);
        when(slang.loadSystemProperties(eq(source))).thenReturn(systemProperties);

		Set<SystemProperty> result = compilerHelper.loadSystemProperties(Arrays.asList(systemPropertyURI.getPath()));

        verify(slang).loadSystemProperties(eq(source));
	}

    @Test
    public void testLoadInputsFromFile() throws Exception {
        Map<String, Serializable> expected = new HashMap<>();
        expected.put("host", "localhost");
        expected.put("port", "22");
        URI inputsFromFile = getClass().getResource("/inputs/inputs.yaml").toURI();
        Map<String, ? extends Serializable> result = compilerHelper.loadInputsFromFile(Collections.singletonList(inputsFromFile.getPath()));
        Assert.assertNotNull(result);
        Assert.assertEquals(expected, result);
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
        public Yaml yaml() {
            Yaml yaml = new Yaml();
            yaml.setBeanAccess(BeanAccess.FIELD);
            return yaml;
        }

    }

}
