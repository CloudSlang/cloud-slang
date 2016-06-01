package io.cloudslang.lang.entities.encryption;

import org.springframework.stereotype.Component;

/**
 * Dummy encryptor
 *
 * Created by Ifat Gavish on 29/05/2016
 */
@Component
public class DummyEncryptor implements Encryption {

    @Override
    public String encrypt(char[] clearText) {
        return new String(clearText);
    }

    @Override
    public char[] decrypt(String cypherText) {
        return cypherText.toCharArray();
    }
}
