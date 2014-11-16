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
import com.hp.score.lang.compiler.SlangTextualKeys;
import com.hp.score.lang.compiler.domain.*;
import com.hp.score.lang.compiler.transformers.Transformer;
import com.hp.score.lang.entities.ScoreLangConstants;
import com.hp.score.lang.entities.bindings.Input;
import com.hp.score.lang.entities.bindings.Output;
import com.hp.score.lang.entities.bindings.Result;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.iterators.PeekingIterator;
import org.apache.commons.lang.StringUtils;
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

    @Autowired
    private List<Transformer> transformers;

    public CompiledExecutable compileExecutable(String namespace, String execName, Map<String, Object> executableRawData,
                                                TreeMap<String, List<SlangFile>> dependenciesByNamespace, SlangFile.Type type) {

        Validate.notEmpty(executableRawData, "executable data for: " + execName + " is empty");

        Map<String, Serializable> preExecutableActionData = new HashMap<>();
        Map<String, Serializable> postExecutableActionData = new HashMap<>();

        List<Transformer> preExecTransformers = Lambda.filter(having(on(Transformer.class).getScopes().contains(Transformer.Scope.BEFORE_EXECUTABLE)), transformers);
        List<Transformer> postExecTransformers = Lambda.filter(having(on(Transformer.class).getScopes().contains(Transformer.Scope.AFTER_EXECUTABLE)), transformers);

        validateKeyWordsExits(executableRawData, ListUtils.union(preExecTransformers, postExecTransformers),
                Arrays.asList(SlangTextualKeys.ACTION_KEY, SlangTextualKeys.WORKFLOW_KEY, SlangTextualKeys.FLOW_NAME_KEY));

        preExecutableActionData.putAll(runTransformers(executableRawData, preExecTransformers));

        postExecutableActionData.putAll(runTransformers(executableRawData, postExecTransformers));

        @SuppressWarnings("unchecked") List<Input> inputs = (List<Input>) preExecutableActionData.remove(SlangTextualKeys.INPUTS_KEY);
        @SuppressWarnings("unchecked") List<Output> outputs = (List<Output>) postExecutableActionData.remove(SlangTextualKeys.OUTPUTS_KEY);
        @SuppressWarnings("unchecked") List<Result> results = (List<Result>) postExecutableActionData.remove(SlangTextualKeys.RESULTS_KEY);

        if (type == SlangFile.Type.FLOW) {
            @SuppressWarnings("unchecked") LinkedHashMap<String, Map<String, Object>> workFlowRawData = (LinkedHashMap<String, Map<String, Object>>) executableRawData.get(SlangTextualKeys.WORKFLOW_KEY);
            if (MapUtils.isEmpty(workFlowRawData)) {
                throw new RuntimeException("flow: " + execName + " has no workflow data");
            }

            CompiledWorkflow onFailureWorkFlow = null;
            @SuppressWarnings("unchecked") LinkedHashMap<String, Map<String, Object>> onFailureData =
                    (LinkedHashMap) workFlowRawData.remove(SlangTextualKeys.ON_FAILURE_KEY);
            if (MapUtils.isNotEmpty(onFailureData)) {
                onFailureWorkFlow = compileWorkFlow(onFailureData, dependenciesByNamespace, null);
            }

            CompiledWorkflow compiledWorkflow = compileWorkFlow(workFlowRawData, dependenciesByNamespace, onFailureWorkFlow);
            return new CompiledFlow(preExecutableActionData, postExecutableActionData, compiledWorkflow, namespace, execName, inputs, outputs, results);
        } else {
            @SuppressWarnings("unchecked") Map<String, Object> actionRawData = (Map<String, Object>) executableRawData.get(SlangTextualKeys.ACTION_KEY);
            if (MapUtils.isEmpty(actionRawData)) {
                throw new RuntimeException("operation: " + execName + " has no action data");
            }
            CompiledDoAction compiledDoAction = compileAction(actionRawData);
            return new CompiledOperation(preExecutableActionData, postExecutableActionData, compiledDoAction, namespace, execName, inputs, outputs, results);
        }
    }

    private CompiledDoAction compileAction(Map<String, Object> actionRawData) {
        Map<String, Serializable> actionData = new HashMap<>();

        List<Transformer> actionTransformers = Lambda.filter(having(on(Transformer.class).getScopes().contains(Transformer.Scope.ACTION)), transformers);

        validateKeyWordsExits(actionRawData, actionTransformers, null);

        actionData.putAll(runTransformers(actionRawData, actionTransformers));

        return new CompiledDoAction(actionData);
    }

    private CompiledWorkflow compileWorkFlow(LinkedHashMap<String, Map<String, Object>> workFlowRawData,
                                             TreeMap<String, List<SlangFile>> dependenciesByNamespace,
                                             CompiledWorkflow onFailureWorkFlow) {

        Deque<CompiledTask> compiledTasks = new LinkedList<>();


        Validate.notEmpty(workFlowRawData, "Flow must have tasks in its workflow");

        PeekingIterator<Map.Entry<String, Map<String, Object>>> iterator = new PeekingIterator<>(workFlowRawData.entrySet().iterator());

        String defaultFailureKey = onFailureWorkFlow != null ? onFailureWorkFlow.getCompiledTasks().getFirst().getName() : ScoreLangConstants.FAILURE_RESULT;

        while (iterator.hasNext()) {
            Map.Entry<String, Map<String, Object>> taskRawData = iterator.next();
            Map.Entry<String, Map<String, Object>> nextTaskData = iterator.peek();
            String followingTaskName = nextTaskData != null ? nextTaskData.getKey() : null;
            CompiledTask compiledTask = compileTask(taskRawData.getKey(), taskRawData.getValue(), followingTaskName, dependenciesByNamespace, defaultFailureKey);
            compiledTasks.add(compiledTask);
        }

        if (onFailureWorkFlow != null) {
            compiledTasks.addAll(onFailureWorkFlow.getCompiledTasks());
        }

        return new CompiledWorkflow(compiledTasks);
    }

    private CompiledTask compileTask(String taskName, Map<String, Object> taskRawData, String followingTaskName,
                                     TreeMap<String, List<SlangFile>> dependenciesByNamespace, String defaultFailureKey) {
        Map<String, Serializable> preTaskData = new HashMap<>();
        Map<String, Serializable> postTaskData = new HashMap<>();

        List<Transformer> preTaskTransformers = Lambda.filter(having(on(Transformer.class).getScopes().contains(Transformer.Scope.BEFORE_TASK)), transformers);
        List<Transformer> postTaskTransformers = Lambda.filter(having(on(Transformer.class).getScopes().contains(Transformer.Scope.AFTER_TASK)), transformers);

        validateKeyWordsExits(taskRawData, ListUtils.union(preTaskTransformers, postTaskTransformers), Arrays.asList(SlangTextualKeys.DO_KEY, SlangTextualKeys.NAVIGATION_KEY));

        preTaskData.putAll(runTransformers(taskRawData, preTaskTransformers));
        postTaskData.putAll(runTransformers(taskRawData, postTaskTransformers));

        @SuppressWarnings("unchecked") Map<String, Object> doRawData = (Map<String, Object>) taskRawData.get(SlangTextualKeys.DO_KEY);
        if (MapUtils.isEmpty(doRawData)) {
            throw new RuntimeException("task: " + taskName + " has no reference information");
        }
        String refString = doRawData.keySet().iterator().next();
        String refId = resolveRefId(refString, dependenciesByNamespace);

        @SuppressWarnings("unchecked") Map<String, String> navigationStrings = (Map<String, String>) postTaskData.get(SlangTextualKeys.NAVIGATION_KEY);

        //default navigation
        if (MapUtils.isEmpty(navigationStrings)) {
            navigationStrings = new HashMap<>();
            navigationStrings.put(ScoreLangConstants.SUCCESS_RESULT, followingTaskName == null ? ScoreLangConstants.SUCCESS_RESULT : followingTaskName);
            navigationStrings.put(ScoreLangConstants.FAILURE_RESULT, defaultFailureKey);
        }

        return new CompiledTask(taskName, preTaskData, postTaskData, navigationStrings, refId);
    }

    private Map<String, Serializable> runTransformers(Map<String, Object> rawData, List<Transformer> scopeTransformers) {
        Map<String, Serializable> transformedData = new HashMap<>();
        for (Transformer transformer : scopeTransformers) {
            String key = keyToTransform(transformer);
            try {
                @SuppressWarnings("unchecked") Object value = transformer.transform(rawData.get(key));
                transformedData.put(key, (Serializable) value);
            } catch (ClassCastException e) {
                String message = "\nFailed casting for transformer: " + transformer.getClass().getName() + " with key: " + key + "\n" +
                        "Raw data is: " + rawData.toString();
                throw new RuntimeException(message, e);
            }
        }
        return transformedData;
    }

    private String resolveRefId(String refIdString, TreeMap<String, List<SlangFile>> dependenciesByNamespace) {
        String importAlias = StringUtils.substringBefore(refIdString, ".");
        List<SlangFile> slangFilesList = dependenciesByNamespace.get(importAlias);
        if (CollectionUtils.isEmpty(slangFilesList)) {
            throw new RuntimeException("No file was found in the path for import: " + importAlias);
        }
        List<Map> executableList = new ArrayList<>();
        for (SlangFile slangFile : slangFilesList) {
            switch (slangFile.getType()) {
                case OPERATIONS:
                    executableList.addAll(slangFile.getOperations());
                    break;
                case FLOW:
                    Map<Object, Object> flow = new HashMap<>();
                    flow.put(slangFile.getFlow().get(SlangTextualKeys.FLOW_NAME_KEY), null);
                    executableList.add(flow);
                    break;
                default:
                    throw new RuntimeException("File: is not a flow and not an operation");
            }
        }
        String refIdSuffix = StringUtils.substringAfter(refIdString, ".");
        Map matchingExecutable = Lambda.selectFirst(executableList, hasKey(refIdSuffix));
        if (matchingExecutable == null) {
            throw new RuntimeException("No executable with name: " + refIdSuffix + " was found in the path that for ref: " + refIdString);
        }
        return slangFilesList.get(0).getNamespace() + "." + refIdSuffix;
    }

    private void validateKeyWordsExits(Map<String, Object> operationRawData, List<Transformer> allRelevantTransformers, List<String> additionalValidKeyWords) {
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

    private String keyToTransform(Transformer transformer) {
        String key;
        if (transformer.keyToTransform() != null) {
            key = transformer.keyToTransform();
        } else {
            String simpleClassName = transformer.getClass().getSimpleName();
            key = simpleClassName.substring(0, simpleClassName.indexOf("Transformer"));
        }
        return key.toLowerCase();
    }
}
