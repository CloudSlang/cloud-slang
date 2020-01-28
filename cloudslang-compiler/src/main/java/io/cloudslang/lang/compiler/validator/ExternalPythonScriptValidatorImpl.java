/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.validator;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ExternalPythonScriptValidatorImpl implements ExternalPythonScriptValidator {

    private static final String METHOD_SIGNATURE_REGEX = "^def\\s+execute\\((([a-zA-Z0-9_]+,?\\s*)*)\\):\\s*\\R";
    private Pattern methodSignaturePattern = Pattern.compile(METHOD_SIGNATURE_REGEX,
            Pattern.DOTALL | Pattern.MULTILINE);

    private static final String METHOD_CONTENT_REGEX = METHOD_SIGNATURE_REGEX + "(.*)";
    private Pattern methodContentPattern = Pattern.compile(METHOD_CONTENT_REGEX, Pattern.DOTALL | Pattern.MULTILINE);

    private static final String SINGLE_COMMENT_REGEX = "#.*?\n";
    private Pattern singleCommentPattern = Pattern.compile(SINGLE_COMMENT_REGEX, Pattern.DOTALL | Pattern.MULTILINE);

    private static final String MULTILINE_COMMENT_REGEX = "'''.*?'''";
    private Pattern multilineCommentPattern = Pattern.compile(MULTILINE_COMMENT_REGEX, Pattern.DOTALL);

    private static final String INPUTS_ARE_MISSING_ERROR = "Inputs are not defined for all execute method parameters.";

    private static final List<String> RESERVERD_KEYWORDS = Arrays.asList("and", "del", "from", "not", "while", "as",
            "elif", "global", "or", "with", "exec", "assert", "else", "if", "pass", "yield", "break", "except",
            "import", "import", "print", "class", "in", "raise", "continue", "finally", "is", "return", "def", "for",
            "lambda", "try");

    @Override
    public void validateExecutionMethodAndInputs(String script, List<String> inputs) {
        Matcher matcher = methodSignaturePattern.matcher(script);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Method {execute} is missing or is invalid.");
        }

        if (matcher.find()) {
            throw new IllegalArgumentException("Overload of the execution method is not allowed.");
        }

        validateInputs(script, inputs);

        if (isExecuteMethodBlank(script)) {
            throw new IllegalArgumentException("Method {execute} cannot be empty.");
        }
    }

    private void validateInputs(String script, List<String> inputs) {
        Matcher matcher = methodSignaturePattern.matcher(script);
        matcher.find();
        String scriptInputsString = matcher.group(1);
        if (StringUtils.isNotBlank(scriptInputsString)) {
            String[] scriptInputs = scriptInputsString.split(",");
            if (scriptInputs.length > 0) {
                if (inputs == null || inputs.size() < scriptInputs.length) {
                    throw new IllegalArgumentException(INPUTS_ARE_MISSING_ERROR);
                }
                Set<String> scriptInputsSet = Arrays.stream(scriptInputs).map(String::trim).collect(Collectors.toSet());
                if (scriptInputs.length != scriptInputsSet.size()) {
                    throw new IllegalArgumentException("There are inputs with the same name in execute method.");
                }
                if (!inputs.containsAll(scriptInputsSet)) {
                    throw new IllegalArgumentException(INPUTS_ARE_MISSING_ERROR);
                }
            }
        }
        validateInputNames(inputs);
    }

    private boolean isExecuteMethodBlank(String script) {
        String noSingleComments = singleCommentPattern.matcher(script + "\n").replaceAll("");
        String noComments = multilineCommentPattern.matcher(noSingleComments).replaceAll("");
        Matcher matcher = methodContentPattern.matcher(noComments);
        String result = null;
        if (matcher.find()) {
            result = matcher.group(3);
        }
        return StringUtils.isBlank(result);
    }

    private void validateInputNames(List<String> inputs) {
        List<String> illegalNames = inputs.stream()
                .filter(RESERVERD_KEYWORDS::contains)
                .collect(Collectors.toList());
        if (!illegalNames.isEmpty()) {
            throw new IllegalArgumentException("Illegal input names: " + String.join(", ", illegalNames));
        }
    }
}
