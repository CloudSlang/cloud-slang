/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.result;

import io.cloudslang.lang.entities.CompilationArtifact;

import java.io.File;
import java.util.List;

public class CompilationModellingResult implements ModellingResult {

    private final CompilationArtifact compilationArtifact;
    private final List<RuntimeException> errors;
    private File file;

    public CompilationModellingResult(CompilationArtifact compilationArtifact, List<RuntimeException> errors) {
        this.compilationArtifact = compilationArtifact;
        this.errors = errors;
    }

    public CompilationArtifact getCompilationArtifact() {
        return compilationArtifact;
    }

    @Override
    public List<RuntimeException> getErrors() {
        return errors;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
