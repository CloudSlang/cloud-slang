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

import io.cloudslang.lang.entities.bindings.InOutParam;

/**
 * @author Bonczidai Levente
 * @since 1/25/2016
 */
public abstract class InOutTransformer extends AbstractInOutForTransformer {

    public abstract Class<? extends InOutParam> getTransformedObjectsClass();

}
