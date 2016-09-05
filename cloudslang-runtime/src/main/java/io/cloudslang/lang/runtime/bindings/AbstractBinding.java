package io.cloudslang.lang.runtime.bindings;

import io.cloudslang.lang.entities.bindings.values.Value;

/**
 * User: bancl
 * Date: 8/12/2016
 */
public class AbstractBinding {
    protected void validateStringValue(String errorMessagePrefix, Value value) {
        if (value != null && value.get() != null && !(value.get() instanceof String)) {
            throw new RuntimeException(errorMessagePrefix + "' should have a String value.");
        }
    }
}
