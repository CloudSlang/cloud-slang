/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.validator;

/**
 * @author Bonczidai Levente
 * @since 8/10/2016
 */
public interface SystemPropertyValidator {
    void validateNamespace(String input);

    void validateKey(String input);
}
