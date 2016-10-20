/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.configuration;



/*
 * Created by orius123 on 05/11/14.
 */

import io.cloudslang.lang.compiler.CachedPrecompileService;
import io.cloudslang.lang.compiler.parser.YamlParser;
import io.cloudslang.lang.entities.encryption.DummyEncryptor;
import io.cloudslang.lang.entities.utils.ApplicationContextProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

@Configuration
@ComponentScan("io.cloudslang.lang.compiler")
public class SlangCompilerSpringConfig {

    @Bean
    @Scope("prototype")
    public Yaml yaml() {
        Yaml yaml = new Yaml();
        yaml.setBeanAccess(BeanAccess.FIELD);
        return yaml;
    }

    @Bean
    public ApplicationContextProvider applicationContextProvider() {
        return new ApplicationContextProvider();
    }

    @Bean
    public DummyEncryptor dummyEncryptor() {
        return new DummyEncryptor();
    }

    @Bean
    public YamlParser yamlParser() {
        return new YamlParser() {
            @Override
            public Yaml getYaml() {
                return yaml();
            }
        };
    }

    @Bean
    public CachedPrecompileService cachedPrecompileService() {
        return new CachedPrecompileService();
    }
}
