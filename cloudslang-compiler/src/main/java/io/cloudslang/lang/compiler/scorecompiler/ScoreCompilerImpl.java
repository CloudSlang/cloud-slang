/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.scorecompiler;

import ch.lambdaj.function.convert.Converter;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.DependenciesHelper;
import io.cloudslang.lang.compiler.modeller.model.Decision;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Operation;
import io.cloudslang.lang.compiler.modeller.model.Step;
import io.cloudslang.lang.compiler.modeller.result.CompilationModellingResult;
import io.cloudslang.lang.compiler.validator.CompileValidator;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
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

import static ch.lambdaj.Lambda.convertMap;


public class ScoreCompilerImpl implements ScoreCompiler {

    private ExecutionPlanBuilder executionPlanBuilder;

    private DependenciesHelper dependenciesHelper;

    private CompileValidator compileValidator;

    @Override
    public CompilationArtifact compile(Executable source, Set<Executable> path) {
        CompilationModellingResult compilationModellingResult = compileSource(source, path);
        List<RuntimeException> errors = compilationModellingResult.getErrors();
        if (CollectionUtils.isNotEmpty(errors)) {
            throw errors.get(0);
        }
        return compilationModellingResult.getCompilationArtifact();
    }

    @Override
    public CompilationModellingResult compileSource(Executable executable, Set<Executable> path) {
        List<RuntimeException> exceptions = new ArrayList<>();
        Map<String, Executable> filteredDependencies = new HashMap<>();
        //we handle dependencies only if the file has imports
        boolean hasDependencies = CollectionUtils.isNotEmpty(executable.getExecutableDependencies()) &&
                executable.getType().equals(SlangTextualKeys.FLOW_TYPE);
        if (hasDependencies) {
            try {
                Validate.notEmpty(path, "Source " + executable.getName() +
                        " has dependencies but no path was given to the compiler");
                Validate.noNullElements(path, "Source " + executable.getName() + " has empty dependencies");
            } catch (RuntimeException ex) {
                exceptions.add(ex);
            }

            //we add the current executable since a dependency can require it
            List<Executable> availableExecutables = new ArrayList<>(path);
            availableExecutables.add(executable);

            try {
                //than we match the references to the actual dependencies
                filteredDependencies = dependenciesHelper.matchReferences(executable, availableExecutables);

                handleOnFailureCustomResults(executable, filteredDependencies);

                List<RuntimeException> errors =
                        compileValidator.validateModelWithDependencies(executable, filteredDependencies);
                exceptions.addAll(errors);
            } catch (RuntimeException ex) {
                exceptions.add(ex);
            }

        }

        try {
            //next we create an execution plan for the required executable
            ExecutionPlan executionPlan = compileToExecutionPlan(executable);

            //and also create execution plans for all other dependencies
            Converter<Executable, ExecutionPlan> converter = new Converter<Executable, ExecutionPlan>() {
                @Override
                public ExecutionPlan convert(Executable compiledExecutable) {
                    return compileToExecutionPlan(compiledExecutable);
                }
            };
            Map<String, ExecutionPlan> dependencies = convertMap(filteredDependencies, converter);
            Collection<Executable> executables = new ArrayList<>(filteredDependencies.values());
            executables.add(executable);

            executionPlan.setSubflowsUUIDs(new HashSet<>(dependencies.keySet()));
            CompilationArtifact compilationArtifact = new CompilationArtifact(executionPlan, dependencies,
                    executable.getInputs(), getSystemPropertiesFromExecutables(executables));
            return new CompilationModellingResult(compilationArtifact, exceptions);
        } catch (RuntimeException ex) {
            exceptions.add(ex);
        }
        return new CompilationModellingResult(null, exceptions);
    }

    private void handleOnFailureCustomResults(Executable executable, Map<String, Executable> filteredDependencies) {
        handleOnFailureStepCustomResults((Flow) executable, filteredDependencies);
        for (Executable dependency : filteredDependencies.values()) {
            if (dependency.getType().equals(SlangTextualKeys.FLOW_TYPE)) {
                handleOnFailureStepCustomResults((Flow) dependency, filteredDependencies);
            }
        }
    }

    private void handleOnFailureStepCustomResults(Flow executable, Map<String, Executable> filteredDependencies) {
        Step onFailureStep = getOnFailureStep(executable);
        if (onFailureStep != null) {
            Executable onFailureDependency = filteredDependencies.get(onFailureStep.getRefId());
            for (Result result : onFailureDependency.getResults()) {
                Map<String, String> navigationString = new HashMap<>();
                navigationString.put(result.getName(), ScoreLangConstants.FAILURE_RESULT);
                onFailureStep.getNavigationStrings().add(navigationString);
            }
        }
    }

    private Step getOnFailureStep(Flow flow) {
        Deque<Step> stepDeque = flow.getWorkflow().getSteps();
        for (Step step : stepDeque) {
            if (step.isOnFailureStep()) {
                return step;
            }
        }
        return null;
    }

    @Override
    public List<RuntimeException> validateSlangModelWithDirectDependencies(Executable slangModel,
                                                                           Set<Executable> directDependenciesModels) {
        Map<String, Executable> dependenciesMap = new HashMap<>();
        for (Executable dependency : directDependenciesModels) {
            dependenciesMap.put(dependency.getId(), dependency);
        }
        return compileValidator.validateModelWithDirectDependencies(slangModel, dependenciesMap);
    }

    /**
     * Utility method that cast a {@link io.cloudslang.lang.compiler.modeller.model.Executable} to its subtype
     * and create an {@link io.cloudslang.score.api.ExecutionPlan} for it
     *
     * @param executable the executable to create an {@link io.cloudslang.score.api.ExecutionPlan} for
     * @return {@link io.cloudslang.score.api.ExecutionPlan} of the given
     * {@link io.cloudslang.lang.compiler.modeller.model.Executable}
     */
    private ExecutionPlan compileToExecutionPlan(Executable executable) {

        switch (executable.getType()) {
            case SlangTextualKeys.OPERATION_TYPE:
                return executionPlanBuilder.createOperationExecutionPlan((Operation) executable);
            case SlangTextualKeys.FLOW_TYPE:
                return executionPlanBuilder.createFlowExecutionPlan((Flow) executable);
            case SlangTextualKeys.DECISION_TYPE:
                return executionPlanBuilder.createDecisionExecutionPlan((Decision) executable);
            default:
                throw new RuntimeException("Executable: " + executable.getName() +
                        " cannot be compiled to an ExecutionPlan since it is not of type flow, operation or decision");
        }
    }

    private Set<String> getSystemPropertiesFromExecutables(Collection<Executable> executables) {
        Set<String> result = new HashSet<>();
        for (Executable executable : executables) {
            result.addAll(executable.getSystemPropertyDependencies());
        }
        return result;
    }

    public void setExecutionPlanBuilder(ExecutionPlanBuilder executionPlanBuilder) {
        this.executionPlanBuilder = executionPlanBuilder;
    }

    public void setDependenciesHelper(DependenciesHelper dependenciesHelper) {
        this.dependenciesHelper = dependenciesHelper;
    }

    public void setCompileValidator(CompileValidator compileValidator) {
        this.compileValidator = compileValidator;
    }
}
