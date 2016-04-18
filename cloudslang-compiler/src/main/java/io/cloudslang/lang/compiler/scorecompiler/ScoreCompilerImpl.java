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
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Step;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.entities.bindings.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.Validate;
import io.cloudslang.lang.compiler.modeller.DependenciesHelper;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

            if(SlangTextualKeys.FLOW_TYPE.equals(executable.getType())){
                //validate that all required & overridable parmateres provided by parent flow to subflow
                validateAllRequiredOverridableInputs(executable, path);
            }

            // Validate that all the steps of a flow have navigations for all the reference's results
            validateAllDependenciesResultsHaveMatchingNavigations(executable, filteredDependencies);

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



    private void validateAllRequiredOverridableInputs(Executable executable, Set<Executable> path) {
        // Get all the steps of the flow
        Collection<Step> steps = ((Flow) executable).getWorkflow().getSteps();

        for(Executable e:path) {
            //get all required & overidable input names that do not have default value
            List<String> requiredOveridableInputsNames = new ArrayList<>();
            for(Input i: e.getInputs()) {
                if(i.isOverridable() && i.isRequired() && (i.getValue() == null)) {
                    requiredOveridableInputsNames.add(i.getName());
                }
            }
            if(!requiredOveridableInputsNames.isEmpty()) {
                //get all step that have reference to the current executable depenedency (sub flow/op)
                List<Step> allStepsWithDependencyReference = new ArrayList<>();
                for (Step s : ((Flow) executable).getWorkflow().getSteps()) {
                    if (s.getRefId().equals(e.getId())) {
                        allStepsWithDependencyReference.add(s);
                    }
                }
                //find the missing inputs
                for(Step s:allStepsWithDependencyReference) {
                    Set<String> argumentNames = new HashSet<>();
                    for (Argument a: s.getArguments()) {
                        argumentNames.add(a.getName());
                    }
                    List<String> missingInputs = new ArrayList<>();
                    for(String i: requiredOveridableInputsNames) {
                        if(!argumentNames.contains(i)) {
                            missingInputs.add(i);
                        }
                    }

                    String errorMessage = "Cannot compile flow \'" + executable.getName() + "\', Step \'" + s.getName() + "\' has missing required inputs " + missingInputs + " for subflow/operation \'" + e.getId() + "\'";
                    Validate.isTrue(missingInputs.isEmpty(), errorMessage);
                };
            }
        };
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
    private void validateAllDependenciesResultsHaveMatchingNavigations(Executable executable, Map<String, Executable> filteredDependencies) {
        if(executable.getType().equals(SlangTextualKeys.OPERATION_TYPE)){
            return;
        }
        Flow flow = (Flow)executable;
        Deque<Step> steps = flow.getWorkflow().getSteps();
        for(Step step : steps){
            List<Map<String, String>> stepNavigations = step.getNavigationStrings();
            String refId = step.getRefId();
            Executable reference = filteredDependencies.get(refId);
            Validate.notNull(reference, "Cannot compile flow: \'" + executable.getName() + "\' since for step: \'" + step.getName()
                    + "\', the dependency: \'" + refId + "\' is missing.");
            List<Result> refResults = reference.getResults();
            for(Result result : refResults){
                String resultName = result.getName();
                Validate.isTrue(navigationListContainsKey(stepNavigations, resultName), "Cannot compile flow: \'" + executable.getName() +
                        "\' since for step: '" + step.getName() + "\', the result \'" + resultName+
                        "\' of its dependency: \'"+ refId + "\' has no matching navigation");
            }
        }
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
