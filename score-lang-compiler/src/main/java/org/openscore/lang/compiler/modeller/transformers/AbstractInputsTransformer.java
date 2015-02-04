/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openscore.lang.compiler.modeller.transformers;

import org.apache.log4j.Logger;
import org.openscore.lang.entities.bindings.Input;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.openscore.lang.compiler.SlangTextualKeys.*;

public abstract class AbstractInputsTransformer {

	private static final Logger logger = Logger.getLogger(AbstractInputsTransformer.class);

	protected Input transformSingleInput(Object rawInput) {
		// - some_input
		// this is our default behaviour that if the user specifies only a key, the key is also the ref we look for
		if(rawInput instanceof String) {
			String inputName = (String)rawInput;
			return new Input(inputName, inputName);
		} else if(rawInput instanceof Map) {
			Map.Entry<String, ?> entry = ((Map<String, ?>)rawInput).entrySet().iterator().next();
			if(entry.getValue() instanceof Map) {
				// - some_inputs:
				// property1: value1
				// property2: value2
				// this is the verbose way of defining inputs with all of the properties available
				return createPropInput((Map.Entry<String, Map<String, Serializable>>)entry);
			}
			// - some_input: some_expression
			// the value of the input is an expression we need to evaluate at runtime
			return new Input(entry.getKey(), entry.getValue().toString());
		}
		throw new RuntimeException("Could not transform Input : " + rawInput);
	}

	private static Input createPropInput(Map.Entry<String, Map<String, Serializable>> entry) {
		Map<String, Serializable> props = entry.getValue();
		List<String> knownKeys = Arrays.asList(REQUIRED_KEY, ENCRYPTED_KEY, OVERRIDABLE_KEY, DEFAULT_KEY, SYSTEM_PROPERTY_KEY);
		for(String key : props.keySet()) {
			if(!knownKeys.contains(key)) {
				logger.warn("key: " + key + " in input: " + entry.getKey() + " is not a known property");
			}
		}
		boolean required = !props.containsKey(REQUIRED_KEY) || (boolean)props.get(REQUIRED_KEY);// default is required=true
		boolean encrypted = props.containsKey(ENCRYPTED_KEY) && (boolean)props.get(ENCRYPTED_KEY);
		boolean overridable = !props.containsKey(OVERRIDABLE_KEY) || (boolean)props.get(OVERRIDABLE_KEY);
		String inputName = entry.getKey();
		String expression = props.containsKey(DEFAULT_KEY) ? props.get(DEFAULT_KEY).toString() : inputName;
		String systemPropertyName = (String)props.get(SYSTEM_PROPERTY_KEY);
		return new Input(inputName, expression, encrypted, required, overridable, systemPropertyName);
	}

}
