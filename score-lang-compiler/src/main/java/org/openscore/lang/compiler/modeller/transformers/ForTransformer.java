/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.openscore.lang.compiler.modeller.transformers;

import org.apache.commons.lang.StringUtils;
import org.openscore.lang.entities.ForLoopStatement;
import org.openscore.lang.entities.ListForLoopStatement;
import org.openscore.lang.entities.MapForLoopStatement;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ForTransformer implements Transformer<String, ForLoopStatement>{

    // case: value in variable_name
    private final static String FOR_REGEX = "^(\\s+)?(\\w+)\\s+(in)\\s+(\\w+)(\\s+)?$";
    // case: key, value
    private final static String KEY_VALUE_PAIR_REGEX = "^(\\s+)?(\\w+)(\\s+)?(,)(\\s+)?(\\w+)(\\s+)?$";
    private final static String FOR_IN_KEYWORD= " in ";

    @Override
    public ForLoopStatement transform(String rawData) {
        if (StringUtils.isEmpty(rawData)) {
            return null;
        }

        ForLoopStatement forLoopStatement;
        String varName;
        String collectionExpression;

        Pattern regexSimpleFor = Pattern.compile(FOR_REGEX);
        Matcher matcherSimpleFor = regexSimpleFor.matcher(rawData);

        if (matcherSimpleFor.find()) {
            // case: value in variable_name
            varName = matcherSimpleFor.group(2);
            collectionExpression = matcherSimpleFor.group(4);
            forLoopStatement = new ListForLoopStatement(varName, collectionExpression);
        } else {
            String beforeInKeyword = StringUtils.substringBefore(rawData, FOR_IN_KEYWORD);
            collectionExpression = StringUtils.substringAfter(rawData, FOR_IN_KEYWORD).trim();

            Pattern regexKeyValueFor = Pattern.compile(KEY_VALUE_PAIR_REGEX);
            Matcher matcherKeyValueFor = regexKeyValueFor.matcher(beforeInKeyword);

            if (matcherKeyValueFor.find()) {
                // case: key, value
                String keyName = matcherKeyValueFor.group(2);
                String valueName = matcherKeyValueFor.group(6);

                forLoopStatement = new MapForLoopStatement(
                        keyName,
                        valueName,
                        collectionExpression);
            } else {
                // case: value in expression_other_than_variable_name
                varName = beforeInKeyword.trim();
                if (isContainInvalidChars(varName)) {
                    throw new RuntimeException("for loop var name cannot contain invalid chars");
                }
                forLoopStatement = new ListForLoopStatement(varName, collectionExpression);
            }
        }

        return forLoopStatement;
    }

    private boolean isContainInvalidChars(String varName) {
        return StringUtils.containsAny(varName, " \t\r\n\b");
    }


    @Override
    public List<Scope> getScopes() {
        return Arrays.asList(Scope.BEFORE_TASK);
    }

    @Override
    public String keyToTransform() {
        return null;
    }

}
