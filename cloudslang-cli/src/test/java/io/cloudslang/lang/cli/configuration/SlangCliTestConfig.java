/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.cli.configuration;

import io.cloudslang.lang.cli.services.ScoreServices;
import io.cloudslang.lang.cli.services.ScoreServicesImpl;
import io.cloudslang.lang.cli.utils.CompilerHelper;
import io.cloudslang.lang.cli.utils.CompilerHelperImpl;
import io.cloudslang.lang.commons.services.api.SlangSourceService;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

/**
 * Date: 12/9/2014
 *
 * @author Bonczidai Levente
 */
@Configuration
@ComponentScan("io.cloudslang.lang.cli")
public class SlangCliTestConfig {

    @Bean
    public ScoreServices scoreServices() {
        return mock(ScoreServicesImpl.class);
    }

    @Bean
    public CompilerHelper compilerHelper() throws IOException {
        return mock(CompilerHelperImpl.class);
    }

    @Bean
    public SlangSourceService slangSourceService() {
        return mock(SlangSourceService.class);
    }
}
