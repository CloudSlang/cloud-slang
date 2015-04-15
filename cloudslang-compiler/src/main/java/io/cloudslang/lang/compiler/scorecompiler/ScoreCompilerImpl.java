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
import io.cloudslang.lang.compiler.modeller.model.Task;
import io.cloudslang.lang.entities.bindings.Result;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.Validate;
import io.cloudslang.lang.compiler.modeller.DependenciesHelper;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.bindings.Input;
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
        boolean hasDependencies = CollectionUtils.isNotEmpty(executable.getDependencies())
                && executable.getType().equals(SlangTextualKeys.FLOW_TYPE);
        if (hasDependencies) {
            Validate.notEmpty(path, "Source " + executable.getName() + " has dependencies but no path was given to the compiler");
            Validate.noNullElements(path, "Source " + executable.getName() + " has empty dependencies");

            //we add the current executable since a dependency can require it
            List<Executable> availableExecutables = new ArrayList<>(path);
            availableExecutables.add(executable);

            //than we match the references to the actual dependencies
            filteredDependencies = dependenciesHelper.matchReferences(executable, availableExecutables);

            // Validate that all the tasks of a flow have navigations for all the reference's results
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
        return new CompilationArtifact(executionPlan, dependencies, executable.getInputs(), getSystemProperties(executables));
    }

    /**
     * Validate that for all the tasks in the flow, all results from the referenced operation or flow have matching navigations
     * If the given {@link io.cloudslang.lang.compiler.modeller.model.Executable} is an operation, the method does nothing
     * Throws {@link java.lang.IllegalArgumentException} if:
     *      - Any reference of the executable is missing
     *      - There is a missing navigation for one of the tasks' references' results
     *
     * @param executable the flow to validate
     * @param filteredDependencies a map holding for each reference name, its {@link io.cloudslang.lang.compiler.modeller.model.Executable} object
     */
    private void validateAllDependenciesResultsHaveMatchingNavigations(Executable executable, Map<String, Executable> filteredDependencies) {
        if(executable.getType().equals(SlangTextualKeys.OPERATION_TYPE)){
            return;
        }
        Flow flow = (Flow)executable;
        Deque<Task> tasks = flow.getWorkflow().getTasks();
        for(Task task : tasks){
            Map<String, String> taskNavigations = task.getNavigationStrings();
            String refId = task.getRefId();
            Executable reference = filteredDependencies.get(refId);
            Validate.notNull(reference, "Cannot compile flow: \'" + executable.getName() + "\' since for task: \'" + task.getName()
                                        + "\', the dependency: \'" + refId + "\' is missing.");
            List<Result> refResults = reference.getResults();
            for(Result result : refResults){
                String resultName = result.getName();
                Validate.isTrue(taskNavigations.containsKey(resultName), "Cannot compile flow: \'" + executable.getName() +
                                                "\' since for task: '" + task.getName() + "\', the result \'" + resultName+
                                                "\' of its dependency: \'"+ refId + "\' has no matching navigation");
            }
        }
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

	private static Collection<Input> getSystemProperties(Collection<Executable> executables) {
		Collection<Input> result = new ArrayList<>();
		for(Executable executable : executables) {
			result.addAll(getSystemProperties(executable.getInputs()));
			if(executable instanceof Flow) {
				for(Task task : ((Flow)executable).getWorkflow().getTasks()) {
					result.addAll(getSystemProperties(task.getInputs()));
				}
			}
		}
		return result;
	}

	private static Collection<Input> getSystemProperties(List<Input> inputs) {
		Collection<Input> result = new ArrayList<>();
		for(Input input : inputs) {
			if(input.getSystemPropertyName() != null) result.add(input);
		}
		return result;
	}

}
