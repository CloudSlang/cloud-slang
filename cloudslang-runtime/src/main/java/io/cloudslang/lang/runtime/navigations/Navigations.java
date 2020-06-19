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
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.runtime.env.Context;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.lang.runtime.steps.AbstractExecutionData;
import io.cloudslang.score.lang.SystemContext;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

import static io.cloudslang.lang.entities.ScoreLangConstants.CURRENT_STEP_ID_KEY;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.SYSTEM_CONTEXT;

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
     * @param systemContext            the system context
     * @param currentStepId            the current step's id
     * @return the step id the score engine needs to navigate to the next step
     */
    public Long navigate(@Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                         @Param(SYSTEM_CONTEXT) SystemContext systemContext,
                         @Param(CURRENT_STEP_ID_KEY) Long currentStepId) {

        if (shouldPause(systemContext)) {
            return currentStepId;
        }

        // If we have an error key stored, we fire an error event and return null as the next position
        if (systemContext.hasStepErrorKey()) {
            AbstractExecutionData.fireEvent(
                    systemContext,
                runEnv,
                ScoreLangConstants.SLANG_EXECUTION_EXCEPTION,
                "Error detected during step",
                LanguageEventData.StepType.NAVIGATION,
                null,
                extractContext(runEnv),
                Pair.of(LanguageEventData.EXCEPTION, systemContext.getStepErrorKey()));
            throw new RuntimeException(systemContext.getStepErrorKey());
        }

        // return the next step position from the run env
        return runEnv.removeNextStepPosition();

    }

    private boolean shouldPause(SystemContext systemContext) {
        return systemContext.isPaused() && !systemContext.hasStepErrorKey();
    }

    private Map<String, Value> extractContext(@Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv) {
        Context context = runEnv.getStack().popContext();
        Map<String, Value> contextMap;
        if (context != null) {
            runEnv.getStack().pushContext(context);
            contextMap = context.getImmutableViewOfVariables();
        } else {
            contextMap = new HashMap<>();
        }
        return contextMap;
    }

}
