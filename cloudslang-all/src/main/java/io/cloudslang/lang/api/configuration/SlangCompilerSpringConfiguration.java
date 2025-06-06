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

import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Conditional(SlangCompilerSpringConfigurationCondition.class)
@Import({SlangCompilerSpringConfig.class})
public class SlangCompilerSpringConfiguration extends AbstractSlangConfiguration {
}
