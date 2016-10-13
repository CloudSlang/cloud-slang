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

import io.cloudslang.lang.compiler.modeller.model.Action;
import java.util.List;

/**
 * Created by Ifat Gavish on 24/02/2016
 */
public class ActionModellingResult implements ModellingResult {

    private final Action action;
    private final List<RuntimeException> errors;

    public ActionModellingResult(Action action, List<RuntimeException> errors) {
        this.action = action;
        this.errors = errors;
    }

    public Action getAction() {
        return action;
    }

    public List<RuntimeException> getErrors() {
        return errors;
    }
}
