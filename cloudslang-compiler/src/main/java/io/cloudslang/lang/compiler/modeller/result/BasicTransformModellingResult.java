/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.result;

import java.util.List;

/**
 * @author Bonczidai Levente
 * @since 7/19/2016
 */
public class BasicTransformModellingResult<T> implements TransformModellingResult<T> {

    private final T transformedData;
    private final List<RuntimeException> errors;

    public BasicTransformModellingResult(T transformedData, List<RuntimeException> errors) {
        this.transformedData = transformedData;
        this.errors = errors;
    }

    @Override
    public T getTransformedData() {
        return transformedData;
    }

    @Override
    public List<RuntimeException> getErrors() {
        return errors;
    }

}
