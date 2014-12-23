/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.lang.cli.configuration;

import org.openscore.lang.cli.services.ScoreServices;
import org.openscore.lang.cli.services.ScoreServicesImpl;
import org.openscore.lang.cli.utils.CompilerHelper;
import org.openscore.lang.cli.utils.CompilerHelperImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

import static org.mockito.Mockito.mock;

/**
 * Date: 12/9/2014
 *
 * @author Bonczidai Levente
 */
@Configuration
@ComponentScan( "org.openscore.lang.cli" )
public class SlangCLITestConfig {

    @Bean
    public ScoreServices scoreServices() {
        return mock(ScoreServicesImpl.class);
    }

    @Bean
    public CompilerHelper compilerHelper() throws IOException {
        return mock(CompilerHelperImpl.class);
    }

}
