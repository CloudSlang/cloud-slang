/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.navigations;

import com.hp.oo.sdk.content.annotations.Param;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.lang.runtime.steps.AbstractExecutionData;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import org.apache.commons.lang3.tuple.Pair;

import static io.cloudslang.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;

/**
 * @author stoneo
 * @version $Id$
 * @since 22/10/2014
 */
public class Navigations {

    /**
     * Returns the next step position to navigate to.
     * In case an error key was set in the runtime services, throw an error event and return null.
     *
     * @param runEnv                   the run environment
     * @param executionRuntimeServices the runtime services
     * @return the step id the score engine needs to navigate to the next step
     */
    public Long navigate(@Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                         @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices) {

        // If we have an error key stored, we fire an error event and return null as the next position
        if (executionRuntimeServices.hasStepErrorKey()) {
            AbstractExecutionData.fireEvent(executionRuntimeServices, runEnv,
                    ScoreLangConstants.SLANG_EXECUTION_EXCEPTION,
                    "Error detected during step", LanguageEventData.StepType.NAVIGATION, null,
                    Pair.of(LanguageEventData.EXCEPTION, executionRuntimeServices.getStepErrorKey()));
            throw new RuntimeException(executionRuntimeServices.getStepErrorKey());
        }

        // return the next step position from the run env
        return runEnv.removeNextStepPosition();

    }

}
