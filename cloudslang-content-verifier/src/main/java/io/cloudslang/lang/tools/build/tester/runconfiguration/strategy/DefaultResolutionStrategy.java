/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.tester.runconfiguration.strategy;

/**
 * Defines a functional interface for resolving the value of an unknown value object of type T.
 * @param <T> the generic type of the entity T.
 */
// @FunctionInterface
public interface DefaultResolutionStrategy<T> {
    T getDefaultWhenUnspecified();
}
