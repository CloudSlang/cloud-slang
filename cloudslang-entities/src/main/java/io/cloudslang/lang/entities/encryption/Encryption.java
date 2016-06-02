package io.cloudslang.lang.entities.encryption;

/**
 * Encryption support
 *
 * Currently OO's Encryptor extends this interface. The correct thing to do is have
 * another interface this one extends for CS, and another extends for AFL
 *
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
}
