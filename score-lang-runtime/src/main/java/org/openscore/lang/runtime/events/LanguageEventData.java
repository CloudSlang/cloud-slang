/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/
package org.openscore.lang.runtime.events;

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
	public static final String OUTPUTS = "OUTPUTS";
    public static final String RESULT = "RESULT";
	public static final String CALL_ARGUMENTS = "CALL_ARGUMENTS";
	public static final String BOUND_INPUTS = "BOUND_INPUTS";
	public static final String RETURN_VALUES = "RETURN_VALUES";
    public static final String NEXT_STEP_POSITION = "nextPosition";
	public static final String ENCRYPTED_VALUE = "*****";
	private static final long serialVersionUID = 2885051907156304718L;

	public enum levelName {
		TASK_NAME,
		EXECUTABLE_NAME
	}

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

	public Exception getException() {
		return (Exception)get(EXCEPTION);
	}

	public void setException(Exception ex) {
		put(EXCEPTION, ex);
	}

	public Map<String, Serializable> getInputs() {
		return (Map<String, Serializable>)get(BOUND_INPUTS);
	}

	public void setInputs(Map<String, Serializable> inputs) {
		put(BOUND_INPUTS, (Serializable)inputs);
	}

	public Map<String, Serializable> getOutputs() {
		return (Map<String, Serializable>)get(OUTPUTS);
	}

	public void setOutputs(Map<String, Serializable> outputs) {
		put(OUTPUTS, (Serializable)outputs);
	}

}
