/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller;

import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.model.Action;
import io.cloudslang.lang.compiler.modeller.model.Decision;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Operation;
import io.cloudslang.lang.compiler.modeller.model.Step;
import io.cloudslang.lang.compiler.modeller.model.Workflow;
import io.cloudslang.lang.compiler.modeller.result.ActionModellingResult;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import io.cloudslang.lang.compiler.modeller.result.StepModellingResult;
import io.cloudslang.lang.compiler.modeller.result.WorkflowModellingResult;
import io.cloudslang.lang.compiler.modeller.transformers.ResultsTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.Transformer;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.compiler.validator.PreCompileValidator;
import io.cloudslang.lang.entities.ExecutableType;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.iterators.PeekingIterator;
import org.apache.commons.lang3.StringUtils;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static io.cloudslang.lang.compiler.SlangTextualKeys.FOR_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.NAVIGATION_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.ON_FAILURE_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.PARALLEL_LOOP_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.WORKFLOW_KEY;
import static io.cloudslang.lang.entities.ScoreLangConstants.LOOP_KEY;
import static io.cloudslang.lang.entities.ScoreLangConstants.NAMESPACE_DELIMITER;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;


public class ExecutableBuilder {

    public static final String UNIQUE_STEP_NAME_MESSAGE_SUFFIX = "Each step name in the workflow must be unique";

    private List<Transformer> transformers;

    private TransformersHandler transformersHandler;

    private DependenciesHelper dependenciesHelper;

    private PreCompileValidator preCompileValidator;

    private ResultsTransformer resultsTransformer;

    private ExecutableValidator executableValidator;

    private List<Transformer> preExecTransformers;
    private List<Transformer> postExecTransformers;

    private List<String> executableAdditionalKeywords = singletonList(SlangTextualKeys.EXECUTABLE_NAME_KEY);
    private List<String> operationAdditionalKeywords =
            asList(SlangTextualKeys.JAVA_ACTION_KEY, SlangTextualKeys.PYTHON_ACTION_KEY);
    private List<String> flowAdditionalKeywords = singletonList(SlangTextualKeys.WORKFLOW_KEY);
    private List<String> allExecutableAdditionalKeywords;

    private List<Transformer> actionTransformers;
    private List<List<String>> executableConstraintGroups;

    private List<Transformer> preStepTransformers;
    private List<Transformer> postStepTransformers;
    private List<String> stepAdditionalKeyWords =
            asList(ScoreLangConstants.LOOP_KEY, SlangTextualKeys.DO_KEY, SlangTextualKeys.NAVIGATION_KEY);
    private List<String> parallelLoopValidKeywords = asList(SlangTextualKeys.DO_KEY, SlangTextualKeys.FOR_KEY);

    // @PostConstruct
    public void initScopedTransformersAndKeys() {
        //executable transformers
        preExecTransformers = filterTransformers(Transformer.Scope.BEFORE_EXECUTABLE);
        postExecTransformers = filterTransformers(Transformer.Scope.AFTER_EXECUTABLE);

        //action transformers and keys
        actionTransformers = filterTransformers(Transformer.Scope.ACTION);

        allExecutableAdditionalKeywords = new ArrayList<>(
                executableAdditionalKeywords.size() + operationAdditionalKeywords.size() +
                        flowAdditionalKeywords.size()
        );
        allExecutableAdditionalKeywords.addAll(executableAdditionalKeywords);
        allExecutableAdditionalKeywords.addAll(operationAdditionalKeywords);
        allExecutableAdditionalKeywords.addAll(flowAdditionalKeywords);

        // keys excluding each other
        executableConstraintGroups = new ArrayList<>();
        executableConstraintGroups.add(ListUtils.union(flowAdditionalKeywords, operationAdditionalKeywords));

        //step transformers
        preStepTransformers = filterTransformers(Transformer.Scope.BEFORE_STEP);
        postStepTransformers = filterTransformers(Transformer.Scope.AFTER_STEP);
    }

    private List<Transformer> filterTransformers(Transformer.Scope scope) {
        return filter(having(on(Transformer.class).getScopes().contains(scope)), transformers);
    }

