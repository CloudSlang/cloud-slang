/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.steps;

import com.hp.oo.sdk.content.annotations.Param;
import io.cloudslang.lang.entities.ListLoopStatement;
import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.entities.MapLoopStatement;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.RuntimeConstants;
import io.cloudslang.lang.runtime.bindings.OutputsBinding;
import io.cloudslang.lang.runtime.bindings.ParallelLoopBinding;
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
import org.apache.commons.lang3.tuple.ImmutablePair;
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
public class ParallelLoopExecutionData extends AbstractExecutionData {

    public static final String BRANCH_EXCEPTION_PREFIX = "Error running branch";

    @Autowired
    private ParallelLoopBinding parallelLoopBinding;

    @Autowired
    private OutputsBinding outputsBinding;

    private static final Logger logger = Logger.getLogger(ParallelLoopExecutionData.class);

    public void addBranches(@Param(ScoreLangConstants.PARALLEL_LOOP_STATEMENT_KEY) LoopStatement parallelLoopStatement,
                            @Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                            @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                            @Param(ScoreLangConstants.NODE_NAME_KEY) String nodeName,

                            //CHECKSTYLE:OFF: checkstyle:parametername
                            @Param(ExecutionParametersConsts.RUNNING_EXECUTION_PLAN_ID) Long RUNNING_EXECUTION_PLAN_ID,
                            //CHECKSTYLE:ON

                            @Param(ScoreLangConstants.NEXT_STEP_ID_KEY) Long nextStepId,
                            @Param(ScoreLangConstants.BRANCH_BEGIN_STEP_ID_KEY) Long branchBeginStep,
                            @Param(ScoreLangConstants.REF_ID) String refId) {

        try {
            Context flowContext = runEnv.getStack().popContext();

            List<Value> splitData = parallelLoopBinding
                    .bindParallelLoopList(parallelLoopStatement, flowContext, runEnv.getSystemProperties(), nodeName);

            fireEvent(executionRuntimeServices, ScoreLangConstants.EVENT_SPLIT_BRANCHES,
                    "parallel loop expression bound", runEnv.getExecutionPath().getCurrentPath(),
                    LanguageEventData.StepType.STEP, nodeName,
                    Pair.of(LanguageEventData.BOUND_PARALLEL_LOOP_EXPRESSION, (Serializable) splitData));

            runEnv.putNextStepPosition(nextStepId);
            runEnv.getExecutionPath().down();

            for (Value splitItem : splitData) {

                // first fire event
                fireEvent(executionRuntimeServices, ScoreLangConstants.EVENT_BRANCH_START,
                        "parallel loop branch created", runEnv.getExecutionPath().getCurrentPath(),
                        LanguageEventData.StepType.STEP, nodeName, Pair.of(ScoreLangConstants.REF_ID, refId),
                        Pair.of(RuntimeConstants.SPLIT_ITEM_KEY, splitItem));
                // take path down one level
                runEnv.getExecutionPath().down();

                RunEnvironment branchRuntimeEnvironment = (RunEnvironment) SerializationUtils.clone(runEnv);
                branchRuntimeEnvironment.resetStacks();

                Context branchContext = (Context) SerializationUtils.clone(flowContext);
                if (parallelLoopStatement instanceof ListLoopStatement) {
                    branchContext.putVariable(((ListLoopStatement) parallelLoopStatement).getVarName(), splitItem);
                } else if (parallelLoopStatement instanceof MapLoopStatement) {
                    branchContext.putVariable(((MapLoopStatement) parallelLoopStatement).getKeyName(),
                            (Value) ((ImmutablePair) splitItem.get()).getLeft());
                    branchContext.putVariable(((MapLoopStatement) parallelLoopStatement).getValueName(),
                            (Value) ((ImmutablePair) splitItem.get()).getRight());
                }
                updateCallArgumentsAndPushContextToStack(branchRuntimeEnvironment,
                        branchContext, new HashMap<String, Value>());

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

            updateCallArgumentsAndPushContextToStack(runEnv, flowContext, new HashMap<String, Value>());
        } catch (RuntimeException e) {
            logger.error("There was an error running the add branches execution step of: \'" + nodeName +
                    "\'. Error is: " + e.getMessage());
            throw new RuntimeException("Error running: " + nodeName + ": " + e.getMessage(), e);
        }

    }

    public void joinBranches(@Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                             @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                             @Param(ScoreLangConstants.STEP_PUBLISH_KEY) List<Output> stepPublishValues,
                             @Param(ScoreLangConstants.STEP_NAVIGATION_KEY)
                                     Map<String, ResultNavigation> stepNavigationValues,
                             @Param(ScoreLangConstants.NODE_NAME_KEY) String nodeName) {
        try {
            runEnv.getExecutionPath().up();
            List<Map<String, Serializable>> branchesContext = Lists.newArrayList();
            Context flowContext = runEnv.getStack().popContext();

            collectBranchesData(executionRuntimeServices, nodeName, branchesContext);

            Map<String, Value> publishValues =
                    bindPublishValues(
                            runEnv,
                            executionRuntimeServices,
                            stepPublishValues,
                            stepNavigationValues,
                            nodeName,
                            branchesContext
                    );

            flowContext.putVariables(publishValues);

            String parallelLoopResult = getParallelLoopResult(branchesContext);

            handleNavigationAndReturnValues(runEnv, executionRuntimeServices,
                    stepNavigationValues, nodeName, publishValues, parallelLoopResult);

            runEnv.getStack().pushContext(flowContext);
            runEnv.getExecutionPath().forward();
        } catch (RuntimeException e) {
            logger.error("There was an error running the joinBranches execution step of: \'" + nodeName +
                    "\'. Error is: " + e.getMessage());
            throw new RuntimeException("Error running: \'" + nodeName + "\': \n" + e.getMessage(), e);
        }
    }

    private void handleNavigationAndReturnValues(
            RunEnvironment runEnv,
            ExecutionRuntimeServices executionRuntimeServices,
            Map<String, ResultNavigation> stepNavigationValues,
            String nodeName,
            Map<String, Value> publishValues,
            String parallelLoopResult) {
        // set the position of the next step - for the use of the navigation
        // find in the navigation values the correct next step position, according to the parallel loop result,
        // and set it
        ResultNavigation navigation = stepNavigationValues.get(parallelLoopResult);
        if (navigation == null) {
            // should always have the executable response mapped to a navigation by the step, if not, it is an error
            throw new RuntimeException("Step: " + nodeName +
                    " has no matching navigation for the parallel loop result: " + parallelLoopResult);
        }
        Long nextStepPosition = navigation.getNextStepId();
        String presetResult = navigation.getPresetResult();

        HashMap<String, Value> outputs = new HashMap<>(publishValues);
        ReturnValues returnValues = new ReturnValues(outputs, presetResult != null ? presetResult : parallelLoopResult);

        fireEvent(executionRuntimeServices, runEnv, ScoreLangConstants.EVENT_JOIN_BRANCHES_END,
                "Parallel loop output binding finished", LanguageEventData.StepType.STEP, nodeName,
                Pair.of(LanguageEventData.OUTPUTS, (Serializable) publishValues),
                Pair.of(LanguageEventData.RESULT, returnValues.getResult()),
                Pair.of(LanguageEventData.NEXT_STEP_POSITION, nextStepPosition));

        runEnv.putReturnValues(returnValues);
        runEnv.putNextStepPosition(nextStepPosition);
    }

    private String getParallelLoopResult(List<Map<String, Serializable>> branchesContext) {
        // if one of the branches failed then return with FAILURE, otherwise return with SUCCESS
        String parallelLoopResult = ScoreLangConstants.SUCCESS_RESULT;
        for (Map<String, Serializable> branchContext : branchesContext) {
            String branchResult = (String) branchContext.get(ScoreLangConstants.BRANCH_RESULT_KEY);
            if (branchResult.equals(ScoreLangConstants.FAILURE_RESULT)) {
                parallelLoopResult = ScoreLangConstants.FAILURE_RESULT;
                break;
            }
        }
        return parallelLoopResult;
    }

    private Map<String, Value> bindPublishValues(
            RunEnvironment runEnv,
            ExecutionRuntimeServices executionRuntimeServices,
            List<Output> stepPublishValues,
            Map<String, ResultNavigation> stepNavigationValues,
            String nodeName,
            List<Map<String, Serializable>> branchesContext) {

        Map<String, Value> publishContext = new HashMap<>();
        publishContext.put(RuntimeConstants.BRANCHES_CONTEXT_KEY, ValueFactory.create((Serializable) branchesContext));

        fireEvent(
                executionRuntimeServices,
                runEnv,
                ScoreLangConstants.EVENT_JOIN_BRANCHES_START,
                "Parallel loop output binding started", LanguageEventData.StepType.STEP, nodeName,
                Pair.of(ScoreLangConstants.STEP_PUBLISH_KEY, (Serializable) stepPublishValues),
                Pair.of(ScoreLangConstants.STEP_NAVIGATION_KEY, (Serializable) stepNavigationValues));

        return outputsBinding.bindOutputs(
                Collections.<String, Value>emptyMap(),
                publishContext,
                runEnv.getSystemProperties(),
                stepPublishValues
        );
    }

    private void collectBranchesData(
            ExecutionRuntimeServices executionRuntimeServices,
            String nodeName,
            List<Map<String, Serializable>> branchesContext) {

        List<EndBranchDataContainer> branches = executionRuntimeServices.getFinishedChildBranchesData();
        for (EndBranchDataContainer branch : branches) {
            checkExceptionInBranch(branch);

            Map<String, Serializable> branchContext = branch.getContexts();
            RunEnvironment branchRuntimeEnvironment = (RunEnvironment) branchContext.get(ScoreLangConstants.RUN_ENV);
            Map<String, Serializable> branchContextMap =
                    convert(branchRuntimeEnvironment.getStack().popContext().getImmutableViewOfVariables());
            ReturnValues executableReturnValues = branchRuntimeEnvironment.removeReturnValues();
            String branchResult = executableReturnValues.getResult();
            branchContextMap.put(ScoreLangConstants.BRANCH_RESULT_KEY, branchResult);
            branchesContext.add(branchContextMap);

            // up branch path
            branchRuntimeEnvironment.getExecutionPath().up();

            fireEvent(executionRuntimeServices, branchRuntimeEnvironment, ScoreLangConstants.EVENT_BRANCH_END,
                    "Parallel loop branch ended", LanguageEventData.StepType.STEP, nodeName,
                    Pair.of(RuntimeConstants.BRANCH_RETURN_VALUES_KEY, executableReturnValues)
            );
        }
    }

    private void checkExceptionInBranch(EndBranchDataContainer branch) {
        //first we check that no exception was thrown during the execution of the branch
        String branchException = branch.getException();
        if (StringUtils.isNotEmpty(branchException)) {
            Map<String, Serializable> systemContextMap = branch.getSystemContext();
            String branchId = null;
            if (MapUtils.isNotEmpty(systemContextMap)) {
                ExecutionRuntimeServices branchExecutionRuntimeServices = new SystemContext(systemContextMap);
                branchId = branchExecutionRuntimeServices.getBranchId();
            }
            logger.error("There was an error running branch: " + branchId + " Error is: " + branchException);
            throw new RuntimeException(BRANCH_EXCEPTION_PREFIX + ": \n" + branchException);
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

    private Map<String, Serializable> convert(Map<String, Value> map) {
        Map<String, Serializable> result = new HashMap<>(map.size());
        for (Map.Entry<String, Value> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue() == null ? null : entry.getValue().get());
        }
        return result;
    }
}
