/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.utils;

import java.io.Serializable;

public class ValidationUtils {

    public static void validateStringValue(String errorMessagePrefix, Serializable value) {
        if (value != null && !(value instanceof String)) {
            throw new RuntimeException(errorMessagePrefix + "' should have a String value, but got value '" + value +
                    "' of type " + value.getClass().getSimpleName() + ".");
        }
    }
}
