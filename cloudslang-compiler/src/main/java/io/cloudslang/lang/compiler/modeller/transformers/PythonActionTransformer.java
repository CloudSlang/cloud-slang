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

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.CompilerConstants;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.result.BasicTransformModellingResult;
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.compiler.validator.ExternalPythonScriptValidator;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SensitivityLevel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.cloudslang.lang.compiler.SlangTextualKeys.PYTHON_ACTION_DEPENDENCIES_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.PYTHON_ACTION_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.PYTHON_ACTION_SCRIPT_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.PYTHON_ACTION_USE_JYTHON_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.PYTHON_ACTION_VERSION_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.INPUTS_KEY;
import static java.util.Collections.emptyList;


public class PythonActionTransformer extends AbstractTransformer
        implements Transformer<Map<String, Serializable>, Map<String, Serializable>> {

    private DependencyFormatValidator dependencyFormatValidator;
    private ExternalPythonScriptValidator externalPythonScriptValidator;

    private static Set<String> mandatoryKeySet = Sets.newHashSet(PYTHON_ACTION_SCRIPT_KEY);
    private static Set<String> optionalKeySet = Sets.newHashSet(PYTHON_ACTION_USE_JYTHON_KEY,
            PYTHON_ACTION_VERSION_KEY, INPUTS_KEY);

    @SuppressWarnings("FieldCanBeLocal") // remove when `dependencies` will be enabled
    private boolean dependenciesEnabled = false;

    @Override
    public TransformModellingResult<Map<String, Serializable>> transform(Map<String, Serializable> rawData) {
        return transform(rawData, CompilerConstants.DEFAULT_SENSITIVITY_LEVEL);
    }

    @Override
    public TransformModellingResult<Map<String, Serializable>> transform(Map<String, Serializable> rawData,
                                                                         SensitivityLevel sensitivityLevel) {
        List<RuntimeException> errors = new ArrayList<>();
        Map<String, Serializable> transformedData = null;

        try {
            if (rawData != null) {
                validateKeySet(rawData.keySet(), mandatoryKeySet, optionalKeySet);
                if (dependenciesEnabled) {
                    @SuppressWarnings("unchecked")
                    Collection<String> dependencies =
                            (List<String>) rawData.get(PYTHON_ACTION_DEPENDENCIES_KEY);
                    if (dependencies != null) {
                        for (String dependency : dependencies) {
                            dependencyFormatValidator.validateDependency(dependency);
                        }
                    }
                }

                if (isExternalPythonExecution(rawData)) {
                    // snake_case -> camelCase
                    rawData.put(ScoreLangConstants.PYTHON_ACTION_USE_JYTHON_KEY,
                            rawData.remove(SlangTextualKeys.PYTHON_ACTION_USE_JYTHON_KEY));
                    // validate script
                    String script = (String) rawData.get(SlangTextualKeys.PYTHON_ACTION_SCRIPT_KEY);
                    List<String> inputs = getInputs(rawData);
                    externalPythonScriptValidator.validateExecutionMethodAndInputs(script, inputs);
                } else {
                    //backwards compatibility
                    rawData.put(ScoreLangConstants.PYTHON_ACTION_USE_JYTHON_KEY, true);
                }
                transformedData = rawData;
            }
        } catch (RuntimeException rex) {
            errors.add(rex);
        }

        return new BasicTransformModellingResult<>(transformedData, errors);
    }

    private List<String> getInputs(Map<String, Serializable> rawData) {
        Object inputsObject = rawData.get(INPUTS_KEY);
        if (inputsObject instanceof Collection) {
            return ((Collection<?>) inputsObject).stream()
                    .flatMap(this::getStreamOfInputs)
                    .collect(Collectors.toList());
        }
        return emptyList();
    }

    private Stream<String> getStreamOfInputs(Object value) {
        if (value instanceof String) {
            return Stream.of((String) value);
        } else if (value instanceof Map) {
            return ((Map<?, ?>) value).keySet().stream().map(Object::toString);
        }
        return Stream.empty();
    }

    private boolean isExternalPythonExecution(Map<String, Serializable> rawData) {
        Boolean isJython = (Boolean) rawData.getOrDefault(PYTHON_ACTION_USE_JYTHON_KEY, true);
        return !isJython;
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.singletonList(Scope.ACTION);
    }

    @Override
    public String keyToTransform() {
        return PYTHON_ACTION_KEY;
    }

    public void setDependencyFormatValidator(DependencyFormatValidator dependencyFormatValidator) {
        this.dependencyFormatValidator = dependencyFormatValidator;
    }

    public void setExternalPythonScriptValidator(ExternalPythonScriptValidator externalPythonScriptValidator) {
        this.externalPythonScriptValidator = externalPythonScriptValidator;
    }
}
