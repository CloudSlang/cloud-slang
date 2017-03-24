/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.parser.utils;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.parser.MetadataParser;
import io.cloudslang.lang.compiler.parser.model.ParsedDescriptionData;
import io.cloudslang.lang.compiler.parser.model.ParsedDescriptionSection;
import io.cloudslang.lang.compiler.utils.MetadataUtils;
import io.cloudslang.lang.compiler.utils.SlangSourceUtils;
import io.cloudslang.lang.compiler.validator.matcher.DescriptionPatternMatcher;
import io.cloudslang.lang.entities.constants.Regex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

public class MetadataValidatorImpl implements MetadataValidator {

    private MetadataParser metadataParser;
    private DescriptionPatternMatcher descriptionPatternMatcher;

    public MetadataValidatorImpl() {
        descriptionPatternMatcher = new DescriptionPatternMatcher();
    }

    @Override
    public List<RuntimeException> validateCheckstyle(SlangSource source) {
        Validate.notNull(source.getContent(), "Source " + source.getName() + " cannot be null");
        try {
            return extractCheckstyleData(source);
        } catch (Throwable e) {
            throw new RuntimeException(
                    "There was a problem extracting checkstyle data for source [" +
                            source.getName() + "]  - " + e.getMessage(), e
            );
        }
    }

    private List<RuntimeException> extractCheckstyleData(SlangSource source) {
        List<String> lines = SlangSourceUtils.readLines(source);
        ParsedDescriptionData parsedDescriptionData = metadataParser.parse(source);
        List<RuntimeException> errors = new ArrayList<>();

        // process flow descriptions
        List<ParsedDescriptionSection> topLevelDescriptions = parsedDescriptionData.getTopLevelDescriptions();
        for (ParsedDescriptionSection topLevelDescription : topLevelDescriptions) {
            errors.addAll(processCommonRules(lines, topLevelDescription, false));
        }

        // process step descriptions
        Collection<ParsedDescriptionSection> stepDescriptions = parsedDescriptionData.getStepDescriptions().values();
        for (ParsedDescriptionSection stepDescription : stepDescriptions) {
            errors.addAll(processCommonRules(lines, stepDescription, true));
        }

        return errors;
    }

    private List<RuntimeException> processCommonRules(
            List<String> lines,
            ParsedDescriptionSection parsedDescriptionSection,
            boolean isStep) {
        List<RuntimeException> errors = new ArrayList<>();
        int startLineNumberZeroBased = parsedDescriptionSection.getStartLineNumber() - 1;

        // validate begin wrapper line
        validateBeginWrapperLine(lines, isStep, errors, startLineNumberZeroBased);

        boolean finished = false;
        String previousTag = null;
        int previousItemEndLineNumber = -1;
        for (int lineNrZeroBased = startLineNumberZeroBased + 1;
             !finished && lineNrZeroBased < lines.size();
             lineNrZeroBased++) {
            String currentLine = lines.get(lineNrZeroBased);

            // #! @tag var: content
            // #! @tag: content
            boolean variableLine = isVariableLine(currentLine);
            boolean variableLineDeclarationOnly = isVariableLineDeclarationOnly(currentLine);
            if (variableLine || variableLineDeclarationOnly || isGeneralLine(currentLine)) {
                // extract tag
                String currentTag;
                if (variableLine) {
                    Pair<String, String> declaration =
                            descriptionPatternMatcher.getDescriptionVariableLineData(currentLine);
                    String[] declarationElements = descriptionPatternMatcher.splitDeclaration(declaration.getLeft());
                    currentTag = declarationElements[0];
                } else if (variableLineDeclarationOnly) {
                    Pair<String, String> declaration =
                            descriptionPatternMatcher.getDescriptionVariableLineDataDeclarationOnly(currentLine);
                    String[] declarationElements = descriptionPatternMatcher.splitDeclaration(declaration.getLeft());
                    currentTag = declarationElements[0];
                } else {
                    Pair<String, String> declaration =
                            descriptionPatternMatcher.getDescriptionGeneralLineData(currentLine);
                    String[] declarationElements = descriptionPatternMatcher.splitDeclaration(declaration.getLeft());
                    currentTag = declarationElements[0];
                }

                // validate empty line
                validateEmptyLine(lines, errors, previousTag, previousItemEndLineNumber, currentTag);

                previousTag = currentTag;
                previousItemEndLineNumber = lineNrZeroBased;
            } else {
                // #! continued from previous line
                if (isNonEmptyComplementaryLine(currentLine)) {
                    previousItemEndLineNumber = lineNrZeroBased;
                } else {
                    // #!!#
                    if (descriptionPatternMatcher.matchesDescriptionEnd(currentLine)) {
                        // validate ending wrapper line
                        validateEndingWrapperLine(lines, isStep, errors, lineNrZeroBased);

                        finished = true;
                    }
                    // otherwise ignore
                }
            }
        }
        return errors;
    }

