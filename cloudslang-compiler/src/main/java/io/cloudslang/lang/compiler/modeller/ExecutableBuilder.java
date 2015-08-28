/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.compiler.modeller;

import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.transformers.Transformer;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.iterators.PeekingIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Workflow;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.compiler.modeller.model.Action;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Operation;
import io.cloudslang.lang.compiler.modeller.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.io.Serializable;
import java.util.*;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;

import static io.cloudslang.lang.compiler.SlangTextualKeys.*;
import static io.cloudslang.lang.entities.ScoreLangConstants.*;

/*
 * Created by orius123 on 09/11/14.
 */
@Component
public class ExecutableBuilder {

    public static final String MULTIPLE_ON_FAILURE_MESSAGE_SUFFIX = "Multiple 'on_failure' properties found";

    @Autowired
    private List<Transformer> transformers;

    @Autowired
    private TransformersHandler transformersHandler;

    private List<Transformer> preExecTransformers;
    private List<Transformer> postExecTransformers;
    private List<String> execAdditionalKeywords = Arrays.asList(SlangTextualKeys.ACTION_KEY, SlangTextualKeys.WORKFLOW_KEY, SlangTextualKeys.EXECUTABLE_NAME_KEY);

    private List<Transformer> actionTransformers;
    private List<List<String>> actionTransformerConstraintGroups;

    private List<Transformer> preTaskTransformers;
    private List<Transformer> postTaskTransformers;
    private List<String> TaskAdditionalKeyWords = Arrays.asList(ScoreLangConstants.LOOP_KEY, SlangTextualKeys.DO_KEY, SlangTextualKeys.NAVIGATION_KEY);

    @PostConstruct
    public void initScopedTransformersAndKeys() {
        //executable transformers
        preExecTransformers = filterTransformers(Transformer.Scope.BEFORE_EXECUTABLE);
        postExecTransformers = filterTransformers(Transformer.Scope.AFTER_EXECUTABLE);

        //action transformers and keys
        actionTransformers = filterTransformers(Transformer.Scope.ACTION);
        //action keys excluding each other
        actionTransformerConstraintGroups = Arrays.asList(Arrays.asList(ScoreLangConstants.PYTHON_SCRIPT_KEY, SlangTextualKeys.JAVA_ACTION));

        //task transformers
        preTaskTransformers = filterTransformers(Transformer.Scope.BEFORE_TASK);
        postTaskTransformers = filterTransformers(Transformer.Scope.AFTER_TASK);
    }

    private List<Transformer> filterTransformers(Transformer.Scope scope) {
        return filter(having(on(Transformer.class).getScopes().contains(scope)), transformers);
    }

