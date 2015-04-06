/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.configuration;

import io.cloudslang.lang.tools.build.tester.SlangTestRunner;
import io.cloudslang.lang.tools.build.verifier.SlangContentVerifier;
import io.cloudslang.lang.api.configuration.SlangSpringConfiguration;
import io.cloudslang.lang.tools.build.SlangBuilder;
import io.cloudslang.lang.tools.build.tester.parse.TestCasesYamlParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by stoneo on 2/9/2015.
 */
@Configuration
@Import(SlangSpringConfiguration.class)
public class SlangBuildSpringConfiguration {

    @Bean
    public SlangBuilder slangBuild(){
        return new SlangBuilder();
    }

    @Bean
    public SlangContentVerifier slangContentVerifier(){
        return new SlangContentVerifier();
    }

    @Bean
    public SlangTestRunner slangTestRunner(){
        return new SlangTestRunner();
    }

    @Bean
    public TestCasesYamlParser parser(){
        return new TestCasesYamlParser();
    }
}
