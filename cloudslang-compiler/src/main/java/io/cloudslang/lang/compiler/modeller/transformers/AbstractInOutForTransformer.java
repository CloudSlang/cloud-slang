/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.transformers;

import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.ScriptFunction;
import io.cloudslang.lang.entities.utils.ExpressionUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbstractInOutForTransformer {

    protected Accumulator extractFunctionData(Serializable... values) {
        return extractFunctionData(Collections.emptyList(), values);
    }

    protected Accumulator extractFunctionData(List<Argument> arguments, Serializable... values) {
        final Set<String> systemPropertyDependencies = new HashSet<>();
        final Set<String> variableSystemPropertyDependencies = new HashSet<>();
        final Set<ScriptFunction> functionDependencies = new HashSet<>();

        for (Serializable value : values) {
            String expression = ExpressionUtils.extractExpression(value);
            if (expression != null) {
                Set<String> propertyDependencies = ExpressionUtils.extractSystemProperties(expression);
                if (CollectionUtils.isNotEmpty(propertyDependencies)) {
                    functionDependencies.add(ScriptFunction.GET_SYSTEM_PROPERTY);
                    systemPropertyDependencies.addAll(propertyDependencies);
                }

                boolean getFunctionFound = ExpressionUtils.matchGetFunction(expression);
                if (getFunctionFound) {
                    functionDependencies.add(ScriptFunction.GET);
                }

                boolean getSpVarFunctionFound = ExpressionUtils.matchGetSystemPropertyVariableFunction(expression);
                if (getSpVarFunctionFound) {
                    functionDependencies.add(ScriptFunction.GET_SP_VAR);
                    Set<String> variablePropertyDependencies = ExpressionUtils.extractVariableSystemProperties(
                            expression, arguments);
                    if (CollectionUtils.isNotEmpty(variablePropertyDependencies)) {
                        variableSystemPropertyDependencies.addAll(variablePropertyDependencies);
                    }
                }

                for (ScriptFunction function : ScriptFunction.values()) {
                    if (ExpressionUtils.matchesFunction(function, expression)) {
                        functionDependencies.add(function);
                    }
                }
            }
        }

        return new Accumulator(functionDependencies, systemPropertyDependencies, variableSystemPropertyDependencies);
    }

    protected static class Accumulator {

        private final Set<ScriptFunction> functionDependencies;
        private final Set<String> systemPropertyDependencies;
        private final Set<String> variableSystemPropertyDependencies;

        public Accumulator(Set<ScriptFunction> functionDependencies, Set<String> systemPropertyDependencies) {
            this.functionDependencies = functionDependencies;
            this.systemPropertyDependencies = systemPropertyDependencies;
            this.variableSystemPropertyDependencies = Collections.emptySet();
        }

        public Accumulator(Set<ScriptFunction> functionDependencies, Set<String> systemPropertyDependencies,
                           Set<String> variableSystemPropertyDependencies) {
            this.functionDependencies = functionDependencies;
            this.systemPropertyDependencies = systemPropertyDependencies;
            this.variableSystemPropertyDependencies = variableSystemPropertyDependencies;
        }

        public Set<ScriptFunction> getFunctionDependencies() {
            return functionDependencies;
        }

        public Set<String> getSystemPropertyDependencies() {
            return systemPropertyDependencies;
        }

        public Set<String> getVariableSystemPropertyDependencies() {
            return variableSystemPropertyDependencies;
        }
    }
}
