/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.lang.compiler.modeller.transformers;

import io.cloudslang.lang.entities.bindings.Output;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Date: 11/7/2014
 *
 * @author Bonczidai Levente
 */
@Component
public class OutputsTransformer extends AbstractOutputsTransformer implements Transformer<List<Object>, List<Output>> {

    @Override
    public List<Output> transform(List<Object> rawData) {
        return super.transform(rawData);
    }

    @Override
    public List<Scope> getScopes() {
        return Arrays.asList(Scope.AFTER_EXECUTABLE);
    }

    @Override
    public String keyToTransform() {
        return null;
    }

}
