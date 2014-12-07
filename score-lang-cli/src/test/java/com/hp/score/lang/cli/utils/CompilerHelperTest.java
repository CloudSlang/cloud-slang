package com.hp.score.lang.cli.utils;


import com.google.common.collect.Sets;
import com.hp.score.lang.api.Slang;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

import static org.mockito.Mockito.mock;

/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
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
        String flowFilePath = getClass().getResource("/flow.yaml").getPath();
        String opFilePath = getClass().getResource("/operation.yaml").getPath();
        compilerHelper.compile(flowFilePath, null, null);
        Mockito.verify(slang).compile(new File(flowFilePath), Sets.newHashSet(new File(flowFilePath), new File(opFilePath)));
    }

    @Test
    public void testFilePathValidWithOtherPathForDepdencies() throws Exception {
        String flowFilePath = getClass().getResource("/flow.yaml").getPath();
        String folderPath = getClass().getResource("/flowsdir/").getPath();
        String flow2FilePath = getClass().getResource("/flowsdir/flow2.yaml").getPath();
        compilerHelper.compile(flowFilePath, null, folderPath);
        Mockito.verify(slang).compile(new File(flowFilePath), Sets.newHashSet(new File(flow2FilePath)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDirPathForDepdencies() throws Exception {
        String flowFilePath = getClass().getResource("/flow.yaml").getPath();
        String invalidDirPath = getClass().getResource("").getPath().concat("xxx");
        compilerHelper.compile(flowFilePath, null, invalidDirPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDirPathForDepdencies2() throws Exception {
        String flowFilePath = getClass().getResource("/flow.yaml").getPath();
        compilerHelper.compile(flowFilePath, null, flowFilePath);
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