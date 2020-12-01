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

import io.cloudslang.lang.compiler.utils.ExternalPythonScriptUtils;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;

@Component
public class ScriptExecutor extends ScriptProcessor {

    @Resource(name = "jythonRuntimeService")
    private PythonRuntimeService jythonRuntimeService;

    @Resource(name = "externalPythonRuntimeService")
    private PythonRuntimeService pythonRuntimeService;

    public Map<String, Value> executeScript(String script, Map<String, Value> callArguments, boolean useJython) {
        return executeScript(emptySet(), script, callArguments, useJython);
    }

    public Map<String, Value> executeScript(Set<String> dependencies, String script, Map<String, Value> callArguments,
                                            boolean useJython) {
        if (useJython) {
            return runJythonAction(dependencies, script, callArguments);
        } else {
            return runExternalPythonAction(dependencies, script, callArguments);
        }
    }

    private Map<String, Value> runExternalPythonAction(Set<String> dependencies, String script,
                                                       Map<String, Value> callArguments) {

        final String[] scriptParams = ExternalPythonScriptUtils.getScriptParams(script);
        final Map<String, Value> neededArguments = new HashMap<>();
        for (String scriptParam : scriptParams) {
            if (callArguments.containsKey(scriptParam)) {
                neededArguments.put(scriptParam, callArguments.get(scriptParam));
            }
        }

        Map<String, Serializable> executionResult = pythonRuntimeService.exec(dependencies, script,
                createPythonContext(neededArguments, true)).getExecutionResult();
        Map<String, Value> result = new HashMap<>();
        for (Map.Entry<String, Serializable> entry : executionResult.entrySet()) {
            Value inputValue = callArguments.get(entry.getKey());
            boolean isSensitive = (inputValue != null) && inputValue.isSensitive();
            result.put(entry.getKey(), ValueFactory.create(entry.getValue(), isSensitive));
        }
        return result;
    }

    private Map<String, Value> runJythonAction(Set<String> dependencies, String script,
                                               Map<String, Value> callArguments) {

        Map<String, Value> result = new HashMap<>();
        Map<String, Serializable> executionResult = jythonRuntimeService
                .exec(dependencies, script, createPythonContext(callArguments, false)).getExecutionResult();

        for (Map.Entry<String, Serializable> entry : executionResult.entrySet()) {
            Value callArgumenet = callArguments.get(entry.getKey());
            Serializable theValue;
            if (entry.getValue() instanceof PyList) {
                ArrayList<String> localList = new ArrayList<>();
                PyList pyList = (PyList) entry.getValue();
                for (Object next : pyList) {
                    localList.add(next == null ? null : next.toString());
                }
                theValue = localList;
            } else if (entry.getValue() instanceof PyObject) {
                theValue = entry.getValue().toString();
            } else {
                theValue = entry.getValue();
            }
            Value value = ValueFactory.create(theValue, callArgumenet != null && callArgumenet.isSensitive());
            result.put(entry.getKey(), value);
        }
        return result;
    }
}
