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
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class MetadataValidatorImpl implements MetadataValidator {

    private static final String DESCRIPTION_DELIMITER_LINE = "#######################################" +
            "#################################################################################";

    public List<RuntimeException> validateCheckstyle(SlangSource source) {
        List<RuntimeException> exceptions = new ArrayList<>();
        String lineBeforeBlockStartTag = null;
        String lineAfterBlockEndTag = null;
        try (BufferedReader reader = new BufferedReader(new StringReader(source.getContent()))) {
            String line = reader.readLine();
            String previousLine = line;
            String previousMetadataLine = null;
            String previousTagLine = null;
            while (line != null) {
                if (DescriptionTag.stringContainsTag(line)) {
                    previousTagLine = line;
                }
                if (line.startsWith(MetadataParser.BLOCK_END_TAG)) {
                    lineAfterBlockEndTag = reader.readLine();
                    break;
                } else if (line.startsWith(MetadataParser.BLOCK_START_TAG)) {
                    lineBeforeBlockStartTag = previousLine;
                    line = reader.readLine();
                    if (line != null && line.startsWith(MetadataParser.BLOCK_END_TAG)) {
                        lineAfterBlockEndTag = reader.readLine();
                        break;
                    }
                } else {
                    if (line.startsWith(MetadataParser.PREFIX)) {
                        previousMetadataLine = line;
                    }
                    previousLine = line;
                    line = reader.readLine();
                }
                validateNewlineBetweenDifferentTags(previousTagLine, previousMetadataLine,
                        line, exceptions, source);
            }
            validateHashTagLines(lineBeforeBlockStartTag, lineAfterBlockEndTag, exceptions, source);
        } catch (IOException e) {
            throw new RuntimeException("Error processing metadata, error extracting metadata from " +
                    source.getName(), e);
        }

        return exceptions;
    }

    private void validateHashTagLines(String lineBeforeBlockStartTag, String lineAfterBlockEndTag,
                                      List<RuntimeException> exceptions, SlangSource source) {
        if (lineBeforeBlockStartTag != null &&
                !StringUtils.contains(lineBeforeBlockStartTag, DESCRIPTION_DELIMITER_LINE)) {
            exceptions.add(new RuntimeException("Before the description start tag there should be a line containing " +
                    "120 '#' characters for " + source.getFilePath()));
        }
        if (lineAfterBlockEndTag != null &&
                !StringUtils.contains(lineAfterBlockEndTag, DESCRIPTION_DELIMITER_LINE)) {
            exceptions.add(new RuntimeException("After the description end tag there should be a line containing " +
                    "120 '#' characters for " + source.getFilePath()));
        }
    }

    private void validateNewlineBetweenDifferentTags(String previousTagLine, String previousMetadataLine, String line,
                                                     List<RuntimeException> exceptions, SlangSource source) {
        if (previousTagLine != null && bothLinesContainTags(previousTagLine, line) &&
                containedTagsAreDifferent(previousTagLine, line) &&
                isNotEmptyLineWithPrefix(previousMetadataLine)) {
            exceptions.add(new RuntimeException("For " + source.getFilePath() +
                    " the newline with metadata prefix '#!' is missing " +
                    "in the description before the following line: " + System.lineSeparator() + line));
        }
    }

    private boolean bothLinesContainTags(String previousTagLine, String line) {
        return DescriptionTag.stringContainsTag(previousTagLine) && DescriptionTag.stringContainsTag(line);
    }

    private boolean containedTagsAreDifferent(String tagLine, String line) {
        return DescriptionTag.getContainedTag(tagLine).compareTo(DescriptionTag.getContainedTag(line)) != 0;
    }

    private boolean isNotEmptyLineWithPrefix(String previousMetadataLine) {
        return !StringUtils.isBlank(StringUtils.remove(previousMetadataLine, MetadataParser.PREFIX));
    }

}
