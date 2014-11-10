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
import static org.hamcrest.Matchers.hasKey;

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

    public ExecutionPlan compileExecutable(Map<String, Object> executableRawData, TreeMap<String, List<SlangFile>> dependenciesByNamespace, SlangFile.Type type) {
        Map<String, Serializable> preExecutableActionData = new HashMap<>();
        Map<String, Serializable> postExecutableActionData = new HashMap<>();
        CompiledDoAction compiledDoAction = null;
        CompiledWorkflow compiledWorkflow = null;

        List<Transformer> preExecTransformers = Lambda.filter(having(on(Transformer.class).getScopes().contains(Transformer.Scope.BEFORE_EXECUTABLE)), transformers);
        List<Transformer> postExecTransformers = Lambda.filter(having(on(Transformer.class).getScopes().contains(Transformer.Scope.AFTER_EXECUTABLE)), transformers);

        validateKeyWordsAreValid(executableRawData, ListUtils.union(preExecTransformers, postExecTransformers), Arrays.asList(SlangTextualKeys.ACTION_KEY, SlangTextualKeys.WORKFLOW_KEY));

        Iterator<Map.Entry<String, Object>> it = executableRawData.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> pairs = it.next();
            String key = pairs.getKey();

            if (key.equals(SlangTextualKeys.ACTION_KEY)) {
                compiledDoAction = compileAction((Map<String, Object>) pairs.getValue());
            }

            if (key.equals(SlangTextualKeys.WORKFLOW_KEY)) {
                compiledWorkflow = compileWorkFlow((Map<String, Object>) pairs.getValue());
            }

            preExecutableActionData.putAll(runTransformers(executableRawData, preExecTransformers, key));

            postExecutableActionData.putAll(runTransformers(executableRawData, postExecTransformers, key));

            it.remove();
        }

        if (type == SlangFile.Type.FLOW) {
            return createFlowExecutionPlan(new CompiledFlow(preExecutableActionData, postExecutableActionData, compiledWorkflow), dependenciesByNamespace);
        } else {
            return createOperationExecutionPlan(new CompiledOperation(preExecutableActionData, postExecutableActionData, compiledDoAction));
        }
    }

    private CompiledDoAction compileAction(Map<String, Object> actionRawData) {
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
        return new CompiledDoAction(actionData);
    }

    private CompiledWorkflow compileWorkFlow(Map<String, Object> workFlowRawData) {
        List<CompiledTask> compiledTasks = new ArrayList<>();

        Validate.notEmpty(workFlowRawData, "Flow must have tasks in its workflow");

        for (Map.Entry<String, Object> taskRawData : workFlowRawData.entrySet()) {
            compiledTasks.add(compileTask(taskRawData.getKey(), (Map<String, Object>) taskRawData.getValue()));
        }
        return new CompiledWorkflow(compiledTasks);
    }

    private ExecutionPlan createOperationExecutionPlan(CompiledOperation compiledOperation) {
        ExecutionPlan executionPlan = new ExecutionPlan();
        executionPlan.setLanguage(SLANG_NAME);
        executionPlan.setBeginStep(1L);

        executionPlan.addStep(stepFactory.createStartStep(1L, compiledOperation.getPreExecActionData()));
        executionPlan.addStep(stepFactory.createActionStep(2L, compiledOperation.getCompiledDoAction().getActionData()));
        executionPlan.addStep(stepFactory.createEndStep(3L, compiledOperation.getPostExecActionData()));
        return executionPlan;
    }

    private ExecutionPlan createFlowExecutionPlan(CompiledFlow compiledFlow, TreeMap<String, List<SlangFile>> dependenciesByNamespace) {
        ExecutionPlan executionPlan = new ExecutionPlan();
        executionPlan.setLanguage(SLANG_NAME);

        Long index = 1L;
        executionPlan.setBeginStep(index);
        executionPlan.addStep(stepFactory.createStartStep(index++, compiledFlow.getPreExecActionData()));
        for (CompiledTask compiledTask : compiledFlow.getCompiledWorkflow().getCompiledTasks()) {
            String refId = resolveRefId(compiledTask.getPreTaskActionData(), dependenciesByNamespace);
            executionPlan.addStep(stepFactory.createBeginTaskStep(index++, compiledTask.getPreTaskActionData(), refId));
            executionPlan.addStep(stepFactory.createFinishTaskStep(index++, compiledTask.getPostTaskActionData()));
        }
        executionPlan.addStep(stepFactory.createEndStep(0L, compiledFlow.getPostExecActionData()));

        return executionPlan;
    }

    private String resolveRefId(Map<String, Serializable> preTaskActionData, TreeMap<String, List<SlangFile>> dependenciesByNamespace) {
        String refId = (String) preTaskActionData.remove(ScoreLangConstants.REF_ID);
        List<SlangFile> slangFilesList = dependenciesByNamespace.get(refId.substring(0, refId.indexOf(".")));
        List<Map> operationList = new ArrayList<>();
        for (SlangFile slangFile : slangFilesList) {
            operationList.addAll(slangFile.getOperations());
        }
        String refIdSuffix = refId.substring(refId.indexOf(".") + 1, refId.length());
        Map matchingOperation = Lambda.selectFirst(operationList, hasKey(refIdSuffix));
        if (matchingOperation == null) {
            throw new RuntimeException("no operation was found in the classpath that for ref: " + refId);
        }
        return slangFilesList.get(0).getNamespace() + "." + refIdSuffix;
    }

    private CompiledTask compileTask(String taskName, Map<String, Object> taskRawData) {
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

        return new CompiledTask(taskName, preTaskData, postTaskData);
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
