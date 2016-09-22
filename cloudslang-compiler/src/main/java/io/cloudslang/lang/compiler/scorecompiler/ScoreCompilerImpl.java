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
import io.cloudslang.lang.compiler.modeller.model.Decision;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Operation;
import io.cloudslang.lang.compiler.modeller.model.Step;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static ch.lambdaj.Lambda.convertMap;

/*
 * Created by stoneo on 2/2/2015.
 */
@Component
public class ScoreCompilerImpl implements ScoreCompiler {

    @Autowired
    private ExecutionPlanBuilder executionPlanBuilder;

    @Autowired
    private DependenciesHelper dependenciesHelper;

    @Autowired
    private CompileValidator compileValidator;

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

            handleOnFailureCustomResults(executable, filteredDependencies);

            List<RuntimeException> errors = compileValidator.validateModelWithDependencies(executable, filteredDependencies);
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
    public List<RuntimeException> validateSlangModelWithDirectDependencies(Executable slangModel, Set<Executable> directDependenciesModels) {
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
     * @return {@link io.cloudslang.score.api.ExecutionPlan} of the given {@link io.cloudslang.lang.compiler.modeller.model.Executable}
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
                throw new RuntimeException("Executable: " + executable.getName() + " cannot be compiled to an ExecutionPlan since it is not of type flow, operation or decision");
        }
    }

    private Set<String> getSystemPropertiesFromExecutables(Collection<Executable> executables) {
        Set<String> result = new HashSet<>();
        for (Executable executable : executables) {
            result.addAll(executable.getSystemPropertyDependencies());
        }
        return result;
    }

}
