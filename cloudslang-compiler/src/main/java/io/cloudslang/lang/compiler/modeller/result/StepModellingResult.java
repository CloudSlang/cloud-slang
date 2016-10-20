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

import io.cloudslang.lang.compiler.modeller.model.Step;
import java.util.List;

/**
 * Created by Ifat Gavish on 24/02/2016
 */
public class StepModellingResult implements ModellingResult {

    private final Step step;
    private final List<RuntimeException> errors;

    public StepModellingResult(Step step, List<RuntimeException> errors) {
        this.step = step;
        this.errors = errors;
    }

    public Step getStep() {
        return step;
    }

    public List<RuntimeException> getErrors() {
        return errors;
    }
}
