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

import io.cloudslang.lang.compiler.parser.model.ParsedDescriptionData;
import io.cloudslang.lang.compiler.parser.model.ParsedDescriptionSection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Bonczidai Levente
 * @since 3/20/2017
 */
public class DescriptionBuilder {
    private List<ParsedDescriptionSection> topLevelDescriptions;
    private Map<String, ParsedDescriptionSection> stepDescriptions;
    private List<RuntimeException> errors;

    private Map<String, String> currentDescription;
    private String mostRecentlyUsedTag;
    private int currentDescriptionStartLine;

    public DescriptionBuilder() {
        init();
    }

    public boolean descriptionOpened() {
        return currentDescription != null;
    }

    public void addError(RuntimeException rex) {
        errors.add(rex);
    }

    public void beginDescription(int startLineNumber) {
        currentDescription = new LinkedHashMap<>();
        currentDescriptionStartLine = startLineNumber;
    }

    public void addToDescription(String tag, String content) {
        String tagContent = currentDescription.get(tag);
        tagContent = tagContent == null ? "" : tagContent;
        if (StringUtils.isNotBlank(content)) {
            if (org.apache.commons.lang.StringUtils.isNotEmpty(tagContent)) {
                tagContent += System.lineSeparator();
            }
            tagContent += content;
        }
        currentDescription.put(tag, tagContent);
        mostRecentlyUsedTag = tag;
    }

    public void addToDescriptionToMostRecentlyUsedTag(String content) {
        if (mostRecentlyUsedTag != null) {
            addToDescription(mostRecentlyUsedTag, content);
        }
    }

    public void endExecutableDescription() {
        topLevelDescriptions.add(new ParsedDescriptionSection(currentDescription, currentDescriptionStartLine));
        resetCurrentDescription();
    }

    public void endStepDescription(String stepName) {
        stepDescriptions.put(stepName, new ParsedDescriptionSection(currentDescription, currentDescriptionStartLine));
        resetCurrentDescription();
    }

    public void resetCurrentDescription() {
        currentDescription = null;
        mostRecentlyUsedTag = null;
        currentDescriptionStartLine = -1;
    }

    public ParsedDescriptionData build() {
        return new ParsedDescriptionData(topLevelDescriptions, stepDescriptions, errors);
    }

    private void init() {
        topLevelDescriptions = new ArrayList<>();
        stepDescriptions = new LinkedHashMap<>();
        errors = new ArrayList<>();

        resetCurrentDescription();
    }

}
