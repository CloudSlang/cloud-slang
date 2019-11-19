package io.cloudslang.lang.compiler.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExternalPythonScriptValidatorImpl implements ExternalPythonScriptValidator {
    private final static String METHOD_SIGNATURE_REGEX = "def\\s+execution\\(([a-zA-Z0-9_]+,?\\s*)*\\):\\s*\\R";
    private Pattern methodSignaturePattern = Pattern.compile(METHOD_SIGNATURE_REGEX);

    @Override
    public void validateExecutionMethodSignature(String script) {
        Matcher matcher = methodSignaturePattern.matcher(script);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Method execution is missing or is invalid");
        }

        // multiple declarations of execution method
        if (matcher.find()) {
            throw new IllegalArgumentException("Overload of the execution method is not allowed");
        }
    }
}
