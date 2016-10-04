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
 * Defines a functional interface for resolving a conflict between 2 objects of type T
 * @param <T> the type of the conflicting entity
 */
// @FunctionalInterface
public interface ConflictResolutionStrategy<T> {
    T resolve(T entity1, T entity2);
}
