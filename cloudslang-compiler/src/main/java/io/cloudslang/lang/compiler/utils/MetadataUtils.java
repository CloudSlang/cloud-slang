/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.utils;

/**
 * @author Bonczidai Levente
 * @since 3/24/2017
 */
public abstract class MetadataUtils {
    public static String generateErrorMessage(int lineNumberZeroBased, String message) {
        return "Error at line [" + (lineNumberZeroBased + 1) + "] - " + message;
    }
}
