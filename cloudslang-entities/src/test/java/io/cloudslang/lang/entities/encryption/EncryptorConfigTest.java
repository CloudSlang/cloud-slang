/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities.encryption;

import configuration.SlangEntitiesSpringConfig;
import io.cloudslang.lang.spi.encryption.Encryption;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {EncryptorConfigTest.Config.class, SlangEntitiesSpringConfig.class})
public class EncryptorConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Before
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
    public void cleanup() throws Exception {
        Field field = EncryptionProvider.class.getDeclaredField("encryptor");
        field.setAccessible(true);
        field.set(null, new AtomicReference<>());
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
    public void testEncryptionWithEncryptorImplementation() {
        String text = "Str1";
        Encryption encryptor = EncryptionProvider.get();
        assertFalse(encryptor instanceof DummyEncryptor);
        assertEquals(encryptor.encrypt(text.toCharArray()), "Encrypted");
        assertTrue(Arrays.equals(encryptor.decrypt(text), "Decrypted".toCharArray()));
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
    public void testEncryptionWithoutEncryptorImplementation() {
        String text = "Str1";
        ((BeanDefinitionRegistry) applicationContext.getAutowireCapableBeanFactory())
                .removeBeanDefinition("getEncryption");

        Encryption encryptor = EncryptionProvider.get();
        assertTrue(encryptor instanceof DummyEncryptor);
        assertEquals(encryptor.encrypt(text.toCharArray()), text);
        assertTrue(Arrays.equals(encryptor.decrypt(text), text.toCharArray()));
    }

    @ComponentScan("io.cloudslang.lang.entities")
    static class Config {

        @Bean
        public Encryption getEncryption() {
            return new Encryption() {
                @Override
                public String encrypt(char[] clearText) {
                    return "Encrypted";
                }

                @Override
                public char[] decrypt(String cypherText) {
                    return "Decrypted".toCharArray();
                }

                @Override
                public boolean isTextEncrypted(String text) {
                    return false;
                }
            };
        }
    }
}
