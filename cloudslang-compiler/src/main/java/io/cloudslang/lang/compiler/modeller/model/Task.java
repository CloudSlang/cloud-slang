/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.compiler.modeller.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Input;

/*
 * Created by orius123 on 06/11/14.
 */
public class Task {

    private final String name;
    private final Map<String, Serializable> preTaskActionData;
    private final Map<String, Serializable> postTaskActionData;
    private final List<Input> inputs;
    private final Map<String, String> navigationStrings;
    private final String refId;
    private final boolean async;

    public Task(
            String name,
            Map<String, Serializable> preTaskActionData,
            Map<String, Serializable> postTaskActionData,
            List<Input> inputs,
            Map<String, String> navigationStrings,
            String refId,
            boolean async) {
        this.name = name;
        this.preTaskActionData = preTaskActionData;
        this.postTaskActionData = postTaskActionData;
        this.inputs = inputs;
        this.navigationStrings = navigationStrings;
        this.refId = refId;
        this.async = async;
    }

    public String getName() {
        return name;
    }

    public Map<String, Serializable> getPreTaskActionData() {
        return preTaskActionData;
    }

    public Map<String, Serializable> getPostTaskActionData() {
        return postTaskActionData;
    }

    public List<Input> getInputs() {
        return inputs;
    }

    public Map<String, String> getNavigationStrings() {
        return navigationStrings;
    }

    public String getRefId() {
        return refId;
    }

    public boolean isAsync() {
        return async;
    }

}
