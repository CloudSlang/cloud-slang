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

import com.hp.oo.sdk.content.plugin.SerializableSessionObject;
import io.cloudslang.lang.compiler.modeller.model.SeqStep;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.runtime.api.sequential.SequentialExecutionParametersProvider;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.substring;

public class CloudSlangSequentialExecutionParametersProviderImpl implements SequentialExecutionParametersProvider {
    public static final String UFT_PARAMETER = "Parameter(";
    private final Map<String, SerializableSessionObject> serializableSessionData;
    private final Map<String, Value> currentContext;
    private final Map<String, Map<String, Object>> nonSerializableExecutionData;
    private final String nodeNameWithDepth;
    private final int depth;
    private final List<SeqStep> seqSteps;

    public CloudSlangSequentialExecutionParametersProviderImpl(
            Map<String, SerializableSessionObject> serializableSessionData,
            Map<String, Value> currentContext,
            Map<String, Map<String, Object>> nonSerializableExecutionData,
            List<SeqStep> seqSteps, String nodeNameWithDepth,
            int depth) {
        this.serializableSessionData = serializableSessionData;
        this.currentContext = currentContext;
        this.nonSerializableExecutionData = nonSerializableExecutionData;
        this.seqSteps = seqSteps;
        this.nodeNameWithDepth = nodeNameWithDepth;
        this.depth = depth;
    }

    @Override
    public Object[] getExecutionParameters() {
        Map<String, String> execParams = new HashMap<>();
        for (SeqStep step : seqSteps) {
            String args = step.getArgs();
            if (StringUtils.startsWith(args, UFT_PARAMETER)) {
                String paramName = substring(args, UFT_PARAMETER.length(), args.length() - 1);
                Value value = currentContext.get(paramName);
                if (value != null) {
                    //TODO handle sensitive values
                    execParams.put(paramName, value.get().toString());
                }
            }
        }
        // TODO fix get execution from map
        return new Object[]{execParams, nonSerializableExecutionData.get("execution").get("execution")};
    }
}
