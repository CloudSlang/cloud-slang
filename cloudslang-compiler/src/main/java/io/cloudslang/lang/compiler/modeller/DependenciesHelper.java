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
import io.cloudslang.lang.compiler.modeller.model.Task;
import io.cloudslang.lang.compiler.modeller.transformers.AggregateTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.PublishTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.Transformer;
import io.cloudslang.lang.entities.bindings.InOutParam;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

@Component
public class DependenciesHelper {

    @Autowired
    private PublishTransformer publishTransformer;

    @Autowired
    private AggregateTransformer aggregateTransformer;

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
        for (String refId : executable.getExecutableDependencies()) {
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

    public Set<String> getSystemPropertiesForFlow(
            List<Input> inputs,
            List<Output> outputs,
            List<Result> results,
            Deque<Task> tasks) {
        Set<String> result = new HashSet<>();
        result.addAll(getSystemPropertiesFromExecutable(inputs, outputs, results));
        for (Task task : tasks) {
            result.addAll(getSystemPropertiesFromTask(task));
        }
        return result;
    }

    public Set<String> getSystemPropertiesForOperation(
            List<Input> inputs,
            List<Output> outputs,
            List<Result> results) {
        return getSystemPropertiesFromExecutable(inputs, outputs, results);
    }

    private Set<String> getSystemPropertiesFromExecutable(
            List<Input> inputs,
            List<Output> outputs,
            List<Result> results) {
        Set<String> result = new HashSet<>();
        result.addAll(getSystemPropertiesFromInOutParam(inputs));
        result.addAll(getSystemPropertiesFromInOutParam(outputs));
        result.addAll(getSystemPropertiesFromInOutParam(results));
        return result;
    }

    private Set<String> getSystemPropertiesFromTask(Task task) {
        Set<String> result = new HashSet<>();
        List<Transformer> relevantTransformers = new ArrayList<>();
        relevantTransformers.add(publishTransformer);
        relevantTransformers.add(aggregateTransformer);

        result.addAll(getSystemPropertiesFromInOutParam(task.getArguments()));
        result.addAll(
                getSystemPropertiesFromPostTaskActionData(
                        task.getPostTaskActionData(),
                        relevantTransformers
                )
        );

        return result;
    }

    private Set<String> getSystemPropertiesFromInOutParam(List<? extends InOutParam> inOutParams) {
        Set<String> result = new HashSet<>();
        if (inOutParams != null) {
            for(InOutParam inOutParam : inOutParams) {
                Set<String> systemPropertyDependencies = inOutParam.getSystemPropertyDependencies();
                if(CollectionUtils.isNotEmpty(systemPropertyDependencies)) {
                    result.addAll(systemPropertyDependencies);
                }
            }
        }
        return result;
    }

    private Set<String> getSystemPropertiesFromPostTaskActionData(
            Map<String, Serializable> postTaskActionData,
            List<Transformer> relevantTransformers) {
        Set<String> result = new HashSet<>();
        for (Transformer transformer : relevantTransformers) {
            String key = TransformersHandler.keyToTransform(transformer);
            Serializable item = postTaskActionData.get(key);
            if (item instanceof Collection) {
                Collection itemsCollection = (Collection) item;
                for (Object itemAsObject : itemsCollection) {
                    if (itemAsObject instanceof Output) {
                        Output itemAsOutput = (Output) itemAsObject;
                        result.addAll(itemAsOutput.getSystemPropertyDependencies());
                    } else {
                        throw new RuntimeException("Incorrect type for post task data items.");
                    }
                }
            } else {
                throw new RuntimeException("Incorrect type for post task data items.");
            }
        }
        return result;
    }

}
