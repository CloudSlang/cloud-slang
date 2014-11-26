/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package com.hp.score.lang.runtime.navigations;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.score.lang.ExecutionRuntimeServices;
import com.hp.score.lang.runtime.env.RunEnvironment;
import com.hp.score.lang.runtime.steps.AbstractSteps;
import org.apache.commons.lang3.tuple.Pair;

import static com.hp.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;
import static com.hp.score.lang.entities.ScoreLangConstants.SLANG_EXECUTION_EXCEPTION;
import static com.hp.score.lang.entities.ScoreLangConstants.RUN_ENV;
import static com.hp.score.lang.runtime.events.LanguageEventData.EXCEPTION;

/**
 * @author stoneo
 * @since 22/10/2014
 * @version $Id$
 */
public class Navigations {

    /**
     * Return the next step position to navigate to
     * In case an error key was set in the runtime services, throw an error event & return null
     *
     * @param runEnv the run environment
     * @param executionRuntimeServices the runtime services
     * @return the step id the score engine needs to navigate to next
     */
	public Long navigate(@Param(RUN_ENV) RunEnvironment runEnv,
                        @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices) {

        // If we have an error key stored, we fire an error event and return null as the next position
		if(executionRuntimeServices.hasStepErrorKey()) {
			AbstractSteps.fireEvent(executionRuntimeServices, runEnv, SLANG_EXECUTION_EXCEPTION, "Error detected during step",
				Pair.of(EXCEPTION, new RuntimeException(executionRuntimeServices.getStepErrorKey())));
			return null;
		}

        // return the next step position from the run env
		return runEnv.removeNextStepPosition();

}

}
