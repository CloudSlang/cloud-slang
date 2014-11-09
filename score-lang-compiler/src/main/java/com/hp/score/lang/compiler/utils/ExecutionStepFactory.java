package com.hp.score.lang.compiler.utils;
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

import com.hp.score.api.ControlActionMetadata;
import com.hp.score.api.ExecutionStep;
import com.hp.score.lang.compiler.SlangTextualKeys;
import com.hp.score.lang.entities.ActionType;
import com.hp.score.lang.entities.ScoreLangConstants;
import org.apache.commons.lang.Validate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * Created by orius123 on 05/11/14.
 */
@Component
public class ExecutionStepFactory {

    private static final String STEPS_PACKAGE = "com.hp.score.lang.runtime.steps";
    private static final String TASK_STEPS_CLASS = ".TaskSteps";
    private static final String OPERATION_STEPS_CLASS = STEPS_PACKAGE + ".OperationSteps";
    private static final String ACTION_STEPS_CLASS = STEPS_PACKAGE + ".ActionSteps";
    private static final String NAVIGATION_ACTIONS_CLASS = "com.hp.score.lang.runtime.navigations.Navigations";
    private static final String SIMPLE_NAVIGATION_METHOD = "navigate";


    public ExecutionStep createBeginTaskStep(Long index, Map<String, Serializable> preTaskData) {
        Validate.notNull(preTaskData, "preOpData is null");
        Map<String, Serializable> actionData = new HashMap<>();
        Serializable refId = preTaskData.remove(ScoreLangConstants.REF_ID);
        actionData.put(ScoreLangConstants.TASK_INPUTS_KEY, preTaskData.get(SlangTextualKeys.INPUTS_KEY));
        actionData.put(ScoreLangConstants.HOOKS, "TBD"); //todo add implementation for user custom hooks
        ExecutionStep beginTaskStep = createGeneralStep(index, TASK_STEPS_CLASS, "beginTask", ++index, actionData);

        HashMap<String, Object> navigationData = new HashMap<>(new HashMap<>(beginTaskStep.getNavigationData()));
        navigationData.put(ScoreLangConstants.REF_ID, refId);
        beginTaskStep.setNavigationData(navigationData);

        return beginTaskStep;
    }

    public ExecutionStep createFinishTaskStep(Long index, Map<String, Serializable> postTaskData) {
        Validate.notNull(postTaskData, "preOpData is null");
        Map<String, Serializable> actionData = new HashMap<>();
        actionData.put(ScoreLangConstants.TASK_PUBLISH_KEY, postTaskData.get(SlangTextualKeys.PUBLISH_KEY));
        actionData.put(ScoreLangConstants.TASK_NAVIGATION_KEY, hackToRunSingleTaskFlow(postTaskData));
        actionData.put(ScoreLangConstants.HOOKS, "TBD"); //todo add implementation for user custom hooks
        ExecutionStep finishTask = createGeneralStep(index, TASK_STEPS_CLASS, "finishTask", ++index, actionData);
        finishTask.setNavigationData(null);
        return finishTask;
    }

    private LinkedHashMap<String, Long> hackToRunSingleTaskFlow(Map<String, Serializable> postTaskData) {
        //todo as it name implies this is a hack to run single task flows
        @SuppressWarnings("unchecked") LinkedHashMap<String, String> navigationStringValues = (LinkedHashMap<String, String>)postTaskData.get(SlangTextualKeys.NAVIGATION_KEY);
        LinkedHashMap<String, Long> navigationValues = new LinkedHashMap<>();
        for (String nextStep : navigationStringValues.keySet()) {
            navigationValues.put(nextStep, 0L);
        }
        return navigationValues;
    }

    public ExecutionStep createStartStep(Long index, Map<String, Serializable> preOpData) {
        Validate.notNull(preOpData, "preOpData is null");
        Map<String, Serializable> actionData = new HashMap<>();
        actionData.put(ScoreLangConstants.OPERATION_INPUTS_KEY, preOpData.get(SlangTextualKeys.INPUTS_KEY));
        actionData.put(ScoreLangConstants.HOOKS, "TBD"); //todo add implementation for user custom hooks
        return createGeneralStep(index, OPERATION_STEPS_CLASS, "start", ++index, actionData);
    }

    public ExecutionStep createActionStep(Long index, Map<String, Serializable> actionData) {
        Validate.notNull(actionData, "actionData is null");
        ActionType actionType = actionData.get(ScoreLangConstants.ACTION_CLASS_KEY) != null ? ActionType.JAVA : ActionType.PYTHON;
        actionData.put(ScoreLangConstants.ACTION_TYPE, actionType);
        return createGeneralStep(index, ACTION_STEPS_CLASS, "doAction", ++index, actionData);
    }

    public ExecutionStep createEndStep(Long index, Map<String, Serializable> postOpData) {
        Validate.notNull(postOpData, "postOpData is null");
        Map<String, Serializable> actionData = new HashMap<>();
        actionData.put(ScoreLangConstants.OPERATION_OUTPUTS_KEY, postOpData.get(SlangTextualKeys.OUTPUTS_KEY));
        actionData.put(ScoreLangConstants.OPERATION_RESULTS_KEY, postOpData.get(SlangTextualKeys.RESULT_KEY));
        actionData.put(ScoreLangConstants.HOOKS, "TBD"); //todo add implementation for user custom hooks
        return createGeneralStep(index, OPERATION_STEPS_CLASS, "end", null, actionData);
    }


    private ExecutionStep createGeneralStep(
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
        navigationData.put(ScoreLangConstants.NEXT_STEP_ID_KEY, nextStepId);

        step.setNavigationData(navigationData);

        return step;
    }

}
