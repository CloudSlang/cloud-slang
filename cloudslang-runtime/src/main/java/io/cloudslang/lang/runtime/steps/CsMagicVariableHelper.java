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
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component("csMagicVariableHelper")
public class CsMagicVariableHelper {

    private static final String RAS_OPERATOR_PATH = "RAS_Operator_Path";

    public Map<String, Value> getGlobalContext(ExecutionRuntimeServices executionRuntimeServices) {
        Map<String, Value> globalContext = new HashMap<>();
        String executionId = String.valueOf(executionRuntimeServices.getExecutionId());
        String userId = executionRuntimeServices.getEffectiveRunningUser();
        String workerGroup = executionRuntimeServices.getWorkerGroupName();
        globalContext.put(RuntimeConstants.EXECUTION_ID, ValueFactory.create(executionId));
        globalContext.put(RuntimeConstants.USER_ID, ValueFactory.create(userId));
        globalContext.put(RuntimeConstants.WORKER_GROUP, ValueFactory.create(workerGroup == null ?
                                                                             RAS_OPERATOR_PATH : workerGroup));
        globalContext.put(RuntimeConstants.RUN_ID, ValueFactory.create(executionId));
        return Collections.unmodifiableMap(globalContext);
    }
}
