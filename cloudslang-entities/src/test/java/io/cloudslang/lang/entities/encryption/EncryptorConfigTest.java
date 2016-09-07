package io.cloudslang.lang.entities.encryption;

import io.cloudslang.lang.entities.utils.ApplicationContextProvider;
import io.cloudslang.lang.spi.encryption.Encryption;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Encryptor configuration test
 * <p>
 * Created by Ifat Gavish on 30/05/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = EncryptorConfigTest.Config.class)
public class EncryptorConfigTest {

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
        ((BeanDefinitionRegistry) ApplicationContextProvider.getApplicationContext().
                getAutowireCapableBeanFactory()).removeBeanDefinition("getEncryption");
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
