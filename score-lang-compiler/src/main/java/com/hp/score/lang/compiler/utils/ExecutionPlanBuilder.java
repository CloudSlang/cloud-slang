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
package com.hp.score.lang.compiler.utils;

import ch.lambdaj.Lambda;
import com.hp.score.lang.compiler.domain.CompiledFlow;
import com.hp.score.lang.compiler.domain.CompiledOperation;
import com.hp.score.lang.compiler.domain.CompiledTask;
import com.hp.score.lang.entities.ResultNavigation;
import com.hp.score.lang.entities.bindings.Result;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.score.api.ExecutionPlan;
import org.eclipse.score.api.ExecutionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

/*
 * Created by orius123 on 11/11/14.
 */
@Component
public class ExecutionPlanBuilder {

    @Autowired
    private ExecutionStepFactory stepFactory;

    private static final String SLANG_NAME = "slang";
    private static final int NUMBER_OF_TASK_EXECUTION_STEPS = 2;
    private static final long FLOW_END_STEP_ID = 0L;
    private static final long FLOW_START_STEP_ID = 1L;

    public ExecutionPlan createOperationExecutionPlan(CompiledOperation compiledOp) {
        ExecutionPlan executionPlan = new ExecutionPlan();
        executionPlan.setName(compiledOp.getName());
        executionPlan.setLanguage(SLANG_NAME);
        executionPlan.setFlowUuid(compiledOp.getId());

        executionPlan.setBeginStep(1L);

        executionPlan.addStep(stepFactory.createStartStep(1L, compiledOp.getPreExecActionData(), compiledOp.getInputs(),
                compiledOp.getName()));
        executionPlan.addStep(stepFactory.createActionStep(2L, compiledOp.getCompiledDoAction().getActionData()));
        executionPlan.addStep(stepFactory.createEndStep(3L, compiledOp.getPostExecActionData(), compiledOp.getOutputs(),
                compiledOp.getResults(), compiledOp.getName()));
        return executionPlan;
    }

    public ExecutionPlan createFlowExecutionPlan(CompiledFlow compiledFlow) {
        ExecutionPlan executionPlan = new ExecutionPlan();
        executionPlan.setName(compiledFlow.getName());
        executionPlan.setLanguage(SLANG_NAME);
        executionPlan.setFlowUuid(compiledFlow.getId());

        executionPlan.setBeginStep(FLOW_START_STEP_ID);
        //flow start step
        executionPlan.addStep(stepFactory.createStartStep(FLOW_START_STEP_ID, compiledFlow.getPreExecActionData(),
                compiledFlow.getInputs(), compiledFlow.getName()));
        //flow end step
        executionPlan.addStep(stepFactory.createEndStep(FLOW_END_STEP_ID, compiledFlow.getPostExecActionData(),
                compiledFlow.getOutputs(), compiledFlow.getResults(), compiledFlow.getName()));

        Map<String, Long> taskReferences = new HashMap<>();
        for (Result result : compiledFlow.getResults()) {
            taskReferences.put(result.getName(), FLOW_END_STEP_ID);
        }

        Deque<CompiledTask> compiledTasks = compiledFlow.getCompiledWorkflow().getCompiledTasks();

        if (CollectionUtils.isEmpty(compiledTasks)) {
            throw new RuntimeException("Flow: " + compiledFlow.getName() + " has no tasks");
        }

        List<ExecutionStep> taskExecutionSteps = buildTaskExecutionSteps(compiledTasks.getFirst(), taskReferences, compiledTasks);
        executionPlan.addSteps(taskExecutionSteps);

        return executionPlan;
    }

    private List<ExecutionStep> buildTaskExecutionSteps(CompiledTask compiledTask,
                                                        Map<String, Long> taskReferences, Deque<CompiledTask> compiledTasks) {

        List<ExecutionStep> taskExecutionSteps = new ArrayList<>();

        String taskName = compiledTask.getName();
        Long currentId = getCurrentId(taskReferences);

        //Begin Task
        taskReferences.put(taskName, currentId);
        taskExecutionSteps.add(stepFactory.createBeginTaskStep(currentId++, compiledTask.getPreTaskActionData(),
                compiledTask.getRefId(), taskName));

        //End Task
        Map<String, ResultNavigation> navigationValues = new HashMap<>();
        for (Map.Entry<String, String> entry : compiledTask.getNavigationStrings().entrySet()) {
            String nextStepName = entry.getValue();
            if (taskReferences.get(nextStepName) == null) {
                CompiledTask nextTaskToCompile = Lambda.selectFirst(compiledTasks, having(on(CompiledTask.class).getName(), equalTo(nextStepName)));
                taskExecutionSteps.addAll(buildTaskExecutionSteps(nextTaskToCompile, taskReferences, compiledTasks));
            }
			long nextStepId = taskReferences.get(nextStepName);
			String presetResult = (FLOW_END_STEP_ID == nextStepId) ? nextStepName : null;
			navigationValues.put(entry.getKey(), new ResultNavigation(nextStepId, presetResult));
        }
        taskExecutionSteps.add(stepFactory.createFinishTaskStep(currentId, compiledTask.getPostTaskActionData(),
                navigationValues, taskName));
        return taskExecutionSteps;
    }

    private Long getCurrentId(Map<String, Long> taskReferences) {
        Long max = Lambda.max(taskReferences);
        return max + NUMBER_OF_TASK_EXECUTION_STEPS;
    }

}
