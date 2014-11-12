/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package com.hp.score.lang.runtime.events;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author moradi
 * @since 03/11/2014
 * @version $Id$
 */
public class LanguageEventData extends HashMap<String, Serializable> {

    public static final String TYPE = "TYPE";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String TIMESTAMP = "TIMESTAMP";
    public static final String EXECUTIONID = "EXECUTIONID";
    public static final String PATH = "PATH";
    public static final String EXCEPTION = "EXCEPTION";
    public static final String CALL_ARGUMENTS = "CALL_ARGUMENTS";
    public static final String INPUTS = "INPUTS";
    public static final String BOUND_INPUTS = "BOUND_INPUTS";
    public static final String RETURN_VALUES = "RETURN_VALUES";
    public static final String OUTPUTS = "OUTPUTS";

    public enum levelName{
        TASK_NAME,EXECUTABLE_NAME;
    }

    public static final String ENCRYPTED_VALUE = "*****";

    private static final long serialVersionUID = 2885051907156304718L;

    public String getEventType() {
        return (String)get(TYPE);
    }

    public void setEventType(String eventType) {
        put(TYPE, eventType);
    }

    public String getDescription() {
        return (String)get(DESCRIPTION);
    }

    public void setDescription(String description) {
        put(DESCRIPTION, description);
    }

    public Date getTimeStamp() {
        return (Date)get(TIMESTAMP);
    }

    public void setTimeStamp(Date timeStamp) {
        put(TIMESTAMP, timeStamp);
    }

    public Long getExecutionId() {
        return (Long)get(EXECUTIONID);
    }

    public void setExecutionId(Long executionId) {
        put(EXECUTIONID, executionId);
    }

    public String getPath() {
        return (String)get(PATH);
    }

    public void setPath(String path) {
        put(PATH, path);
    }

    public Map<String, Serializable> getInputs() {
        return (Map<String, Serializable>)get(INPUTS);
    }

    public void setInputs(Map<String, Serializable> inputs) {
        put(INPUTS, (Serializable)inputs);
    }

    public Map<String, Serializable> getOutputs() {
        return (Map<String, Serializable>) get(OUTPUTS);
    }

    public void setOutputs(Map<String, Serializable> outputs) {
        put(OUTPUTS, (Serializable) outputs);
    }

}
