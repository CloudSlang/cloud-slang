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

import io.cloudslang.lang.compiler.modeller.result.BasicTransformModellingResult;
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.entities.ListLoopStatement;
import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.entities.MapLoopStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Date: 3/25/2015
 *
 * @author Bonczidai Levente
 */
public abstract class AbstractForTransformer extends AbstractInOutForTransformer {

    private ExecutableValidator executableValidator;

    // case: value in variable_name
    private static final String FOR_REGEX = "^(\\s+)?(\\w+)\\s+(in)\\s+(\\w+)(\\s+)?$";
    // case: key, value
    private static final String KEY_VALUE_PAIR_REGEX = "^(\\s+)?(\\w+)(\\s+)?(,)(\\s+)?(\\w+)(\\s+)?$";
    private static final String FOR_IN_KEYWORD = " in ";

    public TransformModellingResult<LoopStatement> transformToLoopStatement(String rawData, boolean isParallelLoop) {
        List<RuntimeException> errors = new ArrayList<>();
        Accumulator dependencyAccumulator = extractFunctionData("${" + rawData + "}");
        if (StringUtils.isEmpty(rawData)) {
            errors.add(new RuntimeException("For statement is empty."));
            return new BasicTransformModellingResult<>(null, errors);
        }

        LoopStatement loopStatement = null;
        String varName;
        String collectionExpression;

        Pattern regexSimpleFor = Pattern.compile(FOR_REGEX);
        Matcher matcherSimpleFor = regexSimpleFor.matcher(rawData);

        try {
            if (matcherSimpleFor.find()) {
                // case: value in variable_name
                varName = matcherSimpleFor.group(2);
                collectionExpression = matcherSimpleFor.group(4);
                loopStatement = createLoopStatement(varName, collectionExpression,
                        dependencyAccumulator, isParallelLoop);
            } else {
                String beforeInKeyword = StringUtils.substringBefore(rawData, FOR_IN_KEYWORD);
                collectionExpression = StringUtils.substringAfter(rawData, FOR_IN_KEYWORD).trim();

                Pattern regexKeyValueFor = Pattern.compile(KEY_VALUE_PAIR_REGEX);
                Matcher matcherKeyValueFor = regexKeyValueFor.matcher(beforeInKeyword);

                if (matcherKeyValueFor.find()) {
                    // case: key, value
                    String keyName = matcherKeyValueFor.group(2);
                    String valueName = matcherKeyValueFor.group(6);
                    loopStatement = createMapForLoopStatement(keyName, valueName,
                            collectionExpression, dependencyAccumulator);
                } else {
                    // case: value in expression_other_than_variable_name
                    varName = beforeInKeyword.trim();
                    loopStatement = createLoopStatement(varName, collectionExpression,
                            dependencyAccumulator, isParallelLoop);
                }
            }
        } catch (RuntimeException rex) {
            errors.add(rex);
        }

        return new BasicTransformModellingResult<>(loopStatement, errors);
    }

    private LoopStatement createMapForLoopStatement(String keyName, String valueName,
                                                    String collectionExpression, Accumulator dependencyAccumulator) {
        executableValidator.validateLoopStatementVariable(keyName);
        executableValidator.validateLoopStatementVariable(valueName);
        return new MapLoopStatement(
                keyName,
                valueName,
                collectionExpression,
                dependencyAccumulator.getFunctionDependencies(),
                dependencyAccumulator.getSystemPropertyDependencies());
    }

    private LoopStatement createLoopStatement(String varName, String collectionExpression,
                                              Accumulator dependencyAccumulator, boolean isParallelLoop) {
        executableValidator.validateLoopStatementVariable(varName);
        return new ListLoopStatement(varName, collectionExpression,
                    dependencyAccumulator.getFunctionDependencies(),
                    dependencyAccumulator.getSystemPropertyDependencies(), isParallelLoop);
    }

    public void setExecutableValidator(ExecutableValidator executableValidator) {
        this.executableValidator = executableValidator;
    }
}
