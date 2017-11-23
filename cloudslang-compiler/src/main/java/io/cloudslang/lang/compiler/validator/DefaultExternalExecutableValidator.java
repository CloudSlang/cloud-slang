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

import io.cloudslang.lang.compiler.parser.model.ParsedSlang;

import java.util.List;
import java.util.Map;

public class DefaultExternalExecutableValidator implements ExecutableValidator {
    @Override
    public void validateNamespace(ParsedSlang parsedSlang) {
    }

    @Override
    public void validateImportsSection(ParsedSlang parsedSlang) {
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
    }

    @Override
    public void validateNavigationStrings(List<Map<String, String>> navigationStrings) {
    }

    @Override
    public void validateBreakKeys(List<String> breakKeys) {
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
}
