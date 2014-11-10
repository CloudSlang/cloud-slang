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
import com.hp.score.api.TriggeringProperties;
import com.hp.score.lang.compiler.domain.SlangFile;
import com.hp.score.lang.compiler.utils.ExecutableBuilder;
import com.hp.score.lang.compiler.utils.NamespaceBuilder;
import com.hp.score.lang.compiler.utils.YamlParser;
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
    private YamlParser yamlParser;

    public TriggeringProperties compile(File source, String operationName, List<File> path) {

        SlangFile slangFile = yamlParser.loadSlangFile(source);

        Map<String, ExecutionPlan> dependencies = null;
        TreeMap<String, List<SlangFile>> dependenciesByNamespace = null;
        if (slangFile.getImports() != null) {
            Validate.noNullElements(path, "File that was requested to compile has imports but no path was given");
            dependenciesByNamespace = namespaceBuilder.buildNamespace(path, slangFile);
            dependencies = compileDependencies(dependenciesByNamespace);
        }

        ExecutionPlan executionPlan;

        switch (slangFile.getType()) {
            case OPERATIONS:
                Validate.notEmpty(operationName, "When compiling an operation you must specify its name");
                List<ExecutionPlan> operationsExecutionPlans = compileOperations(slangFile.getOperations(), dependenciesByNamespace);
                executionPlan = Lambda.selectFirst(operationsExecutionPlans, having(on(ExecutionPlan.class).getName(), equalTo(operationName)));
                break;
            case FLOW:
                executionPlan = compileFlow(slangFile.getFlow(), dependenciesByNamespace);
                break;
            default:
                throw new RuntimeException("Nothing to compile");

        }
        executionPlan.setFlowUuid(getFlowUuid(slangFile, executionPlan));
        return TriggeringProperties.create(executionPlan)
                .setDependencies(dependencies);
    }


    private Map<String, ExecutionPlan> compileDependencies(TreeMap<String, List<SlangFile>> dependencies) {
        Map<String, ExecutionPlan> compiledDependencies = new HashMap<>();
        for (Map.Entry<String, List<SlangFile>> entry : dependencies.entrySet()) {
            for (SlangFile slangFile : entry.getValue()) {
                switch (slangFile.getType()) {
                    case FLOW:
                        ExecutionPlan flowExecutionPlan = compileFlow(slangFile.getFlow(), dependencies);
                        flowExecutionPlan.setFlowUuid(getFlowUuid(slangFile, flowExecutionPlan));
                        compiledDependencies.put(flowExecutionPlan.getFlowUuid(), flowExecutionPlan);
                        break;
                    case OPERATIONS:
                        for (ExecutionPlan operationExecutionPlan : compileOperations(slangFile.getOperations(), dependencies)) {
                            operationExecutionPlan.setFlowUuid(getFlowUuid(slangFile, operationExecutionPlan));
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

    private List<ExecutionPlan> compileOperations(List<Map<String, Map<String, Object>>> operationsRawData, TreeMap<String, List<SlangFile>> dependenciesByNamespace) {
        List<ExecutionPlan> executionPlans = new ArrayList<>();
        for (Map<String, Map<String, Object>> operation : operationsRawData) {
            Map.Entry<String, Map<String, Object>> entry = operation.entrySet().iterator().next();
            ExecutionPlan executionPlan = executableBuilder.compileExecutable(entry.getValue(), dependenciesByNamespace, SlangFile.Type.OPERATIONS);
            executionPlan.setName(entry.getKey());
            executionPlans.add(executionPlan);
        }
        return executionPlans;
    }

    private ExecutionPlan compileFlow(Map<String, Object> flowRawData, TreeMap<String, List<SlangFile>> dependenciesByNamespace) {
        String flowName = (String) flowRawData.remove(SlangTextualKeys.FLOW_NAME_KEY);
        ExecutionPlan executionPlan = executableBuilder.compileExecutable(flowRawData, dependenciesByNamespace, SlangFile.Type.FLOW);
        executionPlan.setName(flowName);
        return executionPlan;
    }

    private String getFlowUuid(SlangFile slangFile, ExecutionPlan executionPlan) {
        return slangFile.getNamespace() + "." + executionPlan.getName();
    }


}