    public ExecutableModellingResult transformToExecutable(ParsedSlang parsedSlang,
                                                           Map<String, Object> executableRawData) {
        List<RuntimeException> errors = new ArrayList<>();
        String execName = preCompileValidator.validateExecutableRawData(parsedSlang, executableRawData, errors);

        errors.addAll(preCompileValidator.checkKeyWords(
                execName,
                "",
                executableRawData,
                ListUtils.union(preExecTransformers, postExecTransformers),
                ParsedSlang.Type.DECISION.equals(parsedSlang.getType()) ?
                        executableAdditionalKeywords : allExecutableAdditionalKeywords,
                executableConstraintGroups
                )
        );

        Map<String, Serializable> preExecutableActionData = new HashMap<>();
        Map<String, Serializable> postExecutableActionData = new HashMap<>();

        String errorMessagePrefix = "For " + parsedSlang.getType().toString().toLowerCase() + " '" + execName +
                "' syntax is illegal.\n";
        preExecutableActionData.putAll(
                transformersHandler
                        .runTransformers(executableRawData, preExecTransformers, errors, errorMessagePrefix));
        postExecutableActionData.putAll(
                transformersHandler
                        .runTransformers(executableRawData, postExecTransformers, errors, errorMessagePrefix));

        @SuppressWarnings("unchecked") List<Input> inputs =
                (List<Input>) preExecutableActionData.remove(SlangTextualKeys.INPUTS_KEY);
        @SuppressWarnings("unchecked") List<Output> outputs =
                (List<Output>) postExecutableActionData.remove(SlangTextualKeys.OUTPUTS_KEY);

        @SuppressWarnings("unchecked") List<Result> results =
                (List<Result>) postExecutableActionData.remove(SlangTextualKeys.RESULTS_KEY);
        results = results == null ? new ArrayList<Result>() : results;

        String namespace = parsedSlang.getNamespace();
        Set<String> executableDependencies;
        Set<String> systemPropertyDependencies = null;
        switch (parsedSlang.getType()) {
            case FLOW:
                resultsTransformer.addDefaultResultsIfNeeded((List) executableRawData
                        .get(SlangTextualKeys.RESULTS_KEY), ExecutableType.FLOW, results, errors);

                Map<String, String> imports = parsedSlang.getImports();

                List<Map<String, Map<String, Object>>> workFlowRawData =
                        preCompileValidator.validateWorkflowRawData(parsedSlang,
                                        executableRawData.get(WORKFLOW_KEY), execName, errors);

                Workflow onFailureWorkFlow =
                        getOnFailureWorkflow(workFlowRawData, imports, errors, namespace, execName);

                WorkflowModellingResult workflowModellingResult =
                        compileWorkFlow(workFlowRawData, imports, onFailureWorkFlow, false, namespace);
                errors.addAll(workflowModellingResult.getErrors());
                Workflow workflow = workflowModellingResult.getWorkflow();

                preCompileValidator.validateResultsHaveNoExpression(results, execName, errors);

                executableDependencies = fetchDirectStepsDependencies(workflow);
                try {
                    systemPropertyDependencies = dependenciesHelper
                            .getSystemPropertiesForFlow(inputs, outputs, results, workflow.getSteps());
                } catch (RuntimeException ex) {
                    errors.add(ex);
                }
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
                return preCompileValidator
                        .validateResult(parsedSlang, execName, new ExecutableModellingResult(flow, errors));

            case OPERATION:
                resultsTransformer.addDefaultResultsIfNeeded((List) executableRawData.get(SlangTextualKeys.RESULTS_KEY),
                        ExecutableType.OPERATION, results, errors);

                Map<String, Object> actionRawData = getActionRawData(executableRawData, errors, parsedSlang, execName);
                ActionModellingResult actionModellingResult = compileAction(actionRawData);
                errors.addAll(actionModellingResult.getErrors());
                final Action action = actionModellingResult.getAction();
                executableDependencies = new HashSet<>();

                preCompileValidator.validateResultTypes(results, execName, errors);
                preCompileValidator.validateDefaultResult(results, execName, errors);

                try {
                    systemPropertyDependencies = dependenciesHelper
                            .getSystemPropertiesForOperation(inputs, outputs, results);
                } catch (RuntimeException ex) {
                    errors.add(ex);
                }
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

                return preCompileValidator
                        .validateResult(parsedSlang, execName, new ExecutableModellingResult(operation, errors));
            case DECISION:
                resultsTransformer.addDefaultResultsIfNeeded((List) executableRawData.get(SlangTextualKeys.RESULTS_KEY),
                        ExecutableType.DECISION, results, errors);

                preCompileValidator.validateResultTypes(results, execName, errors);
                preCompileValidator.validateDecisionResultsSection(executableRawData, execName, errors);
                preCompileValidator.validateDefaultResult(results, execName, errors);

                try {
                    systemPropertyDependencies = dependenciesHelper
                            .getSystemPropertiesForDecision(inputs, outputs, results);
                } catch (RuntimeException ex) {
                    errors.add(ex);
                }
                Decision decision = new Decision(
                        preExecutableActionData,
                        postExecutableActionData,
                        namespace,
                        execName,
                        inputs,
                        outputs,
                        results,
                        Collections.<String>emptySet(),
                        systemPropertyDependencies
                );
                return preCompileValidator.validateResult(
                        parsedSlang,
                        execName,
                        new ExecutableModellingResult(decision, errors)
                );
            default:
                throw new RuntimeException("Error compiling " + parsedSlang.getName() +
                        ". It is not of flow, operations or decision type");
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
            actionRawData.put(SlangTextualKeys.JAVA_ACTION_KEY,
                    executableRawData.get(SlangTextualKeys.JAVA_ACTION_KEY));
        }
        if (pythonActionRawData != null) {
            actionRawData.put(SlangTextualKeys.PYTHON_ACTION_KEY,
                    executableRawData.get(SlangTextualKeys.PYTHON_ACTION_KEY));
        }
        if (MapUtils.isEmpty(actionRawData)) {
            errors.add(new RuntimeException("Error compiling " + parsedSlang.getName() +
                    ". Operation: " + execName + " has no action data"));
        }
        return actionRawData;
    }

