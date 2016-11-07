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

import io.cloudslang.lang.spi.encryption.Encryption;

public class DummyEncryptor implements Encryption {

    @Override
    public String encrypt(char[] clearText) {
        return new String(clearText);
    }

    @Override
    public char[] decrypt(String cypherText) {
        return cypherText.toCharArray();
    }

    @Override
    public boolean isTextEncrypted(String text) {
        return false;
    }
}
