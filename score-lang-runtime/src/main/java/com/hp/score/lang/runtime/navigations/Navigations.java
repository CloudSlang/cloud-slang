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

	public Long navigate(
            @Param(RUN_ENV) RunEnvironment runEnv,
            @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
            @Param(REF_ID) String refId,
		    @Param(ExecutionParametersConsts.RUNNING_EXECUTION_PLAN_ID) Long RUNNING_EXECUTION_PLAN_ID,
            @Param(NEXT_STEP_ID_KEY) Long nextStepId) {

		if(executionRuntimeServices.hasStepErrorKey()) {
			AbstractSteps.fireEvent(executionRuntimeServices, runEnv, EVENT_STEP_ERROR, "Error detected during step",
				Pair.of(EXCEPTION, new RuntimeException(executionRuntimeServices.getStepErrorKey())));
			return null;// TODO TEMP - fail the flow
		}
		Long nextStepPosition = runEnv.removeNextStepPosition();
		if(nextStepPosition != null) {
			return nextStepPosition;
		}
		if(refId != null) {
			ParentFlowStack stack = runEnv.getParentFlowStack();
			stack.pushParentFlowData(new ParentFlowData(RUNNING_EXECUTION_PLAN_ID, nextStepId));
			Long subFlowRunningExecutionPlanId = executionRuntimeServices.getSubFlowRunningExecutionPlan(refId);
			executionRuntimeServices.requestToChangeExecutionPlan(subFlowRunningExecutionPlanId);
			return executionRuntimeServices.getSubFlowBeginStep(refId);
		}
		if(nextStepId == null && !runEnv.getParentFlowStack().isEmpty()) {
			ParentFlowData parentFlowData = runEnv.getParentFlowStack().popParentFlowData();
			executionRuntimeServices.requestToChangeExecutionPlan(parentFlowData.getRunningExecutionPlanId());
			return parentFlowData.getPosition();
		}
		return nextStepId;
	}

}
