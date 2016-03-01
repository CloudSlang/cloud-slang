package io.cloudslang.lang.compiler.modeller.result;

import io.cloudslang.lang.compiler.modeller.model.Executable;

import java.util.List;

/**
 * Created by Ifat Gavish on 24/02/2016
 */
public class ExecutableModellingResult implements ModellingResult {

    private final Executable executable;
    private final List<RuntimeException> errors;

    public ExecutableModellingResult(Executable executable, List<RuntimeException> errors) {
        this.executable = executable;
        this.errors = errors;
    }

    public Executable getExecutable() {
        return executable;
    }

    public List<RuntimeException> getErrors() {
        return errors;
    }
}
