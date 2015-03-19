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
import org.openscore.lang.entities.AsyncLoopStatement;
import org.openscore.lang.entities.ScoreLangConstants;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date: 3/17/2015
 *
 * @author Bonczidai Levente
 */
@Component
public class AsyncLoopForTransformer implements Transformer<String, AsyncLoopStatement> {

    // case: value in variable_name
    private final static String FOR_REGEX = "^(\\s+)?(\\w+)\\s+(in)\\s+(\\w+)(\\s+)?$";
    private final static String FOR_IN_KEYWORD= " in ";

    @Override
    public AsyncLoopStatement transform(String rawData) {
        if (StringUtils.isEmpty(rawData)) {
            return null;
        }

        AsyncLoopStatement AsyncLoopStatement;
        String varName;
        String listExpression;

        Pattern regexFor = Pattern.compile(FOR_REGEX);
        Matcher matcherSimpleFor = regexFor.matcher(rawData);

        if (matcherSimpleFor.find()) {
            // case: value in variable_name
            varName = matcherSimpleFor.group(2);
            listExpression = matcherSimpleFor.group(4);
            AsyncLoopStatement = new AsyncLoopStatement(varName, listExpression);
        } else {
            // case: value in expression_other_than_variable_name
            varName = StringUtils.substringBefore(rawData, FOR_IN_KEYWORD).trim();
            listExpression = StringUtils.substringAfter(rawData, FOR_IN_KEYWORD).trim();

            if (isContainInvalidChars(varName)) {
                throw new RuntimeException("for loop var name cannot contain invalid chars");
            }

            AsyncLoopStatement = new AsyncLoopStatement(varName, listExpression);
        }

        return AsyncLoopStatement;
    }

    private boolean isContainInvalidChars(String varName) {
        return StringUtils.containsAny(varName, " \t\r\n\b");
    }


    @Override
    public List<Transformer.Scope> getScopes() {
        return Arrays.asList(Transformer.Scope.BEFORE_TASK);
    }

    @Override
    public String keyToTransform() {
        return ScoreLangConstants.ASYNC_LOOP_KEY;
    }

}
