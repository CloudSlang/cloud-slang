/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openscore.lang.cli.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openscore.lang.api.Slang;
import org.openscore.lang.compiler.SlangSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;

import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CompilerHelperTest.Config.class)
public class CompilerHelperTest {

    @Autowired
    private CompilerHelper compilerHelper;

    @Autowired
    private Slang slang;

    @Test(expected = IllegalArgumentException.class)
    public void testFilePathWrong() throws Exception {
        compilerHelper.compile(null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFilePathNotFile() throws Exception {
        compilerHelper.compile("xxx", null, null);
    }

    @Before
    public void resetMocks() {
        Mockito.reset(slang);
    }

    @Test
    public void testFilePathValid() throws Exception {
        URL flowFilePath = getClass().getResource("/flow.yaml");
        URL opFilePath = getClass().getResource("/test_op.sl");
        URL flow2FilePath = getClass().getResource("/flowsdir/flow2.yaml");
        compilerHelper.compile(flowFilePath.getPath(), null, null);
        Mockito.verify(slang).compile(SlangSource.fromFile(flowFilePath.toURI()),
                Sets.newHashSet(SlangSource.fromFile(flowFilePath.toURI()),SlangSource.fromFile(flow2FilePath.toURI()),
                        SlangSource.fromFile(opFilePath.toURI())));
    }

    @Test
    public void testFilePathValidWithOtherPathForDependencies() throws Exception {
        URL flowFilePath = getClass().getResource("/flow.yaml");
        URL folderPath = getClass().getResource("/flowsdir/");
        URL flow2FilePath = getClass().getResource("/flowsdir/flow2.yaml");
        compilerHelper.compile(flowFilePath.getPath(), null, Lists.newArrayList(folderPath.getPath()));
        Mockito.verify(slang).compile(SlangSource.fromFile(flowFilePath.toURI()),
                Sets.newHashSet(SlangSource.fromFile(flow2FilePath.toURI())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDirPathForDependencies() throws Exception {
        String flowFilePath = getClass().getResource("/flow.yaml").getPath();
        String invalidDirPath = getClass().getResource("").getPath().concat("xxx");
        compilerHelper.compile(flowFilePath, null, Lists.newArrayList(invalidDirPath));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDirPathForDependencies2() throws Exception {
        String flowFilePath = getClass().getResource("/flow.yaml").getPath();
        compilerHelper.compile(flowFilePath, null, Lists.newArrayList(flowFilePath));
    }

    @Test
    public void testLoadSystemProperties(){
        SlangSource source = new SlangSource("source", "name");
        slang.loadSystemProperties(source);
        Mockito.verify(slang).loadSystemProperties(source);
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

    }

}