    public Executable transformToExecutable(ParsedSlang parsedSlang, String execName, Map<String, Object> executableRawData) {

        Validate.notEmpty(executableRawData, "Error compiling " + parsedSlang.getName() + ". Executable data for: \'" + execName + "\' is empty");
        Validate.notNull(parsedSlang, "Slang source for: \'" + execName + "\' is null");

        Map<String, Serializable> preExecutableActionData = new HashMap<>();
        Map<String, Serializable> postExecutableActionData = new HashMap<>();

        transformersHandler.validateKeyWords(execName, executableRawData,
                ListUtils.union(preExecTransformers, postExecTransformers), execAdditionalKeywords, null);

        preExecutableActionData.putAll(transformersHandler.runTransformers(executableRawData, preExecTransformers));
        postExecutableActionData.putAll(transformersHandler.runTransformers(executableRawData, postExecTransformers));

        @SuppressWarnings("unchecked") List<Input> inputs = (List<Input>) preExecutableActionData.remove(SlangTextualKeys.INPUTS_KEY);
        @SuppressWarnings("unchecked") List<Output> outputs = (List<Output>) postExecutableActionData.remove(SlangTextualKeys.OUTPUTS_KEY);
        @SuppressWarnings("unchecked") List<Result> results = (List<Result>) postExecutableActionData.remove(SlangTextualKeys.RESULTS_KEY);

        String namespace = parsedSlang.getNamespace();
        Map<String, String> imports = parsedSlang.getImports();
        Set<String> dependencies;
        switch (parsedSlang.getType()) {
            case FLOW:

                if(!executableRawData.containsKey(SlangTextualKeys.WORKFLOW_KEY)){
                    throw new RuntimeException("Error compiling " + parsedSlang.getName() + ". Flow: " + execName + " has no workflow property");
                }
                List<Map<String, Map<String, Object>>> workFlowRawData;
                try{
                    workFlowRawData = (List) executableRawData.get(SlangTextualKeys.WORKFLOW_KEY);
                } catch (ClassCastException ex){
                    throw new RuntimeException("Flow: '" + execName + "' syntax is illegal.\nBelow 'workflow' property there should be a list of tasks and not a map");
                }
                if (CollectionUtils.isEmpty(workFlowRawData)) {
                    throw new RuntimeException("Error compiling " + parsedSlang.getName() + ". Flow: " + execName + " has no workflow data");
                }

                Workflow onFailureWorkFlow = null;
                List<Map<String, Map<String, Object>>> onFailureData;
                Iterator<Map<String, Map<String, Object>>> tasksIterator = workFlowRawData.iterator();
                boolean onFailureFound = false;
                while(tasksIterator.hasNext()){
                    Map<String, Map<String, Object>> taskData = tasksIterator.next();
                    String taskName = taskData.keySet().iterator().next();
                    if(taskName.equals(SlangTextualKeys.ON_FAILURE_KEY)){
                        if (onFailureFound) {
                            throw new RuntimeException("Flow: '" + execName + "' syntax is illegal.\n" + MULTIPLE_ON_FAILURE_MESSAGE_SUFFIX);
                        } else {
                            onFailureFound = true;
                        }
                        try{
                            onFailureData = (List<Map<String, Map<String, Object>>>)taskData.values().iterator().next();
                        } catch (ClassCastException ex){
                            throw new RuntimeException("Flow: '" + execName + "' syntax is illegal.\nBelow 'on_failure' property there should be a list of tasks and not a map");
                        }
                        if (CollectionUtils.isNotEmpty(onFailureData)) {
                            onFailureWorkFlow = compileWorkFlow(onFailureData, imports, null, true, namespace);
                        }
                        tasksIterator.remove();
                    }
                }

                Workflow workflow = compileWorkFlow(workFlowRawData, imports, onFailureWorkFlow, false, namespace);
                dependencies = fetchDirectTasksDependencies(workflow);
                return new Flow(preExecutableActionData, postExecutableActionData, workflow, namespace, execName, inputs, outputs, results, dependencies);

            case OPERATION:
                Map<String, Object> actionRawData;
                try{
                   actionRawData = (Map<String, Object>) executableRawData.get(SlangTextualKeys.ACTION_KEY);
                } catch (ClassCastException ex){
                    throw new RuntimeException("Operation: '" + execName + "' syntax is illegal.\nBelow 'action' property there should be a map of values such as: 'python_script:' or 'java_action:'");
                }

                if (MapUtils.isEmpty(actionRawData)) {
                    throw new RuntimeException("Error compiling " + parsedSlang.getName() + ". Operation: " + execName + " has no action data");
                }
                Action action = compileAction(actionRawData);
                dependencies = new HashSet<>();
                return new Operation(preExecutableActionData, postExecutableActionData, action, namespace, execName, inputs, outputs, results, dependencies);
            default:
                throw new RuntimeException("Error compiling " + parsedSlang.getName() + ". It is not of flow or operations type");
        }
    }

    private Action compileAction(Map<String, Object> actionRawData) {
        Map<String, Serializable> actionData = new HashMap<>();

        transformersHandler.validateKeyWords("action data", actionRawData, actionTransformers, null, actionTransformerConstraintGroups);

        actionData.putAll(transformersHandler.runTransformers(actionRawData, actionTransformers));

        return new Action(actionData);
    }

    private Workflow compileWorkFlow(List<Map<String, Map<String, Object>>> workFlowRawData,
                                             Map<String, String> imports,
                                             Workflow onFailureWorkFlow,
                                             boolean onFailureSection,
                                             String namespace) {

        Deque<Task> tasks = new LinkedList<>();

        Validate.notEmpty(workFlowRawData, "Flow must have tasks in its workflow");

        PeekingIterator<Map<String, Map<String, Object>>> iterator =
                new PeekingIterator<>(workFlowRawData.iterator());

        boolean isOnFailureDefined = onFailureWorkFlow != null;

        String defaultFailure = isOnFailureDefined ?
                onFailureWorkFlow.getTasks().getFirst().getName() : ScoreLangConstants.FAILURE_RESULT;

        Set<String> taskNames = new HashSet<>();

        while (iterator.hasNext()) {
            Map<String, Map<String, Object>> taskRawData = iterator.next();
            Map<String, Map<String, Object>> nextTaskData = iterator.peek();
            String taskName = taskRawData.keySet().iterator().next();
            if(taskNames.contains(taskName)){
                throw new RuntimeException("Task name: \'" + taskName + "\' appears more than once in the workflow. Each task name in the workflow must be unique");
            }
            taskNames.add(taskName);
            Map<String, Object> taskRawDataValue;
            String message = "Task: " + taskName + " syntax is illegal.\nBelow task name, there should be a map of values in the format:\ndo:\n\top_name:";
            try {
                taskRawDataValue = taskRawData.values().iterator().next();
                if (MapUtils.isNotEmpty(taskRawDataValue)) {
                    boolean loopKeyFound = taskRawDataValue.containsKey(LOOP_KEY);
                    boolean asyncLoopKeyFound = taskRawDataValue.containsKey(ASYNC_LOOP_KEY);
                    if (loopKeyFound) {
                        if (asyncLoopKeyFound) {
                            throw new RuntimeException("Task: " + taskName + " syntax is illegal.\nBelow task name, there can be either \'loop\' or \'aync_loop\' key.");
                        }

                        message = "Task: " + taskName + " syntax is illegal.\nBelow the 'loop' keyword, there should be a map of values in the format:\nfor:\ndo:\n\top_name:";
                        @SuppressWarnings("unchecked") Map<String, Object> loopRawData = (Map<String, Object>) taskRawDataValue.remove(LOOP_KEY);
                        taskRawDataValue.putAll(loopRawData);
                    }
                    if (asyncLoopKeyFound) {
                        message = "Task: " + taskName + " syntax is illegal.\nBelow the 'async_loop' keyword, there should be a map of values in the format:\nfor:\ndo:\n\top_name:";
                        @SuppressWarnings("unchecked") Map<String, Object> asyncLoopRawData = (Map<String, Object>) taskRawDataValue.remove(ASYNC_LOOP_KEY);
                        asyncLoopRawData.put(ASYNC_LOOP_KEY, asyncLoopRawData.remove(FOR_KEY));
                        taskRawDataValue.putAll(asyncLoopRawData);
                    }
                }
            } catch (ClassCastException ex){
                throw new RuntimeException(message);
            }

            String defaultSuccess;
            if (nextTaskData != null) {
                defaultSuccess = nextTaskData.keySet().iterator().next();
            } else {
                defaultSuccess = onFailureSection ? ScoreLangConstants.FAILURE_RESULT : ScoreLangConstants.SUCCESS_RESULT;
            }
            Task task = compileTask(taskName, taskRawDataValue, defaultSuccess, imports, defaultFailure, namespace);
            tasks.add(task);
        }

        if (isOnFailureDefined) {
            tasks.addAll(onFailureWorkFlow.getTasks());
        }

        return new Workflow(tasks);
    }

