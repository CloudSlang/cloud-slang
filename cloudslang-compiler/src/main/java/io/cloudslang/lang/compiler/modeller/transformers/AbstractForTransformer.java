/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.transformers;

import io.cloudslang.lang.entities.AsyncLoopStatement;
import io.cloudslang.lang.entities.ListForLoopStatement;
import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.entities.MapForLoopStatement;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date: 3/25/2015
 *
 * @author Bonczidai Levente
 */
public abstract class AbstractForTransformer {

    // case: value in variable_name
    private final static String FOR_REGEX = "^(\\s+)?(\\w+)\\s+(in)\\s+(\\w+)(\\s+)?$";
    // case: key, value
    private final static String KEY_VALUE_PAIR_REGEX = "^(\\s+)?(\\w+)(\\s+)?(,)(\\s+)?(\\w+)(\\s+)?$";
    private final static String FOR_IN_KEYWORD= " in ";

    public LoopStatement transformToLoopStatement(String rawData, boolean isAsyncLoop) {
        if (StringUtils.isEmpty(rawData)) {
            return null;
        }

        LoopStatement loopStatement;
        String varName;
        String collectionExpression;

        Pattern regexSimpleFor = Pattern.compile(FOR_REGEX);
        Matcher matcherSimpleFor = regexSimpleFor.matcher(rawData);

        if (matcherSimpleFor.find()) {
            // case: value in variable_name
            varName = matcherSimpleFor.group(2);
            collectionExpression = matcherSimpleFor.group(4);
            if (isAsyncLoop) {
                loopStatement = new AsyncLoopStatement(varName, collectionExpression);
            } else {
                loopStatement = new ListForLoopStatement(varName, collectionExpression);
            }
        } else {
            String beforeInKeyword = StringUtils.substringBefore(rawData, FOR_IN_KEYWORD);
            collectionExpression = StringUtils.substringAfter(rawData, FOR_IN_KEYWORD).trim();

            Pattern regexKeyValueFor = Pattern.compile(KEY_VALUE_PAIR_REGEX);
            Matcher matcherKeyValueFor = regexKeyValueFor.matcher(beforeInKeyword);

            if (matcherKeyValueFor.find()) {
                // case: key, value
                String keyName = matcherKeyValueFor.group(2);
                String valueName = matcherKeyValueFor.group(6);

                loopStatement = new MapForLoopStatement(
                        keyName,
                        valueName,
                        collectionExpression);
            } else {
                // case: value in expression_other_than_variable_name
                varName = beforeInKeyword.trim();
                if (isContainInvalidChars(varName)) {
                    throw new RuntimeException("for loop var name cannot contain invalid chars");
                }
                if (isAsyncLoop) {
                    loopStatement = new AsyncLoopStatement(varName, collectionExpression);
                } else {
                    loopStatement = new ListForLoopStatement(varName, collectionExpression);
                }
            }
        }

        return loopStatement;
    }

    private boolean isContainInvalidChars(String varName) {
        return StringUtils.containsAny(varName, " \t\r\n\b");
    }
}
