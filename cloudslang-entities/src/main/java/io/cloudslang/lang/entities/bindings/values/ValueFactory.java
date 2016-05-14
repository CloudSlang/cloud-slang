package io.cloudslang.lang.entities.bindings.values;

import java.io.Serializable;

/**
 * InOutParam value factory
 *
 * Created by Ifat Gavish on 19/04/2016
 */
public abstract class ValueFactory implements Serializable {

    public static Value create(Serializable content) {
        return create(content, false);
    }

    public static Value create(Serializable serializable, boolean sensitive) {
        return serializable != null && serializable instanceof Value ?
                ValueFactory.createValue(((Value)serializable).get(), ((Value)serializable).isSensitive() || sensitive) :
                ValueFactory.createValue(serializable, sensitive);
    }

    public static PyObjectValue createPyObjectValue(Serializable content, boolean sensitive) {
        return PyObjectValueProxyFactory.create(content, sensitive);
    }

    public static PyObjectValue createPyObjectValue(Value value) {
        return createPyObjectValue(value == null ? null : value.get(), value != null && value.isSensitive());
    }

    private static Value createValue(Serializable content, boolean sensitive) {
        return sensitive ? new SensitiveValue(content) : new SimpleValue(content);
    }
}
