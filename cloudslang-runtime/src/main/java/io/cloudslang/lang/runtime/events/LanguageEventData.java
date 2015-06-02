/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.events;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author moradi
 * @version $Id$
 * @since 03/11/2014
 */
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
    public static final String BOUND_INPUTS = "BOUND_INPUTS";
    public static final String BOUND_ASYNC_LOOP_EXPRESSION = "BOUND_ASYNC_LOOP_EXPRESSION";
    public static final String RETURN_VALUES = "RETURN_VALUES";
    public static final String NEXT_STEP_POSITION = "nextPosition";
    public static final String ENCRYPTED_VALUE = "*****";
    public static final String STEP_TYPE = "STEP_TYPE";
    public static final String STEP_NAME = "STEP_NAME";

    public enum levelName {
        TASK_NAME,
        EXECUTABLE_NAME
    }

    public enum StepType {
        TASK,
        EXECUTABLE,
        ACTION
    }

    public String getStepName() {
        return (String) get(STEP_NAME);
    }

    public void setStepName(String stepName){
        put(STEP_NAME, stepName);
    }

    public StepType getStepType() {
        return (StepType) get(STEP_TYPE);
    }

    public void setStepType(StepType stepType){
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

    public Exception getException() {
        return (Exception) get(EXCEPTION);
    }

    public void setException(Exception ex) {
        put(EXCEPTION, ex);
    }

    public Map<String, Serializable> getInputs() {
        return (Map<String, Serializable>) get(BOUND_INPUTS);
    }

    public void setInputs(Map<String, Serializable> inputs) {
        put(BOUND_INPUTS, (Serializable) inputs);
    }

    public Map<String, Serializable> getOutputs() {
        return (Map<String, Serializable>) get(OUTPUTS);
    }

    public void setOutputs(Map<String, Serializable> outputs) {
        put(OUTPUTS, (Serializable) outputs);
    }
}
