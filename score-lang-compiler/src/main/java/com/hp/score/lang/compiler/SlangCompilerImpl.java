package com.hp.score.lang.compiler;
/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/

/*
 * Created by orius123 on 05/11/14.
 */

import ch.lambdaj.Lambda;
import ch.lambdaj.function.convert.Converter;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.lang.compiler.domain.CompiledExecutable;
import com.hp.score.lang.compiler.domain.CompiledFlow;
import com.hp.score.lang.compiler.domain.CompiledOperation;
import com.hp.score.lang.compiler.domain.SlangFile;
import com.hp.score.lang.compiler.utils.DependenciesHelper;
import com.hp.score.lang.compiler.utils.ExecutableBuilder;
import com.hp.score.lang.compiler.utils.ExecutionPlanBuilder;
import com.hp.score.lang.compiler.utils.YamlParser;
import com.hp.score.lang.entities.CompilationArtifact;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

import static ch.lambdaj.Lambda.convertMap;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

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
        CompiledExecutable executable = transformToExecutable(operationName, slangFile);

        Map<String, CompiledExecutable> filteredDependencies = new HashMap<>();
        //we handle dependencies only if the file has imports
        boolean hasDependencies = MapUtils.isNotEmpty(slangFile.getImports())
                && executable.getType().equals(SlangTextualKeys.FLOW_TYPE);
        if (hasDependencies) {
            Validate.noNullElements(path, "File that was requested to compile has imports but no path was given");

            //we transform also all of the files in the given path to model objects
            Map<String, CompiledExecutable> pathExecutables = transformDependencies(path);

            //we add the current executable since a dependency can require it
            List<CompiledExecutable> availableExecutables = new ArrayList<>(pathExecutables.values());
            availableExecutables.add(executable);

            //than we match the references to the actual dependencies
            filteredDependencies = dependenciesHelper.matchReferences(executable, availableExecutables);
        }

        //next we create an execution plan for the required executable
        ExecutionPlan executionPlan = compileToExecutionPlan(executable);

        //and also create execution plans for all other dependencies
        Map<String, ExecutionPlan> dependencies = convertMap(filteredDependencies, new Converter<CompiledExecutable, ExecutionPlan>() {
            @Override
            public ExecutionPlan convert(CompiledExecutable compiledExecutable) {
                return compileToExecutionPlan(compiledExecutable);
            }
        });

        return new CompilationArtifact(executionPlan, dependencies, executable.getInputs());
    }

    /**
     * Transforms all of the slang files in the given path to {@link com.hp.score.lang.compiler.domain.CompiledExecutable}
     *
     * @param path the path
     * @return a map of {@link com.hp.score.lang.compiler.domain.CompiledExecutable} with their ids as key
     */
    private Map<String, CompiledExecutable> transformDependencies(Set<File> path) {
        //we filter only the slang files from the path
        List<File> filteredPath = dependenciesHelper.fetchSlangFiles(path);

        //we transform and add all of the dependencies to a list of executable
        List<CompiledExecutable> executables = new ArrayList<>();
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
        Map<String, CompiledExecutable> compiledExecutableMap = new HashMap<>();
        for (CompiledExecutable executable : executables) {
            compiledExecutableMap.put(executable.getId(), executable);
        }
        return compiledExecutableMap;
    }

    /**
     * Utility method that cast a {@link com.hp.score.lang.compiler.domain.CompiledExecutable} to its subtype
     * and create an {@link com.hp.score.api.ExecutionPlan} for it
     *
     * @param executable the executable to create an {@link com.hp.score.api.ExecutionPlan} for
     * @return {@link com.hp.score.api.ExecutionPlan} of the given {@link com.hp.score.lang.compiler.domain.CompiledExecutable}
     */
    private ExecutionPlan compileToExecutionPlan(CompiledExecutable executable) {
        ExecutionPlan executionPlan;

        if (executable.getType().equals(SlangTextualKeys.OPERATION_TYPE)) {
            executionPlan = executionPlanBuilder.createOperationExecutionPlan((CompiledOperation) executable);
        } else if (executable.getType().equals(SlangTextualKeys.FLOW_TYPE)) {
            executionPlan = executionPlanBuilder.createFlowExecutionPlan((CompiledFlow) executable);
        } else {
            throw new RuntimeException("Executable: " + executable.getName() + " is not a flow and not an operation");
        }
        return executionPlan;
    }

    /**
     * Utility method that transform a {@link com.hp.score.lang.compiler.domain.SlangFile}
     * into a {@link com.hp.score.lang.compiler.domain.CompiledExecutable}
     * also handles operations files
     *
     * @param operationName the name of the operation to transform from the {@link com.hp.score.lang.compiler.domain.SlangFile}
     * @param slangFile     the file to transform
     * @return {@link com.hp.score.lang.compiler.domain.CompiledExecutable}  of the requested flow/operation
     */
    private CompiledExecutable transformToExecutable(String operationName, SlangFile slangFile) {
        CompiledExecutable executable;

        switch (slangFile.getType()) {
            case OPERATIONS:
                Validate.notEmpty(operationName, "File: " + slangFile.getFileName() + " is operations file " +
                        "you must specify the operation name requested for compiling");
                List<CompiledExecutable> compilesOperations = transformOperations(slangFile);
                // match the requested operation from all the operations in the file
                executable = Lambda.selectFirst(compilesOperations, having(on(CompiledExecutable.class).getName(), equalTo(operationName)));
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
     * transform an operations {@link com.hp.score.lang.compiler.domain.SlangFile} to a List of {@link com.hp.score.lang.compiler.domain.CompiledExecutable}
     *
     * @param slangFile the file to transform the operations from
     * @return List of {@link com.hp.score.lang.compiler.domain.CompiledExecutable} representing the operations in the file
     */
    private List<CompiledExecutable> transformOperations(SlangFile slangFile) {
        List<CompiledExecutable> executables = new ArrayList<>();
        for (Map<String, Map<String, Object>> operation : slangFile.getOperations()) {
            Map.Entry<String, Map<String, Object>> entry = operation.entrySet().iterator().next();
            String operationName = entry.getKey();
            Map<String, Object> operationRawData = entry.getValue();
            executables.add(executableBuilder.transformToExecutable(slangFile, operationName, operationRawData));
        }
        return executables;
    }

    /**
     * transform an flow {@link com.hp.score.lang.compiler.domain.SlangFile} to a {@link com.hp.score.lang.compiler.domain.CompiledExecutable}
     *
     * @param slangFile the file to transform the flow from
     * @return {@link com.hp.score.lang.compiler.domain.CompiledExecutable} representing the flow in the file
     */
    private CompiledExecutable transformFlow(SlangFile slangFile) {
        Map<String, Object> flowRawData = slangFile.getFlow();
        String flowName = (String) flowRawData.get(SlangTextualKeys.FLOW_NAME_KEY);
        if (StringUtils.isBlank(flowName)) {
            throw new RuntimeException("Flow in file: " + slangFile.getFileName() + "have no name");
        }
        return executableBuilder.transformToExecutable(slangFile, flowName, flowRawData);
    }
}
