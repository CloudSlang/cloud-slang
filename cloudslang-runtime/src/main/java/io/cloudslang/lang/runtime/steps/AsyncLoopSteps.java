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
import io.cloudslang.lang.runtime.RuntimeConstants;
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

    public void addBranches(@Param(ScoreLangConstants.ASYNC_LOOP_STATEMENT_KEY) AsyncLoopStatement asyncLoopStatement,
                            @Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                            @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                            @Param(ScoreLangConstants.NODE_NAME_KEY) String nodeName,
                            @Param(ExecutionParametersConsts.RUNNING_EXECUTION_PLAN_ID) Long RUNNING_EXECUTION_PLAN_ID,
                            @Param(ScoreLangConstants.NEXT_STEP_ID_KEY) Long nextStepId,
                            @Param(ScoreLangConstants.BRANCH_BEGIN_STEP_ID_KEY) Long branchBeginStep,
                            @Param(ScoreLangConstants.REF_ID) String refId) {

        try {
            Context flowContext = runEnv.getStack().popContext();

            List<Serializable> splitData = asyncLoopBinding.bindAsyncLoopList(asyncLoopStatement, flowContext, nodeName);

            fireEvent(
                    executionRuntimeServices,
                    ScoreLangConstants.EVENT_ASYNC_LOOP_EXPRESSION_END,
                    "async loop expression bound",
                    runEnv.getExecutionPath().getCurrentPathPeekForward(),
                    Pair.of(LanguageEventData.BOUND_ASYNC_LOOP_EXPRESSION, (Serializable) splitData),
                    Pair.of(LanguageEventData.levelName.TASK_NAME.name(), nodeName));

            runEnv.putNextStepPosition(nextStepId);

            for (Serializable splitItem : splitData) {
                RunEnvironment branchRuntimeEnvironment = (RunEnvironment) SerializationUtils.clone(runEnv);
                Context branchContext = (Context) SerializationUtils.clone(flowContext);
                branchContext.putVariable(asyncLoopStatement.getVarName(), splitItem);

                updateCallArgumentsAndPushContextToStack(branchRuntimeEnvironment, branchContext, new HashMap<String, Serializable>());

                createBranch(
                        branchRuntimeEnvironment,
                        executionRuntimeServices,
                        RUNNING_EXECUTION_PLAN_ID,
                        refId,
                        nextStepId,
                        branchBeginStep);

                fireEvent(
                        executionRuntimeServices,
                        ScoreLangConstants.EVENT_BRANCH_START,
                        "async loop branch created",
                        runEnv.getExecutionPath().getCurrentPathPeekForward(),
                        Pair.of(ScoreLangConstants.REF_ID, refId),
                        Pair.of(RuntimeConstants.SPLIT_ITEM_KEY, splitItem),
                        Pair.of(LanguageEventData.levelName.TASK_NAME.name(), nodeName));
            }

            updateCallArgumentsAndPushContextToStack(runEnv, flowContext, new HashMap<String, Serializable>());

            // forward after the branches are created because begin task method also calls forward
            runEnv.getExecutionPath().forward();
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
            List<Map<String, Serializable>> branchesContext = Lists.newArrayList();
            Context flowContext = runEnv.getStack().popContext();
            Map<String, Serializable> contextBeforeSplit = flowContext.getImmutableViewOfVariables();
            List<String> branchesResult = Lists.newArrayList();

            collectBranchesData(runEnv, executionRuntimeServices, nodeName, branchesContext, branchesResult);

            Map<String, Serializable> publishValues =
                    bindAggregateOutputs(
                            runEnv,
                            executionRuntimeServices,
                            taskAggregateValues,
                            (Serializable) taskNavigationValues,
                            nodeName, (Serializable) branchesContext,
                            contextBeforeSplit
                    );

            flowContext.putVariables(publishValues);

            String asyncLoopResult = getAsyncLoopResult(branchesResult);

            handleNavigationAndReturnValues(runEnv, executionRuntimeServices, taskNavigationValues, nodeName, publishValues, asyncLoopResult);

            runEnv.getStack().pushContext(flowContext);
        } catch (RuntimeException e) {
            logger.error("There was an error running the end task execution step of: \'" + nodeName + "\'. Error is: " + e.getMessage());
            throw new RuntimeException("Error running: \'" + nodeName + "\': " + e.getMessage(), e);
        }
    }

    private void handleNavigationAndReturnValues(
            RunEnvironment runEnv,
            ExecutionRuntimeServices executionRuntimeServices,
            Map<String, ResultNavigation> taskNavigationValues,
            String nodeName,
            Map<String, Serializable> publishValues,
            String asyncLoopResult) {
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

        runEnv.putReturnValues(returnValues);
        runEnv.putNextStepPosition(nextStepPosition);
    }

    private String getAsyncLoopResult(List<String> branchesResult) {
        // if one of the branches failed then return with FAILURE, otherwise return with SUCCESS
        String asyncLoopResult = ScoreLangConstants.SUCCESS_RESULT;
        for (String branchResult : branchesResult) {
            if (branchResult.equals(ScoreLangConstants.FAILURE_RESULT)) {
                asyncLoopResult = ScoreLangConstants.FAILURE_RESULT;
                break;
            }
        }
        return asyncLoopResult;
    }

    private Map<String, Serializable> bindAggregateOutputs(
            RunEnvironment runEnv,
            ExecutionRuntimeServices executionRuntimeServices,
            List<Output> taskAggregateValues,
            Serializable taskNavigationValues,
            String nodeName, Serializable branchesContext,
            Map<String, Serializable> contextBeforeSplit) {

        Map<String, Serializable> aggregateContext = new HashMap<>();
        aggregateContext.put(RuntimeConstants.BRANCHES_CONTEXT_KEY, branchesContext);

        fireEvent(
                executionRuntimeServices,
                runEnv,
                ScoreLangConstants.EVENT_ASYNC_LOOP_OUTPUT_START,
                "Async loop output binding started",
                Pair.of(ScoreLangConstants.TASK_AGGREGATE_KEY, (Serializable) taskAggregateValues),
                Pair.of(ScoreLangConstants.TASK_NAVIGATION_KEY, taskNavigationValues),
                Pair.of(LanguageEventData.levelName.TASK_NAME.name(), nodeName));

        return outputsBinding.bindOutputs(contextBeforeSplit, aggregateContext, taskAggregateValues);
    }

    private void collectBranchesData(
            RunEnvironment runEnv,
            ExecutionRuntimeServices executionRuntimeServices,
            String nodeName,
            List<Map<String, Serializable>> branchesContext,
            List<String> branchesResult) {

        List<EndBranchDataContainer> branches = executionRuntimeServices.getFinishedChildBranchesData();
        for (EndBranchDataContainer branch : branches) {
            Map<String, Serializable> branchContext = branch.getContexts();

            RunEnvironment branchRuntimeEnvironment = (RunEnvironment) branchContext.get(ScoreLangConstants.RUN_ENV);

            branchesContext.add(branchRuntimeEnvironment.getStack().popContext().getImmutableViewOfVariables());

            ReturnValues executableReturnValues = branchRuntimeEnvironment.removeReturnValues();
            branchesResult.add(executableReturnValues.getResult());

            fireEvent(
                    executionRuntimeServices,
                    runEnv,
                    ScoreLangConstants.EVENT_BRANCH_END,
                    "async loop branch ended",
                    Pair.of(RuntimeConstants.BRANCH_RETURN_VALUES_KEY, executableReturnValues),
                    Pair.of(LanguageEventData.levelName.TASK_NAME.name(), nodeName)
            );
        }
    }

    private void createBranch(RunEnvironment runEnv,
                              ExecutionRuntimeServices executionRuntimeServices,
                              Long RUNNING_EXECUTION_PLAN_ID,
                              String refId,
                              Long nextStepId,
                              Long branchBeginStep) {
        pushParentFlowDataOnStack(runEnv, RUNNING_EXECUTION_PLAN_ID, nextStepId);

        Map<String, Serializable> branchContext = new HashMap<>();
        branchContext.put(ScoreLangConstants.RUN_ENV, runEnv);
        executionRuntimeServices.addBranch(branchBeginStep, refId, branchContext);
    }
}
