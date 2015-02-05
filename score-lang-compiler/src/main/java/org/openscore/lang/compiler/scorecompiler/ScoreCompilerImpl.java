/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openscore.lang.compiler.scorecompiler;

import ch.lambdaj.function.convert.Converter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.Validate;
import org.openscore.api.ExecutionPlan;
import org.openscore.lang.compiler.SlangTextualKeys;
import org.openscore.lang.compiler.modeller.DependenciesHelper;
import org.openscore.lang.compiler.modeller.model.Executable;
import org.openscore.lang.compiler.modeller.model.Flow;
import org.openscore.lang.compiler.modeller.model.Operation;
import org.openscore.lang.entities.CompilationArtifact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
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
        boolean hasDependencies = MapUtils.isNotEmpty(executable.getDependencies())
                && executable.getType().equals(SlangTextualKeys.FLOW_TYPE);
        if (hasDependencies) {
            Validate.notEmpty(path, "Source " + executable.getName() + " has dependencies but no path was given to the compiler");
            Validate.noNullElements(path, "Source " + executable.getName() + " has empty dependencies");

            //we add the current executable since a dependency can require it
            List<Executable> availableExecutables = new ArrayList<>(path);
            availableExecutables.add(executable);

            //than we match the references to the actual dependencies
            filteredDependencies = dependenciesHelper.matchReferences(executable, availableExecutables);
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

        return new CompilationArtifact(executionPlan, dependencies, executable.getInputs());
    }

    /**
     * Utility method that cast a {@link org.openscore.lang.compiler.modeller.model.Executable} to its subtype
     * and create an {@link org.openscore.api.ExecutionPlan} for it
     *
     * @param executable the executable to create an {@link org.openscore.api.ExecutionPlan} for
     * @return {@link org.openscore.api.ExecutionPlan} of the given {@link org.openscore.lang.compiler.modeller.model.Executable}
     */
    private ExecutionPlan compileToExecutionPlan(Executable executable) {
        ExecutionPlan executionPlan;

        if (executable.getType().equals(SlangTextualKeys.OPERATION_TYPE)) {
            executionPlan = executionPlanBuilder.createOperationExecutionPlan((Operation) executable);
        } else if (executable.getType().equals(SlangTextualKeys.FLOW_TYPE)) {
            executionPlan = executionPlanBuilder.createFlowExecutionPlan((Flow) executable);
        } else {
            throw new RuntimeException("Executable: " + executable.getName() + " is not a flow and not an operation");
        }
        return executionPlan;
    }

}
