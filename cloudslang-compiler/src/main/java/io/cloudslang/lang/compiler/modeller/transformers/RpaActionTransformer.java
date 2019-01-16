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

import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.entities.RpaStep;
import io.cloudslang.lang.compiler.modeller.result.BasicTransformModellingResult;
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.compiler.validator.PreCompileValidator;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SensitivityLevel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static io.cloudslang.lang.compiler.CompilerConstants.DEFAULT_SENSITIVITY_LEVEL;
import static io.cloudslang.lang.compiler.SlangTextualKeys.RPA_ACTION_KEY;
import static io.cloudslang.lang.compiler.modeller.transformers.Transformer.Scope.ACTION;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;

public class RpaActionTransformer extends AbstractTransformer
        implements Transformer<Map<String, Serializable>, Map<String, Serializable>> {
    private final DependencyFormatValidator dependencyFormatValidator;
    private final PreCompileValidator preCompileValidator;
    private final RpaStepsTransformer rpaStepsTransformer;

    public RpaActionTransformer(DependencyFormatValidator dependencyFormatValidator,
                                PreCompileValidator preCompileValidator,
                                RpaStepsTransformer rpaStepsTransformer) {
        this.dependencyFormatValidator = dependencyFormatValidator;
        this.preCompileValidator = preCompileValidator;
        this.rpaStepsTransformer = rpaStepsTransformer;
    }

    private static final Set<String> MANDATORY_KEY_SET = newHashSet(SlangTextualKeys.RPA_ACTION_GAV_KEY,
            SlangTextualKeys.RPA_STEPS_KEY);
    private static final Set<String> OPTIONAL_KEY_SET = emptySet();

    @Override
    public TransformModellingResult<Map<String, Serializable>> transform(Map<String, Serializable> rawData) {
        return transform(rawData, DEFAULT_SENSITIVITY_LEVEL);
    }

    @Override
    public TransformModellingResult<Map<String, Serializable>> transform(Map<String, Serializable> rawData,
                                                                         SensitivityLevel sensitivityLevel) {
        List<RuntimeException> errors = new ArrayList<>();
        Map<String, Serializable> transformedData = null;

        try {
            if (rawData != null) {
                validateKeySet(rawData.keySet(), MANDATORY_KEY_SET, OPTIONAL_KEY_SET);
                transformGav(rawData);
                transformSteps(rawData, errors, sensitivityLevel);
                transformedData = rawData;
            }
        } catch (RuntimeException rex) {
            errors.add(rex);
        }

        return new BasicTransformModellingResult<>(transformedData, errors);
    }

    private void transformGav(Map<String, Serializable> rawData) {
        String gav = (String) rawData.remove(SlangTextualKeys.RPA_ACTION_GAV_KEY);
        dependencyFormatValidator.validateDependency(gav);
        rawData.put(ScoreLangConstants.RPA_ACTION_GAV_KEY, gav);
    }

    private void transformSteps(Map<String, Serializable> rawData,
                                List<RuntimeException> errors,
                                SensitivityLevel sensitivityLevel) {
        List<Map<String, Map<String, String>>> steps = preCompileValidator
                .validateRpaActionSteps(rawData.remove(SlangTextualKeys.RPA_STEPS_KEY), errors);
        TransformModellingResult<ArrayList<RpaStep>> transformedSteps = rpaStepsTransformer
                .transform(steps, sensitivityLevel);
        errors.addAll(transformedSteps.getErrors());
        ArrayList<RpaStep> transformedData = transformedSteps.getTransformedData();
        if (transformedData.isEmpty()) {
            errors.add(new RuntimeException("Missing rpa operation steps."));
        }
        rawData.put(ScoreLangConstants.RPA_STEPS_KEY, transformedData);
    }

    @Override
    public List<Scope> getScopes() {
        return singletonList(ACTION);
    }

    @Override
    public String keyToTransform() {
        return RPA_ACTION_KEY;
    }
}
