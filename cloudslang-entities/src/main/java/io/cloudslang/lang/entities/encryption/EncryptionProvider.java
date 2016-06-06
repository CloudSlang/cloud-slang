package io.cloudslang.lang.entities.encryption;

import io.cloudslang.lang.entities.utils.ApplicationContextProvider;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Encryptor factory
 *
 * Created by Ifat Gavish on 30/05/2016
 */
public class EncryptionProvider {

    private static AtomicReference<Encryption> encryptor = new AtomicReference<>();

    public static Encryption get() {
        Encryption encryption = encryptor.get();
        if (encryption == null) {
            encryptor.set(create());
            encryption = encryptor.get();
        }
        return encryption;
    }

    private static Encryption create() {
        Map<String, Encryption> encryptorMap = ApplicationContextProvider.getApplicationContext().getBeansOfType(Encryption.class);
        Encryption[] encryptors = encryptorMap.values().toArray(new Encryption[encryptorMap.size()]);
        if (encryptors.length == 0) {
            throw new RuntimeException("No encryptors found");
        } else if (encryptors.length == 1) {
            return encryptors[0];
        } else if (encryptors.length == 2) {
            return encryptors[0] instanceof DummyEncryptor ? encryptors[1] : encryptors[0];
        } else {
            throw new RuntimeException("Too many (" + encryptors.length + ") encryptors found");
        }
    }
}
