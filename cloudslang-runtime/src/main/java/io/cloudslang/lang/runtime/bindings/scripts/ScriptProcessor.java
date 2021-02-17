/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.bindings.scripts;

import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class ScriptProcessor {

    public Map<String, Serializable> createJythonContext(Map<String, Value> context) {
        Map<String, Serializable> jythonContext = new HashMap<>();
        for (Map.Entry<String, ? extends Value> entry : context.entrySet()) {
            jythonContext.put(entry.getKey(), ValueFactory.createPyObjectValueForJython(entry.getValue()));
        }
        return jythonContext;
    }

    public Map<String, Serializable> createExternalPythonContext(Map<String, Value> context) {
        Map<String, Serializable> pythonContext = new HashMap<>();
        for (Map.Entry<String, ? extends Value> entry : context.entrySet()) {
            pythonContext.put(entry.getKey(), ValueFactory.createPyObjectValueForExternalPython(entry.getValue()));
        }
        return pythonContext;
    }
}
