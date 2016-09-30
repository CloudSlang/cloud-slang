package io.cloudslang.lang.tools.build.tester.runconfiguration.strategy;

/**
 * Defines a functional interface for resolving an the value of an unknown value object of type T.
 * @param <T> the generic type of the entity T.
 */
// @FunctionInterface
public interface DefaultResolutionStrategy<T> {
    T getDefaultWhenUnspecified();
}
