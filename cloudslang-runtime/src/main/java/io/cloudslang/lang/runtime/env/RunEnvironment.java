/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.lang.runtime.env;

import com.hp.oo.sdk.content.plugin.SerializableSessionObject;
import io.cloudslang.lang.entities.SystemProperty;
import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: stoneo
 * Date: 20/10/2014
 * Time: 10:28
 */
public class RunEnvironment implements Serializable{

    // Call arguments for the current step
    private Map<String, Serializable> callArguments;

    // Return values from the current step
    private ReturnValues returnValues;

    // The position of the next step
    private Long nextStepPosition;

    // Stack holding the contexts of the parent scopes
    private ContextStack contextStack;

    // Stack of the parent flow's data (for the sub-flow use-case)
    private ParentFlowStack parentFlowStack;

    private ExecutionPath executionPath;

    private final Set<SystemProperty> systemProperties;

    // Map holding serializable data that is common for the entire run
    // This is data that should be shared between different actions with the ability to change the data
    private Map<String, SerializableSessionObject> serializableDataMap;


    public RunEnvironment(Set<SystemProperty> systemProperties) {
        Validate.notNull(systemProperties, "system properties cannot be null");
        contextStack = new ContextStack();
        parentFlowStack = new ParentFlowStack();
        callArguments = new HashMap<>();
        executionPath = new ExecutionPath();
        serializableDataMap = new HashMap<>();
        this.systemProperties = systemProperties;
    }

    public RunEnvironment() {
        this(new HashSet<SystemProperty>());
    }

    public ContextStack getStack(){
        return contextStack;
    }

    public ParentFlowStack getParentFlowStack() {
        return parentFlowStack;
    }

    public Map<String, Serializable> removeCallArguments() {
        Map<String, Serializable> callArgumentsValues = callArguments;
        callArguments = new HashMap<>();
        return callArgumentsValues;
    }

    public void putCallArguments(Map<String, Serializable> callArguments) {
        this.callArguments.putAll(callArguments);
    }

    public ReturnValues removeReturnValues() {
        ReturnValues values = returnValues;
        returnValues = null;
        return values;
    }

    public void putReturnValues(ReturnValues returnValues) {
        this.returnValues = returnValues;
    }

    public Long removeNextStepPosition() {
        Long nextStep = nextStepPosition;
        nextStepPosition = null;
        return nextStep;
    }

    public void putNextStepPosition(Long nextStepPosition) {
        this.nextStepPosition = nextStepPosition;
    }

    public ExecutionPath getExecutionPath() {
        return this.executionPath;
    }

    public Set<SystemProperty> getSystemProperties() {
        return systemProperties;
    }

    public Map<String, SerializableSessionObject> getSerializableDataMap() {
        return serializableDataMap;
    }

    public void resetStacks() {
        contextStack = new ContextStack();
        parentFlowStack = new ParentFlowStack();
    }

}
