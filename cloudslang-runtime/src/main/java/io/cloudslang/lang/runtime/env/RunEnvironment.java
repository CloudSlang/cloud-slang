/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.env;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.hp.oo.sdk.content.plugin.SerializableSessionObject;
import io.cloudslang.lang.entities.NavigationOptions;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.prompt.Prompt;
import io.cloudslang.lang.entities.bindings.values.SensitiveValue;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.score.api.StatefulSessionStack;
import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: stoneo
 * Date: 20/10/2014
 * Time: 10:28
 */
public class RunEnvironment implements Serializable {

    // Call arguments for the current step
    private Map<String, Value> callArguments;

    // Return values from the current step
    private ReturnValues returnValues;

    // The position of the next step
    private Long nextStepPosition;

    // Navigation options, e.g. ROI value for each navigation step
    private Map<String, List<NavigationOptions>> navigationOptions;

    // Stack holding the contexts of the parent scopes
    private ContextStack contextStack;

    // Stack of the parent flow's data (for the sub-flow use-case)
    private ParentFlowStack parentFlowStack;

    private ExecutionPath executionPath;

    private final Set<SystemProperty> systemProperties;

    // Map holding serializable data that is common for the entire run
    // This is data that should be shared between different actions with the ability to change the data
    private Map<String, SerializableSessionObject> serializableDataMap;

    private StatefulSessionStack statefulSessionStack;

    //prompt args for current step
    private Map<String, Prompt> promptArguments;

    //Map holding already prompted values
    private Map<String, Value> promptedValues;

    //flag for keeping track of the changed context
    private boolean contextModified;

    //list of the arguments that need to be rebound if context is modified
    private List<Argument> modifiedArguments;

    public RunEnvironment(Set<SystemProperty> systemProperties) {
        Validate.notNull(systemProperties, "system properties cannot be null");
        contextStack = new ContextStack();
        parentFlowStack = new ParentFlowStack();
        callArguments = new HashMap<>();
        executionPath = new ExecutionPath();
        serializableDataMap = new HashMap<>();
        statefulSessionStack = new StatefulSessionStack();
        this.systemProperties = systemProperties;
        promptArguments = new LinkedHashMap<>();
        promptedValues = new HashMap<>();
        contextModified = false;
    }

    public RunEnvironment() {
        this(new HashSet<SystemProperty>());
    }

    public ContextStack getStack() {
        return contextStack;
    }

    public ParentFlowStack getParentFlowStack() {
        return parentFlowStack;
    }

    public Map<String, Value> removeCallArguments() {
        Map<String, Value> callArgumentsValues = callArguments;
        callArguments = new HashMap<>();
        return callArgumentsValues;
    }

    public void putCallArguments(Map<String, Value> callArguments) {
        this.callArguments.putAll(callArguments);
    }

    public void putPromptArguments(Map<String, Prompt> prompts) {
        this.promptArguments.putAll(prompts);
    }

    public Map<String, Prompt> getPromptArguments() {
        return promptArguments;
    }

    public Map<String, Prompt> removePromptArguments() {
        Map<String, Prompt> promptArguments = this.promptArguments;
        this.promptArguments = new LinkedHashMap<>();
        return promptArguments;
    }

    public void keepPromptedValues(Map<String, Value> promptedValues) {
        this.promptedValues.putAll(promptedValues);
    }

    public Map<String, Value> removePromptedValues() {
        Map<String, Value> promptedValues = this.promptedValues;
        this.promptedValues = new HashMap<>();
        return promptedValues;
    }

