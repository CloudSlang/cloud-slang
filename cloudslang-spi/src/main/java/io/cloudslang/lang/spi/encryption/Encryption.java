/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.spi.encryption;

/**
 * Encryption support
 * <p>
 * Created by Ifat Gavish on 29/05/2016
 */
public interface Encryption {

    /**
     * Encrypts a clear text char array
     *
     * @param clearText The char array
     * @return The encrypted string
     */
    String encrypt(char[] clearText);

    /**
     * Decrypts an encrypted string to a clear text char array
     *
     * @param cypherText The encrypted string
     * @return The clear text char array
     */
    char[] decrypt(String cypherText);

    /**
     * Checks whether provided text char array is already encrypted
     *
     * @param text text to check whether it encrypted or not
     * @return true if text is already encrypted
     */
    boolean isTextEncrypted(String text);
}
