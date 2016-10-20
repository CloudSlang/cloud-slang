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

import io.cloudslang.lang.compiler.modeller.model.Workflow;
import java.util.List;

/**
 * Created by Ifat Gavish on 24/02/2016
 */
public class WorkflowModellingResult implements ModellingResult {

    private final Workflow workflow;
    private final List<RuntimeException> errors;

    public WorkflowModellingResult(Workflow workflow, List<RuntimeException> errors) {
        this.workflow = workflow;
        this.errors = errors;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public List<RuntimeException> getErrors() {
        return errors;
    }
}
