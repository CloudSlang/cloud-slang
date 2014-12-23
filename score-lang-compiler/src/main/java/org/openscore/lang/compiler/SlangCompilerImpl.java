/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.lang.compiler;

import ch.lambdaj.Lambda;
import ch.lambdaj.function.convert.Converter;
import org.openscore.lang.compiler.model.*;
import org.openscore.lang.compiler.utils.DependenciesHelper;
import org.openscore.lang.compiler.utils.ExecutableBuilder;
import org.openscore.lang.compiler.utils.ExecutionPlanBuilder;
import org.openscore.lang.compiler.utils.YamlParser;
import org.openscore.lang.entities.CompilationArtifact;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.openscore.api.ExecutionPlan;
import org.openscore.lang.compiler.model.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ch.lambdaj.Lambda.convertMap;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

/*
 * Created by orius123 on 05/11/14.
 */
@Component
public class SlangCompilerImpl implements SlangCompiler {

    @Autowired
    private ExecutableBuilder executableBuilder;

    @Autowired
    private DependenciesHelper dependenciesHelper;

    @Autowired
    private ExecutionPlanBuilder executionPlanBuilder;

    @Autowired
    private YamlParser yamlParser;

    @Override
    public CompilationArtifact compileFlow(File source, Set<File> path) {
        return compile(source, null, path);
    }

    @Override
    public CompilationArtifact compile(File source, String operationName, Set<File> path) {

        Validate.notNull(source, "You must supply a file to compile");
        Validate.isTrue(!source.isDirectory(), "Source file can't be a directory");

        //first thing we parse the yaml file into java maps
        SlangFile slangFile = yamlParser.loadSlangFile(source);

        //than we transform those maps to model objects
        Executable executable = transformToExecutable(operationName, slangFile);

        Map<String, Executable> filteredDependencies = new HashMap<>();
        //we handle dependencies only if the file has imports
        boolean hasDependencies = MapUtils.isNotEmpty(slangFile.getImports())
                && executable.getType().equals(SlangTextualKeys.FLOW_TYPE);
        if (hasDependencies) {
            Validate.noNullElements(path, "File that was requested to compile has imports but no path was given");

            //we transform also all of the files in the given path to model objects
            Map<String, Executable> pathExecutables = transformDependencies(path);

            //we add the current executable since a dependency can require it
            List<Executable> availableExecutables = new ArrayList<>(pathExecutables.values());
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
     * Transforms all of the slang files in the given path to {@link org.openscore.lang.compiler.model.Executable}
     *
     * @param path the path
     * @return a map of {@link org.openscore.lang.compiler.model.Executable} with their ids as key
     */
    private Map<String, Executable> transformDependencies(Set<File> path) {
        //we filter only the slang files from the path
        List<File> filteredPath = dependenciesHelper.fetchSlangFiles(path);

        //we transform and add all of the dependencies to a list of executable
        List<Executable> executables = new ArrayList<>();
        for (File file : filteredPath) {
            SlangFile slangFile = yamlParser.loadSlangFile(file);
            switch (slangFile.getType()) {
                case FLOW:
                    executables.add(transformFlow(slangFile));
                    break;
                case OPERATIONS:
                    executables.addAll(transformOperations(slangFile));
                    break;
                default:
                    throw new RuntimeException("File: " + file.getName() + " is not of flow type or operations");
            }
        }

        //we put the dependencies in a map with their id as key
        Map<String, Executable> compiledExecutableMap = new HashMap<>();
        for (Executable executable : executables) {
            compiledExecutableMap.put(executable.getId(), executable);
        }
        return compiledExecutableMap;
    }

    /**
     * Utility method that cast a {@link org.openscore.lang.compiler.model.Executable} to its subtype
     * and create an {@link org.openscore.api.ExecutionPlan} for it
     *
     * @param executable the executable to create an {@link org.openscore.api.ExecutionPlan} for
     * @return {@link org.openscore.api.ExecutionPlan} of the given {@link org.openscore.lang.compiler.model.Executable}
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

    /**
     * Utility method that transform a {@link org.openscore.lang.compiler.model.SlangFile}
     * into a {@link org.openscore.lang.compiler.model.Executable}
     * also handles operations files
     *
     * @param operationName the name of the operation to transform from the {@link org.openscore.lang.compiler.model.SlangFile}
     * @param slangFile     the file to transform
     * @return {@link org.openscore.lang.compiler.model.Executable}  of the requested flow/operation
     */
    private Executable transformToExecutable(String operationName, SlangFile slangFile) {
        Executable executable;

        switch (slangFile.getType()) {
            case OPERATIONS:
                Validate.notEmpty(operationName, "File: " + slangFile.getFileName() + " is operations file " +
                        "you must specify the operation name requested for compiling");
                List<Executable> compilesOperations = transformOperations(slangFile);
                // match the requested operation from all the operations in the file
                executable = Lambda.selectFirst(compilesOperations, having(on(Executable.class).getName(), equalTo(operationName)));
                if (executable == null) {
                    throw new RuntimeException("Operation with name: " + operationName + " wasn't found in file: " + slangFile.getFileName());
                }
                break;
            case FLOW:
                executable = transformFlow(slangFile);
                break;
            default:
                throw new RuntimeException("File: " + slangFile.getFileName() + " is not of flow type or operations");
        }
        return executable;
    }

    /**
     * transform an operations {@link org.openscore.lang.compiler.model.SlangFile} to a List of {@link org.openscore.lang.compiler.model.Executable}
     *
     * @param slangFile the file to transform the operations from
     * @return List of {@link org.openscore.lang.compiler.model.Executable} representing the operations in the file
     */
    private List<Executable> transformOperations(SlangFile slangFile) {
        List<Executable> executables = new ArrayList<>();
        for (Map<String, Map<String, Object>> operation : slangFile.getOperations()) {
            Map.Entry<String, Map<String, Object>> entry = operation.entrySet().iterator().next();
            String operationName = entry.getKey();
            Map<String, Object> operationRawData = entry.getValue();
            executables.add(executableBuilder.transformToExecutable(slangFile, operationName, operationRawData));
        }
        return executables;
    }

    /**
     * transform an flow {@link org.openscore.lang.compiler.model.SlangFile} to a {@link org.openscore.lang.compiler.model.Executable}
     *
     * @param slangFile the file to transform the flow from
     * @return {@link org.openscore.lang.compiler.model.Executable} representing the flow in the file
     */
    private Executable transformFlow(SlangFile slangFile) {
        Map<String, Object> flowRawData = slangFile.getFlow();
        String flowName = (String) flowRawData.get(SlangTextualKeys.FLOW_NAME_KEY);
        if (StringUtils.isBlank(flowName)) {
            throw new RuntimeException("Flow in file: " + slangFile.getFileName() + "have no name");
        }
        return executableBuilder.transformToExecutable(slangFile, flowName, flowRawData);
    }
}
