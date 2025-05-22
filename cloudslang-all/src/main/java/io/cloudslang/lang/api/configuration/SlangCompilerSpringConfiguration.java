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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Not a @Configuration on purpose to enable only compiler capabilities
 * when declared as a bean
 */
@Import({SlangCompilerSpringConfig.class})
public class SlangCompilerSpringConfiguration {

    @Bean
    public Slang slang() {
        return new SlangImpl();
    }

}
