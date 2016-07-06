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

/*
 * Created by orius123 on 05/11/14.
 */

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import java.io.Serializable;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PythonActionTransformer extends AbstractTransformer implements Transformer<Map<String, Serializable>, Map<String, Serializable>> {
    @Autowired
    private DependencyFormatValidator dependencyFormatValidator;

    private static Set<String> mandatoryKeySet = Sets.newHashSet(SlangTextualKeys.PYTHON_ACTION_SCRIPT_KEY);
    private static Set<String> optionalKeySet = Sets.newHashSet(SlangTextualKeys.PYTHON_ACTION_DEPENDENCIES_KEY);

    @Override
    public Map<String, Serializable> transform(Map<String, Serializable> rawData) {
        if (rawData != null) {
            validateKeySet(rawData.keySet(), mandatoryKeySet, optionalKeySet);
            Collection<String> dependencies = (List<String>) rawData.get(SlangTextualKeys.PYTHON_ACTION_DEPENDENCIES_KEY);
            if(dependencies != null) {
                for (String dependency: dependencies) {
                    dependencyFormatValidator.validateDependency(dependency);
                }
            }
        }
        return rawData;
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.singletonList(Scope.ACTION);
    }

    @Override
    public String keyToTransform() {
        return SlangTextualKeys.PYTHON_ACTION_KEY;
    }

}
