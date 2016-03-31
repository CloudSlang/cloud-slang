/**
 * ****************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 * <p/>
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * *****************************************************************************
 */
package io.cloudslang.lang.compiler.modeller.transformers;

import io.cloudslang.lang.entities.bindings.ScriptFunction;
import io.cloudslang.lang.entities.utils.ExpressionUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Bonczidai Levente
 * @since 1/25/2016
 */
public abstract class InOutTransformer {

    protected Accumulator extractFunctionData(Serializable value) {
        String expression = ExpressionUtils.extractExpression(value);
        Set<String> systemPropertyDependencies = new HashSet<>();
        Set<ScriptFunction> functionDependencies = new HashSet<>();
        if (expression != null) {
            systemPropertyDependencies = ExpressionUtils.extractSystemProperties(expression);
            if (CollectionUtils.isNotEmpty(systemPropertyDependencies)) {
                functionDependencies.add(ScriptFunction.GET_SYSTEM_PROPERTY);
            }
            boolean getFunctionFound = ExpressionUtils.matchGetFunction(expression);
            if (getFunctionFound) {
                functionDependencies.add(ScriptFunction.GET);
            }
            boolean getExpressionFunctionFound = ExpressionUtils.matchGetExprFunction(expression);
            if (getExpressionFunctionFound) {
                functionDependencies.add(ScriptFunction.GET_EXPRESSION);
            }
        }
        return new Accumulator(functionDependencies, systemPropertyDependencies);
    }

    protected static class Accumulator {

        private final Set<ScriptFunction> functionDependencies;
        private final Set<String> systemPropertyDependencies;

        public Accumulator(Set<ScriptFunction> functionDependencies, Set<String> systemPropertyDependencies) {
            this.functionDependencies = functionDependencies;
            this.systemPropertyDependencies = systemPropertyDependencies;
        }

        public Set<ScriptFunction> getFunctionDependencies() {
            return functionDependencies;
        }

        public Set<String> getSystemPropertyDependencies() {
            return systemPropertyDependencies;
        }

    }

}
