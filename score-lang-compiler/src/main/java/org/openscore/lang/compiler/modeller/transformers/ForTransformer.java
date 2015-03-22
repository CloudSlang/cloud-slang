/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.openscore.lang.compiler.modeller.transformers;

import org.openscore.lang.entities.ForLoopStatement;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ForTransformer extends AbstractForTransformer implements Transformer<String, ForLoopStatement>{

    @Override
    public ForLoopStatement transform(String rawData) {
        return (ForLoopStatement) transformToLoopStatement(rawData, false);
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
