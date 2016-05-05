/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities.bindings;

import io.cloudslang.lang.entities.bindings.values.Value;

import java.util.Set;

/**
 * @author Bonczidai Levente
 * @since 8/14/2015
 */
public class Argument extends InOutParam {

    private boolean overridable;

    public Argument(String name, Value value) {
        super(name, value);
        overridable = false;
    }

    public Argument(
            String name,
            Value value,
            Set<ScriptFunction> scriptFunctions,
            Set<String> systemPropertyDependencies) {
        super(name, value, scriptFunctions, systemPropertyDependencies);
        overridable = false;
    }

    public Argument(String name) {
        super(name, null);
        overridable = true;
    }

    /**
     * only here to satisfy serialization libraries
     */
    @SuppressWarnings("unused")
    private Argument() {
    }

    public boolean isOverridable() {
        return overridable;
    }

}
