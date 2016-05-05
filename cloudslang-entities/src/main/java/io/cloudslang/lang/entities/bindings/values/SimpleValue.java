package io.cloudslang.lang.entities.bindings.values;

import java.io.Serializable;

/**
 * Simple InOutParam value
 *
 * Created by Ifat Gavish on 19/04/2016
 */
public class SimpleValue implements Value {

    private Serializable content;

    public SimpleValue(Serializable content) {
        this.content = content;
    }

    @Override
    public Serializable get() {
        return content;
    }

    @Override
    public boolean isSensitive() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleValue that = (SimpleValue) o;
        return content != null ? content.equals(that.content) : that.content == null;
    }

    @Override
    public int hashCode() {
        return content != null ? content.hashCode() : 0;
    }

    @Override
    public String toString() {
        return content == null ? "null" : content.toString();
    }
}
