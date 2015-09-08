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

import io.cloudslang.lang.entities.bindings.Input;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Map<String, Serializable> bindInputs(List<Input> inputs, Map<String, ? extends Serializable> context,
                                                Map<String, ? extends Serializable> systemProperties) {
        Map<String, Serializable> resultContext = new HashMap<>();

        //we do not want to change original context map
        Map<String, Serializable> srcContext = new HashMap<>(context);

        for (Input input : inputs) {
            bindInput(input, srcContext, resultContext, systemProperties);
        }

        return resultContext;
    }

    private void bindInput(Input input, Map<String, ? extends Serializable> context,
                           Map<String, Serializable> targetContext,
                           Map<String, ? extends Serializable> systemProperties) {

        Serializable value;

        String inputName = input.getName();
        Validate.notEmpty(inputName);

        try {
            value = resolveValue(input, context, targetContext, systemProperties);
        } catch (Throwable t) {
            throw new RuntimeException("Error binding input: '" + inputName + "', \n\tError is: " + t.getMessage(), t);
        }

        if (input.isRequired() && value == null) {
            String errorMessage = "Input with name: \'" + inputName + "\' is Required, but value is empty";
            if (input.getSystemPropertyName() != null){
                errorMessage += "\nThis value can also be supplied using a system property";
            }
            throw new RuntimeException(errorMessage);
        }

        targetContext.put(inputName, value);
    }

    private Serializable resolveValue(
            Input input,
            Map<String, ? extends Serializable> context,
            Map<String, ? extends Serializable> targetContext,
            Map<String, ? extends Serializable> systemProperties) {
        Serializable value = null;

        //we do not want to change original context map
        Map<String, Serializable> scriptContext = new HashMap<>(context);

        String inputName = input.getName();
        Serializable valueFromContext = context.get(inputName);
        if (input.isOverridable()) {
            value = valueFromContext;
        }

        if (value == null) {
            String systemPropertyKey = input.getSystemPropertyName();
            if (StringUtils.isNotEmpty(systemPropertyKey) && MapUtils.isNotEmpty(systemProperties)) {
                value = systemProperties.get(systemPropertyKey);
            }
        }

        if (value == null) {
            scriptContext.put(inputName, valueFromContext);
            if (StringUtils.isNotEmpty(input.getExpression())) {
                //so you can resolve previous inputs already bound
                scriptContext.putAll(targetContext);
                String expr = input.getExpression();
                value = scriptEvaluator.evalExpr(expr, scriptContext);
            }
        }

        return value;
    }

}
