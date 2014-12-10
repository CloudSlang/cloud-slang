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

import com.hp.score.lang.compiler.SlangTextualKeys;
import com.hp.score.lang.compiler.model.*;
import com.hp.score.lang.compiler.model.Executable;
import com.hp.score.lang.compiler.transformers.Transformer;
import com.hp.score.lang.entities.bindings.Input;
import com.hp.score.lang.entities.bindings.Output;
import com.hp.score.lang.entities.bindings.Result;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.iterators.PeekingIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ch.lambdaj.Lambda.exists;
import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static com.hp.score.lang.entities.ScoreLangConstants.FAILURE_RESULT;
import static com.hp.score.lang.entities.ScoreLangConstants.SUCCESS_RESULT;

/*
 * Created by orius123 on 09/11/14.
 */
@Component
public class ExecutableBuilder {

    @Autowired
    private List<Transformer> transformers;

    private List<Transformer> preExecTransformers;
    private List<Transformer> postExecTransformers;
    private List<String> execAdditionalKeywords;

    private List<Transformer> actionTransformers;

    private List<Transformer> preTaskTransformers;
    private List<Transformer> postTaskTransformers;
    private List<String> TaskAdditionalKeyWords;

    @PostConstruct
    public void initScopedTransformersAndKeys() {
        //executable transformers and keys
        preExecTransformers = filterTransformers(Transformer.Scope.BEFORE_EXECUTABLE);
        postExecTransformers = filterTransformers(Transformer.Scope.AFTER_EXECUTABLE);
        execAdditionalKeywords = Arrays.asList(SlangTextualKeys.ACTION_KEY, SlangTextualKeys.WORKFLOW_KEY, SlangTextualKeys.FLOW_NAME_KEY);

        //action transformers and keys
        actionTransformers = filterTransformers(Transformer.Scope.ACTION);

        //task transformers and keys
        preTaskTransformers = filterTransformers(Transformer.Scope.BEFORE_TASK);
        postTaskTransformers = filterTransformers(Transformer.Scope.AFTER_TASK);
        TaskAdditionalKeyWords = Arrays.asList(SlangTextualKeys.DO_KEY, SlangTextualKeys.NAVIGATION_KEY);
    }

    private List<Transformer> filterTransformers(Transformer.Scope scope) {
        return filter(having(on(Transformer.class).getScopes().contains(scope)), transformers);
    }

    public Executable transformToExecutable(SlangFile slangFile, String execName, Map<String, Object> executableRawData) {

        Validate.notEmpty(executableRawData, "Executable data for: " + execName + " is empty");
        Validate.notNull(slangFile, "Slang File for " + execName + " is null");

        Map<String, Serializable> preExecutableActionData = new HashMap<>();
        Map<String, Serializable> postExecutableActionData = new HashMap<>();

        validateKeyWordsExits(execName, executableRawData, ListUtils.union(preExecTransformers, postExecTransformers), execAdditionalKeywords);

        preExecutableActionData.putAll(runTransformers(executableRawData, preExecTransformers));
        postExecutableActionData.putAll(runTransformers(executableRawData, postExecTransformers));

        @SuppressWarnings("unchecked") List<Input> inputs = (List<Input>) preExecutableActionData.remove(SlangTextualKeys.INPUTS_KEY);
        @SuppressWarnings("unchecked") List<Output> outputs = (List<Output>) postExecutableActionData.remove(SlangTextualKeys.OUTPUTS_KEY);
        @SuppressWarnings("unchecked") List<Result> results = (List<Result>) postExecutableActionData.remove(SlangTextualKeys.RESULTS_KEY);

        String namespace = slangFile.getNamespace();
        Map<String, String> imports = slangFile.getImports();

        switch (slangFile.getType()) {
            case FLOW:
                @SuppressWarnings("unchecked") LinkedHashMap<String, Map<String, Object>> workFlowRawData =
                        (LinkedHashMap<String, Map<String, Object>>) executableRawData.get(SlangTextualKeys.WORKFLOW_KEY);
                if (MapUtils.isEmpty(workFlowRawData)) {
                    throw new RuntimeException("Flow: " + execName + " has no workflow data");
                }

                Workflow onFailureWorkFlow = null;
                @SuppressWarnings("unchecked") LinkedHashMap<String, Map<String, Object>> onFailureData =
                        (LinkedHashMap) workFlowRawData.remove(SlangTextualKeys.ON_FAILURE_KEY);
                if (MapUtils.isNotEmpty(onFailureData)) {
                    onFailureWorkFlow = compileWorkFlow(onFailureData, imports, null, true);
                }

                Workflow workflow = compileWorkFlow(workFlowRawData, imports, onFailureWorkFlow, false);

                return new Flow(preExecutableActionData, postExecutableActionData, workflow, namespace, execName, inputs, outputs, results);

            case OPERATIONS:
                @SuppressWarnings("unchecked") Map<String, Object> actionRawData = (Map<String, Object>) executableRawData.get(SlangTextualKeys.ACTION_KEY);
                if (MapUtils.isEmpty(actionRawData)) {
                    throw new RuntimeException("Operation: " + execName + " has no action data");
                }
                Action action = compileAction(actionRawData);
                return new Operation(preExecutableActionData, postExecutableActionData, action, namespace, execName, inputs, outputs, results);
            default:
                throw new RuntimeException("File: " + slangFile.getFileName() + " is not a flow type or operations");
        }
    }

    private Action compileAction(Map<String, Object> actionRawData) {
        Map<String, Serializable> actionData = new HashMap<>();

        validateKeyWordsExits("action data", actionRawData, actionTransformers, null);

        actionData.putAll(runTransformers(actionRawData, actionTransformers));

        return new Action(actionData);
    }

