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



/*
 * Created by orius123 on 05/11/14.
 */

import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.result.BasicTransformModellingResult;
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.compiler.validator.PreCompileValidator;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.InOutParam;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

public class DoTransformer extends InOutTransformer implements Transformer<Map<String, Object>, List<Argument>> {

    private PreCompileValidator preCompileValidator;

    @Override
    public TransformModellingResult<List<Argument>> transform(Map<String, Object> rawData) {
        List<Argument> transformedData = new ArrayList<>();
        List<RuntimeException> errors = new ArrayList<>();
        if (MapUtils.isEmpty(rawData)) {
            return new BasicTransformModellingResult<>(transformedData, errors);
        } else if (rawData.size() > 1) {
            errors.add(
                    new RuntimeException("Step has too many keys under the 'do' keyword,\n" +
                            "May happen due to wrong indentation"
                    )
            );
            return new BasicTransformModellingResult<>(transformedData, errors);
        }
        Map.Entry<String, Object> argumentsEntry = rawData.entrySet().iterator().next();
        Object rawArguments = argumentsEntry.getValue();
        if (rawArguments instanceof List) {
            // list syntax
            List rawArgumentsList = (List) rawArguments;
            for (Object rawArgument : rawArgumentsList) {
                try {
                    Argument argument = transformListArgument(rawArgument);
                    List<RuntimeException> validationErrors =
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
        } else if (rawArguments != null) {
            errors.add(new RuntimeException("Step arguments should be defined using a standard YAML list."));
        }

        return new BasicTransformModellingResult<>(transformedData, errors);
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

    private Argument transformListArgument(Object rawArgument) {
        // - some_arg
        // this is our default behaviour that if the user specifies only a key, the key is also the ref we look for
        if (rawArgument instanceof String) {
            String argumentName = (String) rawArgument;
            return new Argument(argumentName);
        } else if (rawArgument instanceof Map) {
            @SuppressWarnings("unchecked")
            Map.Entry<String, Serializable> entry = ((Map<String, Serializable>) rawArgument).entrySet()
                    .iterator().next();
            Serializable entryValue = entry.getValue();
            // - some_input: some_expression
            return createArgument(entry, entryValue);
        }
        throw new RuntimeException("Could not transform step argument: " + rawArgument);
    }

    private Argument createArgument(Map.Entry<String, Serializable> entry, Serializable entryValue) {
        preCompileValidator.validateStringValue(entry.getKey(), entryValue, this);
        Accumulator accumulator = extractFunctionData(entryValue);
        return new Argument(
                entry.getKey(),
                ValueFactory.create(entryValue),
                accumulator.getFunctionDependencies(),
                accumulator.getSystemPropertyDependencies()
        );
    }

    public void setPreCompileValidator(PreCompileValidator preCompileValidator) {
        this.preCompileValidator = preCompileValidator;
    }
}