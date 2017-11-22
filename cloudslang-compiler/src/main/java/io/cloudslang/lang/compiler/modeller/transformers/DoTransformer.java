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

import io.cloudslang.lang.compiler.CompilerConstants;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.result.BasicTransformModellingResult;
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.compiler.validator.PreCompileValidator;
import io.cloudslang.lang.entities.SensitivityLevel;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.InOutParam;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static io.cloudslang.lang.compiler.SlangTextualKeys.SENSITIVE_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.VALUE_KEY;

/*
 * Created by orius123 on 05/11/14.
 */
public class DoTransformer extends InOutTransformer implements Transformer<Map<String, Object>, List<Argument>> {

    private PreCompileValidator preCompileValidator;
    private ExecutableValidator executableValidator;

    @Override
    public TransformModellingResult<List<Argument>> transform(Map<String, Object> rawData) {
        return transform(rawData, CompilerConstants.DEFAULT_SENSITIVITY_LEVEL);
    }

    @Override
    public TransformModellingResult<List<Argument>> transform(Map<String, Object> rawData,
                                                              SensitivityLevel sensitivityLevel) {
        final List<RuntimeException> validateRawData = validateRawData(rawData);
        if (!validateRawData.isEmpty()) {
            return new BasicTransformModellingResult<>(null, validateRawData);
        }

        final Object rawArguments = rawData.entrySet().iterator().next().getValue();
        if (rawArguments == null) {
            return new BasicTransformModellingResult<>(Collections.emptyList(), Collections.emptyList());
        }

        if (rawArguments instanceof List) {
            final List rawArgumentsList = (List) rawArguments;

            final List<Argument> transformedData = new ArrayList<>();
            final List<RuntimeException> errors = new ArrayList<>();
            // list syntax
            for (final Object rawArgument : rawArgumentsList) {
                try {
                    final Argument argument = transformListArgument(rawArgument, sensitivityLevel);
                    final List<RuntimeException> validationErrors =
                            preCompileValidator.validateNoDuplicateInOutParams(transformedData, argument);
                    if (CollectionUtils.isEmpty(validationErrors)) {
                        transformedData.add(argument);
                    } else {
                        errors.addAll(validationErrors);
                    }
                } catch (RuntimeException rex) {
                    errors.add(rex);
                }
            }
            return new BasicTransformModellingResult<>(transformedData, errors);
        }
        return new BasicTransformModellingResult<>(Collections.emptyList(), Collections.singletonList(
                new RuntimeException("Step arguments should be defined using a standard YAML list.")));
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.singletonList(Scope.BEFORE_STEP);
    }

    @Override
    public String keyToTransform() {
        return SlangTextualKeys.DO_KEY;
    }

    @Override
    public Class<? extends InOutParam> getTransformedObjectsClass() {
        return Argument.class;
    }

    @Override
    public Type getType() {
        return Type.INTERNAL;
    }

    private Argument transformListArgument(Object rawArgument, SensitivityLevel sensitivityLevel) {
        // - some_arg
        // this is our default behaviour that if the user specifies only a key, the key is also the ref we look for
        if (rawArgument instanceof String) {
            String argumentName = (String) rawArgument;
            return createArgument(argumentName, null, false, false, sensitivityLevel);
        } else if (rawArgument instanceof Map) {
            // not so nice casts here - validation happens later on
            // noinspection unchecked
            Map<String, Serializable> rawDataMap = (Map<String, Serializable>) rawArgument;
            Iterator<Map.Entry<String, Serializable>> iterator = rawDataMap.entrySet().iterator();
            Map.Entry<String, ?> entry = iterator.next();
            String entryKey = entry.getKey();
            if (rawDataMap.size() > 1) {
                throw new RuntimeException(
                        "Invalid syntax after step input \"" + entryKey + "\". " +
                                "Please check all step inputs are provided as a list and each input" +
                                " is preceded by a hyphen. Step input \"" + iterator.next().getKey() + "\" is" +
                                " missing the hyphen."
                );
            }
            Serializable entryValue = (Serializable) entry.getValue();
            if (entryValue instanceof Map) {
                // - some_inputs:
                //     property1: value1
                //     property2: value2
                // this is the verbose way of defining inputs with all of the properties available
                // noinspection unchecked
                return createArgumentWithProperties((Map.Entry<String, Map<String, Serializable>>) entry,
                        sensitivityLevel);
            } else {
                // - some_input: some_expression
                return createArgument(entryKey, entryValue, sensitivityLevel);
            }
        }
        throw new RuntimeException("Could not transform step argument: " + rawArgument);
    }

    private Argument createArgumentWithProperties(Map.Entry<String, Map<String, Serializable>> entry,
                                                  SensitivityLevel sensitivityLevel) {
        Map<String, Serializable> props = entry.getValue();
        List<String> knownKeys = Arrays.asList(SENSITIVE_KEY, VALUE_KEY);

        String entryKey = entry.getKey();

        for (String key : props.keySet()) {
            if (!knownKeys.contains(key)) {
                throw new RuntimeException("key: " + key + " in step input: " + entryKey +
                        " is not a known property");
            }
        }

        // default is sensitive=false
        boolean sensitive = props.containsKey(SENSITIVE_KEY) &&
                (boolean) props.get(SENSITIVE_KEY);
        boolean valueKeyFound = props.containsKey(VALUE_KEY);
        Serializable value = valueKeyFound ? props.get(VALUE_KEY) : null;

        return createArgument(entryKey, value, sensitive, valueKeyFound, sensitivityLevel);
    }

    private Argument createArgument(String entryName, Serializable entryValue, SensitivityLevel sensitivityLevel) {
        return createArgument(entryName, entryValue, false, true, sensitivityLevel);
    }

    private Argument createArgument(
            String entryName,
            Serializable entryValue,
            boolean sensitive,
            boolean privateArgument,
            SensitivityLevel sensitivityLevel) {
        executableValidator.validateInputName(entryName);
        preCompileValidator.validateStringValue(entryName, entryValue, this);
        Accumulator accumulator = extractFunctionData(entryValue);
        return new Argument(
                entryName,
                ValueFactory.create(entryValue, sensitive, sensitivityLevel),
                privateArgument,
                accumulator.getFunctionDependencies(),
                accumulator.getSystemPropertyDependencies()
        );
    }

    public void setPreCompileValidator(PreCompileValidator preCompileValidator) {
        this.preCompileValidator = preCompileValidator;
    }

    public void setExecutableValidator(ExecutableValidator executableValidator) {
        this.executableValidator = executableValidator;
    }

    private List<RuntimeException> validateRawData(Map<String, Object> rawData) {
        if (MapUtils.isEmpty(rawData)) {
            return Collections.singletonList(new RuntimeException("Step has no reference information."));
        } else if (rawData.size() > 1) {
            return Collections.singletonList(
                    new RuntimeException("Step has too many keys under the '" + keyToTransform() + "' keyword,\n" +
                            "May happen due to wrong indentation."));
        }
        executableValidator.validateStepReferenceId(rawData.keySet().iterator().next());
        return Collections.emptyList();
    }
}
