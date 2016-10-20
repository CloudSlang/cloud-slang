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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.cloudslang.lang.entities.encryption.EncryptionProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javassist.util.proxy.ProxyObjectInputStream;
import javassist.util.proxy.ProxyObjectOutputStream;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/**
 * Sensitive InOutParam value
 * <p>
 * Created by Ifat Gavish on 19/04/2016
 */
public class SensitiveValue implements Value {

    public static final String SENSITIVE_VALUE_MASK = "********";

    private String content = null;

    /**
     * This variable only used when passing sensitive data between application components which use
     * different encryption key
     * Json serialization in database should not deal with it
     */
    @JsonIgnore
    private Serializable originalContent = null;

    @SuppressWarnings("unused")
    protected SensitiveValue() {
    }

    protected SensitiveValue(Serializable content) {
        originalContent = content;
        encrypt();
    }

    protected SensitiveValue(String content, boolean preEncrypted) {
        if (preEncrypted) {
            this.content = content;
        } else {
            originalContent = content;
            encrypt();
        }
    }

    public void encrypt() {
        if (originalContent != null) {
            content = encrypt(originalContent);
            originalContent = null;
        }
    }

    protected String encrypt(Serializable originalContent) {
        byte[] serialized = serialize(originalContent);
        String serializedAsString = Base64.encodeBase64String(serialized);
        return EncryptionProvider.get().encrypt(serializedAsString.toCharArray());
    }

    public void decrypt() {
        if (content != null) {
            originalContent = decrypt(content);
            content = null;
        }
    }

    protected Serializable decrypt(String content) {
        char[] decrypted = EncryptionProvider.get().decrypt(content);
        String serializedAsString = new String(decrypted);

        byte[] serialized = Base64.decodeBase64(serializedAsString);
        return deserialize(serialized);
    }

    public String getContent() {
        return (content != null) ? content : ((originalContent != null) ? originalContent.toString() : null);
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public Serializable get() {
        return (originalContent != null) ? originalContent : ((content == null) ? null : decrypt(content));
    }

    @JsonIgnore
    @Override
    public boolean isSensitive() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SensitiveValue that = (SensitiveValue) o;

        if (content != null ? !content.equals(that.content) : that.content != null) {
            return false;
        }
        return originalContent != null ? originalContent.equals(that.originalContent) : that.originalContent == null;

    }

    @Override
    public int hashCode() {
        int result = content != null ? content.hashCode() : 0;
        result = 31 * result + (originalContent != null ? originalContent.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return SENSITIVE_VALUE_MASK;
    }

    private byte[] serialize(Serializable data) {
        ObjectOutputStream oos = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = new ProxyObjectOutputStream(baos);
            oos.writeObject(data);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object", e);
        } finally {
            if (oos != null) {
                IOUtils.closeQuietly(oos);
            }
        }
    }

    private Serializable deserialize(byte[] data) {
        ObjectInputStream ois = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ois = new ProxyObjectInputStream(bais);
            return (Serializable) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize object", e);
        } finally {
            if (ois != null) {
                IOUtils.closeQuietly(ois);
            }
        }
    }
}
