/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.compiler.modeller;

import ch.lambdaj.Lambda;
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;
import static io.cloudslang.lang.compiler.SlangTextualKeys.FOR_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.NAVIGATION_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.ON_FAILURE_KEY;
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
    private List<String> execAdditionalKeywords = Arrays.asList(
            SlangTextualKeys.JAVA_ACTION_KEY,
            SlangTextualKeys.PYTHON_ACTION_KEY,
            SlangTextualKeys.WORKFLOW_KEY,
            SlangTextualKeys.EXECUTABLE_NAME_KEY
    );

    private List<Transformer> actionTransformers;
    private List<List<String>> actionKeyConstraintGroups;

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
        //action keys (Java / Python) excluding each other
        actionKeyConstraintGroups = Collections.singletonList(
                Arrays.asList(
                        SlangTextualKeys.PYTHON_ACTION_KEY,
                        SlangTextualKeys.JAVA_ACTION_KEY)
        );

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

        errors.addAll(transformersHandler.checkKeyWords(
                execName,
                executableRawData,
                ListUtils.union(preExecTransformers, postExecTransformers),
                execAdditionalKeywords,
                actionKeyConstraintGroups)
        );

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

    private Map<String, Object> getActionRawData(
            Map<String, Object> executableRawData,
            List<RuntimeException> errors,
            ParsedSlang parsedSlang, String execName) {
        Map<String, Object> actionRawData = new HashMap<>();
        Object javaActionRawData = executableRawData.get(SlangTextualKeys.JAVA_ACTION_KEY);
        Object pythonActionRawData = executableRawData.get(SlangTextualKeys.PYTHON_ACTION_KEY);
        if (javaActionRawData != null) {
            actionRawData.put(SlangTextualKeys.JAVA_ACTION_KEY, executableRawData.get(SlangTextualKeys.JAVA_ACTION_KEY));
        }
        if (pythonActionRawData != null) {
            actionRawData.put(SlangTextualKeys.PYTHON_ACTION_KEY, executableRawData.get(SlangTextualKeys.PYTHON_ACTION_KEY));
        }
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
            Map<String, Map<String, Object>> stepData = stepsIterator.next();
            String stepName = stepData.keySet().iterator().next();
            if(stepName.equals(SlangTextualKeys.ON_FAILURE_KEY)){
                if (onFailureFound) {
                    errors.add(new RuntimeException("Flow: '" + execName + "' syntax is illegal.\n" + MULTIPLE_ON_FAILURE_MESSAGE_SUFFIX));
                } else {
                    onFailureFound = true;
                }
                try{
                    //noinspection unchecked
                    onFailureData = (List<Map<String, Map<String, Object>>>)stepData.values().iterator().next();
                } catch (ClassCastException ex){
                    onFailureData = new ArrayList<>();
                    errors.add(new RuntimeException("Flow: '" + execName + "' syntax is illegal.\nBelow 'on_failure' property there should be a list of steps and not a map"));
                }
                if (CollectionUtils.isNotEmpty(onFailureData)) {
                    if (onFailureData.size() > 1) {
                        errors.add(new RuntimeException("Flow: '" + execName + "' syntax is illegal.\nBelow 'on_failure' property there should be only one step"));
                    }
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

        List<RuntimeException> errors = transformersHandler.checkKeyWords("action data", actionRawData, actionTransformers, null, null);

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
        Set<String> stepNames = new HashSet<>();
        Deque<Step> onFailureSteps = !(onFailureSection || onFailureWorkFlow == null) ? onFailureWorkFlow.getSteps() : new LinkedList<Step>();
        List<String> onFailureStepNames = getStepNames(onFailureSteps);
        boolean onFailureStepFound =  onFailureStepNames.size() > 0;
        String defaultFailure = onFailureStepFound ? onFailureStepNames.get(0) : ScoreLangConstants.FAILURE_RESULT;

        PeekingIterator<Map<String, Map<String, Object>>> iterator = new PeekingIterator<>(workFlowRawData.iterator());
        while (iterator.hasNext()) {
            Map<String, Map<String, Object>> stepRawData = iterator.next();
            Map<String, Map<String, Object>> nextStepData = iterator.peek();
            String stepName = stepRawData.keySet().iterator().next();
            if (stepNames.contains(stepName) || onFailureStepNames.contains(stepName)) {
                errors.add(new RuntimeException("Step name: \'" + stepName + "\' appears more than once in the workflow. " + UNIQUE_STEP_NAME_MESSAGE_SUFFIX));
            }
            stepNames.add(stepName);
            Map<String, Object> stepRawDataValue;
            String message = "Step: " + stepName + " syntax is illegal.\nBelow step name, there should be a map of values in the format:\ndo:\n\top_name:";
            try {
                stepRawDataValue = stepRawData.values().iterator().next();
                if (MapUtils.isNotEmpty(stepRawDataValue)) {
                    boolean loopKeyFound = stepRawDataValue.containsKey(LOOP_KEY);
                    boolean asyncLoopKeyFound = stepRawDataValue.containsKey(ASYNC_LOOP_KEY);
                    if (loopKeyFound) {
                        if (asyncLoopKeyFound) {
                            errors.add(new RuntimeException("Step: " + stepName + " syntax is illegal.\nBelow step name, there can be either \'loop\' or \'aync_loop\' key."));
                        }
                        message = "Step: " + stepName + " syntax is illegal.\nBelow the 'loop' keyword, there should be a map of values in the format:\nfor:\ndo:\n\top_name:";
                        @SuppressWarnings("unchecked") Map<String, Object> loopRawData = (Map<String, Object>) stepRawDataValue.remove(LOOP_KEY);
                        stepRawDataValue.putAll(loopRawData);
                    }
                    if (asyncLoopKeyFound) {
                        message = "Step: " + stepName + " syntax is illegal.\nBelow the 'async_loop' keyword, there should be a map of values in the format:\nfor:\ndo:\n\top_name:";
                        @SuppressWarnings("unchecked") Map<String, Object> asyncLoopRawData = (Map<String, Object>) stepRawDataValue.remove(ASYNC_LOOP_KEY);
                        asyncLoopRawData.put(ASYNC_LOOP_KEY, asyncLoopRawData.remove(FOR_KEY));
                        stepRawDataValue.putAll(asyncLoopRawData);
                    }
                }
            } catch (ClassCastException ex) {
                stepRawDataValue = new HashMap<>();
                errors.add(new RuntimeException(message));
            }

            String defaultSuccess;
            if (nextStepData != null) {
                defaultSuccess = nextStepData.keySet().iterator().next();
            } else {
                defaultSuccess = onFailureSection ? ScoreLangConstants.FAILURE_RESULT : ScoreLangConstants.SUCCESS_RESULT;
            }

            String onFailureStepName = onFailureStepFound ? onFailureStepNames.get(0) : null;
            StepModellingResult stepModellingResult = compileStep(
                    stepName,
                    stepRawDataValue,
                    defaultSuccess,
                    imports,
                    defaultFailure,
                    namespace,
                    onFailureStepName
            );

            errors.addAll(stepModellingResult.getErrors());
            steps.add(stepModellingResult.getStep());
        }

        if (onFailureStepFound) {
            steps.addAll(onFailureSteps);
        }

        List<Step> unreachableSteps = getUnreachableSteps(steps, onFailureSteps);
        for (Step step : unreachableSteps) {
            errors.add(new RuntimeException("Step: " + step.getName() + " is unreachable"));
        }

        return new WorkflowModellingResult(new Workflow(steps), errors);
    }

    private StepModellingResult compileStep(
            String stepName, Map<String,
            Object> stepRawData,
            String defaultSuccess,
            Map<String, String> imports,
            String defaultFailure,
            String namespace,
            String onFailureStepName) {

        List<RuntimeException> errors = new ArrayList<>();
        if (MapUtils.isEmpty(stepRawData)) {
            stepRawData = new HashMap<>();
            errors.add(new RuntimeException("Step: " + stepName + " has no data"));
        }

        Map<String, Serializable> preStepData = new HashMap<>();
        Map<String, Serializable> postStepData = new HashMap<>();

        errors.addAll(transformersHandler.checkKeyWords(stepName, stepRawData, ListUtils.union(preStepTransformers, postStepTransformers), stepAdditionalKeyWords, null));

        String errorMessagePrefix = "For step '" + stepName + "' syntax is illegal.\n";
        preStepData.putAll(transformersHandler.runTransformers(stepRawData, preStepTransformers, errors, errorMessagePrefix));
        postStepData.putAll(transformersHandler.runTransformers(stepRawData, postStepTransformers, errors, errorMessagePrefix));

        replaceOnFailureReference(postStepData, onFailureStepName, stepName);

        @SuppressWarnings("unchecked")
        List<Argument> arguments = (List<Argument>)preStepData.get(SlangTextualKeys.DO_KEY);

        String refId = "";
        Map<String, Object> doRawData;
        try {
            //noinspection unchecked
            doRawData = (Map<String, Object>) stepRawData.get(SlangTextualKeys.DO_KEY);
        } catch (ClassCastException ex) {
            doRawData = new HashMap<>();
        }
        if (MapUtils.isEmpty(doRawData)) {
            errors.add(new RuntimeException("Step: \'" + stepName + "\' has no reference information"));
        } else {
            String refString = doRawData.keySet().iterator().next();
            refId = resolveReferenceID(refString, imports, namespace);
        }

        List<Map<String, String>> navigationStrings = getNavigationStrings(postStepData, defaultSuccess, defaultFailure);

        Step step = new Step(
                stepName,
                preStepData,
                postStepData,
                arguments,
                navigationStrings,
                refId,
                preStepData.containsKey(ScoreLangConstants.ASYNC_LOOP_KEY));
        return new StepModellingResult(step, errors);
    }

    private void replaceOnFailureReference(
            Map<String, Serializable> postStepData,
            String onFailureStepName,
            String stepName) {
        Serializable navigationData = postStepData.get(NAVIGATION_KEY);
        if (navigationData != null) {
            @SuppressWarnings("unchecked") // from NavigateTransformer
                    List<Map<String, String>> navigationStrings = (List<Map<String, String>>) navigationData;
            List<Map<String, String>> transformedNavigationStrings = new ArrayList<>();

            for (Map<String, String> navigation : navigationStrings) {
                Map.Entry<String, String> navigationEntry = navigation.entrySet().iterator().next();
                Map<String, String> transformedNavigation = new HashMap<>(navigation);
                if (navigationEntry.getValue().equals(ON_FAILURE_KEY)) {
                    if (StringUtils.isEmpty(onFailureStepName)) {
                        throw new RuntimeException(
                                "Failed to compile step: '" + stepName +
                                        "'. Navigation: '" + navigationEntry.getKey() + " -> " + navigationEntry.getValue()
                                        + "' is illegal. 'on_failure' section is not defined."
                        );
                    } else {
                        transformedNavigation.put(navigationEntry.getKey(), onFailureStepName);
                    }
                } else {
                    transformedNavigation.put(navigationEntry.getKey(), navigationEntry.getValue());
                }
                transformedNavigationStrings.add(transformedNavigation);
            }
            postStepData.put(NAVIGATION_KEY, (Serializable) transformedNavigationStrings);
        }
    }

    private List<Map<String, String>> getNavigationStrings(Map<String, Serializable> postStepData, String defaultSuccess, String defaultFailure) {
        @SuppressWarnings("unchecked") List<Map<String, String>> navigationStrings =
                (List<Map<String, String>>) postStepData.get(SlangTextualKeys.NAVIGATION_KEY);

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

    private List<String> getStepNames(Deque<Step> steps) {
        List<String> stepNames =  new ArrayList<>();
        for (Step step : steps) {
            stepNames.add(step.getName());
        }
        return stepNames;
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

    private List<Step> getUnreachableSteps(Deque<Step> steps, Deque<Step> onFailureSteps) {
        List<Step> unreachableSteps = new ArrayList<>();
        if (steps.size() > 0) {
            Map<String, Step> reachableSteps = new LinkedHashMap<>();
            getReachableSteps(steps.getFirst(), steps, reachableSteps);
            Step onFailureStep = onFailureSteps.size() == 0 ? null : onFailureSteps.getFirst();
            for (Step step : steps) {
                boolean isOnFailureStep = onFailureStep != null && onFailureStep.getName().equals(step.getName());
                if (reachableSteps.get(step.getName()) == null && !isOnFailureStep) {
                    unreachableSteps.add(step);
                }
            }
        }
        return unreachableSteps;
    }

    private void getReachableSteps(Step step, Deque<Step> steps, Map<String, Step> reachableSteps) {
        reachableSteps.put(step.getName(), step);
        for (Map<String, String> map : step.getNavigationStrings()) {
            Map.Entry<String, String> entry = map.entrySet().iterator().next();
            String nextStepName = entry.getValue();
            if (reachableSteps.get(nextStepName) == null) {
                Step nextStepToCompile = Lambda.selectFirst(steps, having(on(Step.class).getName(), equalTo(nextStepName)));
                if (nextStepToCompile != null) {
                    getReachableSteps(nextStepToCompile, steps, reachableSteps);
                }
            }
        }
    }
}
