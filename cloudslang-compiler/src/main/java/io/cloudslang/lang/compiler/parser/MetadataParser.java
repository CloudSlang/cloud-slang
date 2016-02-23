/*
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package io.cloudslang.lang.compiler.parser;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.parser.utils.DescriptionTag;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * User: bancl
 * Date: 1/12/2016
 */
@Component
public class MetadataParser {

    public static final String PREFIX = "#!";
    public static final int BEGIN_INDEX = 3;
    public static final String BLOCK_END = "#!!#";
    public static final String BLOCK_START = "#!!";
    public static final String COLON = ":";
    public static final List<DescriptionTag> DESCRIPTION_TAGS_LIST = Collections.unmodifiableList(
            Arrays.asList(DescriptionTag.values()));

    @Autowired
    private ParserExceptionHandler parserExceptionHandler;

    public Map<String, String> parse(SlangSource source) {
        Validate.notEmpty(source.getSource(), "Source " + source.getName() + " cannot be empty");
        try {
            String fullDescription = extractFullDescriptionString(source);
            Map<String, String> fullMap = extractFullTagMap(fullDescription);
            checkMapOrder(fullMap);
            return fullMap;
        } catch (Throwable e) {
            throw new RuntimeException("There was a problem parsing the description: " +
                    source.getName() + ".\n" + parserExceptionHandler.getErrorMessage(e), e);
        }
    }

    private void checkMapOrder(Map<String, String> fullMap) {
        if (fullMap.size() > 0) {
            Iterator<String> it = fullMap.keySet().iterator();
            int previousTagPosition = DESCRIPTION_TAGS_LIST.indexOf(getContainedTag(it.next()));
            while (it.hasNext()) {
                int keyTagPosition = DESCRIPTION_TAGS_LIST.indexOf(getContainedTag(it.next()));
                if (previousTagPosition > keyTagPosition) throw new RuntimeException("Order is not preserved.");
                previousTagPosition = keyTagPosition;
            }
        }
    }

    private Map<String, String> extractFullTagMap(String text) {
        Map<String, String> map = new LinkedHashMap<>();
        StringBuilder valueStringBuilder = new StringBuilder();
        DescriptionTag tag;
        try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
            String line = reader.readLine();
            String key = "";
            while (line != null) {
                if (lineContainsATag(line)) {
                    if (!line.contains(COLON)) throw new RuntimeException("Line \"" + line + "\" does not contain colon after name.");
                    tag = getContainedTag(line);
                    if (isSingleTagType(tag)) {
                        line = line.substring(line.indexOf(COLON) + 1);
                        key = tag != null ? tag.getValue() : "";
                    } else {
                        key = line.substring(0, line.indexOf(COLON)).trim();
                        line = line.substring(line.indexOf(COLON) + 1);
                    }
                }
                valueStringBuilder.append(line.trim()).append(System.lineSeparator());
                line = reader.readLine();
                if (line == null || lineContainsATag(line)) {
                    map.put(key, valueStringBuilder.toString().trim());
                    valueStringBuilder.setLength(0);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing metadata, error extracting metadata from ", e);
        }
        return map;
    }

    private boolean isSingleTagType(DescriptionTag tag) {
        return DescriptionTag.DESCRIPTION.equals(tag) || DescriptionTag.PREREQUISITES.equals(tag);
    }

    private DescriptionTag getContainedTag(String line) {
        for (DescriptionTag descriptionTag : DESCRIPTION_TAGS_LIST) {
            if (line.contains(descriptionTag.getValue())) return descriptionTag;
        }
        return null;
    }

    private boolean lineContainsATag(String line) {
        for (DescriptionTag descriptionTag : DESCRIPTION_TAGS_LIST) {
            if (line.contains(descriptionTag.getValue())) return true;
        }
        return false;
    }

    private String extractFullDescriptionString(SlangSource source) {
        StringBuilder sb = new StringBuilder();
        boolean blockEndFound = false, blockStartFound = false;
        String firstLine = "";
        try (BufferedReader reader = new BufferedReader(new StringReader(source.getSource()))) {
            String line = getTrimmedLine(reader);
            while (line != null) {
                if (line.startsWith(BLOCK_END)) {
                    blockEndFound = true;
                    break;
                } else if (line.startsWith(BLOCK_START)) {
                    blockStartFound = true;
                    firstLine = line;
                    line = getTrimmedLine(reader);
                }

                if (blockStartFound && line.startsWith(PREFIX) && (line.length() > 2)) {
                    sb.append(line.substring(BEGIN_INDEX)).append(System.lineSeparator());
                }
                line = getTrimmedLine(reader);
            }
            checkStartingAndClosingTags(sb, firstLine, blockEndFound, blockStartFound);
        } catch (IOException e) {
            throw new RuntimeException("Error processing metadata, error extracting metadata from " +
                    source.getName(), e);
        }
        return sb.toString();
    }

    private String getTrimmedLine(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        return line != null ? line.trim() : null;
    }

    private void checkStartingAndClosingTags(StringBuilder sb, String firstLine, boolean blockEndFound, boolean blockStartFound) {
        if (firstLine.length() > BLOCK_START.length()) {
            throw new RuntimeException("Description is not accepted on the same line as the starting tag.");
        }
        if (blockEndFound && !blockStartFound) {
            throw new RuntimeException("Starting tag missing in the description.");
        }
        if (!blockEndFound && sb.length() > 0) {
            throw new RuntimeException("Closing tag missing in the description.");
        }
    }
}
