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

import java.io.Serializable;
import java.util.*;

import static io.cloudslang.lang.compiler.SlangTextualKeys.DEFAULT_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.ENCRYPTED_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.PRIVATE_INPUT_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.REQUIRED_KEY;

public abstract class AbstractInputsTransformer extends InOutTransformer {

    protected Input transformSingleInput(Object rawInput) {
        // - some_input
        // this is our default behaviour that if the user specifies only a key, the key is also the ref we look for
        if (rawInput instanceof String) {
            String inputName = (String) rawInput;
            return new Input.InputBuilder(inputName, null).build();
        } else if (rawInput instanceof Map) {
            Map.Entry<String, ?> entry = ((Map<String, ?>) rawInput).entrySet().iterator().next();
            Serializable entryValue = (Serializable) entry.getValue();
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
            return createInput(entry.getKey(), entryValue);
        }
        throw new RuntimeException("Could not transform Input : " + rawInput);
    }

    private Input createPropInput(Map.Entry<String, Map<String, Serializable>> entry) {
        Map<String, Serializable> props = entry.getValue();
        List<String> knownKeys = Arrays.asList(REQUIRED_KEY, ENCRYPTED_KEY, PRIVATE_INPUT_KEY, DEFAULT_KEY);

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
        // default is private=false
        boolean privateInput = props.containsKey(PRIVATE_INPUT_KEY) &&
                (boolean) props.get(PRIVATE_INPUT_KEY);
        boolean defaultSpecified = props.containsKey(DEFAULT_KEY);
        String inputName = entry.getKey();
        Serializable value = defaultSpecified ? props.get(DEFAULT_KEY) : null;

        if (privateInput && !defaultSpecified) {
            throw new RuntimeException(
                    "input: " + inputName + " is private but no default value was specified");
        }

        return createInput(inputName, value, encrypted, required, privateInput);
    }

    private Input createInput(
            String name,
            Serializable value) {
        return createInput(name, value, false, true, false);
    }

    private Input createInput(
            String name,
            Serializable value,
            boolean encrypted,
            boolean required,
            boolean privateInput) {
        Accumulator dependencyAccumulator = extractFunctionData(value);
        return new Input.InputBuilder(name, value)
                .withEncrypted(encrypted)
                .withRequired(required)
                .withPrivateInput(privateInput)
                .withFunctionDependencies(dependencyAccumulator.getFunctionDependencies())
                .withSystemPropertyDependencies(dependencyAccumulator.getSystemPropertyDependencies())
                .build();
    }

}
