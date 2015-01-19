/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openscore.lang.compiler.utils;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.iterators.PeekingIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.openscore.lang.compiler.SlangTextualKeys;
import org.openscore.lang.compiler.model.*;
import org.openscore.lang.compiler.transformers.Transformer;
import org.openscore.lang.entities.bindings.Input;
import org.openscore.lang.entities.bindings.Output;
import org.openscore.lang.entities.bindings.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.*;

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

    public Executable transformToExecutable(ParsedSlang parsedSlang, String execName, Map<String, Object> executableRawData) {

        Validate.notEmpty(executableRawData, "Error compiling " + parsedSlang.getName() + ". Executable data for: " + execName + " is empty");
        Validate.notNull(parsedSlang, "Slang source for " + execName + " is null");

        Map<String, Serializable> preExecutableActionData = new HashMap<>();
        Map<String, Serializable> postExecutableActionData = new HashMap<>();

        transformersHandler.validateKeyWords(execName, executableRawData,
                ListUtils.union(preExecTransformers, postExecTransformers), execAdditionalKeywords);

        preExecutableActionData.putAll(transformersHandler.runTransformers(executableRawData, preExecTransformers));
        postExecutableActionData.putAll(transformersHandler.runTransformers(executableRawData, postExecTransformers));

        @SuppressWarnings("unchecked") List<Input> inputs = (List<Input>) preExecutableActionData.remove(SlangTextualKeys.INPUTS_KEY);
        @SuppressWarnings("unchecked") List<Output> outputs = (List<Output>) postExecutableActionData.remove(SlangTextualKeys.OUTPUTS_KEY);
        @SuppressWarnings("unchecked") List<Result> results = (List<Result>) postExecutableActionData.remove(SlangTextualKeys.RESULTS_KEY);

        String namespace = parsedSlang.getNamespace();
        Map<String, String> imports = parsedSlang.getImports();
        resolveVariables(inputs, imports);
        Map<String, SlangFileType> dependencies;
        switch (parsedSlang.getType()) {
            case FLOW:

                if(!executableRawData.containsKey(SlangTextualKeys.WORKFLOW_KEY)){
                    throw new RuntimeException("Error compiling " + parsedSlang.getName() + ". Flow: " + execName + " has no workflow property");
                }
                @SuppressWarnings("unchecked") LinkedHashMap<String, Map<String, Object>> workFlowRawData =
                        (LinkedHashMap<String, Map<String, Object>>) executableRawData.get(SlangTextualKeys.WORKFLOW_KEY);
                if (MapUtils.isEmpty(workFlowRawData)) {
                    throw new RuntimeException("Error compiling " + parsedSlang.getName() + ". Flow: " + execName + " has no workflow data");
                }

                Workflow onFailureWorkFlow = null;
                @SuppressWarnings("unchecked") LinkedHashMap<String, Map<String, Object>> onFailureData =
                        (LinkedHashMap) workFlowRawData.remove(SlangTextualKeys.ON_FAILURE_KEY);
                if (MapUtils.isNotEmpty(onFailureData)) {
                    onFailureWorkFlow = compileWorkFlow(onFailureData, imports, null, true);
                }

                Workflow workflow = compileWorkFlow(workFlowRawData, imports, onFailureWorkFlow, false);
                //todo: add sys vars dependencies?
                dependencies = fetchDirectTasksDependencies(workflow);
                return new Flow(preExecutableActionData, postExecutableActionData, workflow, namespace, execName, inputs, outputs, results, dependencies);

            case OPERATIONS:
                Map<String, Object> actionRawData = (Map<String, Object>) executableRawData.get(SlangTextualKeys.ACTION_KEY);
                if (MapUtils.isEmpty(actionRawData)) {
                    throw new RuntimeException("Error compiling " + parsedSlang.getName() + ". Operation: " + execName + " has no action data");
                }
                Action action = compileAction(actionRawData);
                //todo: add sys vars dependencies?
                dependencies = new HashMap<>();
                return new Operation(preExecutableActionData, postExecutableActionData, action, namespace, execName, inputs, outputs, results, dependencies);
            default:
                throw new RuntimeException("Error compiling " + parsedSlang.getName() + ". It is not of flow or operations type");
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
            Map<String, Object> taskRawDataValue;
            try {
                taskRawDataValue = taskRawData.getValue();
            } catch (ClassCastException ex){
                throw new RuntimeException("Task: " + taskName + " syntax is illegal.\nBelow task name, there should be a map of values in the format:\ndo:\n\top_name:");
            }

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

        try {
            preTaskData.putAll(transformersHandler.runTransformers(taskRawData, preTaskTransformers));
            postTaskData.putAll(transformersHandler.runTransformers(taskRawData, postTaskTransformers));
        } catch (Exception ex){
            throw new RuntimeException("For task: " + taskName + " syntax is illegal.\n" + ex.getMessage(), ex);
        }
        List<Input> inputs = (List<Input>)preTaskData.get(SlangTextualKeys.DO_KEY);
        resolveVariables(inputs, imports);
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

	private static void resolveVariables(List<Input> inputs, Map<String, String> imports) {
		if(inputs == null) return;
		for(Input input : inputs) {
			String variableName = input.getVariableName();
			if(variableName != null) {
				variableName = resolveRefId(variableName, imports);
				input.setVariableName(variableName);
			}
		}
	}

	private static String resolveRefId(String refIdString, Map<String, String> imports) {
		String alias = StringUtils.substringBefore(refIdString, ".");
		Validate.notNull(imports, "No imports specified for source: " + refIdString);
		if(!imports.containsKey(alias)) throw new RuntimeException("Unresovled alias: " + alias);
		String refName = StringUtils.substringAfter(refIdString, ".");
		return imports.get(alias) + "." + refName;
	}

    /**
     * Fetch the first level of the dependencies of the executable (non recursively)
     * @param workflow the workflow of the flow
     * @return a map of dependencies. Key - dependency full name, value - type
     */
    private Map<String, SlangFileType> fetchDirectTasksDependencies(Workflow workflow){
        Map<String, SlangFileType> dependencies = new HashMap<>();
        Deque<Task> tasks = workflow.getTasks();
        for (Task task : tasks) {
            String refId = task.getRefId();
            dependencies.put(refId, SlangFileType.EXECUTABLE);
        }
        return dependencies;
    }

}
