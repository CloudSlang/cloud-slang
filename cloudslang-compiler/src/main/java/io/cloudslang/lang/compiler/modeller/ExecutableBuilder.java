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
import io.cloudslang.lang.compiler.modeller.model.Action;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Operation;
import io.cloudslang.lang.compiler.modeller.model.Step;
import io.cloudslang.lang.compiler.modeller.model.Workflow;
import io.cloudslang.lang.compiler.modeller.result.ActionModellingResult;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import io.cloudslang.lang.compiler.modeller.result.StepModellingResult;
import io.cloudslang.lang.compiler.modeller.result.WorkflowModellingResult;
import io.cloudslang.lang.compiler.modeller.transformers.Transformer;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.iterators.PeekingIterator;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static io.cloudslang.lang.compiler.SlangTextualKeys.FOR_KEY;
import static io.cloudslang.lang.entities.ScoreLangConstants.ASYNC_LOOP_KEY;
import static io.cloudslang.lang.entities.ScoreLangConstants.LOOP_KEY;
import static io.cloudslang.lang.entities.ScoreLangConstants.NAMESPACE_DELIMITER;

/*
 * Created by orius123 on 09/11/14.
 */
@Component
public class ExecutableBuilder {

    public static final String MULTIPLE_ON_FAILURE_MESSAGE_SUFFIX = "Multiple 'on_failure' properties found";
    public static final String UNIQUE_STEP_NAME_MESSAGE_SUFFIX = "Each step name in the workflow must be unique";
    public static final String FLOW_RESULTS_WITH_EXPRESSIONS_MESSAGE =
            "Explicit values are not allowed for flow results. Correct format is:";

    @Autowired
    private List<Transformer> transformers;

    @Autowired
    private TransformersHandler transformersHandler;

    @Autowired
    private DependenciesHelper dependenciesHelper;

    private List<Transformer> preExecTransformers;
    private List<Transformer> postExecTransformers;
    private List<String> execAdditionalKeywords = Arrays.asList(SlangTextualKeys.ACTION_KEY, SlangTextualKeys.WORKFLOW_KEY, SlangTextualKeys.EXECUTABLE_NAME_KEY);

    private List<Transformer> actionTransformers;
    private List<List<String>> actionTransformerConstraintGroups;

    private List<Transformer> preStepTransformers;
    private List<Transformer> postStepTransformers;
    private List<String> stepAdditionalKeyWords = Arrays.asList(ScoreLangConstants.LOOP_KEY, SlangTextualKeys.DO_KEY, SlangTextualKeys.NAVIGATION_KEY);

    @PostConstruct
    public void initScopedTransformersAndKeys() {
        //executable transformers
        preExecTransformers = filterTransformers(Transformer.Scope.BEFORE_EXECUTABLE);
        postExecTransformers = filterTransformers(Transformer.Scope.AFTER_EXECUTABLE);

        //action transformers and keys
        actionTransformers = filterTransformers(Transformer.Scope.ACTION);
        //action keys excluding each other
        actionTransformerConstraintGroups = Collections.singletonList(Arrays.asList(ScoreLangConstants.PYTHON_SCRIPT_KEY, SlangTextualKeys.JAVA_ACTION));

        //step transformers
        preStepTransformers = filterTransformers(Transformer.Scope.BEFORE_STEP);
        postStepTransformers = filterTransformers(Transformer.Scope.AFTER_STEP);
    }

    private List<Transformer> filterTransformers(Transformer.Scope scope) {
        return filter(having(on(Transformer.class).getScopes().contains(scope)), transformers);
    }

