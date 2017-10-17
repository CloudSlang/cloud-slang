/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.model;

import io.cloudslang.lang.entities.bindings.Argument;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ExternalStep extends Step {
    public ExternalStep(
            String name,
            Map<String, Serializable> preStepActionData,
            Map<String, Serializable> postStepActionData,
            List<Argument> arguments,
            List<Map<String, String>> navigationStrings,
            String refId,
            boolean parallelLoop,
            boolean onFailureStep
    ) {
        super(
                name,
                preStepActionData,
                postStepActionData,
                arguments,
                navigationStrings,
                refId,
                parallelLoop,
                onFailureStep
        );
    }

    @Override
    public boolean requiresValidation() {
        return false;
    }
}
