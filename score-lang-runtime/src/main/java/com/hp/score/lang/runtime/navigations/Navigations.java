/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package com.hp.score.lang.runtime.navigations;

import org.apache.commons.lang3.tuple.Pair;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.score.api.execution.ExecutionParametersConsts;
import com.hp.score.lang.ExecutionRuntimeServices;
import com.hp.score.lang.runtime.env.ParentFlowData;
import com.hp.score.lang.runtime.env.ParentFlowStack;
import com.hp.score.lang.runtime.env.RunEnvironment;
import com.hp.score.lang.runtime.steps.AbstractSteps;

import static com.hp.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;
import static com.hp.score.lang.entities.ScoreLangConstants.*;
import static com.hp.score.lang.runtime.events.LanguageEventData.EXCEPTION;

/**
 * @author stoneo
 * @since 22/10/2014
 * @version $Id$
 */
public class Navigations {

    /**
     * Calculate the next step position to navigate to, and return it
     *
     * @param runEnv the run environment
     * @param executionRuntimeServices the runtime services
     * @param RUNNING_EXECUTION_PLAN_ID current running execution plan id
     * @param refId the id of the reference (operation/flow) we need to navigate to
     * @param nextStepId the given id of the next step
     * @return the step id the score engine needs to navigate to next
     */
	public Long navigate(
            @Param(RUN_ENV) RunEnvironment runEnv,
            @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
		    @Param(ExecutionParametersConsts.RUNNING_EXECUTION_PLAN_ID) Long RUNNING_EXECUTION_PLAN_ID,
            @Param(REF_ID) String refId,
            @Param(NEXT_STEP_ID_KEY) Long nextStepId) {

        // If we have an error key stored, we fire an error event and return null as the next position
		if(executionRuntimeServices.hasStepErrorKey()) {
			AbstractSteps.fireEvent(executionRuntimeServices, runEnv, EVENT_STEP_ERROR, "Error detected during step",
				Pair.of(EXCEPTION, new RuntimeException(executionRuntimeServices.getStepErrorKey())));
			return null;
		}

        // If there is a position for the next step, we return it as the next step position
		Long nextStepPosition = runEnv.removeNextStepPosition();
		if(nextStepPosition != null) {
			return nextStepPosition;
		}

        // If the refId was given, we should now navigate to the first step of the given ref (operation/flow).
		if(refId != null) {
            // We create ParentFlowData object containing the current running execution plan id and
            // the next step id to navigate to in the current execution plan,
            // and we push it to the ParentFlowStack for future use (once we finish running the ref operation/flow)
			ParentFlowStack stack = runEnv.getParentFlowStack();
			stack.pushParentFlowData(new ParentFlowData(RUNNING_EXECUTION_PLAN_ID, nextStepId));
            // We request the score engine to switch the execution plan to the one of the given refId once it can
			Long subFlowRunningExecutionPlanId = executionRuntimeServices.getSubFlowRunningExecutionPlan(refId);
			executionRuntimeServices.requestToChangeExecutionPlan(subFlowRunningExecutionPlanId);
            // We return the start step of the given ref as the next step to execute
            // (in the new running execution plan that will be set)
			return executionRuntimeServices.getSubFlowBeginStep(refId);
		}

        // If there was no next step Id given, and we have a parent flow in the stack, it means that we finished the
        // execution of the sub-flow or operation.
        // We now pop the parent flow data from the stack, and request the score engine to switch to the parent
        // execution plan id once it can, and we return the calculated next position that was stored there
		if(nextStepId == null && !runEnv.getParentFlowStack().isEmpty()) {
			ParentFlowData parentFlowData = runEnv.getParentFlowStack().popParentFlowData();
			executionRuntimeServices.requestToChangeExecutionPlan(parentFlowData.getRunningExecutionPlanId());
			return parentFlowData.getPosition();
		}

		return nextStepId;
	}

}
