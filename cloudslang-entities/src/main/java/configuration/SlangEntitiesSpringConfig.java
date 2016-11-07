/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package configuration;



import io.cloudslang.lang.entities.encryption.DummyEncryptor;
import io.cloudslang.lang.spi.encryption.Encryption;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class SlangEntitiesSpringConfig implements ApplicationContextAware {

    public static final String APPLICATION_CONTEXT_BEAN_MISSING = "Application context bean is missing.";
    private static ApplicationContext applicationContext;

    public static Encryption[] getEncryptors() {
        if (applicationContext != null) {
            Map<String, Encryption> encryptorMap = applicationContext.getBeansOfType(Encryption.class);
            return encryptorMap.values().toArray(new Encryption[encryptorMap.size()]);
        } else {
            throw new RuntimeException(APPLICATION_CONTEXT_BEAN_MISSING);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext localApplicationContext) throws BeansException {
        applicationContext = localApplicationContext;
    }

    @Bean
    public DummyEncryptor dummyEncryptor() {
        return new DummyEncryptor();
    }

}
