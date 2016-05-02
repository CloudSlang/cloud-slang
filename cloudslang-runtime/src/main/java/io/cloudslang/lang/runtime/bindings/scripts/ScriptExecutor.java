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

import io.cloudslang.runtime.api.python.PythonRuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * @author Bonczidai Levente
 * @since 1/19/2016
 */
@Component
public class ScriptExecutor {
    @Autowired
    private PythonRuntimeService pythonRuntimeService;

    //we need this method to be synchronized so we will not have multiple scripts run in parallel on the same context
    public Map<String, Serializable> executeScript (String script, Map<String, Serializable> callArguments) {
        return pythonRuntimeService.exec(Collections.<String>emptySet(), script, callArguments);
    }
}
