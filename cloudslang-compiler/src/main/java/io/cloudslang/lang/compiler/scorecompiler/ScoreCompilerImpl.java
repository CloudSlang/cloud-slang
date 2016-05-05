/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.compiler.scorecompiler;

import ch.lambdaj.function.convert.Converter;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.DependenciesHelper;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Operation;
import io.cloudslang.lang.compiler.modeller.model.Step;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.score.api.ExecutionPlan;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static ch.lambdaj.Lambda.convertMap;

/*
 * Created by stoneo on 2/2/2015.
 */
@Component
public class ScoreCompilerImpl implements ScoreCompiler{

    @Autowired
    private ExecutionPlanBuilder executionPlanBuilder;

    @Autowired
    private DependenciesHelper dependenciesHelper;

    @Override
    public CompilationArtifact compile(Executable executable, Set<Executable> path) {

        Map<String, Executable> filteredDependencies = new HashMap<>();
        //we handle dependencies only if the file has imports
        boolean hasDependencies = CollectionUtils.isNotEmpty(executable.getExecutableDependencies())
                && executable.getType().equals(SlangTextualKeys.FLOW_TYPE);
        if (hasDependencies) {
            Validate.notEmpty(path, "Source " + executable.getName() + " has dependencies but no path was given to the compiler");
            Validate.noNullElements(path, "Source " + executable.getName() + " has empty dependencies");

            //we add the current executable since a dependency can require it
            List<Executable> availableExecutables = new ArrayList<>(path);
            availableExecutables.add(executable);

            //than we match the references to the actual dependencies
            filteredDependencies = dependenciesHelper.matchReferences(executable, availableExecutables);

            List<RuntimeException> errors = validateModelWithDependencies(executable, filteredDependencies);
            if (errors.size() > 0) {
                throw errors.get(0);
            }

        }

        //next we create an execution plan for the required executable
        ExecutionPlan executionPlan = compileToExecutionPlan(executable);

        //and also create execution plans for all other dependencies
        Map<String, ExecutionPlan> dependencies = convertMap(filteredDependencies, new Converter<Executable, ExecutionPlan>() {
            @Override
            public ExecutionPlan convert(Executable compiledExecutable) {
                return compileToExecutionPlan(compiledExecutable);
            }
        });
        Collection<Executable> executables = new ArrayList<>(filteredDependencies.values());
        executables.add(executable);

        executionPlan.setSubflowsUUIDs(new HashSet<>(dependencies.keySet()));
        return new CompilationArtifact(executionPlan, dependencies, executable.getInputs(), getSystemPropertiesFromExecutables(executables));
    }

    @Override
    public List<RuntimeException> validateSlangModelWithDependencies(Executable slangModel, Set<Executable> dependenciesModels) {
        Map<String, Executable> dependenciesMap = new HashMap<>();
        for (Executable dependency : dependenciesModels) {
            dependenciesMap.put(dependency.getId(), dependency);
        }
        return validateModelWithDependencies(slangModel, dependenciesMap);
    }

    private List<RuntimeException> validateModelWithDependencies(Executable executable, Map<String, Executable> dependencies) {

        List<RuntimeException> errors = validateRequiredNonPrivateInputs(executable, dependencies);

        // Validate that all the steps of a flow have navigations for all the reference's results
        errors.addAll(validateAllDependenciesResultsHaveMatchingNavigations(executable, dependencies));

        return errors;
    }

    private List<RuntimeException> validateRequiredNonPrivateInputs(
            Executable executable,
            Map<String, Executable> filteredDependencies) {
        Map<String, Executable> dependencies = new HashMap<>(filteredDependencies);
        dependencies.put(executable.getId(), executable);
        Set<Executable> verifiedExecutables = new HashSet<>();
        return validateRequiredNonPrivateInputs(executable, dependencies, verifiedExecutables, new ArrayList<RuntimeException>());
    }

    private List<RuntimeException> validateRequiredNonPrivateInputs(
            Executable executable,
            Map<String, Executable> dependencies,
            Set<Executable> verifiedExecutables,
            List<RuntimeException> errors) {
        //validate that all required & non private parameters with no default value of a reference are provided
        if(!SlangTextualKeys.FLOW_TYPE.equals(executable.getType()) || verifiedExecutables.contains(executable)){
            return errors;
        }
        verifiedExecutables.add(executable);

        Flow flow = (Flow) executable;
        Collection<Step> steps = flow.getWorkflow().getSteps();
        Set<Executable> flowReferences = new HashSet<>();

        for (Step step : steps) {
            Executable reference = dependencies.get(step.getRefId());
            List<String> mandatoryInputNames = getMandatoryInputNames(reference);
            List<String> stepInputNames = getStepInputNames(step);
            List<String> inputsNotWired = getInputsNotWired(mandatoryInputNames, stepInputNames);

            try {
                validateInputNamesEmpty(inputsNotWired, flow, step, reference);
            } catch (RuntimeException e) {
                errors.add(e);
            }

            flowReferences.add(reference);
        }

        for (Executable reference : flowReferences) {
            validateRequiredNonPrivateInputs(reference, dependencies, verifiedExecutables, errors);
        }
        return errors;
    }

