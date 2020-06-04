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
import io.cloudslang.lang.entities.RobotGroupStatement;
import io.cloudslang.lang.entities.SensitivityLevel;
import io.cloudslang.lang.entities.utils.ExpressionUtils;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;

public class RobotGroupTransformer extends AbstractInOutForTransformer
        implements Transformer<String, RobotGroupStatement> {

    @Override
    public TransformModellingResult<RobotGroupStatement> transform(String rawData) {
        return transform(rawData, CompilerConstants.DEFAULT_SENSITIVITY_LEVEL);
    }

    @Override
    public TransformModellingResult<RobotGroupStatement> transform(String rawData, SensitivityLevel sensitivityLevel) {
        if (StringUtils.isBlank(rawData)) {
            return new BasicTransformModellingResult<>(null, Collections.emptyList());
        }

        RobotGroupStatement robotGroupStatement;
        Accumulator dependencyAccumulator;
        String expression;
        try {
            dependencyAccumulator = extractFunctionData(rawData);
            expression = ExpressionUtils.extractExpression(rawData);
        } catch (IllegalStateException | IndexOutOfBoundsException e) {
            return new BasicTransformModellingResult<>(null, Collections.singletonList(e));
        }

        if (expression != null) {
            robotGroupStatement = new RobotGroupStatement(expression,
                    dependencyAccumulator.getFunctionDependencies(),
                    dependencyAccumulator.getSystemPropertyDependencies());
        } else {
            robotGroupStatement = new RobotGroupStatement(rawData, null, null);
        }

        return new BasicTransformModellingResult<>(robotGroupStatement, Collections.emptyList());
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.singletonList(Scope.BEFORE_STEP);
    }

    @Override
    public String keyToTransform() {
        return SlangTextualKeys.ROBOT_GROUP;
    }
}
