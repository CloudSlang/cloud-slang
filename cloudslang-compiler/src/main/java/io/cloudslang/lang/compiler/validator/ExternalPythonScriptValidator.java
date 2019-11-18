package io.cloudslang.lang.compiler.validator;

public interface ExternalPythonScriptValidator {
    void validateExecutionMethodSignature(String script);
}
