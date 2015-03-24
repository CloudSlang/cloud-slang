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

import org.apache.commons.collections4.MapUtils;
import io.cloudslang.lang.entities.bindings.Input;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Component
public class DoTransformer extends AbstractInputsTransformer implements Transformer<Map<String, List>, List<Input>> {

    @Override
    public List<Input> transform(Map<String, List> rawData) {
        //todo handle also String type
        List<Input> result = new ArrayList<>();
        if (MapUtils.isEmpty(rawData)) {
            return result;
        }
        Map.Entry<String, List> inputsEntry = rawData.entrySet().iterator().next();
        if (inputsEntry.getValue() == null) {
            return result;
        }
        for (Object rawInput : inputsEntry.getValue()) {
            Input input = transformSingleInput(rawInput);
            result.add(input);
        }
        return result;
    }

    @Override
    public List<Scope> getScopes() {
        return Arrays.asList(Scope.BEFORE_TASK);
    }

    @Override
    public String keyToTransform() {
        return null;
    }

}
