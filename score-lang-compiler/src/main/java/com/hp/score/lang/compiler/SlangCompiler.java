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
import com.hp.score.api.ExecutionPlan;
import com.hp.score.lang.compiler.domain.CompiledExecutable;
import com.hp.score.lang.compiler.domain.CompiledFlow;
import com.hp.score.lang.compiler.domain.CompiledOperation;
import com.hp.score.lang.compiler.domain.SlangFile;
import com.hp.score.lang.compiler.utils.ExecutableBuilder;
import com.hp.score.lang.compiler.utils.ExecutionPlanBuilder;
import com.hp.score.lang.compiler.utils.NamespaceBuilder;
import com.hp.score.lang.compiler.utils.YamlParser;
import com.hp.score.lang.entities.CompilationArtifact;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

@Component
public class SlangCompiler {

    @Autowired
    private ExecutableBuilder executableBuilder;

    @Autowired
    private NamespaceBuilder namespaceBuilder;

    @Autowired
    private ExecutionPlanBuilder executionPlanBuilder;

    @Autowired
    private YamlParser yamlParser;

    public CompilationArtifact compileFlow(File source, List<File> path) {
        return compile(source, null, path);
    }

    public CompilationArtifact compile(File source, String operationName, List<File> path) {

        Validate.notNull(source, "You must supply a file to compile");
        Validate.isTrue(!source.isDirectory(), "Source file can't be a directory");

        SlangFile slangFile = yamlParser.loadSlangFile(source);

        Map<String, ExecutionPlan> dependencies = null;
        TreeMap<String, List<SlangFile>> dependenciesByNamespace = null;
        if (slangFile.getImports() != null) {
            Validate.noNullElements(path, "File that was requested to compile has imports but no path was given");
            dependenciesByNamespace = namespaceBuilder.buildNamespace(path, slangFile);
            dependencies = compileDependencies(dependenciesByNamespace);
        }

        ExecutionPlan executionPlan;
        CompiledExecutable executable;

        switch (slangFile.getType()) {
            case OPERATIONS:
                Validate.notEmpty(operationName, "When compiling an operation you must specify its name");
                List<CompiledExecutable> compilesOperations = compileOperations(slangFile.getOperations(), dependenciesByNamespace);
                executable = Lambda.selectFirst(compilesOperations, having(on(CompiledExecutable.class).getName(), equalTo(operationName)));
                if (executable == null) {
                    throw new RuntimeException("Operation with name: " + operationName + " wasn't found in file: " + source.getName());
                }
                executionPlan = executionPlanBuilder.createOperationExecutionPlan((CompiledOperation) executable);
                break;
            case FLOW:
                executable = compileFlow(slangFile.getFlow(), dependenciesByNamespace);
                executionPlan = executionPlanBuilder.createFlowExecutionPlan((CompiledFlow) executable);
                break;
            default:
                throw new RuntimeException("Nothing to compile");

        }
        executionPlan.setFlowUuid(getFlowUuid(slangFile, executable));
        return new CompilationArtifact(executionPlan, dependencies, executable.getInputs());
    }


    private Map<String, ExecutionPlan> compileDependencies(TreeMap<String, List<SlangFile>> dependencies) {
        Map<String, ExecutionPlan> compiledDependencies = new HashMap<>();
        for (Map.Entry<String, List<SlangFile>> entry : dependencies.entrySet()) {
            if (entry.getValue() == null) {
                throw new RuntimeException("No dependencies were found in path for: " + entry.getKey());
            }
            for (SlangFile slangFile : entry.getValue()) {
                switch (slangFile.getType()) {
                    case FLOW:
                        CompiledFlow compiledFLow = (CompiledFlow) compileFlow(slangFile.getFlow(), dependencies);
                        ExecutionPlan flowExecutionPlan = executionPlanBuilder.createFlowExecutionPlan(compiledFLow);
                        flowExecutionPlan.setFlowUuid(getFlowUuid(slangFile, compiledFLow));
                        compiledDependencies.put(flowExecutionPlan.getFlowUuid(), flowExecutionPlan);
                        break;
                    case OPERATIONS:
                        for (CompiledExecutable compiledOperation : compileOperations(slangFile.getOperations(), dependencies)) {
                            ExecutionPlan operationExecutionPlan = executionPlanBuilder.createOperationExecutionPlan((CompiledOperation) compiledOperation);
                            operationExecutionPlan.setFlowUuid(getFlowUuid(slangFile, compiledOperation));
                            compiledDependencies.put(operationExecutionPlan.getFlowUuid(), operationExecutionPlan);
                        }
                        break;
                    default:
                        throw new RuntimeException("dependency: " + slangFile.getNamespace() + " is not a flow and not an operation");
                }
            }
        }
        return compiledDependencies;
    }

    private List<CompiledExecutable> compileOperations(List<Map<String, Map<String, Object>>> operationsRawData, TreeMap<String, List<SlangFile>> dependenciesByNamespace) {
        List<CompiledExecutable> executables = new ArrayList<>();
        for (Map<String, Map<String, Object>> operation : operationsRawData) {
            Map.Entry<String, Map<String, Object>> entry = operation.entrySet().iterator().next();
            executables.add(executableBuilder.compileExecutable(entry.getKey(), entry.getValue(), dependenciesByNamespace, SlangFile.Type.OPERATIONS));
        }
        return executables;
    }

    private CompiledExecutable compileFlow(Map<String, Object> flowRawData, TreeMap<String, List<SlangFile>> dependenciesByNamespace) {
        String flowName = (String) flowRawData.get(SlangTextualKeys.FLOW_NAME_KEY);
        return executableBuilder.compileExecutable(flowName, flowRawData, dependenciesByNamespace, SlangFile.Type.FLOW);
    }

    private String getFlowUuid(SlangFile slangFile, CompiledExecutable compiledExecutable) {
        return slangFile.getNamespace() + "." + compiledExecutable.getName();
    }


}
