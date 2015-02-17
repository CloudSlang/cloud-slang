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
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ForTransformer implements Transformer<String, ForLoopStatement>{

    private final static String FOR_REGEX = "^(\\s+)?(\\w+)\\s+(in)\\s+(\\w+)(\\s+)?$";
    private final static String FOR_IN_KEYWORD= " in ";

    @Override
    public ForLoopStatement transform(String rawData) {
        if (StringUtils.isEmpty(rawData)) {
            return null;
        }

        Pattern regex = Pattern.compile(FOR_REGEX);
        Matcher matcher = regex.matcher(rawData);
        String varName;
        String collectionExpression;
        if (matcher.find()) {
            varName = matcher.group(2);
            collectionExpression = matcher.group(4);
        } else {
            varName = StringUtils.substringBefore(rawData, FOR_IN_KEYWORD);
            collectionExpression = StringUtils.substringAfter(rawData, FOR_IN_KEYWORD);
        }
        varName = varName.trim();
        if (isContainInvalidChars(varName)) {
            throw new RuntimeException("for loop var name cannot contain invalid chars");
        }
        return new ForLoopStatement(varName, collectionExpression.trim());
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
