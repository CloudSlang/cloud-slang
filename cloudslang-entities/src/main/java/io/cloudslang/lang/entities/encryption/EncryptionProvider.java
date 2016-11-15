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
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicReference;

import static configuration.SlangEntitiesSpringConfig.APPLICATION_CONTEXT_BEAN_MISSING;

/**
 * Encryptor factory
 * <p>
 * Created by Ifat Gavish on 30/05/2016
 */
public class EncryptionProvider {

    private static AtomicReference<Encryption> encryptor = new AtomicReference<>();

    public static Encryption get() {
        Encryption encryption = encryptor.get();
        if (encryption == null) {
            encryptor.compareAndSet(null, create());
            encryption = encryptor.get();
        }
        return encryption;
    }

    private static Encryption create() {
        Encryption[] encryptors;
        try {
            encryptors = SlangEntitiesSpringConfig.getEncryptors();
        } catch (NoClassDefFoundError theGivenEx) {
            return new DummyEncryptor(); // IntelliJ Plugin case
        }
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
