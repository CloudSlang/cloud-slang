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
package com.hp.score.lang.tests.runtime.builders;

import com.hp.score.api.ControlActionMetadata;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.ExecutionStep;
import com.hp.score.lang.entities.ActionType;
import com.hp.score.lang.entities.ScoreLangConstants;
import com.hp.score.lang.entities.bindings.Result;
import com.hp.score.lang.runtime.navigations.Navigations;
import com.hp.score.lang.runtime.steps.ActionSteps;
import com.hp.score.lang.runtime.steps.OperationSteps;
import com.hp.score.lang.runtime.steps.TaskSteps;
import com.hp.score.lang.tests.runtime.actions.LangActions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * User: stoneo
 * Date: 06/10/2014
 * Time: 09:34
 */
public class POCExecutionPlanActionsBuilder {

    public static final String NEXT_STEP_ID_KEY = "nextStepId";
    public static final String NAVIGATION_ACTIONS_CLASS = Navigations.class.getName();
    public static final String SIMPLE_NAVIGATION_METHOD = "navigate";

    ExecutionPlan executionPlan;

    private Long index = 1L;

    public POCExecutionPlanActionsBuilder() {
        createExecutionPlan();
    }

    private void createExecutionPlan() {
        executionPlan = new ExecutionPlan();
        executionPlan.setFlowUuid("childFlow");
        executionPlan.setBeginStep(1L);
        executionPlan.addStep(createFlowStartStep());
        addFirstStep();
        executionPlan.addStep(createFlowEndStep());
    }

    private void addFirstStep() {
        executionPlan.addStep(createBeginTaskStep());
        executionPlan.addStep(createStartStep());
        executionPlan.addStep(createActionStep(LangActions.class.getName(), "printAndReturnDur"));
        executionPlan.addStep(createEndStep());
        executionPlan.addStep(createFirstFinishTaskStep());
    }

    public ExecutionPlan getExecutionPlan() {
        return executionPlan;
    }

    private ExecutionStep createFlowStartStep() {
        Map<String, Serializable> actionData = new HashMap<>();
        HashMap<String, Serializable> flowInputs = createFlowInputs();
        actionData.put("operationInputs", flowInputs);
        return createGeneralStep(index, OperationSteps.class.getName(), "start", ++index, actionData);
    }

    private ExecutionStep createBeginTaskStep() {
        Map<String, Serializable> actionData = new HashMap<>();
        HashMap<String, Serializable> taskInputs = createBeginTaskTaskInputs();
        actionData.put("taskInputs", taskInputs);
        return createGeneralStep(index, TaskSteps.class.getName(), "beginTask", ++index, actionData);
    }

    private ExecutionStep createStartStep() {
        Map<String, Serializable> actionData = new HashMap<>();
        HashMap<String, Serializable> operationInputs = createOperationInputs();
        actionData.put("operationInputs", operationInputs);
        return createGeneralStep(index, OperationSteps.class.getName(), "start", ++index, actionData);
    }

    private ExecutionStep createActionStep(String actionClassName, String actionMethodName) {
        Map<String, Serializable> actionData = new HashMap<>();
        actionData.put(ScoreLangConstants.ACTION_CLASS_KEY, actionClassName);
        actionData.put(ScoreLangConstants.ACTION_METHOD_KEY, actionMethodName);
        actionData.put("actionType", ActionType.JAVA);
        return createGeneralStep(index, ActionSteps.class.getName(), "doAction", ++index, actionData);
    }

    private ExecutionStep createEndStep() {
        Map<String, Serializable> actionData = new HashMap<>();
        HashMap<String, Serializable> operationOutputs = createOperationOutputs();
        actionData.put("operationOutputs", operationOutputs);
        LinkedList<Result> operationResults = createOperationResults();
        actionData.put("operationResults", operationResults);
        return createGeneralStep(index, OperationSteps.class.getName(), "end", ++index, actionData);
    }

    private ExecutionStep createFirstFinishTaskStep() {
        Map<String, Serializable> actionData = new HashMap<>();
        HashMap<String, Serializable> taskPublishValues = createTaskPublishValues();
        actionData.put("taskPublishValues", taskPublishValues);
        HashMap<String, Long> taskNavigationValues = createFirstTaskNavigationValues();
        actionData.put("taskNavigationValues", taskNavigationValues);
        ExecutionStep finishTask = createGeneralStep(index, TaskSteps.class.getName(), "finishTask", ++index, actionData);
        finishTask.setNavigationData(null);
        return finishTask;
    }

