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
import javafx.util.Pair;
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
    private final boolean external;

    public CloudSlangSequentialExecutionParametersProviderImpl(
            Map<String, Value> currentContext,
            List<SeqStep> seqSteps,
            Boolean external) {
        this.currentContext = currentContext;
        this.seqSteps = seqSteps;
        this.external = external;
    }

    @Override
    public Map<String, Pair<Boolean, Value>> getExecutionParameters() {
        Map<String, Pair<Boolean, Value>> execParams = new HashMap<>();

        if (external) {
            // External seq operation doesn't require step args filtering since all the inputs are external
            currentContext.forEach((key, value) -> execParams.put(key, new Pair<>(false, value)));
        } else {
            for (SeqStep step : seqSteps) {
                String args = step.getArgs();
                if (StringUtils.startsWith(args, SEQUENTIAL_PARAMETER)) {
                    String paramName = substring(args, SEQUENTIAL_PARAMETER.length(), args.length() - 1)
                            .replaceAll("^\"|\"$", "");
                    Value value = currentContext.get(paramName);
                    if (value != null) {
                        execParams.put(paramName, new Pair<>(true, value));
                    }
                }
            }
            addWholeContext(execParams);
        }
        return execParams;
    }

    private void addWholeContext(Map<String, Pair<Boolean, Value>> execParams) {
        currentContext.forEach((key, value) -> {
            if (!execParams.containsKey(key)) {
                execParams.put(key, new Pair<>(false, value));
            }
        });
    }

    @Override
    public boolean getExternal() {
        return external;
    }
}
