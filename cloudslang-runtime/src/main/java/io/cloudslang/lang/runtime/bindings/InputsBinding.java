package io.cloudslang.lang.runtime.bindings;

/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.entities.utils.ExpressionUtils;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class InputsBinding {

    @Autowired
    private ScriptEvaluator scriptEvaluator;

    /**
     * Binds the inputs to a new result map
     *
     * @param inputs  : the inputs to bind
     * @param context : initial context
     * @return : a new map with all inputs resolved (does not include initial context)
     */
    public Map<String, Value> bindInputs(List<Input> inputs, Map<String, ? extends Value> context,
                                         Set<SystemProperty> systemProperties) {
        Map<String, Value> resultContext = new HashMap<>();

        //we do not want to change original context map
        Map<String, Value> srcContext = new HashMap<>(context);

        for (Input input : inputs) {
            bindInput(input, srcContext, resultContext, systemProperties);
        }

        return resultContext;
    }

    private void bindInput(Input input, Map<String, ? extends Value> context, Map<String, Value> targetContext,
                           Set<SystemProperty> systemProperties) {
        Value value;

        String inputName = input.getName();
        Validate.notEmpty(inputName);

        try {
            value = resolveValue(input, context, targetContext, systemProperties);
        } catch (Throwable t) {
            throw new RuntimeException("Error binding input: '" + inputName + "', \n\tError is: " + t.getMessage(), t);
        }

        if (input.isRequired() && (value == null || value.get() == null)) {
            String errorMessage = "Input with name: \'" + inputName + "\' is Required, but value is empty";
            throw new RuntimeException(errorMessage);
        }

        targetContext.put(inputName, value);
    }

    private Value resolveValue(Input input, Map<String, ? extends Value> context, Map<String, ? extends Value> targetContext,
                               Set<SystemProperty> systemProperties) {
        Value value = null;

        //we do not want to change original context map
        Map<String, Value> scriptContext = new HashMap<>(context);

        String inputName = input.getName();
        Value valueFromContext = context.get(inputName);
        boolean sensitive = input.getValue() != null && input.getValue().isSensitive() || valueFromContext != null && valueFromContext.isSensitive();
        if (!input.isPrivateInput()) {
            value = ValueFactory.create(valueFromContext, sensitive);
        }

        if (value == null || value.get() == null) {
            Value rawValue = input.getValue();
            String expressionToEvaluate = ExpressionUtils.extractExpression(rawValue == null ? null : rawValue.get());
            if (expressionToEvaluate != null) {
                if (context.containsKey(inputName)) {
                    scriptContext.put(inputName, valueFromContext);
                }
                //so you can resolve previous inputs already bound
                scriptContext.putAll(targetContext);
                value = scriptEvaluator.evalExpr(expressionToEvaluate, scriptContext, systemProperties, input.getFunctionDependencies());
                value = ValueFactory.create(value, sensitive);
            } else {
                value = rawValue;
            }
        }

        return value;
    }

}