    public ExecutableModellingResult transformToExecutable(ParsedSlang parsedSlang, String execName, Map<String, Object> executableRawData) {

        execName = execName == null ? "" : execName;
        List<RuntimeException> errors = new ArrayList<>();
        validate(executableRawData, errors, parsedSlang, execName);

        Map<String, Serializable> preExecutableActionData = new HashMap<>();
        Map<String, Serializable> postExecutableActionData = new HashMap<>();

        errors.addAll(transformersHandler.checkKeyWords(execName, executableRawData,
                ListUtils.union(preExecTransformers, postExecTransformers), execAdditionalKeywords, null));

        String errorMessagePrefix = "For " + parsedSlang.getType().toString().toLowerCase() + " '" + execName + "' syntax is illegal.\n";
        preExecutableActionData.putAll(transformersHandler.runTransformers(executableRawData, preExecTransformers, errors, errorMessagePrefix));
        postExecutableActionData.putAll(transformersHandler.runTransformers(executableRawData, postExecTransformers, errors, errorMessagePrefix));

        @SuppressWarnings("unchecked") List<Input> inputs = (List<Input>) preExecutableActionData.remove(SlangTextualKeys.INPUTS_KEY);
        @SuppressWarnings("unchecked") List<Output> outputs = (List<Output>) postExecutableActionData.remove(SlangTextualKeys.OUTPUTS_KEY);
        @SuppressWarnings("unchecked") List<Result> results = (List<Result>) postExecutableActionData.remove(SlangTextualKeys.RESULTS_KEY);

        String namespace = parsedSlang.getNamespace();
        Map<String, String> imports = parsedSlang.getImports();
        Set<String> executableDependencies;
        Set<String> systemPropertyDependencies;
        switch (parsedSlang.getType()) {
            case FLOW:
                List<Map<String, Map<String, Object>>> workFlowRawData = getWorkflowRawData(executableRawData, errors, parsedSlang, execName);

                Workflow onFailureWorkFlow = getOnFailureWorkflow(workFlowRawData, imports, errors, namespace, execName);

                WorkflowModellingResult workflowModellingResult = compileWorkFlow(workFlowRawData, imports, onFailureWorkFlow, false, namespace, execName);
                errors.addAll(workflowModellingResult.getErrors());
                Workflow workflow = workflowModellingResult.getWorkflow();

                errors.addAll(validateFlowResultsHaveNoExpression(results, execName));

                executableDependencies = fetchDirectStepsDependencies(workflow);
                systemPropertyDependencies = dependenciesHelper.getSystemPropertiesForFlow(inputs, outputs, results, workflow.getSteps());
                Flow flow = new Flow(
                        preExecutableActionData,
                        postExecutableActionData,
                        workflow,
                        namespace,
                        execName,
                        inputs,
                        outputs,
                        results,
                        executableDependencies,
                        systemPropertyDependencies
                );
                return new ExecutableModellingResult(flow, errors);

            case OPERATION:
                Map<String, Object> actionRawData = getActionRawData(executableRawData, errors, parsedSlang, execName);
                ActionModellingResult actionModellingResult = compileAction(actionRawData);
                errors.addAll(actionModellingResult.getErrors());
                Action action = actionModellingResult.getAction();
                executableDependencies = new HashSet<>();
                systemPropertyDependencies = dependenciesHelper.getSystemPropertiesForOperation(inputs, outputs, results);
                Operation operation = new Operation(
                        preExecutableActionData,
                        postExecutableActionData,
                        action,
                        namespace,
                        execName,
                        inputs,
                        outputs,
                        results,
                        executableDependencies,
                        systemPropertyDependencies
                );
                return new ExecutableModellingResult(operation, errors);
            default:
                throw new RuntimeException("Error compiling " + parsedSlang.getName() + ". It is not of flow or operations type");
        }
    }

    private void validate(Map<String, Object> executableRawData, List<RuntimeException> errors, ParsedSlang parsedSlang, String execName) {
        if (executableRawData == null) {
            throw new IllegalArgumentException("Error compiling " + parsedSlang.getName() + ". Executable data is null");
        }
        if(parsedSlang == null) {
            throw new IllegalArgumentException("Slang source for: \'" + execName + "\' is null");
        }

        if (StringUtils.isBlank(execName)) {
            errors.add(new RuntimeException("Executable in source: " + parsedSlang.getName() + " has no name"));
        }
        if(executableRawData.size() == 0) {
            errors.add(new IllegalArgumentException("Error compiling " + parsedSlang.getName() + ". Executable data for: \'" + execName + "\' is empty"));
        }
    }

    private Map<String, Object> getActionRawData(Map<String, Object> executableRawData, List<RuntimeException> errors,
                                                 ParsedSlang parsedSlang, String execName) {
        Map<String, Object> actionRawData = null;
        try{
            //noinspection unchecked
            actionRawData = (Map<String, Object>) executableRawData.get(SlangTextualKeys.ACTION_KEY);
        } catch (ClassCastException ex){
            errors.add(new RuntimeException("Operation: '" + execName + "' syntax is illegal.\nBelow 'action' property there should be a map of values such as: 'python_script:' or 'java_action:'"));
        }
        actionRawData = actionRawData == null ? new HashMap<String, Object>() : actionRawData;
        if (MapUtils.isEmpty(actionRawData)) {
            errors.add(new RuntimeException("Error compiling " + parsedSlang.getName() + ". Operation: " + execName + " has no action data"));
        }
        return actionRawData;
    }

