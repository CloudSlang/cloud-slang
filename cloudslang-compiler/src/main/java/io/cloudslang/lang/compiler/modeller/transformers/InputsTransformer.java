package io.cloudslang.lang.compiler.modeller.transformers;
/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/


/*
 * Created by orius123 on 05/11/14.
 */

import io.cloudslang.lang.compiler.modeller.result.BasicTransformModellingResult;
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.entities.bindings.Input;
import java.util.Collections;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class InputsTransformer extends AbstractInputsTransformer implements Transformer<List<Object>, List<Input>> {

    /**
     * Transforms a list of inputs in (raw data form) to Input objects.
     * @param rawData : inputs as described in Yaml source.
     * @return : list of inputs after transformation.
     */
    @Override
    public TransformModellingResult<List<Input>> transform(List<Object> rawData) {
        List<Input> transformedData = new ArrayList<>();
        List<RuntimeException> errors = new ArrayList<>();
        if (CollectionUtils.isEmpty(rawData)){
            return new BasicTransformModellingResult<>(transformedData, errors);
        }
        for (Object rawInput : rawData) {
            try {
                Input input = transformSingleInput(rawInput);
                List<RuntimeException> validationErrors = preCompileValidator.validateNoDuplicateInOutParams(transformedData, input);
                if (CollectionUtils.isEmpty(validationErrors)) {
                    transformedData.add(input);
                } else {
                    errors.addAll(validationErrors);
                }
            } catch (RuntimeException rex) {
                errors.add(rex);
            }
        }
        return new BasicTransformModellingResult<>(transformedData, errors);
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.singletonList(Scope.BEFORE_EXECUTABLE);
    }

    @Override
    public String keyToTransform() {
        return null;
    }

}
