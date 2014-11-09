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
import com.hp.score.lang.compiler.SlangTextualKeys;
import com.hp.score.lang.compiler.domain.*;
import com.hp.score.lang.compiler.transformers.Transformer;
import com.hp.score.lang.entities.ScoreLangConstants;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalToIgnoringCase;

/*
 * Created by orius123 on 09/11/14.
 */
@Component
public class ExecutableBuilder {

    private static final String SLANG_NAME = "slang";

    @Autowired
    private List<Transformer> transformers;


    @Autowired
    private ExecutionStepFactory stepFactory;

    public ExecutionPlan compileExecutable(Map<String, Object> operationRawData, Map<String, ExecutionPlan> dependencies, SlangFile.Type type) {
        Map<String, Serializable> preOperationActionData = new HashMap<>();
        Map<String, Serializable> postOperationActionData = new HashMap<>();
        DoAction doAction = null;
        Workflow workflow = null;

        List<Transformer> preOpTransformers = Lambda.filter(having(on(Transformer.class).getScopes().contains(Transformer.Scope.BEFORE_OPERATION)), transformers);
        List<Transformer> postOpTransformers = Lambda.filter(having(on(Transformer.class).getScopes().contains(Transformer.Scope.AFTER_OPERATION)), transformers);

        validateKeyWordsAreValid(operationRawData, ListUtils.union(preOpTransformers, postOpTransformers), Arrays.asList(SlangTextualKeys.ACTION_KEY, SlangTextualKeys.WORKFLOW_KEY));

        Iterator<Map.Entry<String, Object>> it = operationRawData.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> pairs = it.next();
            String key = pairs.getKey();

            if (key.equals(SlangTextualKeys.ACTION_KEY)) {
                doAction = compileAction((Map<String, Object>) pairs.getValue());
            }

            if (key.equals(SlangTextualKeys.WORKFLOW_KEY)) {
                workflow = compileWorkFlow((Map<String, Object>) pairs.getValue());
            }

            preOperationActionData.putAll(runTransformers(operationRawData, preOpTransformers, key));

            postOperationActionData.putAll(runTransformers(operationRawData, postOpTransformers, key));

            it.remove();
        }

