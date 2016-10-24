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
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Bonczidai Levente
 * @since 1/19/2016
 */
@Component
public class ScriptExecutor extends ScriptProcessor {
    @Autowired
    private PythonRuntimeService pythonRuntimeService;

    public Map<String, Value> executeScript(String script, Map<String, Value> callArguments) {
        return executeScript(Collections.<String>emptySet(), script, callArguments);
    }

    public Map<String, Value> executeScript(Set<String> dependencies, String script, Map<String, Value> callArguments) {
        Map<String, Serializable> executionResult = pythonRuntimeService
                .exec(dependencies, script, createPythonContext(callArguments)).getExecutionResult();
        Map<String, Value> result = new HashMap<>();
        for (Map.Entry<String, Serializable> entry : executionResult.entrySet()) {
            Value callArgumenet = callArguments.get(entry.getKey());
            Value value = ValueFactory.create(entry.getValue(), callArgumenet != null && callArgumenet.isSensitive());
            result.put(entry.getKey(), value);
        }
        return result;
    }
}
