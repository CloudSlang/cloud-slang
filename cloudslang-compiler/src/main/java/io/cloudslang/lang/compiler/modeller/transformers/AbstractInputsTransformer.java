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

import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.compiler.validator.PreCompileValidator;
import io.cloudslang.lang.entities.bindings.InOutParam;
import io.cloudslang.lang.entities.bindings.Input;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

import static io.cloudslang.lang.compiler.SlangTextualKeys.DEFAULT_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.PRIVATE_INPUT_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.REQUIRED_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SENSITIVE_KEY;

public abstract class AbstractInputsTransformer extends InOutTransformer {

    protected PreCompileValidator preCompileValidator;

    private ExecutableValidator executableValidator;

    @Override
    public Class<? extends InOutParam> getTransformedObjectsClass() {
        return Input.class;
    }

    protected Input transformSingleInput(Object rawInput) {
        // - some_input
        // this is our default behaviour that if the user specifies only a key, the key is also the ref we look for
        if (rawInput instanceof String) {
            String inputName = (String) rawInput;
            return createInput(inputName, null);
        } else if (rawInput instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, ?> map = (Map<String, ?>) rawInput;
            Iterator<? extends Map.Entry<String, ?>> iterator = map.entrySet().iterator();
            Map.Entry<String, ?> entry = iterator.next();
            Serializable entryValue = (Serializable) entry.getValue();
            if (map.size() > 1) {
                throw new RuntimeException("Invalid syntax after input \"" + entry.getKey() + "\". " +
                        "Please check all inputs are provided as a list and each input is preceded by a hyphen. " +
                        "Input \"" + iterator.next().getKey() + "\" is missing the hyphen.");
            }
            if (entryValue == null) {
                throw new RuntimeException("Could not transform Input : " + rawInput +
                        " since it has a null value.\n\n" +
                        "Make sure a value is specified or that indentation is properly done.");
            }
            if (entryValue instanceof Map) {
                // - some_inputs:
                //     property1: value1
                //     property2: value2
                // this is the verbose way of defining inputs with all of the properties available
                //noinspection unchecked
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
        List<String> knownKeys = Arrays.asList(REQUIRED_KEY, SENSITIVE_KEY, PRIVATE_INPUT_KEY, DEFAULT_KEY);

        for (String key : props.keySet()) {
            if (!knownKeys.contains(key)) {
                throw new RuntimeException("key: " + key + " in input: " + entry.getKey() +
                        " is not a known property");
            }
        }

        // default is required=true
        boolean required = !props.containsKey(REQUIRED_KEY) ||
                (boolean) props.get(REQUIRED_KEY);
        // default is sensitive=false
        boolean sensitive = props.containsKey(SENSITIVE_KEY) &&
                (boolean) props.get(SENSITIVE_KEY);
        // default is private=false
        boolean privateInput = props.containsKey(PRIVATE_INPUT_KEY) &&
                (boolean) props.get(PRIVATE_INPUT_KEY);
        boolean defaultKeyFound = props.containsKey(DEFAULT_KEY);
        String inputName = entry.getKey();
        Serializable value = defaultKeyFound ? props.get(DEFAULT_KEY) : null;
        boolean defaultSpecified = defaultKeyFound && value != null && !StringUtils.EMPTY.equals(value);

        if (privateInput && required && !defaultSpecified) {
            throw new RuntimeException(
                    "Input: '" + inputName + "' is private and required but no default value was specified");
        }

        return createInput(inputName, value, sensitive, required, privateInput);
    }

    private Input createInput(
            String name,
            Serializable value) {
        return createInput(name, value, false, true, false);
    }

    private Input createInput(
            String name,
            Serializable value,
            boolean sensitive,
            boolean required,
            boolean privateInput) {
        executableValidator.validateInputName(name);
        preCompileValidator.validateStringValue(name, value, this);
        Accumulator dependencyAccumulator = extractFunctionData(value);
        return new Input.InputBuilder(name, value, sensitive)
                .withRequired(required)
                .withPrivateInput(privateInput)
                .withFunctionDependencies(dependencyAccumulator.getFunctionDependencies())
                .withSystemPropertyDependencies(dependencyAccumulator.getSystemPropertyDependencies())
                .build();
    }

    public void setPreCompileValidator(PreCompileValidator preCompileValidator) {
        this.preCompileValidator = preCompileValidator;
    }

    public void setExecutableValidator(ExecutableValidator executableValidator) {
        this.executableValidator = executableValidator;
    }
}
