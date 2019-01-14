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
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.runtime.api.rpa.RpaExecutionParametersProvider;

import java.lang.reflect.Method;
import java.util.Map;

public class RpaExecutionParametersProviderImpl implements RpaExecutionParametersProvider {
    private final Map<String, SerializableSessionObject> serializableSessionData;
    private final Map<String, Value> currentContext;
    private final Map<String, Map<String, Object>> nonSerializableExecutionData;
    private final String nodeNameWithDepth;
    private final int depth;
    private Map<String, Map<String, Object>> rpaSteps;

    public RpaExecutionParametersProviderImpl(
            Map<String, SerializableSessionObject> serializableSessionData,
            Map<String, Value> currentContext,
            Map<String, Map<String, Object>> nonSerializableExecutionData,
            Map<String, Map<String, Object>> rpaSteps, String nodeNameWithDepth,
            int depth) {
        this.serializableSessionData = serializableSessionData;
        this.currentContext = currentContext;
        this.nonSerializableExecutionData = nonSerializableExecutionData;
        this.rpaSteps = rpaSteps;
        this.nodeNameWithDepth = nodeNameWithDepth;
        this.depth = depth;
    }

    @Override
    public Object[] getExecutionParameters(Method executionMethod) {
        return new Object[0]; //TODO parameters binding
    }
}
