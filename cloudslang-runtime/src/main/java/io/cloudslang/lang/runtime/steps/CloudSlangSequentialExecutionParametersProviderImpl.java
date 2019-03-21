/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.steps;

import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.runtime.api.sequential.SequentialExecutionParametersProvider;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudSlangSequentialExecutionParametersProviderImpl implements SequentialExecutionParametersProvider,
        Serializable {
    private static final long serialVersionUID = -6086097846840796564L;

    private final Map<String, Value> currentContext;
    private final List<String> seqSteps;

    public CloudSlangSequentialExecutionParametersProviderImpl(
            Map<String, Value> currentContext,
            List<String> seqSteps) {
        this.currentContext = currentContext;
        this.seqSteps = seqSteps;
    }

    @Override
    public Object[] getExecutionParameters() {
        Map<String, String> execParams = new HashMap<>();
        for (String step : seqSteps) {
            Value value = currentContext.get(step);
            if (value != null) {
                //TODO handle sensitive values
                execParams.put(step, value.get().toString());
            }
        }
        // TODO fix get execution from map
        return new Object[]{execParams};
    }
}