    private Workflow getOnFailureWorkflow(List<Map<String, Map<String, Object>>> workFlowRawData,
                                          Map<String, String> imports, List<RuntimeException> errors,
                                          String namespace, String execName) {

        Map<String, Map<String, Object>> onFailureStepData = preCompileValidator.validateOnFailurePosition(
                workFlowRawData,
                execName,
                errors
        );

        Workflow onFailureWorkFlow = null;
        if (MapUtils.isNotEmpty(onFailureStepData)) {
            List<Map<String, Map<String, Object>>> onFailureData;
            try {
                //noinspection unchecked
                onFailureData = (List<Map<String, Map<String, Object>>>) onFailureStepData.values().iterator().next();
            } catch (ClassCastException ex) {
                onFailureData = new ArrayList<>();
                errors.add(new RuntimeException("Flow: '" +
                        execName + "' syntax is illegal.\nBelow 'on_failure' property there " +
                        "should be a list of steps and not a map"));
            }
            if (CollectionUtils.isNotEmpty(onFailureData)) {
                if (onFailureData.size() > 1) {
                    errors.add(new RuntimeException("Flow: '" + execName +
                            "' syntax is illegal.\nBelow 'on_failure' property " +
                            "there should be only one step"));
                }
                handleOnFailureStepNavigationSection(onFailureData, execName, errors);

                WorkflowModellingResult workflowModellingResult =
                        compileWorkFlow(onFailureData, imports, null, true, namespace);
                errors.addAll(workflowModellingResult.getErrors());
                onFailureWorkFlow = workflowModellingResult.getWorkflow();
            } else if (onFailureData == null) {
                errors.add(new RuntimeException("Flow: '" + execName +
                        "' syntax is illegal.\nThere is no step below the 'on_failure' property."));
            }
        }
        return onFailureWorkFlow;
    }

    private void handleOnFailureStepNavigationSection(List<Map<String, Map<String, Object>>> onFailureData,
                                                      String execName, List<RuntimeException> errors) {
        Map.Entry<String, Map<String, Object>> onFailureStep = getFirstOnFailureStep(onFailureData);
        if (onFailureStep.getValue().containsKey(NAVIGATION_KEY)) {
            errors.add(new RuntimeException("Flow: '" + execName +
                    "' syntax is illegal.\nThe step below 'on_failure' property should " +
                    "not contain a \"navigate\" section."));
        }
    }

    private Map.Entry<String, Map<String, Object>> getFirstOnFailureStep(List<Map<String,
            Map<String, Object>>> onFailureData) {
        Map<String, Map<String, Object>> onFailureStepMap = onFailureData.iterator().next();
        return onFailureStepMap.entrySet().iterator().next();
    }