    private List<Map<String, Map<String, Object>>> getWorkflowRawData(Map<String, Object> executableRawData, List<RuntimeException> errors,
                                                                      ParsedSlang parsedSlang, String execName) {
        Object rawData = executableRawData.get(SlangTextualKeys.WORKFLOW_KEY);
        if(rawData == null){
            rawData = new ArrayList<>();
            errors.add(new RuntimeException("Error compiling " + parsedSlang.getName() + ". Flow: " + execName + " has no workflow property"));
        }
        List<Map<String, Map<String, Object>>> workFlowRawData;
        try{
            //noinspection unchecked
            workFlowRawData = (List<Map<String, Map<String, Object>>>) rawData;
        } catch (ClassCastException ex){
            workFlowRawData = new ArrayList<>();
            errors.add(new RuntimeException("Flow: '" + execName + "' syntax is illegal.\nBelow 'workflow' property there should be a list of steps and not a map"));
        }
        if (CollectionUtils.isEmpty(workFlowRawData)) {
            errors.add(new RuntimeException("Error compiling source '" + parsedSlang.getName() + "'. Flow: '" + execName + "' has no workflow data"));
        }
        return workFlowRawData;
    }

    private Workflow getOnFailureWorkflow(List<Map<String, Map<String, Object>>> workFlowRawData, Map<String, String> imports, List<RuntimeException> errors,
                                          String namespace, String execName) {
        Workflow onFailureWorkFlow = null;
        List<Map<String, Map<String, Object>>> onFailureData;
        Iterator<Map<String, Map<String, Object>>> stepsIterator = workFlowRawData.iterator();
        boolean onFailureFound = false;
        while(stepsIterator.hasNext()){
            Map<String, Map<String, Object>> taskData = stepsIterator.next();
            String taskName = taskData.keySet().iterator().next();
            if(taskName.equals(SlangTextualKeys.ON_FAILURE_KEY)){
                if (onFailureFound) {
                    errors.add(new RuntimeException("Flow: '" + execName + "' syntax is illegal.\n" + MULTIPLE_ON_FAILURE_MESSAGE_SUFFIX));
                } else {
                    onFailureFound = true;
                }
                try{
                    //noinspection unchecked
                    onFailureData = (List<Map<String, Map<String, Object>>>)taskData.values().iterator().next();
                } catch (ClassCastException ex){
                    onFailureData = new ArrayList<>();
                    errors.add(new RuntimeException("Flow: '" + execName + "' syntax is illegal.\nBelow 'on_failure' property there should be a list of steps and not a map"));
                }
                if (CollectionUtils.isNotEmpty(onFailureData)) {
                    WorkflowModellingResult workflowModellingResult = compileWorkFlow(onFailureData, imports, null, true, namespace, execName);
                    errors.addAll(workflowModellingResult.getErrors());
                    onFailureWorkFlow = workflowModellingResult.getWorkflow();
                }
                stepsIterator.remove();
            }
        }
        return onFailureWorkFlow;
    }

    private ActionModellingResult compileAction(Map<String, Object> actionRawData) {
        Map<String, Serializable> actionData = new HashMap<>();

        List<RuntimeException> errors = transformersHandler.checkKeyWords("action data", actionRawData, actionTransformers, null, actionTransformerConstraintGroups);

        String errorMessagePrefix = "Action syntax is illegal.\n";
        actionData.putAll(transformersHandler.runTransformers(actionRawData, actionTransformers, errors, errorMessagePrefix));

        Action action = new Action(actionData);
        return new ActionModellingResult(action, errors);
    }

