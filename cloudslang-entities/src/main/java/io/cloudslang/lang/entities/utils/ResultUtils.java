/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities.utils;

import io.cloudslang.lang.entities.bindings.Result;
import java.io.Serializable;
import org.apache.commons.lang3.Validate;

/**
 * @author Bonczidai Levente
 * @since 8/10/2016
 */
public final class ResultUtils {

    private ResultUtils() {
    }

    public static boolean isDefaultResult(Result result) {
        Validate.notNull(result);
        Serializable rawValue = result.getValue() == null ? null : result.getValue().get();
        return rawValue == null || Boolean.TRUE.equals(rawValue);
    }

}
