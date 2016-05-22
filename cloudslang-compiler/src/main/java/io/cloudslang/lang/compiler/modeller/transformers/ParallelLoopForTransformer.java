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

import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.entities.ParallelLoopStatement;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Date: 3/25/2015
 *
 * @author Bonczidai Levente
 */
@Component
public class ParallelLoopForTransformer extends AbstractForTransformer implements Transformer<String, ParallelLoopStatement> {

    @Override
    public ParallelLoopStatement transform(String rawData) {
        return (ParallelLoopStatement) transformToLoopStatement(rawData, true);
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.singletonList(Scope.BEFORE_STEP);
    }

    @Override
    public String keyToTransform() {
        return SlangTextualKeys.PARALLEL_LOOP_KEY;
    }

}
