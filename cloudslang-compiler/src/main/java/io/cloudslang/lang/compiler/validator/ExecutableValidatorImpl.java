/*
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.compiler.validator;

import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * @author Bonczidai Levente
 * @since 8/24/2016
 */
@Component
public class ExecutableValidatorImpl extends AbstractValidator implements ExecutableValidator {

    @Override
    public void validateNamespace(ParsedSlang parsedSlang) {

    }

    @Override
    public void validateImportsSectionAliases(ParsedSlang parsedSlang) {

    }

    @Override
    public void validateImportsSectionValues(ParsedSlang parsedSlang) {

    }

    @Override
    public void validateStepReferenceId(String referenceId) {

    }

    @Override
    public void validateExecutableName(String executableName) {

    }

    @Override
    public void validateStepName(String stepName) {

    }

    @Override
    public void validateResultName(String resultName) {
        validateKeywords(resultName);
    }

    @Override
    public void validateNavigationStrings(List<Map<String, String>> navigationStrings) {

    }

    @Override
    public void validateBreakKeys() {

    }

    @Override
    public void validateInputName(String name) {

    }

    @Override
    public void validateOutputName(String name) {

    }

    @Override
    public void validateLoopStatementVariable(String name) {

    }

    private void validateKeywords(String resultName) {
        if (SlangTextualKeys.ON_FAILURE_KEY.equalsIgnoreCase(resultName)) {
            throw new RuntimeException("Result cannot be called '" + SlangTextualKeys.ON_FAILURE_KEY + "'.");
        }
    }
}
