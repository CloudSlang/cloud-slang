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

import io.cloudslang.lang.entities.CompilationArtifact;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class CompileResult {

    private Map<String, CompilationArtifact> results;
    private Queue<RuntimeException> exceptions;

    public CompileResult() {
        this.results = new HashMap<>();
        this.exceptions = new ArrayDeque<>();
    }

    public Map<String, CompilationArtifact> getResults() {
        return results;
    }

    public void addResults(Map<String, CompilationArtifact> results) {
        this.results.putAll(results);
    }

    public Queue<RuntimeException> getExceptions() {
        return exceptions;
    }

    public void addExceptions(Queue<RuntimeException> exceptions) {
        this.exceptions.addAll(exceptions);
    }

    public void addException(RuntimeException exception) {
        this.exceptions.add(exception);
    }
}
