/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package io.cloudslang.lang.compiler.modeller;

/*
 * Created by orius123 on 05/11/14.
 */

import ch.lambdaj.Lambda;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Task;
import io.cloudslang.lang.compiler.modeller.model.TaskType;
import org.apache.commons.lang.Validate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

@Component
public class DependenciesHelper {
    /**
     * recursive matches executables with their references
     *
     * @param availableDependencies the executables to match from
     * @return a map of a the executables that were successfully matched
     */
    public Map<String, Executable> matchReferences(Executable executable, Collection<Executable> availableDependencies) {
        Validate.isTrue(executable.getType().equals(SlangTextualKeys.FLOW_TYPE), "Executable: \'" + executable.getId() + "\' is not a flow, therefore it has no references");
        Map<String, Executable> resolvedDependencies = new HashMap<>();
        return fetchFlowReferences(executable, availableDependencies, resolvedDependencies);
    }

    private Map<String, Executable> fetchFlowReferences(Executable executable,
                                                                Collection<Executable> availableDependencies,
                                                                Map<String, Executable> resolvedDependencies) {
        for (String refId : executable.getDependencies()) {
            //if it is already in the references we do nothing
            if (resolvedDependencies.get(refId) == null) {
                Executable matchingRef = Lambda.selectFirst(availableDependencies, having(on(Executable.class).getId(), equalTo(refId)));
                if (matchingRef == null) {
                    throw new RuntimeException("Reference: \'" + refId + "\' in executable: \'"
                            + executable.getName() + "\', wasn't found in path");
                }

                //first we put the reference on the map
                resolvedDependencies.put(matchingRef.getId(), matchingRef);
                if (matchingRef.getType().equals(SlangTextualKeys.FLOW_TYPE)) {
                    //if it is a flow  we recursively
                    resolvedDependencies.putAll(fetchFlowReferences(matchingRef, availableDependencies, resolvedDependencies));
                }
            }
        }
        return resolvedDependencies;
    }

    public void resolveTasksType(Executable executable, List<Executable> availableExecutables) {

        Map<String,Executable> executableByRefId = referenceByExecutableId(executable, availableExecutables);
        resolveTasksType(executable,executableByRefId);
        for (Executable executable1 : availableExecutables){
            resolveTasksType(executable1,executableByRefId);
        }
    }

    public void resolveTasksType(Executable executable, Map<String,Executable> executableByRefId) {
        if(executable.getType().equals(SlangTextualKeys.FLOW_TYPE)){
            Flow executableAsFlow = (Flow) executable;
            for(Task task : executableAsFlow.getWorkflow().getTasks()){
                if(executableByRefId.get(task.getRefId()).getType().equals(SlangTextualKeys.FLOW_TYPE)){
                    task.setType(TaskType.SUBFLOW);
                }else if(executableByRefId.get(task.getRefId()).getType().equals(SlangTextualKeys.OPERATION_TYPE)){
                    task.setType(TaskType.OPERATION);
                }
            }
        }
    }

    private Map<String,Executable> referenceByExecutableId(Executable executable, List<Executable> availableExecutables) {
        Map<String,Executable> executableByRefId = new HashMap<>(availableExecutables.size()+1);
        executableByRefId.put(executable.getId(), executable);
        for (Executable executable1 : availableExecutables){
            executableByRefId.put(executable1.getId(), executable1);
        }

        return executableByRefId;
    }
}
