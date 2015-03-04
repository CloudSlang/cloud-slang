package org.openscore.lang.runtime.bindings;

/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/


import org.openscore.lang.entities.bindings.Input;
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
     * @param inputs : the inputs to bind
     * @param context : initial context
     * @return : a new map with all inputs resolved (does not include initial context)
     */
    public Map<String,Serializable> bindInputs(List<Input> inputs, Map<String, ? extends Serializable> context, Map<String, ? extends Serializable> systemProperties) {
        Map<String,Serializable> resultContext = new HashMap<>();
        Map<String,Serializable> srcContext = new HashMap<>(context); //we do not want to change original context map
        for(Input input : inputs){
            bindInput(input, srcContext, resultContext, systemProperties);
        }
        return resultContext;
    }

    private void bindInput(Input input, Map<String, ? extends Serializable> context, Map<String, Serializable> targetContext, Map<String, ? extends Serializable> systemProperties) {
        String inputName = input.getName();
        Validate.notEmpty(inputName);
        Serializable value;
        try {
            value = resolveValue(input, context, targetContext, systemProperties);
        } catch (Throwable t) {
            throw new RuntimeException("Error binding input: '" + inputName + "', error is: \n" + t.getMessage(), t);
        }


        if(input.isRequired() && value == null) {
            throw new RuntimeException("Input with name: \'"+ inputName + "\' is Required, but value is empty");
        }

        targetContext.put(inputName,value);
    }

    private Serializable resolveValue(Input input, Map<String, ? extends Serializable> context, Map<String, ? extends Serializable> targetContext, Map<String, ? extends Serializable> systemProperties) {
        Serializable value = null;
        String inputName = input.getName();

        Map<String,Serializable> scriptContext = new HashMap<>(context); //we do not want to change original context map

        if(input.isOverridable()) {
            value = context.get(inputName);
            scriptContext.put(inputName, value);
        }

        String fqspn = input.getSystemPropertyName();
        if(value == null && fqspn != null && systemProperties != null) value = systemProperties.get(fqspn);
        if(value == null && StringUtils.isNotEmpty(input.getExpression())){
            scriptContext.putAll(targetContext);//so you can resolve previous inputs already bound
            String expr = input.getExpression();
            value = scriptEvaluator.evalExpr(expr, scriptContext);
        }
        return value;
    }

}
