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
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorkerGroupTransformer extends AbstractInOutForTransformer
        implements Transformer<String, WorkerGroupStatement> {

    private static final String SYSTEM_PROPERTY_REGEX = "^\\$\\{get_sp\\('(\\w+)'\\)}$";
    private static final String VARIABLE_REGEX = "^\\$\\{[\\w\\d]+}$";

    private final Pattern regexSysProperty;
    private final Pattern regexVariable;

    public WorkerGroupTransformer() {
        this.regexSysProperty = Pattern.compile(SYSTEM_PROPERTY_REGEX);
        this.regexVariable = Pattern.compile(VARIABLE_REGEX);
    }

    @Override
    public TransformModellingResult<WorkerGroupStatement> transform(String rawData) {
        return transform(rawData, CompilerConstants.DEFAULT_SENSITIVITY_LEVEL);
    }

    @Override
    public TransformModellingResult<WorkerGroupStatement> transform(String rawData, SensitivityLevel sensitivityLevel) {
        if (StringUtils.isBlank(rawData)) {
            return new BasicTransformModellingResult<>(null, Collections.emptyList());
        }

        Accumulator dependencyAccumulator = extractFunctionData(rawData);
        WorkerGroupStatement workerGroupStatement;

        Matcher matcherSysProperty = regexSysProperty.matcher(rawData);
        Matcher matcherVariable = regexVariable.matcher(rawData);

        String expression = findExpression(matcherSysProperty, matcherVariable);

        if (expression != null) {
            workerGroupStatement = new WorkerGroupStatement(rawData.substring(2, rawData.length() - 1),
                    dependencyAccumulator.getFunctionDependencies(),
                    dependencyAccumulator.getSystemPropertyDependencies());
        } else {
            workerGroupStatement = new WorkerGroupStatement(rawData, null, null);
        }

        return new BasicTransformModellingResult<>(workerGroupStatement, Collections.emptyList());
    }

    private String findExpression(Matcher matcherSysProperty, Matcher matcherVariable) {
        String expression = null;
        if (matcherSysProperty.find()) {
            expression = matcherSysProperty.group();
        } else if (matcherVariable.find()) {
            expression = matcherVariable.group();
        }
        return expression;
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
