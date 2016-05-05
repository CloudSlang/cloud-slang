package io.cloudslang.lang.entities.bindings.values;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Sensitive InOutParam value
 *
 * Created by Ifat Gavish on 19/04/2016
 */
public class SensitiveValue implements Value {

    private byte[] content;

    public SensitiveValue(Serializable content) {
        this.content = serialize(content);
    }

    @Override
    public Serializable get() {
        return deserialize(content);
    }

    @Override
    public boolean isSensitive() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SensitiveValue that = (SensitiveValue) o;
        return Arrays.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(content);
    }

    @Override
    public String toString() {
        return "********";
    }

    private byte[] serialize(Serializable content) {
        try {
            if (content == null) {
                return null;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(content);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize content", e);
        }
    }
    private Serializable deserialize(byte[] content) {
        try {
            if (content == null) {
                return null;
            }
            ByteArrayInputStream in = new ByteArrayInputStream(content);
            ObjectInputStream is = new ObjectInputStream(in);
            return (Serializable)is.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize content", e);
        }
    }
}
