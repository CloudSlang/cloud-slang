/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/
package org.openscore.lang.runtime.navigations;

import com.hp.oo.sdk.content.annotations.Param;
import org.openscore.lang.runtime.env.RunEnvironment;
import org.openscore.lang.runtime.steps.AbstractSteps;
import org.apache.commons.lang3.tuple.Pair;
import org.openscore.lang.ExecutionRuntimeServices;

import static org.openscore.lang.entities.ScoreLangConstants.RUN_ENV;
import static org.openscore.lang.entities.ScoreLangConstants.SLANG_EXECUTION_EXCEPTION;
import static org.openscore.lang.runtime.events.LanguageEventData.EXCEPTION;
import static org.openscore.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;

/**
 * @author stoneo
 * @since 22/10/2014
 * @version $Id$
 */
public class Navigations {

    /**
     * Returns the next step position to navigate to.
     * In case an error key was set in the runtime services, throw an error event and return null.
     *
     * @param runEnv the run environment
     * @param executionRuntimeServices the runtime services
     * @return the step id the score engine needs to navigate to the next step
     */
	public Long navigate(@Param(RUN_ENV) RunEnvironment runEnv,
                        @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices) {

        // If we have an error key stored, we fire an error event and return null as the next position
		if(executionRuntimeServices.hasStepErrorKey()) {
			AbstractSteps.fireEvent(executionRuntimeServices, runEnv, SLANG_EXECUTION_EXCEPTION, "Error detected during step",
				Pair.of(EXCEPTION, executionRuntimeServices.getStepErrorKey()));
			return null;
		}

        // return the next step position from the run env
		return runEnv.removeNextStepPosition();

}

}
