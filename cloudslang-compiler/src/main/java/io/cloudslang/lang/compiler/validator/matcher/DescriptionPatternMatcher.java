/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.validator.matcher;

import io.cloudslang.lang.entities.constants.Regex;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Bonczidai Levente
 * @since 3/20/2017
 */
public class DescriptionPatternMatcher {
    private final Pattern descriptionStartPattern;
    private final Pattern descriptionVariableLinePattern;
    private final Pattern descriptionGeneralLinePattern;
    private final Pattern descriptionComplementaryLinePattern;
    private final Pattern descriptionEndPattern;
    private final Pattern commentLinePattern;
    private final Pattern stepStartLinePattern;
    private final Pattern executableDelimiterLinePattern;
    private final Pattern stepDelimiterLinePattern;
    private final Pattern emptyLinePattern;
    private final Pattern descriptionVariableLineDeclarationOnlyPattern;

    public DescriptionPatternMatcher() {
        descriptionStartPattern = Pattern.compile(Regex.DESCRIPTION_START_LINE);
        descriptionVariableLinePattern = Pattern.compile(Regex.DESCRIPTION_VARIABLE_LINE);
        descriptionGeneralLinePattern = Pattern.compile(Regex.DESCRIPTION_GENERAL_LINE);
        descriptionComplementaryLinePattern = Pattern.compile(Regex.DESCRIPTION_COMPLEMENTARY_LINE);
        descriptionEndPattern = Pattern.compile(Regex.DESCRIPTION_END_LINE);
        commentLinePattern = Pattern.compile(Regex.COMMENT_LINE);
        stepStartLinePattern = Pattern.compile(Regex.STEP_START_LINE);
        executableDelimiterLinePattern = Pattern.compile(Regex.EXECUTABLE_DESCRIPTION_DELIMITER_LINE);
        stepDelimiterLinePattern = Pattern.compile(Regex.STEP_DESCRIPTION_DELIMITER_LINE);
        emptyLinePattern = Pattern.compile(Regex.DESCRIPTION_EMPTY_LINE);
        descriptionVariableLineDeclarationOnlyPattern =
                Pattern.compile(Regex.DESCRIPTION_VARIABLE_LINE_DECLARATION_ONLY);
    }

    public boolean matchesDescriptionStart(String input) {
        return descriptionStartPattern.matcher(input).matches();
    }

    public boolean matchesDescriptionEnd(String input) {
        return descriptionEndPattern.matcher(input).matches();
    }

    public boolean matchesDescriptionVariableLine(String input) {
        return descriptionVariableLinePattern.matcher(input).matches();
    }

    public boolean matchesDescriptionGeneralLine(String input) {
        return descriptionGeneralLinePattern.matcher(input).matches();
    }

    public boolean matchesDescriptionComplementaryLine(String input) {
        return descriptionComplementaryLinePattern.matcher(input).matches();
    }

    public boolean matchesCommentLine(String input) {
        return commentLinePattern.matcher(input).matches();
    }

    public boolean matchesStepStartLine(String input) {
        return stepStartLinePattern.matcher(input).matches();
    }

    public boolean matchesExecutableDelimiterLine(String input) {
        return executableDelimiterLinePattern.matcher(input).matches();
    }

    public boolean matchesStepDelimiterLine(String input) {
        return stepDelimiterLinePattern.matcher(input).matches();
    }

    public boolean matchesEmptyLine(String input) {
        return emptyLinePattern.matcher(input).matches();
    }

    public boolean matchesVariableLineDeclarationOnlyLine(String input) {
        return descriptionVariableLineDeclarationOnlyPattern.matcher(input).matches();
    }

    public String getStepName(String input) {
        List<String> matches = getData(stepStartLinePattern, input, Regex.STEP_START_LINE_DATA_GROUP_NR);
        if (CollectionUtils.isNotEmpty(matches)) {
            return matches.get(0);
        } else {
            return null;
        }
    }

    public Pair<String, String> getDescriptionVariableLineData(String input) {
        List<String> matches = getData(
                descriptionVariableLinePattern,
                input,
                Regex.DESCRIPTION_VARIABLE_LINE_DECLARATION_GROUP_NR,
                Regex.DESCRIPTION_VARIABLE_LINE_CONTENT_GROUP_NR
        );
        return new ImmutablePair<>(matches.get(0), matches.get(1));
    }

    public Pair<String, String> getDescriptionVariableLineDataDeclarationOnly(String input) {
        List<String> matches = getData(
                descriptionVariableLineDeclarationOnlyPattern,
                input,
                Regex.DESCRIPTION_VARIABLE_LINE_DECLARATION_ONLY_GROUP_NR
        );
        return new ImmutablePair<>(matches.get(0), "");
    }

    public Pair<String, String> getDescriptionGeneralLineData(String input) {
        List<String> matches = getData(
                descriptionGeneralLinePattern,
                input,
                Regex.DESCRIPTION_GENERAL_LINE_DECLARATION_GROUP_NR,
                Regex.DESCRIPTION_GENERAL_LINE_CONTENT_GROUP_NR
        );
        return new ImmutablePair<>(matches.get(0), matches.get(1));
    }

    public String getDescriptionComplementaryLineData(String input) {
        String data = getData(
                descriptionComplementaryLinePattern,
                input,
                Regex.DESCRIPTION_COMPLEMENTARY_LINE_GROUP_NR
        ).get(0);
        return data == null ? "" : data;
    }

    @SuppressWarnings("unused")
    public boolean isLineAcceptedInsideDescription(String input) {
        // does not allow any line
        return false;
    }

    public String[] splitDeclaration(String declaration) {
        return declaration.split(Regex.DESCRIPTION_DECLARATION_DELIMITER);
    }

    private List<String> getData(Pattern pattern, String input, int... groupNumbers) {
        Matcher matcher = pattern.matcher(input);
        List<String> matchedGroups = new ArrayList<>();
        if (matcher.find()) {
            for (int groupNr : groupNumbers) {
                matchedGroups.add(matcher.group(groupNr));
            }
        }
        return matchedGroups;
    }
}
