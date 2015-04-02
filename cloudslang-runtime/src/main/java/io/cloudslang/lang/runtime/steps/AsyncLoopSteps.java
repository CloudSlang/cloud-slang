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
import io.cloudslang.lang.runtime.bindings.OutputsBinding;
import io.cloudslang.lang.runtime.env.*;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;
import io.cloudslang.score.api.EndBranchDataContainer;
import io.cloudslang.score.api.execution.ExecutionParametersConsts;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import org.python.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.tuple.Pair;

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

    @Autowired
    private OutputsBinding outputsBinding;

    private static final Logger logger = Logger.getLogger(AsyncLoopSteps.class);

    public void addBranches(@Param(ScoreLangConstants.ASYNC_LOOP_KEY) AsyncLoopStatement async_loop,
                            @Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                            @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                            @Param(ScoreLangConstants.NODE_NAME_KEY) String nodeName,
                            @Param(ExecutionParametersConsts.RUNNING_EXECUTION_PLAN_ID) Long RUNNING_EXECUTION_PLAN_ID,
                            @Param(ScoreLangConstants.NEXT_STEP_ID_KEY) Long nextStepId,
                            @Param(ScoreLangConstants.REF_ID) String refId) {

        try {
            Context flowContext = runEnv.getStack().popContext();

            List<Serializable> splitData = asyncLoopBinding.bindAsyncLoopList(async_loop, flowContext, nodeName);

            fireEvent(
                    executionRuntimeServices,
                    runEnv,
                    ScoreLangConstants.EVENT_ASYNC_LOOP_INPUT_END,
                    "async loop list evaluated",
                    Pair.of(LanguageEventData.BOUND_ASYNC_LOOP_INPUTS, (Serializable) splitData),
                    Pair.of(LanguageEventData.levelName.TASK_NAME.name(), nodeName));

            Long branchBeginStep = executionRuntimeServices.getSubFlowBeginStep(refId);
            runEnv.putNextStepPosition(nextStepId);

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

                fireEvent(
                        executionRuntimeServices,
                        runEnv,
                        ScoreLangConstants.EVENT_BRANCH_START,
                        "async loop branch created",
                        Pair.of(ScoreLangConstants.REF_ID, refId),
                        Pair.of(ScoreLangConstants.SPLIT_ITEM_KEY, splitItem),
                        Pair.of(LanguageEventData.levelName.TASK_NAME.name(), nodeName));
            }

            updateCallArgumentsAndPushContextToStack(runEnv, flowContext, new HashMap<String, Serializable>());

        } catch (RuntimeException e) {
            logger.error("There was an error running the add branches execution step of: \'" + nodeName + "\'. Error is: " + e.getMessage());
            throw new RuntimeException("Error running: " + nodeName + ": " + e.getMessage(), e);
        }

    }

    public void joinBranches(@Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                             @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                             @Param(ScoreLangConstants.TASK_AGGREGATE_KEY) List<Output> taskAggregateValues,
                             @Param(ScoreLangConstants.TASK_NAVIGATION_KEY) Map<String, ResultNavigation> taskNavigationValues,
                             @Param(ScoreLangConstants.NODE_NAME_KEY) String nodeName) {
        try {
            List<EndBranchDataContainer> branches = executionRuntimeServices.getFinishedChildBranchesData();
            List<Map<String, Serializable>> branches_context = Lists.newArrayList();
            Context flowContext = runEnv.getStack().popContext();
            Map<String, Serializable> contextBeforeSplit = flowContext.getImmutableViewOfVariables();
            List<String> branchResults = Lists.newArrayList();

            for (EndBranchDataContainer branch : branches) {
                Map<String, Serializable> branchContext = branch.getContexts();

                RunEnvironment branchRuntimeEnvironment = (RunEnvironment) branchContext.get(ScoreLangConstants.RUN_ENV);

                branches_context.add(branchRuntimeEnvironment.getStack().popContext().getImmutableViewOfVariables());

                ReturnValues executableReturnValues = branchRuntimeEnvironment.removeReturnValues();
                branchResults.add(executableReturnValues.getResult());

                fireEvent(
                        executionRuntimeServices,
                        runEnv,
                        ScoreLangConstants.EVENT_BRANCH_END,
                        "async loop branch ended",
                        Pair.of("branchReturnValues", executableReturnValues),
                        Pair.of(LanguageEventData.levelName.TASK_NAME.name(), nodeName)
                );
            }

            Map<String, Serializable> aggregateContext = new HashMap<>();
            aggregateContext.put(ScoreLangConstants.BRANCHES_CONTEXT_KEY, (Serializable) branches_context);

            fireEvent(
                    executionRuntimeServices,
                    runEnv,
                    ScoreLangConstants.EVENT_ASYNC_LOOP_OUTPUT_START,
                    "Async loop output binding started",
                    Pair.of(ScoreLangConstants.TASK_AGGREGATE_KEY, (Serializable) taskAggregateValues),
                    Pair.of(ScoreLangConstants.TASK_NAVIGATION_KEY, (Serializable) taskNavigationValues),
                    Pair.of(LanguageEventData.levelName.TASK_NAME.name(), nodeName));

            Map<String, Serializable> publishValues = outputsBinding.bindOutputs(contextBeforeSplit, aggregateContext, taskAggregateValues);

            flowContext.putVariables(publishValues);

            // if one of the branches failed then return with FAILURE, otherwise return with SUCCESS
            String asyncLoopResult = ScoreLangConstants.SUCCESS_RESULT;
            for (String branchResult : branchResults) {
                if (branchResult.equals(ScoreLangConstants.FAILURE_RESULT)) {
                    asyncLoopResult = ScoreLangConstants.FAILURE_RESULT;
                    break;
                }
            }

            // set the position of the next step - for the use of the navigation
            // find in the navigation values the correct next step position, according to the async loop result, and set it
            ResultNavigation navigation = taskNavigationValues.get(asyncLoopResult);
            if (navigation == null) {
                // should always have the executable response mapped to a navigation by the task, if not, it is an error
                throw new RuntimeException("Task: " + nodeName + " has no matching navigation for the async loop result: " + asyncLoopResult);
            }
            Long nextStepPosition = navigation.getNextStepId();
            String presetResult = navigation.getPresetResult();

            HashMap<String, Serializable> outputs = new HashMap<>(publishValues);
            ReturnValues returnValues = new ReturnValues(outputs, presetResult != null ? presetResult : asyncLoopResult);

            fireEvent(
                    executionRuntimeServices,
                    runEnv,
                    ScoreLangConstants.EVENT_ASYNC_LOOP_OUTPUT_END,
                    "Async loop output binding finished",
                    Pair.of(LanguageEventData.OUTPUTS, (Serializable) publishValues),
                    Pair.of(LanguageEventData.RESULT, returnValues.getResult()),
                    Pair.of(LanguageEventData.NEXT_STEP_POSITION, nextStepPosition),
                    Pair.of(LanguageEventData.levelName.TASK_NAME.name(), nodeName));

            runEnv.getStack().pushContext(flowContext);
            runEnv.putReturnValues(returnValues);
            runEnv.putNextStepPosition(nextStepPosition);
        } catch (RuntimeException e) {
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
        pushParentFlowDataOnStack(runEnv, RUNNING_EXECUTION_PLAN_ID, nextStepId);

        Map<String, Serializable> branchContext = new HashMap<>(context);
        branchContext.put(ScoreLangConstants.RUN_ENV, runEnv);
        executionRuntimeServices.addBranch(branchBeginStep, refId, branchContext);
    }
}
