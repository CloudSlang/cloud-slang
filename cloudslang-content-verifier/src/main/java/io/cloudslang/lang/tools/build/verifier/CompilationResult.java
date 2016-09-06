/*
 * (c) Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package io.cloudslang.lang.tools.build.verifier;

import io.cloudslang.lang.compiler.modeller.model.Executable;

import java.util.Map;
import java.util.Queue;

/**
 * Created by bancl on 9/5/2016.
 */
public class CompilationResult {

    private Map<String, Executable> results;
    private Queue<RuntimeException> exceptions;

    public CompilationResult(Map<String, Executable> results, Queue<RuntimeException> exceptions) {
        this.results = results;
        this.exceptions = exceptions;
    }

    public Map<String, Executable> getResults() {
        return results;
    }

    public void setResults(Map<String, Executable> results) {
        this.results = results;
    }

    public Queue<RuntimeException> getExceptions() {
        return exceptions;
    }

    public void setExceptions(Queue<RuntimeException> exceptions) {
        this.exceptions = exceptions;
    }
}
