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
import io.cloudslang.lang.runtime.bindings.OutputsBinding;
import io.cloudslang.lang.runtime.env.Context;
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.api.EndBranchDataContainer;
import io.cloudslang.score.api.execution.ExecutionParametersConsts;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import io.cloudslang.score.lang.SystemContext;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.python.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collections;
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
public class AsyncLoopExecutionData extends AbstractExecutionData {

    public static final String BRANCH_EXCEPTION_PREFIX = "Error running branch";

    @Autowired
    private AsyncLoopBinding asyncLoopBinding;

    @Autowired
    private OutputsBinding outputsBinding;

    private static final Logger logger = Logger.getLogger(AsyncLoopExecutionData.class);

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

            List<Serializable> splitData = asyncLoopBinding.bindAsyncLoopList(asyncLoopStatement, flowContext, runEnv.getSystemProperties(), nodeName);

            fireEvent(executionRuntimeServices, ScoreLangConstants.EVENT_SPLIT_BRANCHES,
                    "async loop expression bound", runEnv.getExecutionPath().getCurrentPath(),
                    LanguageEventData.StepType.STEP, nodeName,
                    Pair.of(LanguageEventData.BOUND_ASYNC_LOOP_EXPRESSION, (Serializable) splitData));

            runEnv.putNextStepPosition(nextStepId);
            runEnv.getExecutionPath().down();

            for (Serializable splitItem : splitData) {

                // first fire event
                fireEvent(executionRuntimeServices, ScoreLangConstants.EVENT_BRANCH_START,
                        "async loop branch created", runEnv.getExecutionPath().getCurrentPath(),
                        LanguageEventData.StepType.STEP, nodeName, Pair.of(ScoreLangConstants.REF_ID, refId),
                        Pair.of(RuntimeConstants.SPLIT_ITEM_KEY, splitItem));
                // take path down one level
                runEnv.getExecutionPath().down();

                RunEnvironment branchRuntimeEnvironment = (RunEnvironment) SerializationUtils.clone(runEnv);
                branchRuntimeEnvironment.resetStacks();

                Context branchContext = (Context) SerializationUtils.clone(flowContext);
                branchContext.putVariable(asyncLoopStatement.getVarName(), splitItem);
                updateCallArgumentsAndPushContextToStack(branchRuntimeEnvironment,
                        branchContext, new HashMap<String, Serializable>());

                createBranch(
                        branchRuntimeEnvironment,
                        executionRuntimeServices,
                        refId,
                        branchBeginStep);

                // take path up level
                runEnv.getExecutionPath().up();

                // forward for next branch
                runEnv.getExecutionPath().forward();
            }