    public Map<String, Value> getCallArguments() {
        return callArguments;
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

    public List<NavigationOptions> removeStepNavigationOptions(String key) {
        if (navigationOptions != null) {
            return navigationOptions.remove(key);
        }
        return null;
    }

    public void putStepNavigationOptions(String key, List<NavigationOptions> stepNavigationOptions) {
        if (navigationOptions == null) {
            navigationOptions = new HashMap<>();
        }
        this.navigationOptions.put(key, stepNavigationOptions);
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

    public boolean containsSensitiveData() {
        return containsSensitiveCallArgument() ||
                containsSensitiveReturnValues() ||
                containsSensitiveSystemProperties() ||
                containsSensitiveContexts();
    }

    private boolean containsSensitiveData(Collection<Value> data) {
        if (data != null) {
            for (Value value : data) {
                if (value.isSensitive()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void decryptSensitiveData() {
        for (Value value : prepareValuesForEncryptDecrypt()) {
            if (value.get() instanceof HashMap) {
                HashMap<String, Value> map = (HashMap<String, Value>) value.get();
                for (Serializable valueInMap : map.values()) {
                    if (valueInMap instanceof Value && ((Value) valueInMap).isSensitive()) {
                        ((SensitiveValue) valueInMap).decrypt();
                    }
                }
            } else if (value.isSensitive()) {
                ((SensitiveValue) value).decrypt();
            }
        }
    }

    public void encryptSensitiveData() {
        for (Value value : prepareValuesForEncryptDecrypt()) {
            if (value.get() instanceof HashMap) {
                HashMap<String, Value> map = (HashMap<String, Value>) value.get();
                for (Serializable valueInMap : map.values()) {
                    if (valueInMap instanceof Value && ((Value) valueInMap).isSensitive()) {
                        ((SensitiveValue) valueInMap).encrypt();
                    }
                }
            } else if (value.isSensitive()) {
                ((SensitiveValue) value).encrypt();
            }
        }
    }

    private boolean containsSensitiveCallArgument() {
        return callArguments != null && containsSensitiveData(callArguments.values());
    }

    private boolean containsSensitiveReturnValues() {
        return (returnValues != null) && (returnValues.getOutputs() != null) &&
                containsSensitiveData(returnValues.getOutputs().values());
    }

    private boolean containsSensitiveSystemProperties() {
        return (systemProperties != null) &&
                containsSensitiveData(Collections2.transform(systemProperties, new Function<SystemProperty, Value>() {
                    @Override
                    public Value apply(SystemProperty systemProperty) {
                        return systemProperty.getValue();
                    }
                }));
    }

    private boolean containsSensitiveContexts() {
        boolean hasSensitive = false;
        ContextStack tempStack = new ContextStack();
        Context context;
        while (!hasSensitive && (context = contextStack.popContext()) != null) {
            hasSensitive = containsSensitiveData(context.getImmutableViewOfLanguageVariables().values());
            hasSensitive = hasSensitive || containsSensitiveData(context.getImmutableViewOfVariables().values());
            tempStack.pushContext(context);
        }
        while ((context = tempStack.popContext()) != null) {
            contextStack.pushContext(context);
        }

        return hasSensitive;
    }

    private List<Value> prepareValuesForEncryptDecrypt() {
        List<Value> valuesToCheck = new LinkedList<>();
        if (callArguments != null) {
            valuesToCheck.addAll(callArguments.values());
        }
        if ((returnValues != null) && (returnValues.getOutputs() != null)) {
            valuesToCheck.addAll(returnValues.getOutputs().values());
        }
        if (systemProperties != null) {
            valuesToCheck.addAll(Collections2.transform(systemProperties, new Function<SystemProperty, Value>() {
                @Override
                public Value apply(SystemProperty systemProperty) {
                    return systemProperty.getValue();
                }
            }));
        }
        ContextStack tempStack = new ContextStack();
        Context context;
        while ((context = contextStack.popContext()) != null) {
            valuesToCheck.addAll(context.getImmutableViewOfLanguageVariables().values());
            valuesToCheck.addAll(context.getImmutableViewOfVariables().values());
            tempStack.pushContext(context);
        }
        while ((context = tempStack.popContext()) != null) {
            contextStack.pushContext(context);
        }

        return valuesToCheck;
    }

    public StatefulSessionStack getStatefulSessionsStack() {
        return statefulSessionStack;
    }

    public void setStatefulSessionsStack(StatefulSessionStack statefulSessionsStack) {
        this.statefulSessionStack = statefulSessionsStack;
    }

    public boolean isContextModified() {
        return contextModified;
    }

    public void setContextModified(boolean contextModified) {
        this.contextModified = contextModified;
    }

    public List<Argument> getModifiedArguments() {
        return modifiedArguments;
    }

    public void setModifiedArguments(List<Argument> modifiedArguments) {
        this.modifiedArguments = modifiedArguments;
    }
}
