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
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.result.BasicTransformModellingResult;
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PythonActionTransformer extends AbstractTransformer
        implements Transformer<Map<String, Serializable>, Map<String, Serializable>> {

    private DependencyFormatValidator dependencyFormatValidator;

    private static Set<String> mandatoryKeySet = Sets.newHashSet(SlangTextualKeys.PYTHON_ACTION_SCRIPT_KEY);
    private static Set<String> optionalKeySet = Collections.emptySet();

    @SuppressWarnings("FieldCanBeLocal") // remove when `dependencies` will be enabled
    private boolean dependenciesEnabled = false;

    @Override
    public TransformModellingResult<Map<String, Serializable>> transform(Map<String, Serializable> rawData) {
        List<RuntimeException> errors = new ArrayList<>();
        Map<String, Serializable> transformedData = null;

        try {
            if (rawData != null) {
                validateKeySet(rawData.keySet(), mandatoryKeySet, optionalKeySet);
                if (dependenciesEnabled) {
                    @SuppressWarnings("unchecked")
                    Collection<String> dependencies =
                            (List<String>) rawData.get(SlangTextualKeys.PYTHON_ACTION_DEPENDENCIES_KEY);
                    if (dependencies != null) {
                        for (String dependency : dependencies) {
                            dependencyFormatValidator.validateDependency(dependency);
                        }
                    }
                }
                transformedData = rawData;
            }
        } catch (RuntimeException rex) {
            errors.add(rex);
        }

        return new BasicTransformModellingResult<>(transformedData, errors);
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.singletonList(Scope.ACTION);
    }

    @Override
    public String keyToTransform() {
        return SlangTextualKeys.PYTHON_ACTION_KEY;
    }

    public void setDependencyFormatValidator(DependencyFormatValidator dependencyFormatValidator) {
        this.dependencyFormatValidator = dependencyFormatValidator;
    }
}
