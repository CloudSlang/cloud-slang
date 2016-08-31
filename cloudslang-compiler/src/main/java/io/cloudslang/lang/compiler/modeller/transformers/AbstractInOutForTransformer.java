package io.cloudslang.lang.compiler.modeller.transformers;

import io.cloudslang.lang.entities.bindings.ScriptFunction;
import io.cloudslang.lang.entities.utils.ExpressionUtils;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;

/**
 * User: bancl
 * Date: 8/11/2016
 */
public class AbstractInOutForTransformer {

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
            boolean checkEmptyFunctionFound = ExpressionUtils.matchCheckEmptyFunction(expression);
            if (checkEmptyFunctionFound) {
                functionDependencies.add(ScriptFunction.CHECK_EMPTY);
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
