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
import io.cloudslang.lang.entities.ScoreLangConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JavaActionTransformer extends AbstractTransformer
        implements Transformer<Map<String, String>, Map<String, String>> {

    private DependencyFormatValidator dependencyFormatValidator;

    private static Set<String> mandatoryKeySet = Sets.newHashSet(
            SlangTextualKeys.JAVA_ACTION_CLASS_NAME_KEY,
            SlangTextualKeys.JAVA_ACTION_METHOD_NAME_KEY,
            SlangTextualKeys.JAVA_ACTION_GAV_KEY
    );
    private static Set<String> optionalKeySet = Sets.newHashSet();

    @Override
    public TransformModellingResult<Map<String, String>> transform(Map<String, String> rawData) {
        Map<String, String> transformedData = null;
        List<RuntimeException> errors = new ArrayList<>();

        try {
            if (rawData != null) {
                validateKeySet(rawData.keySet(), mandatoryKeySet, optionalKeySet);
                transformKeys(rawData);
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
        return SlangTextualKeys.JAVA_ACTION_KEY;
    }

    private void transformKeys(Map<String, String> rawData) {
        // snake_case -> camelCase
        rawData.put(ScoreLangConstants.JAVA_ACTION_CLASS_KEY,
                rawData.remove(SlangTextualKeys.JAVA_ACTION_CLASS_NAME_KEY));
        rawData.put(ScoreLangConstants.JAVA_ACTION_METHOD_KEY,
                rawData.remove(SlangTextualKeys.JAVA_ACTION_METHOD_NAME_KEY));
        String gav = rawData.remove(SlangTextualKeys.JAVA_ACTION_GAV_KEY);
        dependencyFormatValidator.validateDependency(gav);
        rawData.put(ScoreLangConstants.JAVA_ACTION_GAV_KEY, gav);
    }

    public void setDependencyFormatValidator(DependencyFormatValidator dependencyFormatValidator) {
        this.dependencyFormatValidator = dependencyFormatValidator;
    }
}