    private List<String> getMandatoryInputNames(Executable executable) {
        List<String> inputNames = new ArrayList<>();
        for (Input input : executable.getInputs()) {
            if (!input.isPrivateInput() && input.isRequired() && input.getValue() == null) {
                inputNames.add(input.getName());
            }
        }
        return inputNames;
    }

    private List<String> getStepInputNames(Step step) {
        List<String> inputNames = new ArrayList<>();
        for (Argument argument : step.getArguments()) {
            inputNames.add(argument.getName());
        }
        return inputNames;
    }

    private List<String> getInputsNotWired(List<String> mandatoryInputNames, List<String> stepInputNames) {
        List<String> inputsNotWired = new ArrayList<>(mandatoryInputNames);
        inputsNotWired.removeAll(stepInputNames);
        return inputsNotWired;
    }

    private void validateInputNamesEmpty(List<String> inputsNotWired, Flow flow, Step step, Executable reference) {
        Validate.isTrue(
                CollectionUtils.isEmpty(inputsNotWired),
                prepareErrorMessageValidateInputNamesEmpty(inputsNotWired, flow, step, reference)
        );
    }

    private String prepareErrorMessageValidateInputNamesEmpty(List<String> inputsNotWired, Flow flow, Step step, Executable reference) {
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

    /**
     * Validate that for all the steps in the flow, all results from the referenced operation or flow have matching navigations
     * If the given {@link io.cloudslang.lang.compiler.modeller.model.Executable} is an operation, the method does nothing
     * Throws {@link java.lang.IllegalArgumentException} if:
     *      - Any reference of the executable is missing
     *      - There is a missing navigation for one of the steps' references' results
     *
     * @param executable the flow to validate
     * @param filteredDependencies a map holding for each reference name, its {@link io.cloudslang.lang.compiler.modeller.model.Executable} object
     */
    private List<RuntimeException> validateAllDependenciesResultsHaveMatchingNavigations(Executable executable, Map<String, Executable> filteredDependencies) {
        List<RuntimeException> errors = new ArrayList<>();
        if(executable.getType().equals(SlangTextualKeys.OPERATION_TYPE)){
            return errors;
        }
        Flow flow = (Flow)executable;
        Deque<Step> steps = flow.getWorkflow().getSteps();
        for(Step step : steps){
            List<Map<String, String>> stepNavigations = step.getNavigationStrings();
            String refId = step.getRefId();
            Executable reference = filteredDependencies.get(refId);
            if (reference == null) {
                errors.add(new IllegalArgumentException("Cannot compile flow: \'" + executable.getName() + "\' since for step: \'" + step.getName()
                        + "\', the dependency: \'" + refId + "\' is missing."));
            }
            List<Result> refResults = reference.getResults();
            for(Result result : refResults){
                String resultName = result.getName();
                if (!navigationListContainsKey(stepNavigations, resultName)){
                    errors.add(new IllegalArgumentException("Cannot compile flow: \'" + executable.getName() +
                            "\' since for step: '" + step.getName() + "\', the result \'" + resultName+
                            "\' of its dependency: \'"+ refId + "\' has no matching navigation"));
                }
            }
        }
        return errors;
    }

    private boolean navigationListContainsKey(List<Map<String, String>> stepNavigations, String resultName) {
        for (Map<String, String> map : stepNavigations) {
            if (map.containsKey(resultName)) return true;
        }
        return false;
    }

    /**
     * Utility method that cast a {@link io.cloudslang.lang.compiler.modeller.model.Executable} to its subtype
     * and create an {@link io.cloudslang.score.api.ExecutionPlan} for it
     *
     * @param executable the executable to create an {@link io.cloudslang.score.api.ExecutionPlan} for
     * @return {@link io.cloudslang.score.api.ExecutionPlan} of the given {@link io.cloudslang.lang.compiler.modeller.model.Executable}
     */
    private ExecutionPlan compileToExecutionPlan(Executable executable) {

        switch (executable.getType()){
            case SlangTextualKeys.OPERATION_TYPE:
                return executionPlanBuilder.createOperationExecutionPlan((Operation) executable);
            case SlangTextualKeys.FLOW_TYPE:
                return executionPlanBuilder.createFlowExecutionPlan((Flow) executable);
            default:
                throw new RuntimeException("Executable: " + executable.getName() + " cannot be compiled to an ExecutionPlan since it is not a flow and not an operation");
        }
    }

    private Set<String> getSystemPropertiesFromExecutables(Collection<Executable> executables) {
        Set<String> result = new HashSet<>();
        for(Executable executable : executables) {
            result.addAll(executable.getSystemPropertyDependencies());
        }
        return result;
    }

}
