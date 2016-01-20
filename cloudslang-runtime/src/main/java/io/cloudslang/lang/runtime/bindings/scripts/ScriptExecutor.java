/**
 * ****************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 * <p/>
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * *****************************************************************************
 */
package io.cloudslang.lang.runtime.bindings.scripts;

import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Bonczidai Levente
 * @since 1/19/2016
 */
@Component
public class ScriptExecutor extends AbstractScriptInterpreter {

    //we need this method to be synchronized so we will not have multiple scripts run in parallel on the same context
    public synchronized Map<String, Serializable> executeScript(
            Map<String, Serializable> callArguments,
            String script) {
        cleanInterpreter();
        try {
            return executeScript(script, callArguments);
        } catch (Exception e) {
            throw new RuntimeException("Error executing python script: " + e, e);
        }
    }

    private Map<String, Serializable> executeScript(String script, Map<String, Serializable> userVars) {
        Iterator<Map.Entry<String, Serializable>> iterator = userVars.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Serializable> entry = iterator.next();
            setValueInContext(entry.getKey(), entry.getValue());
            iterator.remove();
        }
        return exec(script);
    }

}
