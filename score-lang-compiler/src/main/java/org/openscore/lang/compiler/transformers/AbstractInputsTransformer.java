package org.openscore.lang.compiler.transformers;
/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/


import org.openscore.lang.compiler.SlangTextualKeys;
import org.openscore.lang.entities.bindings.Input;

import java.io.Serializable;
import java.util.Map;

public abstract class AbstractInputsTransformer {

    protected Input transformSingleInput(Object rawInput){
        //- some_input
        //this is our default behavior that if the user specifies only a key, the key is also the ref we look for
        if (rawInput instanceof String) {
            return (createRefInput((String) rawInput));
        } else if (rawInput instanceof Map) {
            Map.Entry entry = (Map.Entry) ((Map) rawInput).entrySet().iterator().next();
            // - some_inputs:
            //      property1: value1
            //      property2: value2
            // this is the verbose way of defining inputs with all of the properties available
            if (entry.getValue() instanceof Map) {
                return (createPropInput(entry));
            }
            // - some_input: some_expression
            // the value of the input is an expression we need to evaluate at runtime
            else {
                return (createInlineExpressionInput(entry));
            }
        }
        throw new RuntimeException("Could not transform Input : "+ rawInput);
    }

    private Input createPropInput(Map.Entry<String, Map<String, Serializable>> entry) {
        Map<String, Serializable> prop = entry.getValue();
        boolean required = !prop.containsKey(SlangTextualKeys.REQUIRED_KEY) || ((boolean) prop.get(SlangTextualKeys.REQUIRED_KEY));//default is required=true
        boolean encrypted = prop.containsKey(SlangTextualKeys.ENCRYPTED_KEY) && ((boolean) prop.get(SlangTextualKeys.ENCRYPTED_KEY));
        boolean override = prop.containsKey(SlangTextualKeys.OVERRIDE_KEY) && ((boolean) prop.get(SlangTextualKeys.OVERRIDE_KEY));

        String expressionProp = prop.containsKey(SlangTextualKeys.DEFAULT_KEY) ? (prop.get(SlangTextualKeys.DEFAULT_KEY).toString()) : null;

        return createPropInput(entry.getKey(), required, encrypted, expressionProp, override);
    }

    private Input createPropInput(String inputName, boolean required,boolean encrypted, String expression,
                                  boolean override){
        if(expression == null) {
            expression = inputName ;
        }
        return new Input(inputName, expression, encrypted, required, override);
    }

    private Input createInlineExpressionInput(Map.Entry<String, Object> entry) {
        return createPropInput(entry.getKey() , true, false, entry.getValue().toString(), false);
    }

    private Input createRefInput(String rawInput) {
        return new Input(rawInput, rawInput);
    }
}
