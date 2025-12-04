/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities;

import io.cloudslang.lang.entities.encryption.EncryptionProvider;
import org.junit.Before;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Base class for tests that depend on EncryptionProvider and Spring context. 
 * Clears the EncryptionProvider static cache and injects the test's ApplicationContext 
 * into SlangEntitiesSpringConfig before each test to prevent order-dependent failures.
 */
public abstract class SpringEncryptionTestBase {

    @Autowired
    private ApplicationContext applicationContext;

    @Before
    public void clearEncryptionProviderCache() throws Exception {
        Field field = EncryptionProvider.class.getDeclaredField("encryptor");
        field.setAccessible(true);
        field.set(null, new AtomicReference<>());
    }

    @Before
    public void setUpApplicationContext() throws Exception {
        Field field = Class.forName("configuration.SlangEntitiesSpringConfig")
                .getDeclaredField("applicationContext");
        field.setAccessible(true);
        field.set(null, applicationContext);
    }
}
