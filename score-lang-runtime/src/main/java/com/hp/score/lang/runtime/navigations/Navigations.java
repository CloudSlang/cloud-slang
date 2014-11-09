/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package com.hp.score.lang.runtime.navigations;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.score.api.execution.ExecutionParametersConsts;
import com.hp.score.lang.ExecutionRuntimeServices;
import com.hp.score.lang.entities.ScoreLangConstants;
import com.hp.score.lang.runtime.env.ParentFlowData;
import com.hp.score.lang.runtime.env.ParentFlowStack;
import com.hp.score.lang.runtime.env.RunEnvironment;

/**
 * User: stoneo
 * Date: 22/10/2014
 * Time: 15:32
 */
public class Navigations {

    public Long navigate(String subFlowId,
                         ExecutionRuntimeServices executionRuntimeServices,
                         @Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                         @Param(ExecutionParametersConsts.RUNNING_EXECUTION_PLAN_ID) Long RUNNING_EXECUTION_PLAN_ID,
                         @Param(ScoreLangConstants.NEXT_STEP_ID_KEY) Long nextStepId) {

        Long nextStepPosition = runEnv.removeNextStepPosition();
        if (nextStepPosition != null) {
            return nextStepPosition;
        }

        if (subFlowId != null) {
            ParentFlowStack stack = runEnv.getParentFlowStack();
            stack.pushParentFlowData(new ParentFlowData(RUNNING_EXECUTION_PLAN_ID, nextStepId));

            Long subFlowRunningExecutionPlanId = executionRuntimeServices.getSubFlowRunningExecutionPlan(subFlowId);
            executionRuntimeServices.requestToChangeExecutionPlan(subFlowRunningExecutionPlanId);
            return executionRuntimeServices.getSubFlowBeginStep(subFlowId);
        }

        if (nextStepId == null && !runEnv.getParentFlowStack().isEmpty()) {
            ParentFlowData parentFlowData = runEnv.getParentFlowStack().popParentFlowData();
            executionRuntimeServices.requestToChangeExecutionPlan(parentFlowData.getRunningExecutionPlanId());
            return parentFlowData.getPosition();
        }

        return nextStepId;
    }
}
