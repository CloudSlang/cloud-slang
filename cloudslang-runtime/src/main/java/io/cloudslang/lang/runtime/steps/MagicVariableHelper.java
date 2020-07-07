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
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.RuntimeConstants;
import io.cloudslang.score.lang.ExecutionRuntimeServices;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MagicVariableHelper {

    public Map<String, Value> getGlobalContext(ExecutionRuntimeServices executionRuntimeServices) {
        Map<String, Value> globalContext = new HashMap<>();
        String executionId = String.valueOf(executionRuntimeServices.getExecutionId());
        globalContext.put(RuntimeConstants.EXECUTION_ID, ValueFactory.create(executionId));
        return Collections.unmodifiableMap(globalContext);
    }
}
