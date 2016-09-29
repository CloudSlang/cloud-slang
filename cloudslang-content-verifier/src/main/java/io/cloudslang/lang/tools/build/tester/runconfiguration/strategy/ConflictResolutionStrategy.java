package io.cloudslang.lang.tools.build.tester.runconfiguration.strategy;


// @FunctionalInterface
public interface ConflictResolutionStrategy<T> {
    T resolve(T entity1, T entity2);
}
