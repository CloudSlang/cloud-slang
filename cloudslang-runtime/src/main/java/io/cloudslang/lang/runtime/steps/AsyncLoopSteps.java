/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.steps;

import com.hp.oo.sdk.content.annotations.Param;
import io.cloudslang.lang.entities.AsyncLoopStatement;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.runtime.bindings.AsyncLoopBinding;
import io.cloudslang.lang.runtime.bindings.LoopsBinding;
import io.cloudslang.lang.runtime.env.*;
import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;
import io.cloudslang.score.api.EndBranchDataContainer;
import io.cloudslang.score.api.execution.ExecutionParametersConsts;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.cloudslang.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;

/**
 * Date: 3/25/2015
 *
 * @author Bonczidai Levente
 */
@Component
public class AsyncLoopSteps extends AbstractSteps {

    @Autowired
    private AsyncLoopBinding asyncLoopBinding;

    @Autowired
    private LoopsBinding loopsBinding;

    private static final Logger logger = Logger.getLogger(AsyncLoopSteps.class);

    public void addBranches(@Param(ScoreLangConstants.ASYNC_LOOP_KEY) AsyncLoopStatement async_loop,
                            @Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                            @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                            @Param(ScoreLangConstants.NODE_NAME_KEY) String nodeName,
                            @Param(ExecutionParametersConsts.RUNNING_EXECUTION_PLAN_ID) Long RUNNING_EXECUTION_PLAN_ID,
                            @Param(ScoreLangConstants.NEXT_STEP_ID_KEY) Long nextStepId,
                            @Param(ScoreLangConstants.REF_ID) String refId) {

        try {
            runEnv.getExecutionPath().forward();
            runEnv.removeCallArguments();
            runEnv.removeReturnValues();

            Context flowContext = runEnv.getStack().popContext();

            List<Serializable> splitData = asyncLoopBinding.bindAsyncLoopList(async_loop, flowContext, nodeName);

            Long branchBeginStep = executionRuntimeServices.getSubFlowBeginStep(refId);
            //runEnv.putNextStepPosition(branchBeginStep);
            runEnv.putNextStepPosition(nextStepId);
            runEnv.getExecutionPath().down();

            for (Serializable splitItem : splitData) {
                RunEnvironment branchRuntimeEnvironment = (RunEnvironment) SerializationUtils.clone(runEnv);
                Context branchContext = (Context) SerializationUtils.clone(flowContext);
                branchContext.putVariable(async_loop.getVarName(), splitItem);

                updateCallArgumentsAndPushContextToStack(branchRuntimeEnvironment, branchContext, new HashMap<String, Serializable>());

                createBranch(
                        branchRuntimeEnvironment,
                        executionRuntimeServices,
                        RUNNING_EXECUTION_PLAN_ID,
                        refId,
                        nextStepId,
                        branchBeginStep,
                        branchContext.getImmutableViewOfVariables());
            }

        } catch (RuntimeException e) {
            logger.error("There was an error running the add branches execution step of: \'" + nodeName + "\'. Error is: " + e.getMessage());
            throw new RuntimeException("Error running: " + nodeName + ": " + e.getMessage(), e);
        }

    }

    public void joinBranches(@Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                             @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                             @Param(ScoreLangConstants.TASK_PUBLISH_KEY) List<Output> taskPublishValues,
                             @Param(ScoreLangConstants.TASK_NAVIGATION_KEY) Map<String, ResultNavigation> taskNavigationValues,
                             @Param(ScoreLangConstants.PREVIOUS_STEP_ID_KEY) Long previousStepId,
                             @Param(ScoreLangConstants.NODE_NAME_KEY) String nodeName) {
        try {
            List<EndBranchDataContainer> branches = executionRuntimeServices.getFinishedChildBranchesData();

            for (EndBranchDataContainer branch : branches) {
                Map<String,Serializable> branchContext  = branch.getContexts();

                //TODO - async loop - process contexts
            }

            //if(runEnv.getExecutionPath().getDepth() > 0) runEnv.getExecutionPath().up();
            Map<String, Serializable> outputs = new HashMap<>();
            ReturnValues returnValues = new ReturnValues(outputs, "SUCCESS");
            runEnv.putReturnValues(returnValues);
            runEnv.putNextStepPosition(0L);
        } catch (RuntimeException e){
            logger.error("There was an error running the end task execution step of: \'" + nodeName + "\'. Error is: " + e.getMessage());
            throw new RuntimeException("Error running: \'" + nodeName + "\': " + e.getMessage(), e);
        }
    }

    private void createBranch(RunEnvironment runEnv,
                              ExecutionRuntimeServices executionRuntimeServices,
                              Long RUNNING_EXECUTION_PLAN_ID,
                              String refId,
                              Long nextStepId,
                              Long branchBeginStep,
                              Map<String, Serializable> context) {

        // TODO - async task - extract method here
        // create ParentFlowData object containing the current running execution plan id and
        // the next step id to navigate to in the current execution plan,
        // and push it to the ParentFlowStack for future use (once we finish running the ref operation/flow)
        ParentFlowStack stack = runEnv.getParentFlowStack();
        stack.pushParentFlowData(new ParentFlowData(RUNNING_EXECUTION_PLAN_ID, nextStepId));

        Map<String, Serializable> branchContext = new HashMap<>(context);
        branchContext.put(ScoreLangConstants.RUN_ENV, runEnv);
        executionRuntimeServices.addBranch(branchBeginStep, refId, branchContext);
    }
}
