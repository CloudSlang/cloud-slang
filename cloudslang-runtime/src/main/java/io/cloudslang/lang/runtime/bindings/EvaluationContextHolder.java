/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.bindings;

import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.ScriptFunction;
import io.cloudslang.lang.entities.bindings.values.Value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//param object for expression evaluation
final class EvaluationContextHolder {
    private final Map<String, ? extends Value> srcContext;
    private final Map<String, ? extends Value> targetContext;
    private final Set<SystemProperty> systemProperties;
    private final Value inputValue;
    private final String inputName;
    private final Set<ScriptFunction> scriptFunctions;

    public EvaluationContextHolder(Map<String, ? extends Value> srcContext,
                                   Map<String, ? extends Value> targetContext,
                                   Set<SystemProperty> systemProperties,
                                   Value inputValue,
                                   String inputName,
                                   Set<ScriptFunction> scriptFunctions) {
        this.srcContext = srcContext;
        this.targetContext = targetContext;
        this.systemProperties = systemProperties;
        this.inputValue = inputValue;
        this.inputName = inputName;
        this.scriptFunctions = scriptFunctions;
    }

    public Set<SystemProperty> getSystemProperties() {
        return new HashSet<>(systemProperties);
    }


    public Set<ScriptFunction> getFunctionDependencies() {
        return new HashSet<>(scriptFunctions);
    }

    public Map<String, Value> createEvaluationContext() {
        Map<String, Value> evaluationContext = new HashMap<>(srcContext);
        evaluationContext.put(inputName, inputValue);
        //so you can resolve previous arguments already bound
        evaluationContext.putAll(targetContext);
        return evaluationContext;
    }

    public EvaluationContextHolder overrideInputValue(Value value) {
        return new EvaluationContextHolder(srcContext,
                targetContext,
                systemProperties,
                value,
                inputName,
                scriptFunctions);
    }
}
