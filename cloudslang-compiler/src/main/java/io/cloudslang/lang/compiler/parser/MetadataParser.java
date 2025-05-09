/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.parser;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.parser.model.ParsedDescriptionData;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import io.cloudslang.lang.compiler.utils.MetadataUtils;
import io.cloudslang.lang.compiler.utils.SlangSourceUtils;
import io.cloudslang.lang.compiler.validator.matcher.DescriptionPatternMatcher;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class MetadataParser {
    private ParserExceptionHandler parserExceptionHandler;
    private DescriptionPatternMatcher descriptionPatternMatcher;

    private static final String namespace = "namespace:";

    public MetadataParser() {
        descriptionPatternMatcher = new DescriptionPatternMatcher();
    }

    public ParsedDescriptionData parse(SlangSource source) {
        Validate.notNull(source.getContent(), "Source " + source.getName() + " cannot be null");
        try {
            return extractDescriptionData(source);
        } catch (Throwable e) {
            throw new RuntimeException("There was a problem parsing the description: " +
                    source.getName() + "." + System.lineSeparator() + parserExceptionHandler.getErrorMessage(e), e);
        }
    }

    private ParsedDescriptionData extractDescriptionData(SlangSource source) {
        List<String> lines = SlangSourceUtils.readLines(source);
        return processRawLines(lines);
    }

    private ParsedDescriptionData processRawLines(List<String> lines) {
        DescriptionBuilder descriptionBuilder = new DescriptionBuilder();
        boolean descriptionStarted = false;
        // whenever tha 'namespace' keyword is hit, we will return the description

        int descriptionStartLine = -1;
        int descriptionEndLine = -1;
        List<String> descriptionRawLines = new ArrayList<>();
        final ListIterator<String> lineIterator = lines.listIterator();
        int counter = -1;
        while (lineIterator.hasNext()) {
            counter++;
            String currentLine = lineIterator.next();
            if (currentLine.startsWith(namespace)) {
                break;
            }
            if (descriptionStarted) {
                descriptionRawLines.add(currentLine);
            }
            if (descriptionPatternMatcher.matchesDescriptionStart(currentLine)) {
                descriptionStarted = true;
                descriptionStartLine = counter;
                handleBlockStart(descriptionBuilder, counter);
            }
            if (descriptionPatternMatcher.matchesDescriptionEnd(currentLine)) {
                if (descriptionStarted) {
                    descriptionEndLine = counter;
                    break;
                } else {
                    throw new IllegalArgumentException("Description ended, but never started.");
                }
            }
        }
        // remove the end line if any description is present
        if (!descriptionRawLines.isEmpty()) {
            descriptionRawLines.remove(descriptionRawLines.size() - 1);
        }


        if (descriptionStarted && descriptionEndLine != -1) {
            int lineNrZeroBased = -1 + descriptionStartLine;
            // no start or end lines now only the description content
            for (String currentLine : descriptionRawLines) {
                lineNrZeroBased++;
                // #! @tag var: content <=> @tag var
                if (descriptionPatternMatcher.matchesDescriptionVariableLine(currentLine)) {
                    handleDescriptionLineVariableSyntax(descriptionBuilder, currentLine);
                } else {
                    // #! @tag: content
                    if (descriptionPatternMatcher.matchesDescriptionGeneralLine(currentLine)) {
                        handleDescriptionLineGeneralSyntax(descriptionBuilder, currentLine);
                    } else {
                        if (descriptionPatternMatcher.matchesVariableLineDeclarationOnlyLine(currentLine)) {
                            handleDescriptionLineVariableDeclarationOnlySyntax(descriptionBuilder, currentLine);
                        } else {
                            // #! continued from previous line
                            if (descriptionPatternMatcher.matchesDescriptionComplementaryLine(currentLine)) {
                                handleDescriptionLineComplementarySyntax(descriptionBuilder, currentLine);
                            } else {
                                // check if line is allowed inside description
                                handleNonDescriptionLineInsideDescription(
                                        descriptionBuilder,
                                        currentLine,
                                        lineNrZeroBased
                                );
                            }
                        }
                    }
                }
            }
            handleDescriptionEnd(descriptionBuilder, lines, descriptionEndLine);
        }
        return descriptionBuilder.build();
    }

    private void handleNonDescriptionLineInsideDescription(
            DescriptionBuilder descriptionBuilder,
            String currentLine,
            int lineNumberZeroBased) {
        // check line fits into description
        if (!descriptionPatternMatcher.isLineAcceptedInsideDescription(currentLine)) {
            descriptionBuilder.addError(
                    new RuntimeException(
                            MetadataUtils.generateErrorMessage(
                                    lineNumberZeroBased,
                                    "Line is not acceptable inside description section"
                            )
                    )
            );
            descriptionBuilder.resetCurrentDescription();
        }
    }

    private void handleDescriptionEnd(DescriptionBuilder descriptionBuilder, List<String> lines,
                                      int currentLineNr) {
        // block end
        if (descriptionBuilder.descriptionOpened()) {
            String stepName = tryExtractStepName(lines, currentLineNr + 1);
            if (stepName == null) {
                // general description
                descriptionBuilder.endExecutableDescription();
            } else {
                // step description
                descriptionBuilder.endStepDescription(stepName);
            }
        }
        // ignore
    }

    private String tryExtractStepName(List<String> lines, int lineNr) {
        String stepName = null;
        int nrOfLines = lines.size();

        if (inRange(lineNr, nrOfLines)) {
            String currentLine = lines.get(lineNr);

            while (isIgnorableLine(currentLine) && inRange(lineNr, nrOfLines)) {
                currentLine = lines.get(lineNr++);
            }

            if (inRange(lineNr, nrOfLines)) {
                // investigate line
                if (descriptionPatternMatcher.matchesStepStartLine(currentLine)) {
                    stepName = descriptionPatternMatcher.getStepName(currentLine);
                }
            }
        }

        return stepName;
    }

    private boolean isIgnorableLine(String line) {
        return StringUtils.isBlank(line) || descriptionPatternMatcher.matchesCommentLine(line);
    }

    private boolean inRange(int nr, int nrOfLines) {
        return nr < nrOfLines;
    }

    private void handleDescriptionLineComplementarySyntax(DescriptionBuilder descriptionBuilder, String
            currentLine) {
        // if description is opened
        if (descriptionBuilder.descriptionOpened()) {
            // add
            String data = descriptionPatternMatcher.getDescriptionComplementaryLineData(currentLine);
            data = data.trim();
            descriptionBuilder.addToDescriptionToMostRecentlyUsedTag(data);
        }
        // otherwise ignore
    }

    private void handleDescriptionLineGeneralSyntax(DescriptionBuilder descriptionBuilder, String currentLine) {
        // if description is opened
        if (descriptionBuilder.descriptionOpened()) {
            // add
            Pair<String, String> data = descriptionPatternMatcher.getDescriptionGeneralLineData(currentLine);
            descriptionBuilder.addToDescription(data.getLeft(), data.getRight());
        }
        // otherwise ignore
    }

    private void handleDescriptionLineVariableSyntax(DescriptionBuilder descriptionBuilder, String currentLine) {
        // if description is opened
        if (descriptionBuilder.descriptionOpened()) {
            // add
            Pair<String, String> data = descriptionPatternMatcher.getDescriptionVariableLineData(currentLine);
            descriptionBuilder.addToDescription(data.getLeft(), data.getRight());
        }
        // otherwise ignore
    }

    private void handleDescriptionLineVariableDeclarationOnlySyntax(
            DescriptionBuilder descriptionBuilder,
            String currentLine) {
        // if description is opened
        if (descriptionBuilder.descriptionOpened()) {
            // add
            Pair<String, String> data =
                    descriptionPatternMatcher.getDescriptionVariableLineDataDeclarationOnly(currentLine);
            descriptionBuilder.addToDescription(data.getLeft(), data.getRight());
        }
        // otherwise ignore
    }

    private void handleBlockStart(DescriptionBuilder descriptionBuilder, int currentLineNumberZeroBased) {
        // not clear state
        if (descriptionBuilder.descriptionOpened()) {
            // raise exception, begin description
            descriptionBuilder.addError(
                    new RuntimeException(
                            MetadataUtils.generateErrorMessage(
                                    currentLineNumberZeroBased,
                                    "Description already in progress"
                            )
                    )
            );
        }
        descriptionBuilder.beginDescription(currentLineNumberZeroBased + 1);
    }

    public void setParserExceptionHandler(ParserExceptionHandler parserExceptionHandler) {
        this.parserExceptionHandler = parserExceptionHandler;
    }
}
