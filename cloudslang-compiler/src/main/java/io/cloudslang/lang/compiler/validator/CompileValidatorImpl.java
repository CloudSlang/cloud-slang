/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.validator;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Step;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.entities.utils.ArgumentUtils;
import io.cloudslang.lang.entities.utils.InputUtils;
import io.cloudslang.lang.entities.utils.ListUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.python.google.common.collect.Lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CompileValidatorImpl extends AbstractValidator implements CompileValidator {

    public static final String DUPLICATE_EXECUTABLE_FOUND = "Duplicate executable found: '%s'";

    @Override
    public List<RuntimeException> validateModelWithDependencies(
            Executable executable,
            Map<String, Executable> filteredDependencies) {
        Map<String, Executable> dependencies = new HashMap<>(filteredDependencies);
        dependencies.put(executable.getId(), executable);
        Set<Executable> verifiedExecutables = new HashSet<>();
        return validateModelWithDependencies(executable, dependencies, verifiedExecutables,
                new ArrayList<RuntimeException>(), true);
    }

    private List<RuntimeException> validateModelWithDependencies(
            Executable executable,
            Map<String, Executable> dependencies,
            Set<Executable> verifiedExecutables,
            List<RuntimeException> errors,
            boolean recursive) {
        //validate that all required & non private parameters with no default value of a reference are provided
        if (!SlangTextualKeys.FLOW_TYPE.equals(executable.getType()) || verifiedExecutables.contains(executable)) {
            return errors;
        }
        verifiedExecutables.add(executable);

        Flow flow = (Flow) executable;
        Collection<Step> steps = flow.getWorkflow().getSteps();
        Set<Executable> flowReferences = new HashSet<>();

        for (Step step : steps) {
            Executable reference = dependencies.get(step.getRefId());
            errors.addAll(validateStepAgainstItsDependency(flow, step, dependencies));
            flowReferences.add(reference);
        }

        if (recursive) {
            for (Executable reference : flowReferences) {
                validateModelWithDependencies(reference, dependencies, verifiedExecutables, errors, true);
            }
        }
        return errors;
    }

    @Override
    public List<RuntimeException> validateModelWithDirectDependencies(Executable executable,
                                                                      Map<String, Executable> directDependencies) {
        List<RuntimeException> errors = new ArrayList<>();
        Set<Executable> verifiedExecutables = new HashSet<>();
        return validateModelWithDependencies(executable, directDependencies, verifiedExecutables, errors, false);
    }

    @Override
    public List<RuntimeException> validateNoDuplicateExecutables(
            Executable currentExecutable,
            SlangSource currentSource,
            Map<Executable, SlangSource> allAvailableExecutables) {
        List<RuntimeException> errors = new ArrayList<>();
        for (Map.Entry<Executable, SlangSource> entry : allAvailableExecutables.entrySet()) {
            Executable executable = entry.getKey();
            if (currentExecutable.getId().equalsIgnoreCase(executable.getId()) &&
                    !currentSource.equals(entry.getValue())) {
                errors.add(new RuntimeException(String.format(DUPLICATE_EXECUTABLE_FOUND, currentExecutable.getId())));
            }
        }
        return errors;
    }

    private List<RuntimeException> validateStepAgainstItsDependency(Flow flow, Step step,
                                                                    Map<String, Executable> dependencies) {
        List<RuntimeException> errors = new ArrayList<>();
        String refId = step.getRefId();
        Executable reference = dependencies.get(refId);
        if (reference == null) {
            throw new RuntimeException("Dependency " + step.getRefId() + " used by step: " + step.getName() +
                    " must be supplied for validation");
        }
        errors.addAll(validateMandatoryInputsAreWired(flow, step, reference));
        errors.addAll(validateStepInputNamesDifferentFromDependencyOutputNames(flow, step, reference));
        errors.addAll(validateNavigationSectionAgainstDependencyResults(flow, step, reference));
        errors.addAll(validateBreakSection(flow, step, reference));
        return errors;
    }

    private List<RuntimeException> validateBreakSection(Flow parentFlow, Step step, Executable reference) {
        List<RuntimeException> errors = new ArrayList<>();
        @SuppressWarnings("unchecked") // from BreakTransformer
                List<String> breakValues = (List<String>) step.getPostStepActionData().get(SlangTextualKeys.BREAK_KEY);

        if (isForLoop(step, breakValues)) {
            List<String> referenceResultNames = getResultNames(reference);
            Collection<String> nonExistingResults = ListUtils.subtract(breakValues, referenceResultNames);

            if (CollectionUtils.isNotEmpty(nonExistingResults)) {
                errors.add(new IllegalArgumentException("Cannot compile flow '" + parentFlow.getId() +
                        "' since in step '" + step.getName() + "' the results " +
                        nonExistingResults + " declared in '" + SlangTextualKeys.BREAK_KEY +
                        "' section are not declared in the dependency '" + reference.getId() + "' result section."));
            }
        }

        return errors;
    }

    private boolean isForLoop(Step step, List<String> breakValuesList) {
        Serializable forData = step.getPreStepActionData().get(SlangTextualKeys.FOR_KEY);
        return (forData != null) && CollectionUtils.isNotEmpty(breakValuesList);
    }

    private List<RuntimeException> validateNavigationSectionAgainstDependencyResults(Flow flow, Step step,
                                                                                     Executable reference) {
        List<RuntimeException> errors = new ArrayList<>();
        String refId = step.getRefId();
        if (reference == null) {
            errors.add(new IllegalArgumentException(
                    getErrorMessagePrefix(flow, step) + " the dependency '" + refId + "' is missing."
            ));
        } else {
            if (!step.isOnFailureStep()) { // on_failure step cannot have navigation section
                validateResultNamesAndNavigationSection(flow, step, refId, reference, errors);
            }
        }
        return errors;
    }

    private void validateResultNamesAndNavigationSection(Flow flow, Step step, String refId,
                                                         Executable reference, List<RuntimeException> errors) {
        List<String> stepNavigationKeys = getMapKeyList(step.getNavigationStrings());
        List<String> refResults = mapResultsToNames(reference.getResults());
        List<String> possibleResults;

        possibleResults = getPossibleResults(step, refResults);

        List<String> stepNavigationKeysWithoutMatchingResult = ListUtils.subtract(stepNavigationKeys, possibleResults);
        List<String> refResultsWithoutMatchingNavigation = ListUtils.subtract(possibleResults, stepNavigationKeys);

        if (CollectionUtils.isNotEmpty(refResultsWithoutMatchingNavigation)) {
            if (step.isParallelLoop()) {
                errors.add(new IllegalArgumentException(
                        getErrorMessagePrefix(flow, step) + " the parallel loop results " +
                                refResultsWithoutMatchingNavigation + " have no matching navigation."
                ));
            } else {
                errors.add(new IllegalArgumentException(
                        getErrorMessagePrefix(flow, step) + " the results " + refResultsWithoutMatchingNavigation +
                                " of its dependency '" + refId + "' have no matching navigation."
                ));
            }
        }
        if (CollectionUtils.isNotEmpty(stepNavigationKeysWithoutMatchingResult)) {
            if (step.isParallelLoop()) {
                errors.add(new IllegalArgumentException(
                        getErrorMessagePrefix(flow, step) + " the navigation keys " +
                                stepNavigationKeysWithoutMatchingResult + " have no matching results." +
                                " The parallel loop depending on '" + refId +
                                "' can have the following results: " + possibleResults + "."
                ));
            } else {
                errors.add(new IllegalArgumentException(
                        getErrorMessagePrefix(flow, step) + " the navigation keys " +
                                stepNavigationKeysWithoutMatchingResult +
                                " have no matching results in its dependency '" +
                                refId + "'."
                ));
            }
        }
    }

    private List<String> getPossibleResults(Step step, List<String> refResults) {
        List<String> possibleResults;
        if (step.isParallelLoop()) {
            possibleResults = Lists.newArrayList(ScoreLangConstants.SUCCESS_RESULT);
            if (refResults.contains(ScoreLangConstants.FAILURE_RESULT)) {
                possibleResults.add(ScoreLangConstants.FAILURE_RESULT);
            }

        } else {
            possibleResults = refResults;
        }
        return possibleResults;
    }

    private String getErrorMessagePrefix(Flow flow, Step step) {
        return "Cannot compile flow '" + flow.getName() +
                "' since for step '" + step.getName() + "'";
    }

    private List<String> getMapKeyList(List<Map<String, String>> collection) {
        List<String> result = new ArrayList<>();
        for (Map<String, String> element : collection) {
            result.add(element.keySet().iterator().next());
        }
        return result;
    }

    private List<String> mapResultsToNames(List<Result> results) {
        List<String> resultNames = new ArrayList<>();
        for (Result element : results) {
            resultNames.add(element.getName());
        }
        return resultNames;
    }

    private List<RuntimeException> validateStepInputNamesDifferentFromDependencyOutputNames(Flow flow, Step step,
                                                                                            Executable reference) {
        List<RuntimeException> errors = new ArrayList<>();
        List<Argument> stepArguments = step.getArguments();
        List<Output> outputs = reference.getOutputs();
        String errorMessage = "Cannot compile flow '" + flow.getId() +
                "'. Step '" + step.getName() +
                "' has input '" + NAME_PLACEHOLDER +
                "' with the same name as the one of the outputs of '" + reference.getId() + "'.";
        try {
            validateListsHaveMutuallyExclusiveNames(stepArguments, outputs, errorMessage);
        } catch (RuntimeException e) {
            errors.add(e);
        }
        return errors;
    }

    private List<String> getMandatoryInputNames(Executable executable) {
        List<String> inputNames = new ArrayList<>();
        for (Input input : executable.getInputs()) {
            if (InputUtils.isMandatory(input)) {
                inputNames.add(input.getName());
            }
        }
        return inputNames;
    }

    private List<String> getStepInputNamesWithNonEmptyValue(Step step) {
        List<String> inputNames = new ArrayList<>();
        for (Argument argument : step.getArguments()) {
            if (ArgumentUtils.isDefined(argument)) {
                inputNames.add(argument.getName());
            }
        }
        return inputNames;
    }

    private List<String> getInputsNotWired(List<String> mandatoryInputNames, List<String> stepInputNames) {
        List<String> inputsNotWired = new ArrayList<>(mandatoryInputNames);
        inputsNotWired.removeAll(stepInputNames);
        return inputsNotWired;
    }

    private List<RuntimeException> validateMandatoryInputsAreWired(Flow flow, Step step, Executable reference) {
        List<RuntimeException> errors = new ArrayList<>();
        List<String> mandatoryInputNames = getMandatoryInputNames(reference);
        List<String> stepInputNames = getStepInputNamesWithNonEmptyValue(step);
        List<String> inputsNotWired = getInputsNotWired(mandatoryInputNames, stepInputNames);
        if (!CollectionUtils.isEmpty(inputsNotWired)) {
            errors.add(new IllegalArgumentException(
                    prepareErrorMessageValidateInputNamesEmpty(inputsNotWired, flow, step, reference))
            );
        }
        return errors;
    }

    private String prepareErrorMessageValidateInputNamesEmpty(List<String> inputsNotWired, Flow flow,
                                                              Step step, Executable reference) {
        StringBuilder inputsNotWiredBuilder = new StringBuilder();
        for (String inputName : inputsNotWired) {
            inputsNotWiredBuilder.append(inputName);
            inputsNotWiredBuilder.append(", ");
        }
        String inputsNotWiredAsString = inputsNotWiredBuilder.toString();
        if (StringUtils.isNotEmpty(inputsNotWiredAsString)) {
            inputsNotWiredAsString = inputsNotWiredAsString.substring(0, inputsNotWiredAsString.length() - 2);
        }
        return "Cannot compile flow '" + flow.getId() +
                "'. Step '" + step.getName() +
                "' does not declare all the mandatory inputs of its reference." +
                " The following inputs of '" + reference.getId() +
                "' are not private, required and with no default value: " +
                inputsNotWiredAsString + ".";
    }
}
