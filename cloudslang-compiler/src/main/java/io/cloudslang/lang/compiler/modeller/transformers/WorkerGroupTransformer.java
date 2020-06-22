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
import io.cloudslang.lang.entities.utils.ExpressionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WorkerGroupTransformer extends AbstractInOutForTransformer
        implements Transformer<Object, WorkerGroupStatement> {

    @Override
    public TransformModellingResult<WorkerGroupStatement> transform(Object rawData) {
        return transform(rawData, CompilerConstants.DEFAULT_SENSITIVITY_LEVEL);
    }

    @Override
    public TransformModellingResult<WorkerGroupStatement> transform(Object rawData, SensitivityLevel sensitivityLevel) {
        String value = null;
        boolean override = false;
        if (rawData instanceof String) {
            value = (String) rawData;
        } else if (rawData instanceof Map) {
            value = String.valueOf(((Map<String, Object>) rawData).get("value"));
            override = (boolean) ((Map<String, Object>) rawData).get("override");
        }
        if (StringUtils.isBlank(value)) {
            return new BasicTransformModellingResult<>(null, Collections.emptyList());
        }

        WorkerGroupStatement workerGroupStatement;
        Accumulator dependencyAccumulator;
        String expression;
        try {
            dependencyAccumulator = extractFunctionData(value);
            expression = ExpressionUtils.extractExpression(value);
        } catch (IllegalStateException | IndexOutOfBoundsException e) {
            return new BasicTransformModellingResult<>(null, Collections.singletonList(e));
        }

        if (expression != null) {
            workerGroupStatement = new WorkerGroupStatement(expression,
                    override,
                    dependencyAccumulator.getFunctionDependencies(),
                    dependencyAccumulator.getSystemPropertyDependencies());
        } else {
            workerGroupStatement = new WorkerGroupStatement(value, override,null, null);
        }

        return new BasicTransformModellingResult<>(workerGroupStatement, Collections.emptyList());
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
