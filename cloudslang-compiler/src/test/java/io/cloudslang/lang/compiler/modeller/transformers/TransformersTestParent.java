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

import java.util.List;

import junit.framework.Assert;

/**
 * @author Bonczidai Levente
 * @since 7/19/2016
 */
public abstract class TransformersTestParent {

    protected <F, T> void transformAndThrowFirstException(Transformer<F, T> transformer, F arg) {
        List<RuntimeException> errors = transformer.transform(arg).getErrors();
        Assert.assertNotNull(errors);
        Assert.assertTrue(errors.size() > 0);
        throw errors.get(0);
    }

    protected <F, T> void transformAndAssertNoErrorsTransformer(Transformer<F, T> transformer, F arg) {
        List<RuntimeException> errors = transformer.transform(arg).getErrors();
        Assert.assertNotNull(errors);
        Assert.assertTrue(errors.size() == 0);
    }

}
