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

import io.cloudslang.lang.compiler.modeller.result.BasicTransformModellingResult;
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.compiler.validator.PreCompileValidator;
import io.cloudslang.lang.entities.ExecutableType;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.InOutParam;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

public class ResultsTransformer extends InOutTransformer implements Transformer<List, List<Result>> {

    private PreCompileValidator preCompileValidator;

    private ExecutableValidator executableValidator;

    @Override
    public TransformModellingResult<List<Result>> transform(List rawData) {
        List<Result> transformedData = new ArrayList<>();
        List<RuntimeException> errors = new ArrayList<>();

        // If there are no results specified, add the default SUCCESS & FAILURE results
        if (CollectionUtils.isEmpty(rawData)) {
            return postProcessResults(transformedData, errors);
        }
        for (Object rawResult : rawData) {
            try {
                if (rawResult instanceof String) {
                    //- some_result
                    addResult(transformedData, createNoExpressionResult((String) rawResult), errors);
                } else if (rawResult instanceof Map) {
                    // - some_result: some_expression
                    // the value of the result is an expression we need to evaluate at runtime
                    @SuppressWarnings("unchecked") Map.Entry<String, Serializable> entry =
                            (Map.Entry<String, Serializable>) (((Map) rawResult).entrySet()).iterator().next();
                    addResult(transformedData, createExpressionResult(entry.getKey(), entry.getValue()), errors);
                }
            } catch (RuntimeException rex) {
                errors.add(rex);
            }
        }
        return postProcessResults(transformedData, errors);
    }

    public void addDefaultResultsIfNeeded(List rawResults, ExecutableType executableType,
                                          List<Result> resolvedResults, List<RuntimeException> errors) {
        if (rawResults == null && CollectionUtils.isEmpty(resolvedResults)) {
            switch (executableType) {
                case FLOW:
                    addResult(resolvedResults, createNoExpressionResult(ScoreLangConstants.SUCCESS_RESULT), errors);
                    addResult(resolvedResults, createNoExpressionResult(ScoreLangConstants.FAILURE_RESULT), errors);
                    break;
                case OPERATION:
                    addResult(resolvedResults, createNoExpressionResult(ScoreLangConstants.SUCCESS_RESULT), errors);
                    break;
                case DECISION:
                    break;
                default:
                    throw new RuntimeException("Not implemented for executable type: " + executableType);
            }
        }
    }

    private void addResult(List<Result> results, Result element, List<RuntimeException> errors) {
        List<RuntimeException> validationErrors = preCompileValidator.validateNoDuplicateInOutParams(results, element);
        if (CollectionUtils.isEmpty(validationErrors)) {
            results.add(element);
        } else {
            errors.addAll(validationErrors);
        }
    }

    private TransformModellingResult<List<Result>> postProcessResults(
            List<Result> transformedData,
            List<RuntimeException> errors) {
        return new BasicTransformModellingResult<>(transformedData, errors);
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.singletonList(Scope.AFTER_EXECUTABLE);
    }

    @Override
    public String keyToTransform() {
        return null;
    }

    @Override
    public Class<? extends InOutParam> getTransformedObjectsClass() {
        return Result.class;
    }

    private Result createNoExpressionResult(String resultName) {
        return createExpressionResult(resultName, null);
    }

    private Result createExpressionResult(String resultName, Serializable resultValue) {
        executableValidator.validateResultName(resultName);
        if (resultValue == null) {
            return new Result(resultName, null);
        } else {
            Accumulator accumulator = extractFunctionData(resultValue);
            return new Result(
                    resultName,
                    ValueFactory.create(resultValue),
                    accumulator.getFunctionDependencies(),
                    accumulator.getSystemPropertyDependencies()
            );
        }
    }

    public void setPreCompileValidator(PreCompileValidator preCompileValidator) {
        this.preCompileValidator = preCompileValidator;
    }

    public void setExecutableValidator(ExecutableValidator executableValidator) {
        this.executableValidator = executableValidator;
    }
}
