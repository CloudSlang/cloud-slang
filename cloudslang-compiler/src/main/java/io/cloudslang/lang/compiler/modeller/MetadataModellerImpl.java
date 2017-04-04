/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller;

import io.cloudslang.lang.compiler.modeller.model.Metadata;
import io.cloudslang.lang.compiler.modeller.model.StepMetadata;
import io.cloudslang.lang.compiler.modeller.result.MetadataModellingResult;
import io.cloudslang.lang.compiler.parser.model.ParsedDescriptionData;
import io.cloudslang.lang.compiler.parser.model.ParsedDescriptionSection;
import io.cloudslang.lang.compiler.parser.utils.DescriptionTag;
import io.cloudslang.lang.compiler.parser.utils.StepDescriptionTag;
import io.cloudslang.lang.compiler.validator.matcher.DescriptionPatternMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class MetadataModellerImpl implements MetadataModeller {
    private DescriptionPatternMatcher descriptionPatternMatcher;

    public MetadataModellerImpl() {
        descriptionPatternMatcher = new DescriptionPatternMatcher();
    }

    @Override
    public MetadataModellingResult createModel(ParsedDescriptionData parsedDescriptionData) {
        List<ParsedDescriptionSection> topLevelDescriptions = parsedDescriptionData.getTopLevelDescriptions();
        List<RuntimeException> errors = new ArrayList<>(parsedDescriptionData.getErrors());
        Pair<Metadata, List<RuntimeException>> executableMetadata = null;

        // executable metadata
        if (CollectionUtils.isNotEmpty(topLevelDescriptions)) {
            if (topLevelDescriptions.size() > 1) {
                errors.add(
                        new RuntimeException(
                                "Multiple top level descriptions found at line numbers: " +
                                        getLineNumbers(topLevelDescriptions)
                        )
                );
            }
            executableMetadata =
                    transformToExecutableMetadata(topLevelDescriptions.get(0).getData());
            errors.addAll(executableMetadata.getRight());
        }

        // step metadata
        Pair<List<StepMetadata>, List<RuntimeException>> stepsModellingResult =
                transformStepsData(parsedDescriptionData.getStepDescriptions());
        List<StepMetadata> stepDescriptions = stepsModellingResult.getLeft();
        errors.addAll(stepsModellingResult.getRight());

        return new MetadataModellingResult(
                executableMetadata == null ? new Metadata() : executableMetadata.getLeft(),
                stepDescriptions,
                errors
        );
    }

    private String getLineNumbers(List<ParsedDescriptionSection> topLevelDescriptions) {
        int[] lineNumbers = new int[topLevelDescriptions.size()];
        for (int i = 0; i < topLevelDescriptions.size(); i++) {
            lineNumbers[i] = topLevelDescriptions.get(i).getStartLineNumber();
        }
        return Arrays.toString(lineNumbers);
    }

    private Pair<Metadata, List<RuntimeException>> transformToExecutableMetadata(
            Map<String, String> parsedData) {
        String description = "";
        String prerequisites = "";
        Map<String, String> inputs = new LinkedHashMap<>();
        Map<String, String> outputs = new LinkedHashMap<>();
        Map<String, String> results = new LinkedHashMap<>();
        List<RuntimeException> errors = new ArrayList<>();

        for (Map.Entry<String, String> entry : parsedData.entrySet()) {
            String declaration = entry.getKey();
            String[] declarationElements = descriptionPatternMatcher.splitDeclaration(declaration);
            String tag = declarationElements[0];
            if (DescriptionTag.isDescriptionTag(tag)) {
                String content = entry.getValue();
                DescriptionTag descriptionTag = DescriptionTag.fromString(tag);
                if (descriptionTag != null) {
                    switch (descriptionTag) {
                        case DESCRIPTION:
                            description = content;
                            break;
                        case PREREQUISITES:
                            prerequisites = content;
                            break;
                        case INPUT:
                            processExecutableDeclaration(declarationElements, errors, tag, inputs, content);
                            break;
                        case OUTPUT:
                            processExecutableDeclaration(declarationElements, errors, tag, outputs, content);
                            break;
                        case RESULT:
                            processExecutableDeclaration(declarationElements, errors, tag, results, content);
                            break;
                        default:
                            // shouldn't get here
                            errors.add(new NotImplementedException("Unrecognized tag: " + descriptionTag));
                    }
                } else {
                    // shouldn't get here
                    errors.add(new RuntimeException("Unrecognized tag: " + tag));
                }
            } else {
                errors.add(new RuntimeException("Unrecognized tag for executable description section: " + tag));
            }
        }

        Metadata executableMetadata = new Metadata(
                description,
                prerequisites,
                inputs,
                outputs,
                results
        );
        return new ImmutablePair<>(executableMetadata, errors);
    }

    private void processExecutableDeclaration(
            String[] declarationElements,
            List<RuntimeException> errors,
            String tag,
            Map<String, String> parameters,
            String content) {
        processDeclaration(declarationElements, errors, tag, parameters, content, "executable");
    }

    private void processStepDeclaration(
            String[] declarationElements,
            List<RuntimeException> errors,
            String tag,
            Map<String, String> parameters,
            String content,
            String stepName) {
        processDeclaration(declarationElements, errors, tag, parameters, content, "step[" + stepName + "]");
    }

    private void processDeclaration(
            String[] declarationElements,
            List<RuntimeException> errors,
            String tag,
            Map<String, String> parameters,
            String content,
            String identifier) {
        if (declarationElements.length != 2) {
            errors.add(
                    new RuntimeException(
                            "For " + identifier + " parameter name for tag[" + tag + "] is missing. " +
                                    "Format should be [" + tag + " name]"
                    )
            );
        } else {
            String name = declarationElements[1];
            parameters.put(name, content);
        }
    }

    private Pair<List<StepMetadata>, List<RuntimeException>> transformStepsData(
            Map<String, ParsedDescriptionSection> stepsData) {
        List<StepMetadata> stepsMetadata = new ArrayList<>();
        List<RuntimeException> errors = new ArrayList<>();
        for (Map.Entry<String, ParsedDescriptionSection> entry : stepsData.entrySet()) {
            String stepName = entry.getKey();
            ParsedDescriptionSection parsedData = entry.getValue();
            Pair<StepMetadata, List<RuntimeException>> transformResult =
                    transformToStepMetadata(stepName, parsedData.getData());
            stepsMetadata.add(transformResult.getLeft());
            errors.addAll(transformResult.getRight());
        }

        return new ImmutablePair<>(stepsMetadata, errors);
    }

    private Pair<StepMetadata, List<RuntimeException>> transformToStepMetadata(
            String stepName,
            Map<String, String> parsedData) {
        Map<String, String> inputs = new LinkedHashMap<>();
        Map<String, String> outputs = new LinkedHashMap<>();
        List<RuntimeException> errors = new ArrayList<>();

        for (Map.Entry<String, String> entry : parsedData.entrySet()) {
            String declaration = entry.getKey();
            String[] declarationElements = descriptionPatternMatcher.splitDeclaration(declaration);
            String tag = declarationElements[0];
            if (StepDescriptionTag.isStepDescriptionTag(tag)) {
                String content = entry.getValue();
                StepDescriptionTag descriptionTag = StepDescriptionTag.fromString(tag);
                if (descriptionTag != null) {
                    switch (descriptionTag) {
                        case INPUT:
                            processStepDeclaration(declarationElements, errors, tag, inputs, content, stepName);
                            break;
                        case OUTPUT:
                            processStepDeclaration(declarationElements, errors, tag, outputs, content, stepName);
                            break;
                        default:
                            // shouldn't get here
                            errors.add(new NotImplementedException("Unrecognized tag: " + descriptionTag));
                    }
                } else {
                    // shouldn't get here
                    errors.add(new RuntimeException("Unrecognized tag: " + tag));
                }
            } else {
                errors.add(new RuntimeException("Unrecognized tag for step description section: " + tag));
            }
        }

        StepMetadata stepMetadata = new StepMetadata(
                stepName,
                inputs,
                outputs
        );
        return new ImmutablePair<>(stepMetadata, errors);
    }

}
