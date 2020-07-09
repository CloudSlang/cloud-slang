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

import io.cloudslang.lang.entities.PromptType;
import io.cloudslang.lang.entities.SensitivityLevel;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.compiler.validator.PreCompileValidator;
import io.cloudslang.lang.entities.bindings.InOutParam;
import io.cloudslang.lang.entities.bindings.Input;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;


import static com.google.common.collect.Sets.newHashSet;
import static io.cloudslang.lang.compiler.SlangTextualKeys.DEFAULT_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.PRIVATE_INPUT_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.PROMPT_DELIMITER_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.PROMPT_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.PROMPT_MESSAGE_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.PROMPT_OPTIONS_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.PROMPT_TYPE_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.REQUIRED_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SENSITIVE_KEY;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;

public abstract class AbstractInputsTransformer extends InOutTransformer {

    private static final String DEFAULT_PROMPT_MESSAGE = "Enter a value for '%s'";
    private static final HashSet<String> KNOWN_KEYS = newHashSet(
            REQUIRED_KEY,
            SENSITIVE_KEY,
            PRIVATE_INPUT_KEY,
            DEFAULT_KEY,
            PROMPT_KEY);
    private static final String DEFAULT_DELIMITER = ",";

    protected PreCompileValidator preCompileValidator;

    private ExecutableValidator executableValidator;

    @Override
    public Class<? extends InOutParam> getTransformedObjectsClass() {
        return Input.class;
    }

    protected Input transformSingleInput(Object rawInput, SensitivityLevel sensitivityLevel) {
        // - some_input
        // this is our default behaviour that if the user specifies only a key, the key is also the ref we look for
        if (rawInput instanceof String) {
            String inputName = (String) rawInput;
            return createInput(inputName, null, sensitivityLevel);
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
                return createPropInput((Map.Entry<String, Map<String, Serializable>>) entry, sensitivityLevel);
            }
            // - some_input: some_expression
            // the value of the input is an expression we need to evaluate at runtime
            return createInput(entry.getKey(), entryValue, sensitivityLevel);
        }
        throw new RuntimeException("Could not transform Input : " + rawInput);
    }

    private Input createPropInput(Map.Entry<String, Map<String, Serializable>> entry,
                                  SensitivityLevel sensitivityLevel) {
        Map<String, Serializable> props = entry.getValue();

        for (String key : props.keySet()) {
            if (!KNOWN_KEYS.contains(key)) {
                throw new RuntimeException(format("key: %s in input: %s is not a known property",
                        key, entry.getKey()));
            }
        }

        // default is required=true
        boolean required = (boolean) props.getOrDefault(REQUIRED_KEY, true);
        // default is sensitive=false
        boolean sensitive = (boolean) props.getOrDefault(SENSITIVE_KEY, false);
        // default is private=false
        boolean privateInput = (boolean) props.getOrDefault(PRIVATE_INPUT_KEY, false);

        boolean defaultKeyFound = props.containsKey(DEFAULT_KEY);
        final String inputName = entry.getKey();
        final Serializable value = defaultKeyFound ? props.get(DEFAULT_KEY) : null;
        boolean defaultSpecified = defaultKeyFound && value != null && !StringUtils.EMPTY.equals(value);

        @SuppressWarnings("unchecked")
        Map<String, String> promptSettings = (Map<String, String>) props.get(PROMPT_KEY);
        if (!privateInput && isNotEmpty(promptSettings)) {
            final PromptType type = ofNullable(promptSettings.get(PROMPT_TYPE_KEY))
                    .map(PromptType::fromString)
                    .orElse(PromptType.TEXT);

            final String message = ofNullable(promptSettings.get(PROMPT_MESSAGE_KEY))
                    .orElseGet(() -> String.format(DEFAULT_PROMPT_MESSAGE, inputName));

            final String options = promptSettings.get(PROMPT_OPTIONS_KEY);
            final String delimiter = promptSettings.getOrDefault(PROMPT_DELIMITER_KEY, DEFAULT_DELIMITER);

            return createInput(inputName, value, sensitive, required, privateInput, sensitivityLevel,
                    type, message, options, delimiter);
        }

        if (privateInput && required && !defaultSpecified) {
            throw new RuntimeException(PreCompileValidator.VALIDATION_ERROR +
                    "Input: '" + inputName + "' is private and required but no default value was specified");
        }

        return createInput(inputName, value, sensitive, required, privateInput, sensitivityLevel);
    }

    private Input createInput(
            String name,
            Serializable value, SensitivityLevel sensitivityLevel) {
        return createInput(name, value, false, true, false, sensitivityLevel);
    }

    private Input createInput(
            String name,
            Serializable value,
            boolean sensitive,
            boolean required,
            boolean privateInput, SensitivityLevel sensitivityLevel) {
        return createInput(name, value, sensitive, required, privateInput, sensitivityLevel, null, null, null, null);
    }

    private Input createInput(
            String name,
            Serializable value,
            boolean sensitive,
            boolean required,
            boolean privateInput, SensitivityLevel sensitivityLevel,
            PromptType promptType, String message, String options, String delimiter) {
        executableValidator.validateInputName(name);
        preCompileValidator.validateStringValue(name, value, this);
        Accumulator dependencyAccumulator = extractFunctionData(value, message);
        return new Input.InputBuilder(name, value, sensitive, sensitivityLevel)
                .withRequired(required)
                .withPrivateInput(privateInput)
                .withFunctionDependencies(dependencyAccumulator.getFunctionDependencies())
                .withSystemPropertyDependencies(dependencyAccumulator.getSystemPropertyDependencies())
                .withPrompt(promptType)
                .withPromptMessage(message)
                .withPromptOptions(options)
                .withPromptDelimiter(delimiter)
                .build();
    }

    public void setPreCompileValidator(PreCompileValidator preCompileValidator) {
        this.preCompileValidator = preCompileValidator;
    }

    public void setExecutableValidator(ExecutableValidator executableValidator) {
        this.executableValidator = executableValidator;
    }
}
