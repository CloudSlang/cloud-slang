/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.api.configuration;

import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.api.SlangImpl;
import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.runtime.configuration.SlangRuntimeSpringConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * User: stoneo
 * Date: 03/12/2014
 * Time: 10:39
 */
@Configuration
@Import({SlangRuntimeSpringConfig.class, SlangCompilerSpringConfig.class})
public class SlangSpringConfiguration {

    @Bean
    public Slang slang() {
        return new SlangImpl();
    }

}
