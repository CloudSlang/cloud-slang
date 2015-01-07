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

import org.apache.log4j.Logger;
import org.openscore.lang.entities.bindings.Input;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.openscore.lang.compiler.SlangTextualKeys.DEFAULT_KEY;
import static org.openscore.lang.compiler.SlangTextualKeys.ENCRYPTED_KEY;
import static org.openscore.lang.compiler.SlangTextualKeys.OVERRIDE_KEY;
import static org.openscore.lang.compiler.SlangTextualKeys.REQUIRED_KEY;

public abstract class AbstractInputsTransformer {

    private final Logger logger = Logger.getLogger(AbstractInputsTransformer.class);

    protected Input transformSingleInput(Object rawInput) {
        /*
            this is our default behavior that if the user specifies only a key, the key is also the ref we look for
            - some_input
        */
        if (rawInput instanceof String) {
            return (createRefInput((String) rawInput));
        } else if (rawInput instanceof Map) {
            Map.Entry entry = (Map.Entry) ((Map) rawInput).entrySet().iterator().next();
            /*
                this is the verbose way of defining inputs with all of the properties available
                - some_inputs:
                     property1: value1
                     property2: value2
            */
            if (entry.getValue() instanceof Map) {
                return (createPropInput(entry));
            }
            /*
                the value of the input is an expression we need to evaluate at runtime
                - some_input: some_expression
            */
            else {
                return (createInlineExpressionInput(entry));
            }
        }
        throw new RuntimeException("Could not transform Input : " + rawInput);
    }

    private Input createPropInput(Map.Entry<String, Map<String, Serializable>> entry) {
        Map<String, Serializable> prop = entry.getValue();
        boolean required = !prop.containsKey(REQUIRED_KEY) || ((boolean) prop.get(REQUIRED_KEY)); // default is required=true
        boolean encrypted = prop.containsKey(ENCRYPTED_KEY) && ((boolean) prop.get(ENCRYPTED_KEY));
        boolean override = prop.containsKey(OVERRIDE_KEY) && ((boolean) prop.get(OVERRIDE_KEY));

        String expressionProp = prop.containsKey(DEFAULT_KEY) ? prop.get(DEFAULT_KEY).toString() : null;

        List<String> knownKeys = Arrays.asList(REQUIRED_KEY, ENCRYPTED_KEY, OVERRIDE_KEY, DEFAULT_KEY);

        for (String key : prop.keySet()){
            if (!knownKeys.contains(key)) {
                logger.warn("key: " + key + " in input: " + entry.getKey() + " is not a known property");
            }
        }

        return createPropInput(entry.getKey(), required, encrypted, expressionProp, override);
    }

    private Input createPropInput(String inputName, boolean required, boolean encrypted, String expression,
                                  boolean override
    ) {
        if (expression == null) {
            expression = inputName;
        }
        return new Input(inputName, expression, encrypted, required, override);
    }

    private Input createInlineExpressionInput(Map.Entry<String, Object> entry) {
        return createPropInput(entry.getKey(), true, false, entry.getValue().toString(), false);
    }

    private Input createRefInput(String rawInput) {
        return new Input(rawInput, rawInput);
    }
}