    private ExecutionStep createFlowEndStep() {
        Map<String, Serializable> actionData = new HashMap<>();
        HashMap<String, Serializable> flowOutputs = createFlowOutputs();
        actionData.put("operationOutputs", flowOutputs);
        LinkedList<Result> flowResults = createFlowResults();
        actionData.put("operationResults", flowResults);
        return createGeneralStep(index, OperationSteps.class.getName(), "end", null, actionData);
    }

    private HashMap<String, Serializable> createBeginTaskTaskInputs() {
        LinkedHashMap<String, Serializable> taskInputs = new LinkedHashMap<>();
        taskInputs.put("first_name", null);
        taskInputs.put("mail_server", null);
        taskInputs.put("admin_user", null);
        return taskInputs;
    }

    private HashMap<String, Serializable> createFlowInputs() {
        LinkedHashMap<String, Serializable> flowInputs = new LinkedHashMap<>();
        flowInputs.put("first_name", "emp_first_name");
        flowInputs.put("mail_server", null);
        flowInputs.put("admin_user", null);
        return flowInputs;
    }

    private HashMap<String, Serializable> createOperationInputs() {
        LinkedHashMap<String, Serializable> operationInputs = new LinkedHashMap<>();
        operationInputs.put("user", "first_name");
        operationInputs.put("string", "first_name");
        return operationInputs;
    }

    private HashMap<String, Serializable> createOperationOutputs() {
        LinkedHashMap<String, Serializable> operationOutputs = new LinkedHashMap<>();
        operationOutputs.put("user", null);
        operationOutputs.put("duration", "dur");
        return operationOutputs;
    }

    private LinkedList<Result> createOperationResults() {
        LinkedList<Result> operationResults = new LinkedList<>();
        operationResults.add(new Result(ScoreLangConstants.SUCCESS_RESULT, "1==1"));
        operationResults.add(new Result(ScoreLangConstants.FAILURE_RESULT, "1==2"));
        return operationResults;
    }

    private HashMap<String, Serializable> createTaskPublishValues() {
        LinkedHashMap<String, Serializable> taskPublishValues = new LinkedHashMap<>();
        taskPublishValues.put("user", null);
        taskPublishValues.put("duration", null);
        return taskPublishValues;
    }

    private HashMap<String, Long> createFirstTaskNavigationValues() {
        LinkedHashMap<String, Long> navigationValues = new LinkedHashMap<>();
        navigationValues.put(ScoreLangConstants.SUCCESS_RESULT, index + 1);
        navigationValues.put(ScoreLangConstants.FAILURE_RESULT, null);
        return navigationValues;
    }

    private HashMap<String, Serializable> createFlowOutputs() {
        LinkedHashMap<String, Serializable> flowOutputs = new LinkedHashMap<>();
        flowOutputs.put("user", null);
        flowOutputs.put("duration", null);
        return flowOutputs;
    }

    private LinkedList<Result> createFlowResults() {
        LinkedList<Result> flowResults = new LinkedList<>();
        flowResults.add(new Result(ScoreLangConstants.SUCCESS_RESULT, null));
        flowResults.add(new Result(ScoreLangConstants.FAILURE_RESULT, null));
        return flowResults;
    }

    public ExecutionStep createGeneralStep(
            Long stepId,
            String actionClassName,
            String actionMethodName,
            Long nextStepId,
            Map<String, Serializable> actionData) {

        ExecutionStep step = new ExecutionStep(stepId);
        step.setAction(new ControlActionMetadata(actionClassName, actionMethodName));
        step.setActionData(actionData);

        step.setNavigation(new ControlActionMetadata(NAVIGATION_ACTIONS_CLASS, SIMPLE_NAVIGATION_METHOD));
        Map<String, Object> navigationData = new HashMap<>(2);
        navigationData.put(NEXT_STEP_ID_KEY, nextStepId);

        step.setNavigationData(navigationData);

//        step.setSplitStep(false);

        return step;
    }

}
