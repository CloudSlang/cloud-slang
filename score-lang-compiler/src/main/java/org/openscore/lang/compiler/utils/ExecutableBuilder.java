package org.openscore.lang.compiler.utils;/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/


import org.openscore.lang.compiler.SlangTextualKeys;
import org.openscore.lang.compiler.model.*;
import org.openscore.lang.compiler.transformers.Transformer;
import org.openscore.lang.entities.bindings.Input;
import org.openscore.lang.entities.bindings.Output;
import org.openscore.lang.entities.bindings.Result;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.iterators.PeekingIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.openscore.lang.entities.ScoreLangConstants.FAILURE_RESULT;
import static org.openscore.lang.entities.ScoreLangConstants.SUCCESS_RESULT;

/*
 * Created by orius123 on 09/11/14.
 */
@Component
public class ExecutableBuilder {

    @Autowired
    private List<Transformer> transformers;

    @Autowired
    private TransformersHandler transformersHandler;

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
        execAdditionalKeywords = Arrays.asList(
                SlangTextualKeys.ACTION_KEY, SlangTextualKeys.WORKFLOW_KEY, SlangTextualKeys.FLOW_NAME_KEY);

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

        transformersHandler.validateKeyWords(execName, executableRawData,
                ListUtils.union(preExecTransformers, postExecTransformers), execAdditionalKeywords);

        preExecutableActionData.putAll(transformersHandler.runTransformers(executableRawData, preExecTransformers));
        postExecutableActionData.putAll(transformersHandler.runTransformers(executableRawData, postExecTransformers));

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

        transformersHandler.validateKeyWords("action data", actionRawData, actionTransformers, null);

        actionData.putAll(transformersHandler.runTransformers(actionRawData, actionTransformers));

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

        transformersHandler.validateKeyWords(taskName, taskRawData, ListUtils.union(preTaskTransformers, postTaskTransformers), TaskAdditionalKeyWords);

        preTaskData.putAll(transformersHandler.runTransformers(taskRawData, preTaskTransformers));
        postTaskData.putAll(transformersHandler.runTransformers(taskRawData, postTaskTransformers));

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
}
