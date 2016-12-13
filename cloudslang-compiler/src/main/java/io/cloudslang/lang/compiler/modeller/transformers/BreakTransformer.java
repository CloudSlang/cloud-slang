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
import io.cloudslang.lang.entities.ScoreLangConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BreakTransformer implements Transformer<List<String>, List<String>> {

    private ExecutableValidator executableValidator;

    @Override
    public TransformModellingResult<List<String>> transform(List<String> rawData) {
        List<String> transformedData = new ArrayList<>();
        List<RuntimeException> errors = new ArrayList<>();

        if (rawData == null) {
            transformedData.add(ScoreLangConstants.FAILURE_RESULT);
        } else {
            try {
                executableValidator.validateBreakKeys(rawData);
                transformedData = rawData;
            } catch (RuntimeException rex) {
                errors.add(rex);
            }
        }

        return new BasicTransformModellingResult<>(transformedData, errors);
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.singletonList(Scope.AFTER_STEP);
    }

    @Override
    public String keyToTransform() {
        return null;
    }

    public void setExecutableValidator(ExecutableValidator executableValidator) {
        this.executableValidator = executableValidator;
    }
}
