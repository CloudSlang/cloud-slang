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

import io.cloudslang.lang.compiler.SlangTextualKeys;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author rabinovi
 * @since 4/26/2016
 */

@Component
public class DependenciesTransformer implements Transformer<List<String>, List<String>> {
    @Override
    public List<String> transform(List<String> rawData) {
        return (rawData == null) ? Collections.<String>emptyList() : rawData;
    }

    @Override
    public List<Scope> getScopes() {return Arrays.asList(Scope.ACTION);}

    @Override
    public String keyToTransform() {return SlangTextualKeys.PYTHON_ACTION_DEPENDENCIES_KEY;}
}
