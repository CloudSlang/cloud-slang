/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.result;

import io.cloudslang.lang.entities.SystemProperty;

import java.util.List;
import java.util.Set;

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