    private void validateEndingWrapperLine(
            List<String> lines,
            boolean isStep,
            List<RuntimeException> errors,
            int lineNrZeroBased) {
        int targetedLineNumberZeroBased = lineNrZeroBased + 1;
        String nextLine = tryExtractLine(lines, targetedLineNumberZeroBased);
        if (isStep) {
            if (nextLine == null || !descriptionPatternMatcher.matchesStepDelimiterLine(nextLine)) {
                errors.add(
                        new RuntimeException(
                                generateErrorMessage(lineNrZeroBased,
                                        "Next line should be delimiter line (90 characters of `#`)"
                                )
                        )
                );
            }
        } else {
            if (nextLine == null || !descriptionPatternMatcher.matchesExecutableDelimiterLine(nextLine)) {
                errors.add(
                        new RuntimeException(
                                generateErrorMessage(lineNrZeroBased,
                                        "Next line should be delimiter line (120 characters of `#`)"
                                )
                        )
                );
            }
        }
    }

    private void validateEmptyLine(
            List<String> lines,
            List<RuntimeException> errors,
            String previousTag,
            int previousItemEndLineNumber,
            String currentTag) {
        if (previousTag != null && !previousTag.equals(currentTag)) {
            int targetedLineNumberZeroBased = previousItemEndLineNumber + 1;
            String targetedLine = lines.get(targetedLineNumberZeroBased);
            if (!descriptionPatternMatcher.matchesEmptyLine(targetedLine)) {
                errors.add(
                        new RuntimeException(
                                generateErrorMessage(
                                        targetedLineNumberZeroBased,
                                        "There should be an empty line between two sections of different tags" +
                                                " (" + previousTag + " and " + currentTag + ")"
                                )
                        )
                );
            }
        }
    }

    private void validateBeginWrapperLine(
            List<String> lines,
            boolean isStep,
            List<RuntimeException> errors,
            int startLineNumberZeroBased) {
        int targetedLineNumberZeroBased = startLineNumberZeroBased - 1;
        String previousLine = tryExtractLine(lines, targetedLineNumberZeroBased);
        if (isStep) {
            if (previousLine == null || !descriptionPatternMatcher.matchesStepDelimiterLine(previousLine)) {
                errors.add(
                        new RuntimeException(
                                generateErrorMessage(startLineNumberZeroBased,
                                        "Previous line should be delimiter line (90 characters of `#`)"
                                )
                        )
                );
            }
        } else {
            if (previousLine == null || !descriptionPatternMatcher.matchesExecutableDelimiterLine(previousLine)) {
                errors.add(
                        new RuntimeException(
                                generateErrorMessage(startLineNumberZeroBased,
                                        "Previous line should be delimiter line (120 characters of `#`)"
                                )
                        )
                );
            }
        }
    }

    private String generateErrorMessage(int lineNumberZeroBased, String data) {
        return MetadataUtils.generateErrorMessage(lineNumberZeroBased, data);
    }

    private boolean isNonEmptyComplementaryLine(String currentLine) {
        return
                descriptionPatternMatcher.matchesDescriptionComplementaryLine(currentLine) &&
                        !currentLine.trim().equals(Regex.DESCRIPTION_TOKEN);
    }

    private boolean isGeneralLine(String currentLine) {
        return descriptionPatternMatcher.matchesDescriptionGeneralLine(currentLine);
    }

    private boolean isVariableLine(String currentLine) {
        return descriptionPatternMatcher.matchesDescriptionVariableLine(currentLine);
    }

    private boolean isVariableLineDeclarationOnly(String currentLine) {
        return descriptionPatternMatcher.matchesVariableLineDeclarationOnlyLine(currentLine);
    }

    private String tryExtractLine(List<String> lines, int lineNr) {
        if (lineNr >= 0 && lineNr < lines.size()) {
            return lines.get(lineNr);
        }
        return null;
    }

    public void setMetadataParser(MetadataParser metadataParser) {
        this.metadataParser = metadataParser;
    }

}
