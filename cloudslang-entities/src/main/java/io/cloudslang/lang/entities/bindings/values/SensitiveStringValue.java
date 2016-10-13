/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities.bindings.values;

import io.cloudslang.lang.entities.encryption.EncryptionProvider;
import java.io.Serializable;

/**
 * User: eisentha
 * Date: 2016-07-27
 */
public class SensitiveStringValue extends SensitiveValue {

    @SuppressWarnings("unused")
    public SensitiveStringValue() {
    }

    public SensitiveStringValue(String content, boolean preEncrypted) {
        super(content, preEncrypted);
    }

    @Override
    protected String encrypt(Serializable originalContent) {
        String plaintext = (String) originalContent;
        return EncryptionProvider.get().encrypt(plaintext.toCharArray());
    }

    @Override
    protected Serializable decrypt(String content) {
        char[] decrypted = EncryptionProvider.get().decrypt(content);
        return new String(decrypted);
    }
}
