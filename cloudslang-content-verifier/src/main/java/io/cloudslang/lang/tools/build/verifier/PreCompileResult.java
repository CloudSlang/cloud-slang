/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.verifier;

import io.cloudslang.lang.compiler.modeller.model.Executable;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class PreCompileResult {

    private Map<String, Executable> results;
    private Queue<RuntimeException> exceptions;

    public PreCompileResult() {
        this.results = new HashMap<>();
        this.exceptions = new ArrayDeque<>();
    }

    public Map<String, Executable> getResults() {
        return results;
    }

    public void addResults(Map<String, Executable> results) {
        this.results.putAll(results);
    }

    public Queue<RuntimeException> getExceptions() {
        return exceptions;
    }

    public void addExceptions(Queue<RuntimeException> exceptions) {
        this.exceptions.addAll(exceptions);
    }
}
