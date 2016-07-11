package io.cloudslang.fortest;

import io.cloudslang.lang.entities.bindings.values.SensitiveValue;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.spi.encryption.Encryption;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 10/07/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SensitiveValueTest.SensitiveValueTestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SensitiveValueTest {
    private static final String ENCYPTED = "{Encrypted}";

    @Test
    public void testSensitiveValueEncryptDecrypt() {
        String originalValue = "OriginalSensitiveValue";
        SensitiveValue value = (SensitiveValue) ValueFactory.create(originalValue, true);
        assertEquals("{Encrypted}rO0ABXQAFk9yaWdpbmFsU2Vuc2l0aXZlVmFsdWU=", value.getContent());
        assertEquals(originalValue, value.get());
        value = (SensitiveValue) ValueFactory.create(value.getContent(), true);
        assertEquals("{Encrypted}rO0ABXQAFk9yaWdpbmFsU2Vuc2l0aXZlVmFsdWU=", value.getContent());
        assertEquals(originalValue, value.get());
        assertTrue(value.isSensitive());


        value.encrypt();
        assertEquals("{Encrypted}rO0ABXQAFk9yaWdpbmFsU2Vuc2l0aXZlVmFsdWU=", value.getContent());
        assertEquals(originalValue, value.get());
        assertTrue(value.isSensitive());

        value.decrypt();
        assertEquals(originalValue, value.getContent());
        assertEquals(originalValue, value.get());

        assertTrue(value.isSensitive());

        value.decrypt();
        assertEquals(originalValue, value.getContent());
        assertEquals(originalValue, value.get());

        assertTrue(value.isSensitive());

        value.encrypt();
        assertEquals("{Encrypted}rO0ABXQAFk9yaWdpbmFsU2Vuc2l0aXZlVmFsdWU=", value.getContent());
        assertEquals(originalValue, value.get());

        assertTrue(value.isSensitive());

        value.encrypt();
        assertEquals("{Encrypted}rO0ABXQAFk9yaWdpbmFsU2Vuc2l0aXZlVmFsdWU=", value.getContent());
        assertEquals(originalValue, value.get());
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
                    return ENCYPTED + new String(clearText);
                }

                @Override
                public char[] decrypt(String cypherText) {
                    return cypherText.substring(ENCYPTED.length()).toCharArray();
                }

                @Override
                public boolean isTextEncrypted(String text) {
                    return text.startsWith(ENCYPTED);
                }
            };
        }
    }
}
