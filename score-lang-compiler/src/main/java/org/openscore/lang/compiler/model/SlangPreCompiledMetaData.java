package org.openscore.lang.compiler.model;

/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/


import org.openscore.lang.entities.bindings.Input;
import org.openscore.lang.entities.bindings.Output;
import org.openscore.lang.entities.bindings.Result;

import java.util.List;
import java.util.Map;

/**
 * Created by stoneo on 1/4/2015.
 */
public class SlangPreCompiledMetaData {

    private final String namespace;
    private final String name;
    private final List<Input> inputs;
    private final List<Output> outputs;
    private final List<Result> results;
    private final Map<String, SlangFileType> dependencies;

    public SlangPreCompiledMetaData(String namespace,
                         String name,
                         List<Input> inputs,
                         List<Output> outputs,
                         List<Result> results,
                         Map<String, SlangFileType> dependencies) {
        this.namespace = namespace;
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
        this.results = results;
        this.dependencies = dependencies;
    }

    public SlangPreCompiledMetaData(Executable executable,
                                    Map<String, SlangFileType> dependencies){
        this(executable.getNamespace(),
                executable.getName(),
                executable.getInputs(),
                executable.getOutputs(), executable.getResults(), dependencies);
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public List<Input> getInputs() {
        return inputs;
    }

    public List<Output> getOutputs() {
        return outputs;
    }

    public List<Result> getResults() {
        return results;
    }

    public Map<String, SlangFileType> getDependencies() {
        return dependencies;
    }

    public enum SlangFileType {
        EXECUTABLE,
        SYSTEM_VARIABLE
    }

}
