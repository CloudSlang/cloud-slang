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

import org.openscore.lang.entities.AsyncLoopStatement;
import org.openscore.lang.entities.ScoreLangConstants;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Date: 3/17/2015
 *
 * @author Bonczidai Levente
 */
@Component
public class AsyncLoopForTransformer extends AbstractForTransformer implements Transformer<String, AsyncLoopStatement> {

    @Override
    public AsyncLoopStatement transform(String rawData) {
        return (AsyncLoopStatement) transformToLoopStatement(rawData, true);
    }

    @Override
    public List<Transformer.Scope> getScopes() {
        return Arrays.asList(Transformer.Scope.BEFORE_TASK);
    }

    @Override
    public String keyToTransform() {
        return ScoreLangConstants.ASYNC_LOOP_KEY;
    }

}