    private ActionModellingResult compileAction(Map<String, Object> actionRawData) {
        Map<String, Serializable> actionData = new HashMap<>();

        List<RuntimeException> errors = preCompileValidator
                .checkKeyWords("action data", "", actionRawData, actionTransformers, null, null);

        String errorMessagePrefix = "Action syntax is illegal.\n";
        actionData.putAll(
                transformersHandler.runTransformers(actionRawData, actionTransformers, errors, errorMessagePrefix));

        Action action = new Action(actionData);
        return new ActionModellingResult(action, errors);
    }

    private WorkflowModellingResult compileWorkFlow(List<Map<String, Map<String, Object>>> workFlowRawData,
                                                    Map<String, String> imports,
                                                    Workflow onFailureWorkFlow,
                                                    boolean onFailureSection,
                                                    String namespace) {

        List<RuntimeException> errors = new ArrayList<>();

        Deque<Step> steps = new LinkedList<>();
        Set<String> stepNames = new HashSet<>();
        Deque<Step> onFailureSteps = !(onFailureSection || onFailureWorkFlow == null) ?
                onFailureWorkFlow.getSteps() : new LinkedList<Step>();
        List<String> onFailureStepNames = getStepNames(onFailureSteps);
        boolean onFailureStepFound = onFailureStepNames.size() > 0;
        String defaultFailure = onFailureStepFound ? onFailureStepNames.get(0) : ScoreLangConstants.FAILURE_RESULT;

        PeekingIterator<Map<String, Map<String, Object>>> iterator = new PeekingIterator<>(workFlowRawData.iterator());
        while (iterator.hasNext()) {
            Map<String, Map<String, Object>> stepRawData = iterator.next();
            String stepName = getStepName(stepRawData);
            validateStepName(stepName, errors);
            if (stepNames.contains(stepName) || onFailureStepNames.contains(stepName)) {
                errors.add(new RuntimeException("Step name: \'" + stepName +
                        "\' appears more than once in the workflow. " + UNIQUE_STEP_NAME_MESSAGE_SUFFIX));
            }
            stepNames.add(stepName);
            Map<String, Object> stepRawDataValue;
            String message = "Step: " + stepName + " syntax is illegal.\nBelow step name, there should " +
                    "be a map of values in the format:\ndo:\n\top_name:";
            try {
                stepRawDataValue = stepRawData.values().iterator().next();
                if (MapUtils.isNotEmpty(stepRawDataValue)) {
                    boolean loopKeyFound = stepRawDataValue.containsKey(LOOP_KEY);
                    boolean parallelLoopKeyFound = stepRawDataValue.containsKey(PARALLEL_LOOP_KEY);
                    if (loopKeyFound) {
                        if (parallelLoopKeyFound) {
                            errors.add(new RuntimeException("Step: " + stepName +
                                    " syntax is illegal.\nBelow step name, " +
                                    "there can be either \'loop\' or \'aync_loop\' key."));
                        }
                        message = "Step: " + stepName + " syntax is illegal.\nBelow the 'loop' keyword, there " +
                                "should be a map of values in the format:\nfor:\ndo:\n\top_name:";
                        @SuppressWarnings("unchecked")
                        Map<String, Object> loopRawData = (Map<String, Object>) stepRawDataValue.remove(LOOP_KEY);
                        stepRawDataValue.putAll(loopRawData);
                    }
                    if (parallelLoopKeyFound) {
                        message = "Step: " + stepName +
                                " syntax is illegal.\nBelow the 'parallel_loop' keyword, there " +
                                "should be a map of values in the format:\nfor:\ndo:\n\top_name:";
                        @SuppressWarnings("unchecked")
                        Map<String, Object> parallelLoopRawData =
                                (Map<String, Object>) stepRawDataValue.remove(PARALLEL_LOOP_KEY);

                        errors.addAll(
                                preCompileValidator.checkKeyWords(
                                        stepName,
                                        SlangTextualKeys.PARALLEL_LOOP_KEY,
                                        parallelLoopRawData,
                                        Collections.<Transformer>emptyList(),
                                        parallelLoopValidKeywords,
                                        null
                                )
                        );

                        parallelLoopRawData.put(PARALLEL_LOOP_KEY, parallelLoopRawData.remove(FOR_KEY));
                        stepRawDataValue.putAll(parallelLoopRawData);
                    }
                }
            } catch (ClassCastException ex) {
                stepRawDataValue = new HashMap<>();
                errors.add(new RuntimeException(message));
            }

            String defaultSuccess;
            Map<String, Map<String, Object>> nextStepData = iterator.peek();
            if (nextStepData != null) {
                defaultSuccess = nextStepData.keySet().iterator().next();
            } else {
                defaultSuccess = onFailureSection ?
                        ScoreLangConstants.FAILURE_RESULT : ScoreLangConstants.SUCCESS_RESULT;
            }

            String onFailureStepName = onFailureStepFound ? onFailureStepNames.get(0) : null;
            StepModellingResult stepModellingResult = compileStep(
                    stepName,
                    stepRawDataValue,
                    defaultSuccess,
                    imports,
                    defaultFailure,
                    namespace,
                    onFailureStepName,
                    onFailureSection
            );

            errors.addAll(stepModellingResult.getErrors());
            steps.add(stepModellingResult.getStep());
        }

        if (onFailureStepFound) {
            steps.addAll(onFailureSteps);
        }

        return new WorkflowModellingResult(new Workflow(steps), errors);
    }

