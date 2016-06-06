/**
 * ****************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 * <p/>
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * *****************************************************************************
 */
package io.cloudslang.lang.entities.bindings.values;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.cloudslang.lang.entities.encryption.EncryptionProvider;
import javassist.util.proxy.ProxyObjectInputStream;
import javassist.util.proxy.ProxyObjectOutputStream;
import org.python.apache.commons.compress.utils.IOUtils;
import org.python.apache.xerces.impl.dv.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Sensitive InOutParam value
 *
 * Created by Ifat Gavish on 19/04/2016
 */
public class SensitiveValue implements Value {

    public static final String SENSITIVE_VALUE_MASK = "********";

    private String content;

    @SuppressWarnings("unused")
    protected SensitiveValue() {
    }

    protected SensitiveValue(Serializable content) {
        byte[] serialized = serialize(content);
        String encoded = Base64.encode(serialized);
        this.content = EncryptionProvider.get().encrypt(encoded.toCharArray());
    }

    public String getContent() {
        return content;
    }

    protected void setContent(String content) {
        this.content = content;
    }

    @Override
    public Serializable get() {
        char[] decrypted = EncryptionProvider.get().decrypt(content);
        byte[] decoded = Base64.decode(new String(decrypted));
        return deserialize(decoded);
    }

    @JsonIgnore
    @Override
    public boolean isSensitive() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SensitiveValue that = (SensitiveValue) o;
        return content != null ? content.equals(that.content) : that.content == null;
    }

    @Override
    public int hashCode() {
        return content != null ? content.hashCode() : 0;
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
            return (Serializable)ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize object", e);
        } finally {
            if (ois != null) {
                IOUtils.closeQuietly(ois);
            }
        }
    }
}
