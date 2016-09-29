package io.cloudslang.lang.tools.build.tester.runconfiguration.strategy;


// @FunctionInterface
public interface DefaultResolutionStrategy<T> {
    T getDefaultWhenUnspecified();
}