            updateCallArgumentsAndPushContextToStack(runEnv, flowContext, new HashMap<String, Serializable>());
        } catch (RuntimeException e) {
            logger.error("There was an error running the add branches execution step of: \'" + nodeName + "\'. Error is: " + e.getMessage());
            throw new RuntimeException("Error running: " + nodeName + ": " + e.getMessage(), e);
        }

    }

    public void joinBranches(@Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                             @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                             @Param(ScoreLangConstants.STEP_AGGREGATE_KEY) List<Output> stepAggregateValues,
                             @Param(ScoreLangConstants.STEP_NAVIGATION_KEY) Map<String, ResultNavigation> stepNavigationValues,
                             @Param(ScoreLangConstants.NODE_NAME_KEY) String nodeName) {
        try {
            runEnv.getExecutionPath().up();
            List<Map<String, Serializable>> branchesContext = Lists.newArrayList();
            Context flowContext = runEnv.getStack().popContext();
            List<String> branchesResult = Lists.newArrayList();

            collectBranchesData(executionRuntimeServices, nodeName, branchesContext, branchesResult);

            Map<String, Serializable> publishValues =
                    bindAggregateOutputs(
                            runEnv,
                            executionRuntimeServices,
                            stepAggregateValues,
                            stepNavigationValues,
                            nodeName,
                            branchesContext
                    );

            flowContext.putVariables(publishValues);

            String asyncLoopResult = getAsyncLoopResult(branchesResult);

            handleNavigationAndReturnValues(runEnv, executionRuntimeServices, stepNavigationValues, nodeName, publishValues, asyncLoopResult);

            runEnv.getStack().pushContext(flowContext);
            runEnv.getExecutionPath().forward();
        } catch (RuntimeException e) {
            logger.error("There was an error running the joinBranches execution step of: \'" + nodeName + "\'. Error is: " + e.getMessage());
            throw new RuntimeException("Error running: \'" + nodeName + "\': \n" + e.getMessage(), e);
        }
    }

    private void handleNavigationAndReturnValues(
            RunEnvironment runEnv,
            ExecutionRuntimeServices executionRuntimeServices,
            Map<String, ResultNavigation> stepNavigationValues,
            String nodeName,
            Map<String, Serializable> publishValues,
            String asyncLoopResult) {
        // set the position of the next step - for the use of the navigation
        // find in the navigation values the correct next step position, according to the async loop result, and set it
        ResultNavigation navigation = stepNavigationValues.get(asyncLoopResult);
        if (navigation == null) {
            // should always have the executable response mapped to a navigation by the step, if not, it is an error
            throw new RuntimeException("Step: " + nodeName + " has no matching navigation for the async loop result: " + asyncLoopResult);
        }
        Long nextStepPosition = navigation.getNextStepId();
        String presetResult = navigation.getPresetResult();

        HashMap<String, Serializable> outputs = new HashMap<>(publishValues);
        ReturnValues returnValues = new ReturnValues(outputs, presetResult != null ? presetResult : asyncLoopResult);

        fireEvent(executionRuntimeServices, runEnv, ScoreLangConstants.EVENT_JOIN_BRANCHES_END,
                "Async loop output binding finished", LanguageEventData.StepType.STEP, nodeName,
                Pair.of(LanguageEventData.OUTPUTS, (Serializable) publishValues),
                Pair.of(LanguageEventData.RESULT, returnValues.getResult()),
                Pair.of(LanguageEventData.NEXT_STEP_POSITION, nextStepPosition));

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
            List<Output> stepAggregateValues,
            Map<String, ResultNavigation> stepNavigationValues,
            String nodeName,
            List<Map<String, Serializable>> branchesContext) {

        Map<String, Serializable> aggregateContext = new HashMap<>();
        aggregateContext.put(RuntimeConstants.BRANCHES_CONTEXT_KEY, (Serializable) branchesContext);

        fireEvent(
                executionRuntimeServices,
                runEnv,
                ScoreLangConstants.EVENT_JOIN_BRANCHES_START,
                "Async loop output binding started", LanguageEventData.StepType.STEP, nodeName,
                Pair.of(ScoreLangConstants.STEP_AGGREGATE_KEY, (Serializable) stepAggregateValues),
                Pair.of(ScoreLangConstants.STEP_NAVIGATION_KEY, (Serializable) stepNavigationValues));

        return outputsBinding.bindOutputs(
                Collections.<String, Serializable>emptyMap(),
                aggregateContext,
                runEnv.getSystemProperties(),
                stepAggregateValues
        );
    }

    private void collectBranchesData(
            ExecutionRuntimeServices executionRuntimeServices,
            String nodeName,
            List<Map<String, Serializable>> branchesContext,
            List<String> branchesResult) {

        List<EndBranchDataContainer> branches = executionRuntimeServices.getFinishedChildBranchesData();
        for (EndBranchDataContainer branch : branches) {

            //first we check that no exception was thrown during the execution of the branch
            String branchException = branch.getException();
            if (StringUtils.isNotEmpty(branchException)) {
                Map<String, Serializable> systemContextMap = branch.getSystemContext();
                String branchID = null;
                if (MapUtils.isNotEmpty(systemContextMap)) {
                    ExecutionRuntimeServices branchExecutionRuntimeServices = new SystemContext(systemContextMap);
                    branchID = branchExecutionRuntimeServices.getBranchId();
                }
                logger.error("There was an error running branch: " + branchID + " Error is: " + branchException);
                throw new RuntimeException(BRANCH_EXCEPTION_PREFIX + ": \n" + branchException);
            }

            Map<String, Serializable> branchContext = branch.getContexts();

            RunEnvironment branchRuntimeEnvironment = (RunEnvironment) branchContext.get(ScoreLangConstants.RUN_ENV);

            branchesContext.add(branchRuntimeEnvironment.getStack().popContext().getImmutableViewOfVariables());

            ReturnValues executableReturnValues = branchRuntimeEnvironment.removeReturnValues();
            branchesResult.add(executableReturnValues.getResult());

            // up branch path
            branchRuntimeEnvironment.getExecutionPath().up();

            fireEvent(executionRuntimeServices, branchRuntimeEnvironment, ScoreLangConstants.EVENT_BRANCH_END,
                    "async loop branch ended", LanguageEventData.StepType.STEP, nodeName,
                    Pair.of(RuntimeConstants.BRANCH_RETURN_VALUES_KEY, executableReturnValues)
            );
        }
    }

    private void createBranch(RunEnvironment runEnv,
                              ExecutionRuntimeServices executionRuntimeServices,
                              String refId,
                              Long branchBeginStep) {
        Map<String, Serializable> branchContext = new HashMap<>();
        branchContext.put(ScoreLangConstants.RUN_ENV, runEnv);
        executionRuntimeServices.addBranch(branchBeginStep, refId, branchContext);
    }
}
