package com.hp.score.lang.compiler.utils;/*
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

import ch.lambdaj.Lambda;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.ExecutionStep;
import com.hp.score.lang.compiler.domain.CompiledFlow;
import com.hp.score.lang.compiler.domain.CompiledOperation;
import com.hp.score.lang.compiler.domain.CompiledTask;
import com.hp.score.lang.entities.bindings.Result;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

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

    public ExecutionPlan createOperationExecutionPlan(CompiledOperation compiledOp) {
        ExecutionPlan executionPlan = new ExecutionPlan();
        executionPlan.setName(compiledOp.getName());
        executionPlan.setLanguage(SLANG_NAME);
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

        final long FLOW_END_STEP_INDEX = 0L;

        Long stepsIndex = 1L;
        executionPlan.setBeginStep(stepsIndex);
        //flow start step
        executionPlan.addStep(stepFactory.createStartStep(stepsIndex++, compiledFlow.getPreExecActionData(),
                compiledFlow.getInputs(), compiledFlow.getName()));
        //flow end step
        executionPlan.addStep(stepFactory.createEndStep(FLOW_END_STEP_INDEX, compiledFlow.getPostExecActionData(),
                compiledFlow.getOutputs(), compiledFlow.getResults(), compiledFlow.getName()));

        Map<String, Long> taskReferences = new HashMap<>();
        for (Result result : compiledFlow.getResults()) {
            taskReferences.put(result.getName(), FLOW_END_STEP_INDEX);
        }

        List<CompiledTask> compiledTasks = compiledFlow.getCompiledWorkflow().getCompiledTasks();

        if (CollectionUtils.isEmpty(compiledTasks)) {
            throw new RuntimeException("Flow: " + compiledFlow.getName() + " has no tasks");
        }

        List<ExecutionStep> taskExecutionSteps = buildTaskExecutionSteps(compiledTasks.get(0), stepsIndex, taskReferences, compiledTasks);
        executionPlan.addSteps(taskExecutionSteps);

        return executionPlan;
    }

    private List<ExecutionStep> buildTaskExecutionSteps(CompiledTask compiledTask, Long currentId,
                                                        Map<String, Long> taskReferences, List<CompiledTask> compiledTasks) {
        List<ExecutionStep> taskExecutionSteps = new ArrayList<>();
        String taskName = compiledTask.getName();
        //Begin Task
        taskReferences.put(taskName, currentId);
        taskExecutionSteps.add(stepFactory.createBeginTaskStep(currentId++, compiledTask.getPreTaskActionData(),
                compiledTask.getRefId(), taskName));
        Long endTaskId = currentId++;

        //End Task
        Map<String, Long> navigationValues = new HashMap<>();
        for (Map.Entry<String, String> entry : compiledTask.getNavigationStrings().entrySet()) {
            String nextStepName = entry.getValue();
            if (taskReferences.get(nextStepName) == null) {
                CompiledTask nextTaskToCompile = Lambda.selectFirst(compiledTasks, having(on(CompiledTask.class).getName(), equalTo(nextStepName)));
                taskExecutionSteps.addAll(buildTaskExecutionSteps(nextTaskToCompile, currentId, taskReferences, compiledTasks));
            }
            navigationValues.put(entry.getKey(), taskReferences.get(nextStepName));
        }
        taskExecutionSteps.add(stepFactory.createFinishTaskStep(endTaskId, compiledTask.getPostTaskActionData(),
                navigationValues, taskName));
        return taskExecutionSteps;
    }

}
