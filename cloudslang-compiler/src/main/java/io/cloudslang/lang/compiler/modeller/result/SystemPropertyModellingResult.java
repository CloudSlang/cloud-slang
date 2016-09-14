package io.cloudslang.lang.compiler.modeller.result;

import io.cloudslang.lang.entities.SystemProperty;

import java.util.List;
import java.util.Set;

/**
 * Created by bancl on 9/13/2016.
 */
public class SystemPropertyModellingResult implements ModellingResult {

    private final List<RuntimeException> errors;
    private final Set<SystemProperty> systemProperties;

    public SystemPropertyModellingResult(Set<SystemProperty> systemProperties, List<RuntimeException> errors) {
        this.systemProperties = systemProperties;
        this.errors = errors;
    }

    public Set<SystemProperty> getSystemProperties() {
        return systemProperties;
    }

    @Override
    public List<RuntimeException> getErrors() {
        return errors;
    }
}