    private Workflow compileWorkFlow(LinkedHashMap<String, Map<String, Object>> workFlowRawData,
                                             Map<String, String> imports,
                                             Workflow onFailureWorkFlow,
                                             boolean onFailureSection) {

        Deque<Task> tasks = new LinkedList<>();

        Validate.notEmpty(workFlowRawData, "Flow must have tasks in its workflow");

        PeekingIterator<Map.Entry<String, Map<String, Object>>> iterator =
                new PeekingIterator<>(workFlowRawData.entrySet().iterator());

        boolean isOnFailureDefined = onFailureWorkFlow != null;

        String defaultFailure = isOnFailureDefined ?
                onFailureWorkFlow.getTasks().getFirst().getName() : FAILURE_RESULT;

        while (iterator.hasNext()) {
            Map.Entry<String, Map<String, Object>> taskRawData = iterator.next();
            Map.Entry<String, Map<String, Object>> nextTaskData = iterator.peek();
            String taskName = taskRawData.getKey();
            Map<String, Object> taskRawDataValue = taskRawData.getValue();
            String defaultSuccess;
            if (nextTaskData != null) {
                defaultSuccess = nextTaskData.getKey();
            } else {
                defaultSuccess = onFailureSection ? FAILURE_RESULT : SUCCESS_RESULT;
            }
            Task task = compileTask(taskName, taskRawDataValue, defaultSuccess, imports, defaultFailure);
            tasks.add(task);
        }

        if (isOnFailureDefined) {
            tasks.addAll(onFailureWorkFlow.getTasks());
        }

        return new Workflow(tasks);
    }

    private Task compileTask(String taskName, Map<String, Object> taskRawData, String defaultSuccess,
                                     Map<String, String> imports, String defaultFailure) {

        if (MapUtils.isEmpty(taskRawData)) {
            throw new RuntimeException("Task: " + taskName + " has no data");
        }

        Map<String, Serializable> preTaskData = new HashMap<>();
        Map<String, Serializable> postTaskData = new HashMap<>();

        validateKeyWordsExits(taskName, taskRawData, ListUtils.union(preTaskTransformers, postTaskTransformers), TaskAdditionalKeyWords);

        preTaskData.putAll(runTransformers(taskRawData, preTaskTransformers));
        postTaskData.putAll(runTransformers(taskRawData, postTaskTransformers));

        @SuppressWarnings("unchecked") Map<String, Object> doRawData = (Map<String, Object>) taskRawData.get(SlangTextualKeys.DO_KEY);
        if (MapUtils.isEmpty(doRawData)) {
            throw new RuntimeException("Task: " + taskName + " has no reference information");
        }
        String refString = doRawData.keySet().iterator().next();
        String refId = resolveRefId(refString, imports);

        @SuppressWarnings("unchecked") Map<String, String> navigationStrings = (Map<String, String>) postTaskData.get(SlangTextualKeys.NAVIGATION_KEY);

        //default navigation
        if (MapUtils.isEmpty(navigationStrings)) {
            navigationStrings = new HashMap<>();
            navigationStrings.put(SUCCESS_RESULT, defaultSuccess);
            navigationStrings.put(FAILURE_RESULT, defaultFailure);
        }

        return new Task(taskName, preTaskData, postTaskData, navigationStrings, refId);
    }

    private String resolveRefId(String refIdString, Map<String, String> imports) {
        String importAlias = StringUtils.substringBefore(refIdString, ".");
        String refName = StringUtils.substringAfter(refIdString, ".");
        return imports.get(importAlias) + "." + refName;
    }

    private Map<String, Serializable> runTransformers(Map<String, Object> rawData, List<Transformer> scopeTransformers) {
        Map<String, Serializable> transformedData = new HashMap<>();
        for (Transformer transformer : scopeTransformers) {
            String key = keyToTransform(transformer);
            Object value = rawData.get(key);
            try {
                @SuppressWarnings("unchecked") Object transformedValue = transformer.transform(value);
                transformedData.put(key, (Serializable) transformedValue);
            } catch (ClassCastException e) {
                Class transformerType = getTransformerFromType(transformer);
                if (value instanceof Map && transformerType.equals(List.class)) {
                    throw new RuntimeException("Key: '" + key + "' expected a list but got a map\n" +
                            "By the Yaml spec lists properties are marked with a '- ' (dash followed by a space)");
                }
                if (value instanceof List && transformerType.equals(Map.class)) {
                    throw new RuntimeException("Key: '" + key + "' expected a map but got a list\n" +
                            "By the Yaml spec maps properties are NOT marked with a '- ' (dash followed by a space)");
                }
                String message = "\nFailed casting for key: " + key +
                        "Raw data is: " + key + " : " +rawData.get(key).toString() +
                        "\n Transformer is: " + transformer.getClass().getName();
                throw new RuntimeException(message, e);
            }
        }
        return transformedData;
    }

    private Class getTransformerFromType(Transformer transformer){
        ResolvableType resolvableType = ResolvableType.forClass(Transformer.class, transformer.getClass());
        return resolvableType.getGeneric(0).resolve();
    }

    private void validateKeyWordsExits(String dataLogicalName, Map<String, Object> rawData, List<Transformer> allRelevantTransformers, List<String> additionalValidKeyWords) {
        Set<String> validKeywords = new HashSet<>();

        if (additionalValidKeyWords != null) {
            validKeywords.addAll(additionalValidKeyWords);
        }

        for (Transformer transformer : allRelevantTransformers) {
            validKeywords.add(keyToTransform(transformer));
        }

        for (String key : rawData.keySet()) {
            if (!(exists(validKeywords, equalToIgnoringCase(key)))) {
                throw new RuntimeException("No transformer were found for key: " + key + " at: " + dataLogicalName);
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
