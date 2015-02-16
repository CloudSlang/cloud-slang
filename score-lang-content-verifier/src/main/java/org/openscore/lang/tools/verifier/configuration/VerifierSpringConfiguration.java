/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.openscore.lang.tools.verifier.configuration;

import org.openscore.lang.compiler.configuration.SlangCompilerSpringConfig;
import org.openscore.lang.tools.verifier.SlangContentVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by stoneo on 2/9/2015.
 */
@Configuration
@Import(SlangCompilerSpringConfig.class)
public class VerifierSpringConfiguration {
    @Bean
    public SlangContentVerifier verifierHelper(){
        return new SlangContentVerifier();
    }
}
