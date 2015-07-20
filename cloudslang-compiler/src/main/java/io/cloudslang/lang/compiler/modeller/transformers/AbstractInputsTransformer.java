/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.compiler.modeller.transformers;

import io.cloudslang.lang.entities.bindings.Input;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.cloudslang.lang.compiler.SlangTextualKeys.DEFAULT_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.ENCRYPTED_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.OVERRIDABLE_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.REQUIRED_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SYSTEM_PROPERTY_KEY;

public abstract class AbstractInputsTransformer {

    protected Input transformSingleInput(Object rawInput) {
        // - some_input
        // this is our default behaviour that if the user specifies only a key, the key is also the ref we look for
        if (rawInput instanceof String) {
            String inputName = (String) rawInput;
            return new Input(inputName, null);
        } else if (rawInput instanceof Map) {
            Map.Entry<String, ?> entry = ((Map<String, ?>) rawInput).entrySet().iterator().next();
            Object entryValue = entry.getValue();
            if(entryValue == null){
                throw new RuntimeException("Could not transform Input : " + rawInput + " Since it has a null value.\n\nMake sure a value is specified or that indentation is properly done.");
            }
            if (entryValue instanceof Map) {
                // - some_inputs:
                // property1: value1
                // property2: value2
                // this is the verbose way of defining inputs with all of the properties available
                return createPropInput((Map.Entry<String, Map<String, Serializable>>) entry);
            }
            // - some_input: some_expression
            // the value of the input is an expression we need to evaluate at runtime
            return new Input(entry.getKey(), entryValue
                                                  .toString());
        }
        throw new RuntimeException("Could not transform Input : " + rawInput);
    }

    private static Input createPropInput(Map.Entry<String, Map<String, Serializable>> entry) {
        Map<String, Serializable> props = entry.getValue();
        List<String> knownKeys = Arrays.asList(REQUIRED_KEY, ENCRYPTED_KEY,
                OVERRIDABLE_KEY, DEFAULT_KEY, SYSTEM_PROPERTY_KEY);

        for (String key : props.keySet()) {
            if (!knownKeys.contains(key)) {
                throw new RuntimeException("key: " + key + " in input: " + entry.getKey() + " is not a known property");
            }
        }

        // default is required=true
        boolean required = !props.containsKey(REQUIRED_KEY) ||
                (boolean) props.get(REQUIRED_KEY);
        // default is encrypted=false
        boolean encrypted = props.containsKey(ENCRYPTED_KEY) &&
                (boolean) props.get(ENCRYPTED_KEY);
        // default is overridable=true
        boolean overridable = !props.containsKey(OVERRIDABLE_KEY) ||
                (boolean) props.get(OVERRIDABLE_KEY);
        boolean defaultSpecified = props.containsKey(DEFAULT_KEY);
        String inputName = entry.getKey();
        String expression = defaultSpecified ? props.get(DEFAULT_KEY).toString() : null;
        String systemPropertyName = (String) props.get(SYSTEM_PROPERTY_KEY);

        if (!overridable && !defaultSpecified && StringUtils.isEmpty(systemPropertyName)) {
            throw new RuntimeException(
                    "input: " + inputName + " is not overridable but no default value or system property was specified");
        }

        return new Input(inputName, expression, encrypted, required, overridable, systemPropertyName);
    }
}
