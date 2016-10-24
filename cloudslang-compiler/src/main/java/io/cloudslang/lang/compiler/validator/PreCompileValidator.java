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

import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import io.cloudslang.lang.compiler.modeller.transformers.InOutTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.Transformer;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.entities.bindings.InOutParam;
import io.cloudslang.lang.entities.bindings.Result;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface PreCompileValidator {

    String validateExecutableRawData(ParsedSlang parsedSlang,
                                     Map<String, Object> executableRawData,
                                     List<RuntimeException> errors);

    List<Map<String, Map<String, Object>>> validateWorkflowRawData(ParsedSlang parsedSlang,
                                                                   Object workflowRawData,
                                                                   String executableName,
                                                                   List<RuntimeException> errors);

    ExecutableModellingResult validateResult(ParsedSlang parsedSlang,
                                             String executableName,
                                             ExecutableModellingResult result);

    List<RuntimeException> checkKeyWords(
            String dataLogicalName,
            String parentProperty,
            Map<String, Object> rawData,
            List<Transformer> allRelevantTransformers,
            List<String> additionalValidKeyWords,
            List<List<String>> constraintGroups);

    Map<String, Map<String, Object>> validateOnFailurePosition(
            List<Map<String, Map<String, Object>>> workFlowRawData,
            String execName,
            List<RuntimeException> errors);

    void validateDecisionResultsSection(Map<String, Object> executableRawData,
                                        String artifact,
                                        List<RuntimeException> errors);

    List<RuntimeException> validateNoDuplicateInOutParams(List<? extends InOutParam> inputs, InOutParam element);

    void validateResultsHaveNoExpression(List<Result> results, String artifactName, List<RuntimeException> errors);

    void validateResultTypes(List<Result> results, String artifactName, List<RuntimeException> errors);

    void validateDefaultResult(List<Result> results, String artifactName, List<RuntimeException> errors);

    void validateStringValue(String name, Serializable value, InOutTransformer transformer);
}
