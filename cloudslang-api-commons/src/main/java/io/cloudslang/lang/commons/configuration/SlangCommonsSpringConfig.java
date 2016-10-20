/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.commons.configuration;

import io.cloudslang.lang.api.configuration.SlangSpringConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Bonczidai Levente
 * @since 8/23/2016
 */
@Configuration
@Import({SlangSpringConfiguration.class})
@ComponentScan("io.cloudslang.lang.commons")
public class SlangCommonsSpringConfig {
}
