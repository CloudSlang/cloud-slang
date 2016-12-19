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
import io.cloudslang.lang.compiler.modeller.result.ParseMetadataModellingResult;
import io.cloudslang.lang.compiler.parser.utils.DescriptionTag;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.text.StrBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetadataParser {

    private static final String PREFIX = "#!";
    private static final int BEGIN_INDEX = 3;
    private static final String BLOCK_END_TAG = "#!!#";
    private static final String BLOCK_START_TAG = "#!!";
    private static final String COLON = ":";

    private ParserExceptionHandler parserExceptionHandler;

    public ParseMetadataModellingResult parse(SlangSource source) {
        Validate.notEmpty(source.getContent(), "Source " + source.getName() + " cannot be empty");
        try {
            List<RuntimeException> errors = new ArrayList<>();
            String description = extractDescriptionString(source, errors);
            Map<String, String> tagMap = extractTagMap(description, source,  errors);
            checkMapOrder(tagMap, source, errors);
            return new ParseMetadataModellingResult(tagMap, errors);
        } catch (Throwable e) {
            throw new RuntimeException("There was a problem parsing the description: " +
                    source.getName() + ".\n" + parserExceptionHandler.getErrorMessage(e), e);
        }
    }

    private void checkMapOrder(Map<String, String> tagMap, SlangSource source, List<RuntimeException> errors) {
        if (!tagMap.isEmpty()) {
            Iterator<String> it = tagMap.keySet().iterator();
            int previousTagPosition = DescriptionTag.asList().indexOf(DescriptionTag.getContainedTag(it.next()));
            while (it.hasNext()) {
                int keyTagPosition = DescriptionTag.asList().indexOf(DescriptionTag.getContainedTag(it.next()));
                if (previousTagPosition > keyTagPosition) {
                    errors.add(new RuntimeException("Order is not preserved for " + source.getName()));
                }
                previousTagPosition = keyTagPosition;
            }
        }
    }

    private Map<String, String> extractTagMap(String text, SlangSource source, List<RuntimeException> errors) {
        Map<String, String> map = new LinkedHashMap<>();
        StrBuilder valueStringBuilder = new StrBuilder();
        DescriptionTag tag;
        try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
            String line = reader.readLine();
            String key = "";
            while (line != null) {
                if (DescriptionTag.stringContainsTag(line)) {
                    tag = DescriptionTag.getContainedTag(line);
                    if (checkTagIsFollowedByColon(line, tag, source, errors)) {
                        if (DescriptionTag.isUniqueTagType(tag)) {
                            line = line.substring(line.indexOf(COLON) + 1);
                            key = tag != null ? tag.getValue() : "";
                        } else {
                            key = line.substring(0, line.indexOf(COLON)).trim();
                            line = line.substring(line.indexOf(COLON) + 1);
                        }
                    } else {
                        key = line;
                    }
                }
                valueStringBuilder.appendln(line.trim());
                line = reader.readLine();
                putValueInMapAndResetBuilder(map, key, valueStringBuilder, line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing metadata, error extracting metadata from " +
                    source.getName(), e);
        }
        return map;
    }

    private boolean checkTagIsFollowedByColon(String line, DescriptionTag tag, SlangSource source,
                                              List<RuntimeException> errors) {
        Pattern pattern = Pattern.compile("@[\\w\\s]+:");
        Matcher matcher = pattern.matcher(line);
        if (!matcher.lookingAt()) {
            throwExceptionIfColonMissing(line, tag, source, errors);
            return false;
        }
        return true;
    }

    private void throwExceptionIfColonMissing(String line, DescriptionTag tag, SlangSource source,
                                              List<RuntimeException> errors) {
        String lineWithoutTag = line.replace(tag.getValue(), "").trim();
        StringTokenizer stringTokenizer = new StringTokenizer(lineWithoutTag);
        if ((DescriptionTag.isUniqueTagType(tag) && stringTokenizer.countTokens() == 1) ||
                stringTokenizer.countTokens() > 1) {
            errors.add(new RuntimeException("Line \"" + line +
                    "\" does not contain colon between the tag name and the description of the tag for " +
                    source.getName()));
        }
    }

    private void putValueInMapAndResetBuilder(Map<String, String> map,
                                              String key, StrBuilder valueStringBuilder, String line) {
        if (line == null || DescriptionTag.stringContainsTag(line)) {
            String value = valueStringBuilder.trim().build();
            if (StringUtils.equals(key, value)) {
                map.put(key, "");
            } else {
                map.put(key, value);
            }
            valueStringBuilder.clear();
        }
    }

    private String extractDescriptionString(SlangSource source, List<RuntimeException> errors) {
        StrBuilder strBuilder = new StrBuilder();
        boolean blockEndTagFound = false;
        boolean blockStartTagFound = false;
        String firstLine = "";
        try (BufferedReader reader = new BufferedReader(new StringReader(source.getContent()))) {
            String line = getTrimmedLine(reader);
            while (line != null) {
                if (line.startsWith(BLOCK_END_TAG)) {
                    blockEndTagFound = true;
                    break;
                } else if (line.startsWith(BLOCK_START_TAG)) {
                    blockStartTagFound = true;
                    firstLine = line;
                    line = getTrimmedLine(reader);
                    if (line.startsWith(BLOCK_END_TAG)) {
                        break;
                    }
                }

                appendValidLineToOutput(strBuilder, blockStartTagFound, line);
                line = getTrimmedLine(reader);
            }
            checkStartingAndClosingTags(strBuilder, firstLine, blockEndTagFound, blockStartTagFound, source, errors);
        } catch (IOException e) {
            throw new RuntimeException("Error processing metadata, error extracting metadata from " +
                    source.getName(), e);
        }
        return strBuilder.build();
    }

    private void appendValidLineToOutput(StrBuilder sb, boolean blockStartTagFound, String line) {
        if (blockStartTagFound && line.startsWith(PREFIX) && (line.length() > 2)) {
            sb.appendln(line.substring(BEGIN_INDEX));
        }
    }

    private String getTrimmedLine(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        return line != null ? line.trim() : null;
    }

    private void checkStartingAndClosingTags(StrBuilder sb, String firstLine, boolean blockEndTagFound,
                                             boolean blockStartTagFound, SlangSource source,
                                             List<RuntimeException> errors) {
        if (firstLine.length() > BLOCK_START_TAG.length()) {
            errors.add(new RuntimeException("Description is not accepted on the same line as the starting tag for " +
                    source.getName()));
        }
        if (blockEndTagFound && !blockStartTagFound) {
            errors.add(new RuntimeException("Starting tag missing in the description for " + source.getName()));
        }
        if (!blockEndTagFound && sb.length() > 0) {
            errors.add(new RuntimeException("Closing tag missing in the description for " + source.getName()));
        }
    }

    public void setParserExceptionHandler(ParserExceptionHandler parserExceptionHandler) {
        this.parserExceptionHandler = parserExceptionHandler;
    }
}