    private WorkflowModellingResult compileWorkFlow(List<Map<String, Map<String, Object>>> workFlowRawData,
                                                    Map<String, String> imports,
                                                    Workflow onFailureWorkFlow,
                                                    boolean onFailureSection,
                                                    String namespace,
                                                    String execName) {

        List<RuntimeException> errors = new ArrayList<>();
        if (workFlowRawData.isEmpty()) {
            errors.add(new IllegalArgumentException("For flow '" + execName + "' syntax is illegal. Flow must have steps in its workflow."));
        }

        Deque<Step> steps = new LinkedList<>();
        Set<String> taskNames = new HashSet<>();
        Deque<Step> onFailureSteps = !(onFailureSection || onFailureWorkFlow == null) ? onFailureWorkFlow.getSteps() : new LinkedList<Step>();
        List<String> onFailureTaskNames = getTaskNames(onFailureSteps);
        boolean onFailureTasksFound =  onFailureTaskNames.size() > 0;
        String defaultFailure = onFailureTasksFound ? onFailureTaskNames.get(0) : ScoreLangConstants.FAILURE_RESULT;

        PeekingIterator<Map<String, Map<String, Object>>> iterator = new PeekingIterator<>(workFlowRawData.iterator());
        while (iterator.hasNext()) {
            Map<String, Map<String, Object>> taskRawData = iterator.next();
            Map<String, Map<String, Object>> nextTaskData = iterator.peek();
            String taskName = taskRawData.keySet().iterator().next();
            if (taskNames.contains(taskName) || onFailureTaskNames.contains(taskName)) {
                errors.add(new RuntimeException("Step name: \'" + taskName + "\' appears more than once in the workflow. " + UNIQUE_STEP_NAME_MESSAGE_SUFFIX));
            }
            taskNames.add(taskName);
            Map<String, Object> taskRawDataValue;
            String message = "Step: " + taskName + " syntax is illegal.\nBelow step name, there should be a map of values in the format:\ndo:\n\top_name:";
            try {
                taskRawDataValue = taskRawData.values().iterator().next();
                if (MapUtils.isNotEmpty(taskRawDataValue)) {
                    boolean loopKeyFound = taskRawDataValue.containsKey(LOOP_KEY);
                    boolean asyncLoopKeyFound = taskRawDataValue.containsKey(ASYNC_LOOP_KEY);
                    if (loopKeyFound) {
                        if (asyncLoopKeyFound) {
                            errors.add(new RuntimeException("Step: " + taskName + " syntax is illegal.\nBelow step name, there can be either \'loop\' or \'aync_loop\' key."));
                        }
                        message = "Step: " + taskName + " syntax is illegal.\nBelow the 'loop' keyword, there should be a map of values in the format:\nfor:\ndo:\n\top_name:";
                        @SuppressWarnings("unchecked") Map<String, Object> loopRawData = (Map<String, Object>) taskRawDataValue.remove(LOOP_KEY);
                        taskRawDataValue.putAll(loopRawData);
                    }
                    if (asyncLoopKeyFound) {
                        message = "Step: " + taskName + " syntax is illegal.\nBelow the 'async_loop' keyword, there should be a map of values in the format:\nfor:\ndo:\n\top_name:";
                        @SuppressWarnings("unchecked") Map<String, Object> asyncLoopRawData = (Map<String, Object>) taskRawDataValue.remove(ASYNC_LOOP_KEY);
                        asyncLoopRawData.put(ASYNC_LOOP_KEY, asyncLoopRawData.remove(FOR_KEY));
                        taskRawDataValue.putAll(asyncLoopRawData);
                    }
                }
            } catch (ClassCastException ex) {
                taskRawDataValue = new HashMap<>();
                errors.add(new RuntimeException(message));
            }

            String defaultSuccess;
            if (nextTaskData != null) {
                defaultSuccess = nextTaskData.keySet().iterator().next();
            } else {
                defaultSuccess = onFailureSection ? ScoreLangConstants.FAILURE_RESULT : ScoreLangConstants.SUCCESS_RESULT;
            }
            StepModellingResult stepModellingResult = compileTask(taskName, taskRawDataValue, defaultSuccess, imports, defaultFailure, namespace);
            errors.addAll(stepModellingResult.getErrors());
            steps.add(stepModellingResult.getStep());
        }

        if (onFailureTasksFound) {
            steps.addAll(onFailureSteps);
        }

        return new WorkflowModellingResult(new Workflow(steps), errors);
    }

