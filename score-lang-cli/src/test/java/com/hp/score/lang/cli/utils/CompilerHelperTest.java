package com.hp.score.lang.cli.utils;


import com.hp.score.lang.compiler.SlangCompiler;
import com.hp.score.lang.compiler.SlangCompilerImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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

    /*@Autowired(required = false)
    private SlangCompiler slangCompiler;*/

    @Test(expected = RuntimeException.class)
    public void testFilePathWrong() throws Exception {
        compilerHelper.compile(null,null,null);
    }


    @Configuration
    static class Config{

        @Bean
        public SlangCompiler compiler(){
            return mock(SlangCompiler.class);
        }

        @Bean
        public CompilerHelper compilerHelper(){
            return new CompilerHelper();
        }

    }



}