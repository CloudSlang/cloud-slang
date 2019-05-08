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
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.entities.SensitivityLevel;
import io.cloudslang.lang.entities.bindings.Output;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.cloudslang.lang.compiler.SlangTextualKeys.SENSITIVE_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_OUTPUT_ROBOT_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.VALUE_KEY;

/**
 * Date: 11/7/2014
 *
 * @author Bonczidai Levente
 */
public class OutputsTransformer extends AbstractOutputsTransformer implements Transformer<List<Object>, List<Output>> {

    private static final List<String> ACTIVITY_OUTPUTS_KNOWN_KEYS =
            Arrays.asList(SENSITIVE_KEY, VALUE_KEY, SEQ_OUTPUT_ROBOT_KEY);

    protected List<String> getKnownKeys() {
        return ACTIVITY_OUTPUTS_KNOWN_KEYS;
    }

    @Override
    public TransformModellingResult<List<Output>> transform(List<Object> rawData) {
        return transform(rawData, CompilerConstants.DEFAULT_SENSITIVITY_LEVEL);
    }

    @Override
    public TransformModellingResult<List<Output>> transform(List<Object> rawData, SensitivityLevel sensitivityLevel) {
        return super.transform(rawData);
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.singletonList(Scope.AFTER_EXECUTABLE);
    }

    @Override
    public String keyToTransform() {
        return null;
    }

    @Override
    void handleOutputProperties(List<Output> transformedData,
                                Map.Entry<String, ?> entry, List<RuntimeException> errors) {
        //noinspection unchecked
        addOutput(transformedData, createPropOutput((Map.Entry<String, Map<String, Serializable>>) entry), errors);
    }
}
