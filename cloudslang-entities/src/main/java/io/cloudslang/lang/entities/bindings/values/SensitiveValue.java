package io.cloudslang.lang.entities.bindings.values;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang.SerializationUtils;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Sensitive InOutParam value
 *
 * Created by Ifat Gavish on 19/04/2016
 */
public class SensitiveValue implements Value {

    private byte[] content;

    @SuppressWarnings("unused")
    public SensitiveValue() {
    }

    public SensitiveValue(Serializable content) {
        this.content = SerializationUtils.serialize(content);
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public Serializable get() {
        return (Serializable)SerializationUtils.deserialize(content);
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
}
