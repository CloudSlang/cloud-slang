package io.cloudslang.lang.compiler.modeller.result;

import io.cloudslang.lang.entities.CompilationArtifact;

import java.io.File;
import java.util.List;

/**
 * Created by bancl on 9/20/2016.
 */
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