    private String getStepName(Map<String, Map<String, Object>> stepRawData) {
        return stepRawData.keySet().iterator().next();
    }

    private String validateStepName(String stepName, List<RuntimeException> errors) {
        try {
            executableValidator.validateStepName(stepName);
        } catch (RuntimeException rex) {
            errors.add(rex);
        }
        return stepName;
    }

    private StepModellingResult compileStep(
            String stepName,
            Map<String, Object> stepRawData,
            String defaultSuccess,
            Map<String, String> imports,
            String defaultFailure,
            String namespace,
            String onFailureStepName,
            boolean onFailureSection) {

        List<RuntimeException> errors = new ArrayList<>();
        if (MapUtils.isEmpty(stepRawData)) {
            stepRawData = new HashMap<>();
            errors.add(new RuntimeException("Step: " + stepName + " has no data"));
        }

        Map<String, Serializable> preStepData = new HashMap<>();
        Map<String, Serializable> postStepData = new HashMap<>();

        errors.addAll(preCompileValidator
                .checkKeyWords(stepName, "", stepRawData,
                        ListUtils.union(preStepTransformers, postStepTransformers), stepAdditionalKeyWords, null));

        String errorMessagePrefix = "For step '" + stepName + "' syntax is illegal.\n";
        preStepData.putAll(transformersHandler
                .runTransformers(stepRawData, preStepTransformers, errors, errorMessagePrefix));
        postStepData.putAll(transformersHandler
                .runTransformers(stepRawData, postStepTransformers, errors, errorMessagePrefix));

        replaceOnFailureReference(postStepData, onFailureStepName);

        @SuppressWarnings("unchecked")
        List<Argument> arguments = (List<Argument>) preStepData.get(SlangTextualKeys.DO_KEY);

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
            try {
                String refString = doRawData.keySet().iterator().next();
                refId = resolveReferenceId(refString, imports, namespace);
            } catch (RuntimeException rex) {
                errors.add(rex);
            }
        }

        List<Map<String, String>> navigationStrings =
                getNavigationStrings(postStepData, defaultSuccess, defaultFailure, errors);

