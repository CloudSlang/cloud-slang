/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.events;

import io.cloudslang.lang.entities.ExecutableType;
import io.cloudslang.lang.entities.bindings.values.SensitiveValue;
import io.cloudslang.lang.entities.bindings.values.Value;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author moradi
 * @version $Id$
 * @since 03/11/2014
 */
@SuppressWarnings("unchecked")
public class LanguageEventData extends HashMap<String, Serializable> {

    public static final String TYPE = "TYPE";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String TIMESTAMP = "TIMESTAMP";
    public static final String EXECUTION_ID = "EXECUTION_ID";
    public static final String PATH = "PATH";
    public static final String EXCEPTION = "EXCEPTION";
    public static final String OUTPUTS = "OUTPUTS";
    public static final String RESULT = "RESULT";
    public static final String CALL_ARGUMENTS = "CALL_ARGUMENTS";
    public static final String INPUTS = "INPUTS";
    public static final String ARGUMENTS = "ARGUMENTS";
    public static final String BOUND_INPUTS = "BOUND_INPUTS";
    public static final String BOUND_ARGUMENTS = "BOUND_ARGUMENTS";
    public static final String BOUND_PARALLEL_LOOP_EXPRESSION = "BOUND_PARALLEL_LOOP_EXPRESSION";
    public static final String NEXT_STEP_POSITION = "nextPosition";
    public static final String STEP_TYPE = "STEP_TYPE";
    public static final String STEP_NAME = "STEP_NAME";

    public enum StepType {
        FLOW("flow"),
        OPERATION("operation"),
        DECISION("decision"),
        STEP("step"),
        ACTION("action"),
        NAVIGATION("navigation");

        private final String value;
        private static final StepType[] EXECUTABLE_TYPES;

        static {
            EXECUTABLE_TYPES = new StepType[3];
            EXECUTABLE_TYPES[0] = FLOW;
            EXECUTABLE_TYPES[1] = OPERATION;
            EXECUTABLE_TYPES[2] = DECISION;
        }

        StepType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static StepType[] getExecutableTypes() {
            return Arrays.copyOf(EXECUTABLE_TYPES, EXECUTABLE_TYPES.length);
        }
    }

    public static StepType convertExecutableType(ExecutableType executableType) {
        switch (executableType) {
            case FLOW:
                return StepType.FLOW;
            case OPERATION:
                return StepType.OPERATION;
            case DECISION:
                return StepType.DECISION;
            default:
                throw new RuntimeException("Not implemented for executable type: " + executableType);
        }
    }

    public String getStepName() {
        return (String) get(STEP_NAME);
    }

    public void setStepName(String stepName) {
        put(STEP_NAME, stepName);
    }

    public StepType getStepType() {
        return (StepType) get(STEP_TYPE);
    }

    public void setStepType(StepType stepType) {
        put(STEP_TYPE, stepType);
    }

    public String getEventType() {
        return (String) get(TYPE);
    }

    public void setEventType(String eventType) {
        put(TYPE, eventType);
    }

    public String getDescription() {
        return (String) get(DESCRIPTION);
    }

    public void setDescription(String description) {
        put(DESCRIPTION, description);
    }

    public Date getTimeStamp() {
        return (Date) get(TIMESTAMP);
    }

    public void setTimeStamp(Date timeStamp) {
        put(TIMESTAMP, timeStamp);
    }

    public Long getExecutionId() {
        return (Long) get(EXECUTION_ID);
    }

    public void setExecutionId(Long executionId) {
        put(EXECUTION_ID, executionId);
    }

    public String getPath() {
        return (String) get(PATH);
    }

    public void setPath(String path) {
        put(PATH, path);
    }

    public String getResult() {
        return (String) get(RESULT);
    }

    public void setResult(String result) {
        put(RESULT, result);
    }

    public String getException() {
        return (String) get(EXCEPTION);
    }

    public void setException(String exceptionMessage) {
        put(EXCEPTION, exceptionMessage);
    }

    public Map<String, Serializable> getInputs() {
        return (Map<String, Serializable>) get(BOUND_INPUTS);
    }

    public void setInputs(Map<String, Serializable> inputs) {
        put(BOUND_INPUTS, (Serializable) inputs);
    }

    public Map<String, Serializable> getArguments() {
        return (Map<String, Serializable>) get(BOUND_ARGUMENTS);
    }

    public void setArguments(Map<String, Serializable> arguments) {
        put(BOUND_ARGUMENTS, (Serializable) arguments);
    }

    public Map<String, Serializable> getOutputs() {
        return (Map<String, Serializable>) get(OUTPUTS);
    }

    public void setOutputs(Map<String, Serializable> outputs) {
        put(OUTPUTS, (Serializable) outputs);
    }

    public Map<String, Serializable> getCallArguments() {
        return (Map<String, Serializable>) get(CALL_ARGUMENTS);
    }

    public void setCallArguments(Map<String, Serializable> callArguments) {
        put(CALL_ARGUMENTS, (Serializable) callArguments);
    }

    public List<Serializable> getParallelLoopBoundExpression() {
        return (List<Serializable>) get(BOUND_PARALLEL_LOOP_EXPRESSION);
    }

    public void setParallelLoopBoundExpression(List<Serializable> parallelLoopBoundExpression) {
        put(BOUND_PARALLEL_LOOP_EXPRESSION, (Serializable) parallelLoopBoundExpression);
    }

    @SuppressWarnings("unchecked")
    public static Serializable maskSensitiveValues(Serializable serializable) {
        if (serializable instanceof Map) {
            return (Serializable) maskSensitiveValues((Map<String, Serializable>) serializable);
        } else if (serializable instanceof List) {
            return (Serializable) maskSensitiveValues((List<Serializable>) serializable);
        } else {
            return serializable;
        }
    }

    public static Map<String, Serializable> maskSensitiveValues(Map<String, ? extends Serializable> values) {
        Map<String, Serializable> result = new LinkedHashMap<>();
        for (Map.Entry<String, ? extends Serializable> entry : values.entrySet()) {
            result.put(entry.getKey(), getMaskedValue(entry.getValue()));
        }
        return result;
    }

    public static List<Serializable> maskSensitiveValues(List<? extends Serializable> values) {
        List<Serializable> result = new ArrayList<>();
        for (Serializable value : values) {
            result.add(getMaskedValue(value));
        }
        return result;
    }

    private static Serializable getMaskedValue(Serializable value) {
        if (value != null && value instanceof Value) {
            return ((Value) value).isSensitive() ? SensitiveValue.SENSITIVE_VALUE_MASK : ((Value) value).get();
        }
        return value;
    }
}
