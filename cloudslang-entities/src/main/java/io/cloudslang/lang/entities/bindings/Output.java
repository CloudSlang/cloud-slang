/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
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
 * @version $Id$
 * @since 11/7/2014
 */
public class Output extends InOutParam {

    private static final long serialVersionUID = -5390581034091916685L;

    public Output(String name, Value value) {
        super(name, value);
    }

    public Output(
            String name,
            Value value,
            Set<ScriptFunction> scriptFunctions,
            Set<String> systemPropertyDependencies) {
        super(name, value, scriptFunctions, systemPropertyDependencies);
    }

    /**
     * only here to satisfy serialization libraries
     */
    @SuppressWarnings("unused")
    private Output() {
    }

}