    private Task compileTask(String taskName, Map<String, Object> taskRawData, String defaultSuccess,
                                     Map<String, String> imports, String defaultFailure, String namespace) {

        if (MapUtils.isEmpty(taskRawData)) {
            throw new RuntimeException("Task: " + taskName + " has no data");
        }

        Map<String, Serializable> preTaskData = new HashMap<>();
        Map<String, Serializable> postTaskData = new HashMap<>();

        transformersHandler.validateKeyWords(taskName, taskRawData, ListUtils.union(preTaskTransformers, postTaskTransformers), TaskAdditionalKeyWords, null);

        try {
            preTaskData.putAll(transformersHandler.runTransformers(taskRawData, preTaskTransformers));
            postTaskData.putAll(transformersHandler.runTransformers(taskRawData, postTaskTransformers));
        } catch (Exception ex){
            throw new RuntimeException("For task: " + taskName + " syntax is illegal.\n" + ex.getMessage(), ex);
        }
        List<Input> inputs = (List<Input>)preTaskData.get(SlangTextualKeys.DO_KEY);
        @SuppressWarnings("unchecked") Map<String, Object> doRawData = (Map<String, Object>) taskRawData.get(SlangTextualKeys.DO_KEY);
        if (MapUtils.isEmpty(doRawData)) {
            throw new RuntimeException("Task: \'" + taskName + "\' has no reference information");
        }
        String refString = doRawData.keySet().iterator().next();
        String refId = resolveRefId(refString, imports, namespace);

        @SuppressWarnings("unchecked") Map<String, String> navigationStrings = (Map<String, String>) postTaskData.get(SlangTextualKeys.NAVIGATION_KEY);

        //default navigation
        if (MapUtils.isEmpty(navigationStrings)) {
            navigationStrings = new HashMap<>();
            navigationStrings.put(ScoreLangConstants.SUCCESS_RESULT, defaultSuccess);
            navigationStrings.put(ScoreLangConstants.FAILURE_RESULT, defaultFailure);
        }

        return new Task(
                taskName,
                preTaskData,
                postTaskData,
                inputs,
                navigationStrings,
                refId,
                preTaskData.containsKey(ScoreLangConstants.ASYNC_LOOP_KEY));
    }

	private static String resolveRefId(String refIdString, Map<String, String> imports, String namespace) {
        if (refIdString.contains(".")) {
            Validate.notNull(imports, "No imports specified for source: " + refIdString);
            String alias = StringUtils.substringBefore(refIdString, ".");
            if (! imports.containsKey(alias)) throw new RuntimeException("Unresolved alias: " + alias);
            String refName = StringUtils.substringAfter(refIdString, ".");
            return imports.get(alias) + "." + refName;
        } else {
            return namespace + "." + refIdString;
        }
	}

    /**
     * Fetch the first level of the dependencies of the executable (non recursively)
     * @param workflow the workflow of the flow
     * @return a map of dependencies. Key - dependency full name, value - type
     */
    private Set<String> fetchDirectTasksDependencies(Workflow workflow){
        Set<String> dependencies = new HashSet<>();
        Deque<Task> tasks = workflow.getTasks();
        for (Task task : tasks) {
            dependencies.add(task.getRefId());
        }
        return dependencies;
    }

}