        if (type == SlangFile.Type.FLOW) {
            return createFlowExecutionPlan(new Flow(preOperationActionData, postOperationActionData, workflow), dependencies);
        } else {
            return createOperationExecutionPlan(new Operation(preOperationActionData, postOperationActionData, doAction));
        }
    }

    private DoAction compileAction(Map<String, Object> actionRawData) {
        Map<String, Serializable> actionData = new HashMap<>();

        List<Transformer> actionTransformers = Lambda.filter(having(on(Transformer.class).getScopes().contains(Transformer.Scope.ACTION)), transformers);

        validateKeyWordsAreValid(actionRawData, actionTransformers, null);

        Iterator<Map.Entry<String, Object>> it = actionRawData.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> pairs = it.next();
            String key = pairs.getKey();

            actionData.putAll(runTransformers(actionRawData, actionTransformers, key));

            it.remove();
        }
        return new DoAction(actionData);
    }

    private Workflow compileWorkFlow(Map<String, Object> workFlowRawData) {
        List<Task> tasks = new ArrayList<>();

        Validate.notEmpty(workFlowRawData, "Flow must have tasks in its workflow");

        for (Map.Entry<String, Object> taskRawData : workFlowRawData.entrySet()) {
            tasks.add(compileTask(taskRawData.getKey(), (Map<String, Object>) taskRawData.getValue()));
        }
        return new Workflow(tasks);
    }

    private ExecutionPlan createOperationExecutionPlan(Operation operation) {
        ExecutionPlan executionPlan = new ExecutionPlan();
        executionPlan.setLanguage(SLANG_NAME);
        executionPlan.setBeginStep(1L);

        executionPlan.addStep(stepFactory.createStartStep(1L, operation.getPreExecActionData()));
        executionPlan.addStep(stepFactory.createActionStep(2L, operation.getDoAction().getActionData()));
        executionPlan.addStep(stepFactory.createEndStep(3L, operation.getPostExecActionData()));
        return executionPlan;
    }

    private ExecutionPlan createFlowExecutionPlan(Flow flow, Map<String, ExecutionPlan> dependencies) {
        ExecutionPlan executionPlan = new ExecutionPlan();
        executionPlan.setLanguage(SLANG_NAME);

        Long index = 1L;
        executionPlan.setBeginStep(index);
        executionPlan.addStep(stepFactory.createStartStep(index++, flow.getPreExecActionData()));
        for (Task task : flow.getWorkflow().getTasks()) {
            executionPlan.addStep(stepFactory.createBeginTaskStep(index++, task.getPreTaskActionData()));
            executionPlan.addStep(stepFactory.createFinishTaskStep(index++, task.getPostTaskActionData()));
        }
        executionPlan.addStep(stepFactory.createEndStep(0L, flow.getPostExecActionData()));

        return executionPlan;
    }

    private Task compileTask(String taskName, Map<String, Object> taskRawData) {
        Map<String, Serializable> preTaskData = new HashMap<>();
        Map<String, Serializable> postTaskData = new HashMap<>();

        List<Transformer> preTaskTransformers = Lambda.filter(having(on(Transformer.class).getScopes().contains(Transformer.Scope.BEFORE_TASK)), transformers);
        List<Transformer> postTaskTransformers = Lambda.filter(having(on(Transformer.class).getScopes().contains(Transformer.Scope.AFTER_TASK)), transformers);

        validateKeyWordsAreValid(taskRawData, ListUtils.union(preTaskTransformers, postTaskTransformers), Arrays.asList(SlangTextualKeys.DO_KEY));

        Iterator<Map.Entry<String, Object>> it = taskRawData.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> pairs = it.next();
            String key = pairs.getKey();

            if (key.equals(SlangTextualKeys.DO_KEY)) {
                LinkedHashMap<String, Object> doRawData = (LinkedHashMap<String, Object>) pairs.getValue();
                String operationName = doRawData.keySet().iterator().next();
                preTaskData.put(ScoreLangConstants.REF_ID, operationName);
            }

            preTaskData.putAll(runTransformers(taskRawData, preTaskTransformers, key));

            postTaskData.putAll(runTransformers(taskRawData, postTaskTransformers, key));

            it.remove();
        }

        return new Task(taskName, preTaskData, postTaskData);
    }

    private Map<String, Serializable> runTransformers(Map<String, Object> rawData, List<Transformer> transformers, String key) {
        Map<String, Serializable> transformedData = new HashMap<>();
        for (Transformer transformer : transformers) {
            if (shouldApplyTransformer(transformer, key)) {
                Object value = transformer.transform(rawData.get(key));
                transformedData.put(key, (Serializable) value);
            }
        }
        return transformedData;
    }

    private void validateKeyWordsAreValid(Map<String, Object> operationRawData, List<Transformer> allRelevantTransformers, List<String> additionalValidKeyWords) {
        Set<String> validKeywords = new HashSet<>();

        if (additionalValidKeyWords != null) {
            validKeywords.addAll(additionalValidKeyWords);
        }

        for (Transformer transformer : allRelevantTransformers) {
            validKeywords.add(keyToTransform(transformer));
        }

        for (String key : operationRawData.keySet()) {
            if (!(Lambda.exists(validKeywords, equalToIgnoringCase(key)))) {
                throw new RuntimeException("No transformer were found for key: " + key);
            }
        }
    }

    private boolean shouldApplyTransformer(Transformer transformer, String key) {
        String transformerName = keyToTransform(transformer);
        return transformerName.equalsIgnoreCase(key);
    }

    private String keyToTransform(Transformer transformer) {
        String key;
        if (transformer.keyToTransform() != null) {
            key = transformer.keyToTransform();
        } else {
            String simpleClassName = transformer.getClass().getSimpleName();
            key = simpleClassName.substring(0, simpleClassName.indexOf("Transformer"));
        }
        return key;
    }
}
