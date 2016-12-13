/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller;

/*
 * Created by orius123 on 05/11/14.
 */

import ch.lambdaj.Lambda;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Step;
import io.cloudslang.lang.compiler.modeller.transformers.PublishTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.Transformer;
import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.entities.bindings.InOutParam;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.entities.constants.Messages;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.Validate;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.selectFirst;
import static org.hamcrest.Matchers.equalTo;

public class DependenciesHelper {

    private PublishTransformer publishTransformer;

    public Set<String> fetchDependencies(Executable executable, Map<String, Executable> availableDependencies) {
        Validate.notNull(executable);
        Validate.notNull(availableDependencies);

        switch (executable.getType()) {
            case SlangTextualKeys.OPERATION_TYPE:
                return new HashSet<>();
            case SlangTextualKeys.DECISION_TYPE:
                return new HashSet<>();
            case SlangTextualKeys.FLOW_TYPE:
                return processFlowForDependencies((Flow) executable, availableDependencies);
            default:
                throw new NotImplementedException(Messages.UNKNOWN_EXECUTABLE_TYPE);
        }
    }

    private Set<String> processFlowForDependencies(Flow flow, Map<String, Executable> availableDependencies) {
        Set<String> flowDependencies = new HashSet<>();
        for (Step step : flow.getWorkflow().getSteps()) {
            String stepReferenceId = step.getRefId();
            Executable stepReference = availableDependencies.get(stepReferenceId);

            flowDependencies.add(stepReferenceId);
            flowDependencies.addAll(fetchDependencies(stepReference, availableDependencies));
        }
        return flowDependencies;
    }

    /**
     * recursive matches executables with their references
     *
     * @param availableDependencies the executables to match from
     * @return a map of a the executables that were successfully matched
     */
    public Map<String, Executable> matchReferences(Executable executable,
                                                   Collection<Executable> availableDependencies) {
        Validate.isTrue(executable.getType().equals(SlangTextualKeys.FLOW_TYPE),
                "Executable: \'" + executable.getId() + "\' is not a flow, therefore it has no references");
        Map<String, Executable> resolvedDependencies = new HashMap<>();
        return fetchFlowReferences(executable, availableDependencies, resolvedDependencies);
    }

    private Map<String, Executable> fetchFlowReferences(Executable executable,
                                                        Collection<Executable> availableDependencies,
                                                        Map<String, Executable> resolvedDependencies) {
        for (String refId : executable.getExecutableDependencies()) {
            //if it is already in the references we do nothing
            if (resolvedDependencies.get(refId) == null) {
                Executable matchingRef = selectFirst(availableDependencies,
                        having(on(Executable.class).getId(), equalTo(refId)));
                if (matchingRef == null) {
                    throw new RuntimeException("Reference: \'" + refId + "\' in executable: \'" +
                            executable.getName() + "\', wasn't found in path");
                }

                //first we put the reference on the map
                resolvedDependencies.put(matchingRef.getId(), matchingRef);
                if (matchingRef.getType().equals(SlangTextualKeys.FLOW_TYPE)) {
                    //if it is a flow  we recursively
                    resolvedDependencies
                            .putAll(fetchFlowReferences(matchingRef, availableDependencies, resolvedDependencies));
                }
            }
        }
        return resolvedDependencies;
    }

    public Set<String> getSystemPropertiesForFlow(
            List<Input> inputs,
            List<Output> outputs,
            List<Result> results,
            Deque<Step> steps) {
        Set<String> result = new HashSet<>();
        result.addAll(getSystemPropertiesFromExecutable(inputs, outputs, results));
        for (Step step : steps) {
            result.addAll(getSystemPropertiesFromStep(step));
        }
        return result;
    }

    public Set<String> getSystemPropertiesForOperation(
            List<Input> inputs,
            List<Output> outputs,
            List<Result> results) {
        return getSystemPropertiesFromExecutable(inputs, outputs, results);
    }

    public Set<String> getSystemPropertiesForDecision(
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

    private Set<String> getSystemPropertiesFromStep(Step step) {
        Set<String> result = new HashSet<>();
        List<Transformer> relevantTransformers = new ArrayList<>();
        relevantTransformers.add(publishTransformer);

        result.addAll(getSystemPropertiesFromLoopStatement(step.getPreStepActionData()));
        result.addAll(getSystemPropertiesFromInOutParam(step.getArguments()));
        result.addAll(
                getSystemPropertiesFromPostStepActionData(
                        step.getPostStepActionData(),
                        relevantTransformers,
                        step.getName()
                )
        );

        return result;
    }

    private Set<String> getSystemPropertiesFromLoopStatement(Map<String, Serializable> preStepActionData) {
        Set<String> result = new HashSet<>();
        for (Map.Entry<String, Serializable> entry : preStepActionData.entrySet()) {
            if (entry.getValue() instanceof LoopStatement) {
                result.addAll(((LoopStatement) entry.getValue()).getSystemPropertyDependencies());
            }
        }
        return result;
    }

    private Set<String> getSystemPropertiesFromInOutParam(List<? extends InOutParam> inOutParams) {
        Set<String> result = new HashSet<>();
        if (inOutParams != null) {
            for (InOutParam inOutParam : inOutParams) {
                Set<String> systemPropertyDependencies = inOutParam.getSystemPropertyDependencies();
                if (CollectionUtils.isNotEmpty(systemPropertyDependencies)) {
                    result.addAll(systemPropertyDependencies);
                }
            }
        }
        return result;
    }

    private Set<String> getSystemPropertiesFromPostStepActionData(
            Map<String, Serializable> postStepActionData,
            List<Transformer> relevantTransformers,
            String stepName) {
        Set<String> result = new HashSet<>();
        for (Transformer transformer : relevantTransformers) {
            String key = TransformersHandler.keyToTransform(transformer);
            Serializable item = postStepActionData.get(key);
            if (item instanceof Collection) {
                Collection itemsCollection = (Collection) item;
                for (Object itemAsObject : itemsCollection) {
                    if (itemAsObject instanceof Output) {
                        Output itemAsOutput = (Output) itemAsObject;
                        result.addAll(itemAsOutput.getSystemPropertyDependencies());
                    } else {
                        throw new RuntimeException("For step: " + stepName +
                                " - Incorrect type for post step data items.");
                    }
                }
            } else {
                throw new RuntimeException("For step: " + stepName + " - Incorrect type for post step data items.");
            }
        }
        return result;
    }

    public void setPublishTransformer(PublishTransformer publishTransformer) {
        this.publishTransformer = publishTransformer;
    }
}
