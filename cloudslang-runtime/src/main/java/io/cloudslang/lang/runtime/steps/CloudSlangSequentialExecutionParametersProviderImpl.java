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
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.substring;
import static org.apache.commons.lang3.tuple.Pair.of;

public class CloudSlangSequentialExecutionParametersProviderImpl implements SequentialExecutionParametersProvider,
        Serializable {
    private static final long serialVersionUID = -6086097846840796564L;

    public static final String SEQUENTIAL_PARAMETER = "Parameter(";
    private final Map<String, Value> currentContext;
    private final List<SeqStep> seqSteps;
    private final boolean external;

    public CloudSlangSequentialExecutionParametersProviderImpl(Map<String, Value> currentContext,
                                                               List<SeqStep> seqSteps,
                                                               Boolean external) {
        this.currentContext = currentContext;
        this.seqSteps = seqSteps;
        this.external = external;
    }

    @Override
    public Map<String, Pair<Serializable, Boolean>> getExecutionParameters() {
        Set<String> paramsUsedInScript;
        if (external) {
            paramsUsedInScript = new HashSet<>();
        } else {
            paramsUsedInScript = seqSteps.stream()
                    .map(SeqStep::getArgs)
                    .filter(args -> startsWith(args, SEQUENTIAL_PARAMETER))
                    .map(this::extractParameter)
                    .collect(toSet());
        }

        return currentContext.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> of(entry.getValue(), paramsUsedInScript.contains(entry.getKey()))));
    }

    private String extractParameter(String args) {
        return substring(args, SEQUENTIAL_PARAMETER.length(), args.length() - 1).replaceAll("^\"|\"$", "");
    }

    @Override
    public boolean getExternal() {
        return external;
    }
}
