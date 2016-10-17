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

/**
 * @author Bonczidai Levente
 * @since 8/24/2016
 */
public interface ExecutableValidator {
    void validateNamespace(ParsedSlang parsedSlang);

    void validateImportsSection(ParsedSlang parsedSlang);

    void validateStepReferenceId(String referenceId);

    void validateExecutableName(String executableName);

    void validateStepName(String stepName);

    void validateResultName(String resultName);

    void validateNavigationStrings(List<Map<String, String>> navigationStrings);

    void validateBreakKeys(List<String> breakKeys);

    void validateInputName(String name);

    void validateOutputName(String name);

    void validateLoopStatementVariable(String name);
}