    private StepModellingResult compileTask(String taskName, Map<String, Object> taskRawData, String defaultSuccess,
                                            Map<String, String> imports, String defaultFailure, String namespace) {

        List<RuntimeException> errors = new ArrayList<>();
        if (MapUtils.isEmpty(taskRawData)) {
            taskRawData = new HashMap<>();
            errors.add(new RuntimeException("Step: " + taskName + " has no data"));
        }

        Map<String, Serializable> preTaskData = new HashMap<>();
        Map<String, Serializable> postTaskData = new HashMap<>();

        errors.addAll(transformersHandler.checkKeyWords(taskName, taskRawData, ListUtils.union(preStepTransformers, postStepTransformers), stepAdditionalKeyWords, null));

        String errorMessagePrefix = "For step '" + taskName + "' syntax is illegal.\n";
        preTaskData.putAll(transformersHandler.runTransformers(taskRawData, preStepTransformers, errors, errorMessagePrefix));
        postTaskData.putAll(transformersHandler.runTransformers(taskRawData, postStepTransformers, errors, errorMessagePrefix));

        @SuppressWarnings("unchecked")
        List<Argument> arguments = (List<Argument>)preTaskData.get(SlangTextualKeys.DO_KEY);

        String refId = "";
        Map<String, Object> doRawData;
        try {
            //noinspection unchecked
            doRawData = (Map<String, Object>) taskRawData.get(SlangTextualKeys.DO_KEY);
        } catch (ClassCastException ex) {
            doRawData = new HashMap<>();
        }
        if (MapUtils.isEmpty(doRawData)) {
            errors.add(new RuntimeException("Step: \'" + taskName + "\' has no reference information"));
        } else {
            String refString = doRawData.keySet().iterator().next();
            refId = resolveReferenceID(refString, imports, namespace);
        }

        List<Map<String, String>> navigationStrings = getNavigationStrings(postTaskData, defaultSuccess, defaultFailure);

        Step step = new Step(
                taskName,
                preTaskData,
                postTaskData,
                arguments,
                navigationStrings,
                refId,
                preTaskData.containsKey(ScoreLangConstants.ASYNC_LOOP_KEY));
        return new StepModellingResult(step, errors);
    }

    private List<Map<String, String>> getNavigationStrings(Map<String, Serializable> postTaskData, String defaultSuccess, String defaultFailure) {
        @SuppressWarnings("unchecked") List<Map<String, String>> navigationStrings =
                (List<Map<String, String>>) postTaskData.get(SlangTextualKeys.NAVIGATION_KEY);

        //default navigation
        if (CollectionUtils.isEmpty(navigationStrings)) {
            navigationStrings = new ArrayList<>();
            Map<String, String> successMap = new HashMap<>();
            successMap.put(ScoreLangConstants.SUCCESS_RESULT, defaultSuccess);
            Map<String, String> failureMap = new HashMap<>();
            failureMap.put(ScoreLangConstants.FAILURE_RESULT, defaultFailure);
            navigationStrings.add(successMap);
            navigationStrings.add(failureMap);
        }
        return navigationStrings;
    }

    private static String resolveReferenceID(String rawReferenceID, Map<String, String> imports, String namespace) {
        int numberOfDelimiters = StringUtils.countMatches(rawReferenceID, NAMESPACE_DELIMITER);
        String resolvedReferenceID;

        if (numberOfDelimiters == 0) {
            // implicit namespace
            resolvedReferenceID = namespace + NAMESPACE_DELIMITER + rawReferenceID;
        } else {
            String prefix = StringUtils.substringBefore(rawReferenceID, NAMESPACE_DELIMITER);
            String suffix = StringUtils.substringAfter(rawReferenceID, NAMESPACE_DELIMITER);
            if (MapUtils.isNotEmpty(imports) && imports.containsKey(prefix)) {
                // expand alias
                resolvedReferenceID = imports.get(prefix) + NAMESPACE_DELIMITER + suffix;
            } else {
                // full path without alias expanding
                resolvedReferenceID = rawReferenceID;
            }
        }

        return resolvedReferenceID;
    }

    /**
     * Fetch the first level of the dependencies of the executable (non recursively)
     * @param workflow the workflow of the flow
     * @return a map of dependencies. Key - dependency full name, value - type
     */
    private Set<String> fetchDirectStepsDependencies(Workflow workflow){
        Set<String> dependencies = new HashSet<>();
        Deque<Step> steps = workflow.getSteps();
        for (Step step : steps) {
            dependencies.add(step.getRefId());
        }
        return dependencies;
    }

    private List<String> getTaskNames(Deque<Step> steps) {
        List<String> taskNames =  new ArrayList<>();
        for (Step step : steps) {
            taskNames.add(step.getName());
        }
        return taskNames;
    }

    private List<RuntimeException> validateFlowResultsHaveNoExpression(List<Result> results, String flowName) {
        List<RuntimeException> errors = new ArrayList<>();
        for (Result result : results) {
            if (result.getValue() != null) {
                errors.add(
                    new RuntimeException(
                            "Flow: '" + flowName + "' syntax is illegal. Error compiling result: '" +
                                    result.getName() + "'. " + FLOW_RESULTS_WITH_EXPRESSIONS_MESSAGE +
                                    " '- " + result.getName() + "'."
                    )
                );
            }
        }
        return errors;
    }

}
