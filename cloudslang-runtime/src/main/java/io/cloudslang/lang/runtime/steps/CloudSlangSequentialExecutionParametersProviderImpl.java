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

import io.cloudslang.lang.compiler.modeller.model.SeqStep;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.runtime.api.sequential.SequentialExecutionParametersProvider;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.substring;

public class CloudSlangSequentialExecutionParametersProviderImpl implements SequentialExecutionParametersProvider,
        Serializable {
    private static final long serialVersionUID = -6086097846840796564L;

    public static final String SEQUENTIAL_PARAMETER = "Parameter(";
    private final Map<String, Value> currentContext;
    private final List<SeqStep> seqSteps;

    public CloudSlangSequentialExecutionParametersProviderImpl(
            Map<String, Value> currentContext,
            List<SeqStep> seqSteps) {
        this.currentContext = currentContext;
        this.seqSteps = seqSteps;
    }

    @Override
    public Object[] getExecutionParameters() {
        Map<String, String> execParams = new HashMap<>();
        for (SeqStep step : seqSteps) {
            String args = step.getArgs();
            if (StringUtils.startsWith(args, SEQUENTIAL_PARAMETER)) {
                String paramName = substring(args, SEQUENTIAL_PARAMETER.length(), args.length() - 1)
                        .replaceAll("^\"|\"$", "");
                Value value = currentContext.get(paramName);
                if (value != null) {
                    //TODO handle sensitive values
                    execParams.put(paramName, value.get().toString());
                }
            }
        }
        // TODO fix get execution from map
        return new Object[]{execParams};
    }
}
