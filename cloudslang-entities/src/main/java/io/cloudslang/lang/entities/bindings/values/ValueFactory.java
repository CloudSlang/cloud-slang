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

import java.io.Serializable;

/**
 * InOutParam value factory
 * <p>
 * Created by Ifat Gavish on 19/04/2016
 */
public abstract class ValueFactory implements Serializable {

    public static Value create(Serializable content) {
        return create(content, SensitiveDataLevel.NONE);
    }

    public static Value create(Serializable serializable, SensitiveDataLevel sensitiveDataLevel) {
        return serializable != null && serializable instanceof Value ?
                ValueFactory.createValue(((Value) serializable).get(),
                        ((Value) serializable).isSensitive() ? SensitiveDataLevel.ENCRYPTED : sensitiveDataLevel) :
                ValueFactory.createValue(serializable, sensitiveDataLevel);
    }

    public static SensitiveStringValue createEncryptedString(String value) {
        return new SensitiveStringValue(value, false);
    }

    public static SensitiveStringValue createEncryptedString(String value, boolean preEncrypted) {
        return new SensitiveStringValue(value, preEncrypted);
    }

    public static PyObjectValue createPyObjectValue(Serializable content, boolean sensitive) {
        return PyObjectValueProxyFactory.create(content, sensitive);
    }

    public static PyObjectValue createPyObjectValue(Value value) {
        return createPyObjectValue(value == null ? null : value.get(), value != null && value.isSensitive());
    }

    private static Value createValue(Serializable content, SensitiveDataLevel sensitiveDataLevel) {
        return SensitiveDataLevel.NONE.equals(sensitiveDataLevel) ?
                new SimpleValue(content) : new SensitiveValue(content, sensitiveDataLevel);
    }
}
