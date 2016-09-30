package io.cloudslang.lang.tools.build.tester.runconfiguration.strategy;

/**
 * Defines a functional interface for resolving a conflict between 2 objects of type T
 * @param <T> the type of the conflicting entity
 */
// @FunctionalInterface
public interface ConflictResolutionStrategy<T> {
    T resolve(T entity1, T entity2);
}
