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

import org.apache.commons.lang.StringUtils;
import org.openscore.lang.entities.LoopStatement;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class ForTransformer implements Transformer<String, LoopStatement>{
    @Override
    public LoopStatement transform(String rawData) {
        if (StringUtils.isEmpty(rawData)) {
            return null;
        }
        String[] strings = rawData.split(Pattern.quote(" in "));
        String varName = strings[0];
        String collectionExpression = strings[1];
        return new LoopStatement(varName, collectionExpression, LoopStatement.Type.FOR);
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
