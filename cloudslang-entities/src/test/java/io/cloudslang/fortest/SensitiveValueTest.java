/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.fortest;

import configuration.SlangEntitiesSpringConfig;
import io.cloudslang.lang.entities.bindings.values.SensitiveValue;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.spi.encryption.Encryption;

import java.io.Serializable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 10/07/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SensitiveValueTest.SensitiveValueTestConfig.class, SlangEntitiesSpringConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SensitiveValueTest {
    private static final String ENCRYPTED = "{Encrypted}";

    @Test
    public void testSensitiveValueEncryptDecrypt() {
        final String originalValue = "OriginalSensitiveValue";
        final String expectedEncryptedString = "{Encrypted}rO0ABXQAFk9yaWdpbmFsU2Vuc2l0aXZlVmFsdWU=";

        SensitiveValue value = (SensitiveValue) ValueFactory.create(originalValue, true);
        verifyEncrypted(value, originalValue, expectedEncryptedString);

        value.encrypt();
        verifyEncrypted(value, originalValue, expectedEncryptedString);

        value.decrypt();
        verifyDecrypted(value, originalValue);

        value.decrypt();
        verifyDecrypted(value, originalValue);

        value.encrypt();
        verifyEncrypted(value, originalValue, expectedEncryptedString);

        value.encrypt();
        verifyEncrypted(value, originalValue, expectedEncryptedString);
    }

    @Test
    public void testEncryptedStringSensitiveValue() {
        final String originalValue = "foo";
        final String encryptedString = "{Encrypted}foo";

        SensitiveValue value = ValueFactory.createEncryptedString(originalValue, false);
        verifyEncrypted(value, originalValue, encryptedString);

        value.encrypt();
        verifyEncrypted(value, originalValue, encryptedString);

        value.decrypt();
        verifyDecrypted(value, originalValue);

        value.decrypt();
        verifyDecrypted(value, originalValue);

        value.encrypt();
        verifyEncrypted(value, originalValue, encryptedString);

        value.encrypt();
        verifyEncrypted(value, originalValue, encryptedString);
    }

    @Test
    public void testEncryptedStringSensitiveValuePreEncrypted() {
        final String encryptedString = "{Encrypted}bar";
        final String expectedDecryptedValue = "bar";

        SensitiveValue value = ValueFactory.createEncryptedString(encryptedString, true);
        verifyEncrypted(value, expectedDecryptedValue, encryptedString);

        value.encrypt();
        verifyEncrypted(value, expectedDecryptedValue, encryptedString);

        value.decrypt();
        verifyDecrypted(value, expectedDecryptedValue);

        value.decrypt();
        verifyDecrypted(value, expectedDecryptedValue);

        value.encrypt();
        verifyEncrypted(value, expectedDecryptedValue, encryptedString);

        value.encrypt();
        verifyEncrypted(value, expectedDecryptedValue, encryptedString);
    }

    private void verifyEncrypted(SensitiveValue value, Serializable expectedOriginalValue,
                                 String expectedEncryptedString) {

        verifySensitiveValue(value, expectedOriginalValue, expectedEncryptedString);
    }

    private void verifyDecrypted(SensitiveValue value, Serializable expectedOriginalValue) {
        // When the value is decrypted, the inner content should be the toString() result of the original value
        String expectedContent = expectedOriginalValue.toString();
        verifySensitiveValue(value, expectedOriginalValue, expectedContent);
    }

    private void verifySensitiveValue(SensitiveValue value, Serializable expectedOriginalValue,
                                      String expectedContent) {

        assertEquals(expectedContent, value.getContent());
        assertEquals(expectedOriginalValue, value.get());
        assertEquals(SensitiveValue.SENSITIVE_VALUE_MASK, value.toString());
        assertTrue(value.isSensitive());
    }

    @Configuration
    @ComponentScan("io.cloudslang.lang.entities.utils")
    static class SensitiveValueTestConfig {

        @Bean
        public Encryption getTestEncryption() {
            return new Encryption() {

                @Override
                public String encrypt(char[] clearText) {
                    return ENCRYPTED + new String(clearText);
                }

                @Override
                public char[] decrypt(String cypherText) {
                    return cypherText.substring(ENCRYPTED.length()).toCharArray();
                }

                @Override
                public boolean isTextEncrypted(String text) {
                    return text.startsWith(ENCRYPTED);
                }
            };
        }
    }
}
