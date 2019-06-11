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
import io.cloudslang.lang.entities.SensitivityLevel;
import io.cloudslang.lang.entities.WorkerGroupStatement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorkerGroupTransformer extends AbstractInOutForTransformer
        implements Transformer<String, WorkerGroupStatement> {

    private static final String VARIABLE_REGEX = "^\\$\\{get\\_sp\\(\\'(\\w+)\\'\\)\\}$";

    @Override
    public TransformModellingResult<WorkerGroupStatement> transform(String rawData) {
        return transform(rawData, CompilerConstants.DEFAULT_SENSITIVITY_LEVEL);
    }

    @Override
    public TransformModellingResult<WorkerGroupStatement> transform(String rawData, SensitivityLevel sensitivityLevel) {
        Accumulator dependencyAccumulator = extractFunctionData(rawData);
        List<RuntimeException> errors = new ArrayList<>();

        WorkerGroupStatement workerGroupStatement = null;
        String expression;

        Pattern regexVariable = Pattern.compile(VARIABLE_REGEX);
        Matcher matcherVariable = regexVariable.matcher(rawData);

        try {
            if (matcherVariable.find()) {
                expression = matcherVariable.group();

                workerGroupStatement = new WorkerGroupStatement(expression,
                        dependencyAccumulator.getFunctionDependencies(),
                        dependencyAccumulator.getSystemPropertyDependencies());
            } else {
                workerGroupStatement = workerGroupStatement = new WorkerGroupStatement(rawData,
                        dependencyAccumulator.getFunctionDependencies(),
                        dependencyAccumulator.getSystemPropertyDependencies());
            }
        } catch (RuntimeException e) {
            errors.add(e);
        }

        return new BasicTransformModellingResult<>(workerGroupStatement, errors);
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.singletonList(Scope.BEFORE_STEP);
    }

    @Override
    public String keyToTransform() {
        return SlangTextualKeys.WORKER_GROUP;
    }
}
