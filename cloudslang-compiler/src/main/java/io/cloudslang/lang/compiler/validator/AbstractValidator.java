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

import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.validator.matcher.NamespacePatternMatcher;
import io.cloudslang.lang.compiler.validator.matcher.PatternMatcher;
import io.cloudslang.lang.compiler.validator.matcher.ResultNamePatternMatcher;
import io.cloudslang.lang.compiler.validator.matcher.SimpleNamePatternMatcher;
import io.cloudslang.lang.compiler.validator.matcher.VariableNamePatternMatcher;
import io.cloudslang.lang.entities.bindings.InOutParam;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.entities.constants.Regex;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractValidator {
    public static final String NAME_PLACEHOLDER = "name_placeholder01";

    protected PatternMatcher namespacePatternMatcher;
    protected PatternMatcher simpleNamePatternMatcher;
    protected PatternMatcher resultNamePatternMatcher;
    protected PatternMatcher variableNamePatternMatcher;

    public AbstractValidator() {
        namespacePatternMatcher = new NamespacePatternMatcher();
        simpleNamePatternMatcher = new SimpleNamePatternMatcher();
        resultNamePatternMatcher = new ResultNamePatternMatcher();
        variableNamePatternMatcher = new VariableNamePatternMatcher();
    }

    protected void validateNamespaceRules(String input) {
        validateChars(namespacePatternMatcher, input);
        validateDelimiter(input);
    }

    protected void validateSimpleNameRules(String input) {
        validateChars(simpleNamePatternMatcher, input);
    }

    protected void validateResultNameRules(String input) {
        validateChars(resultNamePatternMatcher, input);
        validateKeywords(input);
    }

    protected void validateVariableNameRules(String input) {
        validateChars(variableNamePatternMatcher, input);
    }

    protected void validateListsHaveMutuallyExclusiveNames(List<? extends InOutParam> inOutParams,
                                                           List<Output> outputs, String errorMessage) {
        for (InOutParam inOutParam : CollectionUtils.emptyIfNull(inOutParams)) {

            if (inOutParam instanceof Input && ((Input) inOutParam).isPrivateInput()) {
                continue;
            }

            for (Output output : CollectionUtils.emptyIfNull(outputs)) {
                if (StringUtils.equalsIgnoreCase(inOutParam.getName(), output.getName())) {
                    throw new IllegalArgumentException(errorMessage.replace(NAME_PLACEHOLDER, inOutParam.getName()));
                }
            }
        }
    }

    protected List<String> getResultNames(Executable executable) {
        List<String> resultNames = new ArrayList<>();
        for (Result result : executable.getResults()) {
            resultNames.add(result.getName());
        }
        return resultNames;
    }

    private void validateKeywords(String resultName) {
        if (SlangTextualKeys.ON_FAILURE_KEY.equalsIgnoreCase(resultName)) {
            throw new RuntimeException("Result cannot be called '" + SlangTextualKeys.ON_FAILURE_KEY + "'.");
        }
    }

    private void validateChars(PatternMatcher patternMatcher, String input) {
        if (!patternMatcher.matchesEndToEnd(input)) {
            throw new RuntimeException("Argument[" + input + "] violates character rules.");
        }
    }

    private void validateDelimiter(String input) {
        if (input.startsWith(Regex.NAMESPACE_PROPERTY_DELIMITER)) {
            throw new RuntimeException(
                    "Argument[" + input + "] cannot start with delimiter[" + Regex.NAMESPACE_PROPERTY_DELIMITER + "]."
            );
        }
        if (input.endsWith(Regex.NAMESPACE_PROPERTY_DELIMITER)) {
            throw new RuntimeException(
                    "Argument[" + input + "] cannot end with delimiter[" + Regex.NAMESPACE_PROPERTY_DELIMITER + "]."
            );
        }
        String[] parts = input.split(Regex.NAMESPACE_DELIMITER_ESCAPED);
        for (String part : parts) {
            if ("".equals(part)) {
                throw new RuntimeException(
                        "Argument[" + input + "] cannot contain multiple delimiters[" +
                                Regex.NAMESPACE_PROPERTY_DELIMITER + "] without content."
                );
            }
        }
    }
}
