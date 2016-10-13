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

/**
 * Script processor
 * <p>
 * Created by Ifat Gavish on 25/05/2016
 */
public abstract class ScriptProcessor {

    protected Map<String, Serializable> createPythonContext(Map<String, Value> context) {
        Map<String, Serializable> pythonContext = new HashMap<>();
        for (Map.Entry<String, ? extends Value> entry : context.entrySet()) {
            pythonContext.put(entry.getKey(), ValueFactory.createPyObjectValue(entry.getValue()));
        }
        return pythonContext;
    }
}
