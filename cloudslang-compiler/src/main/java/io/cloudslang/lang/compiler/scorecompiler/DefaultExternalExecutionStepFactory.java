/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.scorecompiler;

import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.score.api.ExecutionStep;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class DefaultExternalExecutionStepFactory implements ExternalExecutionStepFactory {


    private static final String EXTERNAL_STEPS_NOT_SUPPORTED = "CloudSlang does not support compiling external " +
            "steps. To provide this functionality, you must extend all necessary classes.";

    @Override
    public ExecutionStep createBeginExternalFlowStep(Long index, List<Argument> stepInputs,
                                                     Map<String, Serializable> preStepData,
                                                     String refId,
                                                     String stepName) {
        throw new UnsupportedOperationException(EXTERNAL_STEPS_NOT_SUPPORTED);
    }

    @Override
    public ExecutionStep createFinishExternalFlowStep(Long index,
                                                      Map<String, Serializable> postStepData,
                                                      Map<String, ResultNavigation> navigationValues,
                                                      String stepName,
                                                      boolean parallelLoop) {
        throw new UnsupportedOperationException(EXTERNAL_STEPS_NOT_SUPPORTED);
    }
}
