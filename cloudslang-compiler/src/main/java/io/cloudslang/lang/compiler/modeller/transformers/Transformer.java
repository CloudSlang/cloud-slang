/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
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

import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import java.util.List;

public interface Transformer<F, T> {

    TransformModellingResult<T> transform(F rawData);

    List<Scope> getScopes();

    String keyToTransform();

    enum Scope {
        BEFORE_STEP,
        AFTER_STEP,
        BEFORE_EXECUTABLE,
        AFTER_EXECUTABLE,
        ACTION
    }

}
