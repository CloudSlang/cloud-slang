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

import io.cloudslang.lang.entities.bindings.Input;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Component
public class InputsTransformer extends AbstractInputsTransformer implements Transformer<List<Object>, List<Input>> {

    /**
     * Transforms a list of inputs in (raw data form) to Input objects.
     * @param rawData : inputs as described in Yaml source.
     * @return : list of inputs after transformation.
     */
    @Override
    public List<Input> transform(List<Object> rawData) {
        List<Input> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(rawData)){
            return result;
        }
        for (Object rawInput : rawData) {
            Input input = transformSingleInput(rawInput);
            result.add(input);
        }
        return result;
    }

    @Override
    public List<Scope> getScopes() {
        return Arrays.asList(Scope.BEFORE_EXECUTABLE);
    }

    @Override
    public String keyToTransform() {
        return null;
    }

}
