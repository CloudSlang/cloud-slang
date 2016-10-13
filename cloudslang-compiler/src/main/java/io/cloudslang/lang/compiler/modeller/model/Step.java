/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.model;

import io.cloudslang.lang.entities.bindings.Argument;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/*
 * Created by orius123 on 06/11/14.
 */
public class Step {

    private final String name;
    private final Map<String, Serializable> preStepActionData;
    private final Map<String, Serializable> postStepActionData;
    private final List<Argument> arguments;
    private final List<Map<String, String>> navigationStrings;
    private final String refId;
    private final boolean parallelLoop;
    private final boolean onFailureStep;

    public Step(
            String name,
            Map<String, Serializable> preStepActionData,
            Map<String, Serializable> postStepActionData,
            List<Argument> arguments,
            List<Map<String, String>> navigationStrings,
            String refId,
            boolean parallelLoop,
            boolean onFailureStep) {
        this.name = name;
        this.preStepActionData = preStepActionData;
        this.postStepActionData = postStepActionData;
        this.arguments = arguments;
        this.navigationStrings = navigationStrings;
        this.refId = refId;
        this.parallelLoop = parallelLoop;
        this.onFailureStep = onFailureStep;
    }

    public String getName() {
        return name;
    }

    public Map<String, Serializable> getPreStepActionData() {
        return preStepActionData;
    }

    public Map<String, Serializable> getPostStepActionData() {
        return postStepActionData;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public List<Map<String, String>> getNavigationStrings() {
        return navigationStrings;
    }

    public String getRefId() {
        return refId;
    }

    public boolean isParallelLoop() {
        return parallelLoop;
    }

    public boolean isOnFailureStep() {
        return onFailureStep;
    }

}
