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

import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.TriggeringProperties;
import com.hp.score.lang.compiler.domain.SlangFile;
import com.hp.score.lang.compiler.utils.ExecutableBuilder;
import com.hp.score.lang.compiler.utils.NamespaceBuilder;
import com.hp.score.lang.compiler.utils.YamlParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Component
public class SlangCompiler {

    @Autowired
    private ExecutableBuilder executableBuilder;

    @Autowired
    private NamespaceBuilder namespaceBuilder;

    @Autowired
    private YamlParser yamlParser;

    public TriggeringProperties compileWithDependencies(File source, List<File> classpath){
        TriggeringProperties triggeringProperties = TriggeringProperties.create(new ExecutionPlan());
        triggeringProperties.setDependencies(new HashMap<String, ExecutionPlan>());
        return triggeringProperties;
    }


    public ExecutionPlan compile(File source, List<File> classpath) {

        SlangFile slangFile = yamlParser.loadMomaFile(source);

        Map<String, ExecutionPlan> dependencies = handleDependencies(classpath, slangFile);

        switch (slangFile.getType()) {
            case OPERATIONS:
                List<ExecutionPlan> operationsExecutionPlans = compileOperations(slangFile.getOperations(), dependencies);
                //todo for now we get(0) (the first) operation always so we are able to compile one op, should be changed to get the op by name.
                ExecutionPlan executionPlan = operationsExecutionPlans.get(0);
                executionPlan.setFlowUuid(slangFile.getNamespace() + "." + executionPlan.getName());
                return executionPlan;
            case FLOW:
                return compileFlow(slangFile.getFlow(), dependencies);
            default:
                throw new RuntimeException("Nothing to compile");
        }
    }

    private Map<String, ExecutionPlan> handleDependencies(List<File> classpath, SlangFile slangFile) {
//        first we build a map of all the relevant files we got in the classpath sorted by their namespace
        TreeMap<String, File> namespaces = new TreeMap<>();
        if (classpath != null) {
            List<File> filterClassPath = namespaceBuilder.filterClassPath(classpath);
            namespaces = namespaceBuilder.sortByNameSpace(filterClassPath);
        }

//        then we filter the files that their namespace was not imported
        Map<String, ExecutionPlan> dependencies = new HashMap<>();
        if (slangFile.getImports() != null) {
            TreeMap<String, File> importsFiles = namespaceBuilder.filterNonImportedFiles(namespaces, slangFile.getImports());
            dependencies = compileDependencies(importsFiles, classpath);
            //todo cyclic dependencies
        }
        return dependencies;
    }

    private TreeMap<String, ExecutionPlan> compileDependencies(TreeMap<String, File> dependencies, List<File> classpath) {
        TreeMap<String, ExecutionPlan> compiledDependencies = new TreeMap<>();
        for (Map.Entry<String, File> entry : dependencies.entrySet()) {
            //todo another hack...... for operation support.....
            if (entry.getValue() != null && !entry.getValue().getName().contains(SlangTextualKeys.OPERATIONS_KEY)) {
                ExecutionPlan executionPlan = compile(entry.getValue(), classpath);
                compiledDependencies.put(entry.getKey(), executionPlan);
            }
        }

        return compiledDependencies;
    }

    private List<ExecutionPlan> compileOperations(List<Map> operationsRawData, Map<String, ExecutionPlan> dependencies) {
        List<ExecutionPlan> executionPlans = new ArrayList<>();
        for (Map operation : operationsRawData) {
            Map.Entry<String, Map> entry = (Map.Entry<String, Map>) operation.entrySet().iterator().next();
            ExecutionPlan executionPlan = executableBuilder.compileExecutable(entry.getValue(), dependencies, SlangFile.Type.OPERATIONS);
            executionPlan.setName(entry.getKey());
            executionPlans.add(executionPlan);
        }
        return executionPlans;
    }

    private ExecutionPlan compileFlow(Map<String, Object> flowRawData, Map<String, ExecutionPlan> dependencies) {
        String flowName = (String) flowRawData.remove(SlangTextualKeys.FLOW_NAME_KEY);
        ExecutionPlan executionPlan = executableBuilder.compileExecutable(flowRawData, dependencies, SlangFile.Type.FLOW);
        executionPlan.setName(flowName);
        return executionPlan;
    }


}
