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
import io.cloudslang.lang.entities.ListParallelLoopStatement;
import io.cloudslang.lang.entities.MapParallelLoopStatement;
import io.cloudslang.lang.entities.ParallelLoopStatement;
import io.cloudslang.lang.entities.SensitivityLevel;
import io.cloudslang.lang.entities.utils.ExpressionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.cloudslang.lang.compiler.SlangTextualKeys.FOR_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.MAX_THROTTLE_KEY;

public class ParallelLoopForTransformer extends AbstractForTransformer
        implements Transformer<Object, ParallelLoopStatement> {

    @Override
    public TransformModellingResult<ParallelLoopStatement> transform(Object rawData) {
        return transform(rawData, CompilerConstants.DEFAULT_SENSITIVITY_LEVEL);
    }

    @Override
    public TransformModellingResult<ParallelLoopStatement> transform(Object rawData,
                                                                     SensitivityLevel sensitivityLevel) {
        String value = null;
        String throttle = null;

        if (rawData instanceof Map) {
            Map<String, Object> parallelLoopRawData = (Map<String, Object>) rawData;
            value = Objects.toString(parallelLoopRawData.get(FOR_KEY), null);
            throttle = Objects.toString(parallelLoopRawData.get(MAX_THROTTLE_KEY), null);
        }

        // throttle can be null
        return transformToParallelLoopStatement(value, throttle);
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.singletonList(Scope.BEFORE_STEP);
    }

    @Override
    public String keyToTransform() {
        return SlangTextualKeys.PARALLEL_LOOP_KEY;
    }

    private TransformModellingResult<ParallelLoopStatement> transformToParallelLoopStatement(String value,
                                                                                             String throttle) {

        List<RuntimeException> errors = new ArrayList<>();

        if (StringUtils.isEmpty(value)) {
            errors.add(new RuntimeException("For statement is empty."));
            return new BasicTransformModellingResult<>(null, errors);
        }

        ParallelLoopStatement parallelLoopStatement = null;
        String varName;
        String collectionExpression;
        Pattern regexSimpleFor = Pattern.compile(FOR_REGEX);
        Matcher matcherSimpleFor = regexSimpleFor.matcher(value);
        Accumulator dependencyAccumulator;

        if (throttle == null) {
            dependencyAccumulator = extractFunctionData("${" + value + "}");
        } else {
            dependencyAccumulator = extractFunctionData("${" + value + "}", throttle);
            String throttleExpression = ExpressionUtils.extractExpression(throttle);
            throttle = throttleExpression != null ? throttleExpression : throttle;
        }

        try {
            if (matcherSimpleFor.find()) {
                // case: value in variable_name
                varName = matcherSimpleFor.group(2);
                collectionExpression = matcherSimpleFor.group(4);
                parallelLoopStatement = createListParallelLoopStatement(varName, collectionExpression, throttle,
                        dependencyAccumulator);
            } else {
                String beforeInKeyword = StringUtils.substringBefore(value, FOR_IN_KEYWORD);
                collectionExpression = StringUtils.substringAfter(value, FOR_IN_KEYWORD).trim();

                Pattern regexKeyValueFor = Pattern.compile(KEY_VALUE_PAIR_REGEX);
                Matcher matcherKeyValueFor = regexKeyValueFor.matcher(beforeInKeyword);

                if (matcherKeyValueFor.find()) {
                    // case: key, value
                    String keyName = matcherKeyValueFor.group(2);
                    String valueName = matcherKeyValueFor.group(6);
                    parallelLoopStatement = createMapParallelLoopStatement(keyName, valueName, throttle,
                            collectionExpression, dependencyAccumulator);
                } else {
                    // case: value in expression_other_than_variable_name
                    varName = beforeInKeyword.trim();
                    parallelLoopStatement = createListParallelLoopStatement(varName, collectionExpression, throttle,
                            dependencyAccumulator);
                }
            }
        } catch (RuntimeException rex) {
            errors.add(rex);
        }

        return new BasicTransformModellingResult<>(parallelLoopStatement, errors);
    }

    private ParallelLoopStatement createMapParallelLoopStatement(String keyName,
                                                                 String valueName,
                                                                 String throttleExpression,
                                                                 String collectionExpression,
                                                                 Accumulator dependencyAccumulator) {
        super.validateLoopStatementVariable(keyName);
        super.validateLoopStatementVariable(valueName);
        return new MapParallelLoopStatement(
                keyName,
                valueName,
                collectionExpression,
                throttleExpression,
                dependencyAccumulator.getFunctionDependencies(),
                dependencyAccumulator.getSystemPropertyDependencies());
    }

    private ParallelLoopStatement createListParallelLoopStatement(String varName,
                                                                  String collectionExpression,
                                                                  String throttleExpression,
                                                                  Accumulator dependencyAccumulator) {
        super.validateLoopStatementVariable(varName);
        return new ListParallelLoopStatement(
                varName,
                collectionExpression,
                throttleExpression,
                dependencyAccumulator.getFunctionDependencies(),
                dependencyAccumulator.getSystemPropertyDependencies());
    }


}