        Step step = new Step(
                stepName,
                preStepData,
                postStepData,
                arguments,
                navigationStrings,
                refId,
                preStepData.containsKey(SlangTextualKeys.PARALLEL_LOOP_KEY),
                onFailureSection);
        return new StepModellingResult(step, errors);
    }

    private void replaceOnFailureReference(
            Map<String, Serializable> postStepData,
            String onFailureStepName) {
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
                        transformedNavigation.put(navigationEntry.getKey(), ScoreLangConstants.FAILURE_RESULT);
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

    private List<Map<String, String>> getNavigationStrings(
            Map<String, Serializable> postStepData,
            String defaultSuccess,
            String defaultFailure,
            List<RuntimeException> errors) {
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
            return navigationStrings;
        } else {
            try {
                executableValidator.validateNavigationStrings(navigationStrings);
                return navigationStrings;
            } catch (RuntimeException rex) {
                errors.add(rex);
                return new ArrayList<>();
            }
        }
    }

    private String resolveReferenceId(String rawReferenceId, Map<String, String> imports, String namespace) {
        executableValidator.validateStepReferenceId(rawReferenceId);

        int numberOfDelimiters = StringUtils.countMatches(rawReferenceId, NAMESPACE_DELIMITER);
        String resolvedReferenceId;

        if (numberOfDelimiters == 0) {
            // implicit namespace
            resolvedReferenceId = namespace + NAMESPACE_DELIMITER + rawReferenceId;
        } else {
            String prefix = StringUtils.substringBefore(rawReferenceId, NAMESPACE_DELIMITER);
            String suffix = StringUtils.substringAfter(rawReferenceId, NAMESPACE_DELIMITER);
            if (MapUtils.isNotEmpty(imports) && imports.containsKey(prefix)) {
                // expand alias
                resolvedReferenceId = imports.get(prefix) + NAMESPACE_DELIMITER + suffix;
            } else {
                // full path without alias expanding
                resolvedReferenceId = rawReferenceId;
            }
        }

        return resolvedReferenceId;
    }

    /**
     * Fetch the first level of the dependencies of the executable (non recursively)
     *
     * @param workflow the workflow of the flow
     * @return a map of dependencies. Key - dependency full name, value - type
     */
    private Set<String> fetchDirectStepsDependencies(Workflow workflow) {
        Set<String> dependencies = new HashSet<>();
        Deque<Step> steps = workflow.getSteps();
        for (Step step : steps) {
            dependencies.add(step.getRefId());
        }
        return dependencies;
    }

    private List<String> getStepNames(Deque<Step> steps) {
        List<String> stepNames = new ArrayList<>();
        for (Step step : steps) {
            stepNames.add(step.getName());
        }
        return stepNames;
    }

    public void setTransformers(List<Transformer> transformers) {
        this.transformers = transformers;
    }

    public void setTransformersHandler(TransformersHandler transformersHandler) {
        this.transformersHandler = transformersHandler;
    }

    public void setDependenciesHelper(DependenciesHelper dependenciesHelper) {
        this.dependenciesHelper = dependenciesHelper;
    }

    public void setPreCompileValidator(PreCompileValidator preCompileValidator) {
        this.preCompileValidator = preCompileValidator;
    }

    public void setResultsTransformer(ResultsTransformer resultsTransformer) {
        this.resultsTransformer = resultsTransformer;
    }

    public void setExecutableValidator(ExecutableValidator executableValidator) {
        this.executableValidator = executableValidator;
    }

    public void setPreExecTransformers(List<Transformer> preExecTransformers) {
        this.preExecTransformers = preExecTransformers;
    }

    public void setPostExecTransformers(List<Transformer> postExecTransformers) {
        this.postExecTransformers = postExecTransformers;
    }

    public void setExecutableAdditionalKeywords(List<String> executableAdditionalKeywords) {
        this.executableAdditionalKeywords = executableAdditionalKeywords;
    }

    public void setOperationAdditionalKeywords(List<String> operationAdditionalKeywords) {
        this.operationAdditionalKeywords = operationAdditionalKeywords;
    }

    public void setFlowAdditionalKeywords(List<String> flowAdditionalKeywords) {
        this.flowAdditionalKeywords = flowAdditionalKeywords;
    }

    public void setAllExecutableAdditionalKeywords(List<String> allExecutableAdditionalKeywords) {
        this.allExecutableAdditionalKeywords = allExecutableAdditionalKeywords;
    }

    public void setActionTransformers(List<Transformer> actionTransformers) {
        this.actionTransformers = actionTransformers;
    }

    public void setExecutableConstraintGroups(List<List<String>> executableConstraintGroups) {
        this.executableConstraintGroups = executableConstraintGroups;
    }

    public void setPreStepTransformers(List<Transformer> preStepTransformers) {
        this.preStepTransformers = preStepTransformers;
    }

    public void setPostStepTransformers(List<Transformer> postStepTransformers) {
        this.postStepTransformers = postStepTransformers;
    }

    public void setStepAdditionalKeyWords(List<String> stepAdditionalKeyWords) {
        this.stepAdditionalKeyWords = stepAdditionalKeyWords;
    }

    public void setParallelLoopValidKeywords(List<String> parallelLoopValidKeywords) {
        this.parallelLoopValidKeywords = parallelLoopValidKeywords;
    }
}
