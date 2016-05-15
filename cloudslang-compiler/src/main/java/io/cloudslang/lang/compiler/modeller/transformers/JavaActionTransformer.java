package io.cloudslang.lang.compiler.modeller.transformers;
/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/


/*
 * Created by orius123 on 05/11/14.
 */

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.entities.ScoreLangConstants;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class JavaActionTransformer extends AbstractTransformer implements Transformer<Map<String, String>, Map<String, String>> {

    private static Set<String> mandatoryKeySet = Sets.newHashSet(
            SlangTextualKeys.JAVA_ACTION_CLASS_NAME_KEY,
            SlangTextualKeys.JAVA_ACTION_METHOD_NAME_KEY
    );
    private static Set<String> optionalKeySet = Sets.newHashSet(SlangTextualKeys.JAVA_ACTION_GAV_KEY);

    @Override
    public Map<String, String> transform(Map<String, String> rawData) {
        if (rawData != null) {
            validateKeySet(
                    rawData.keySet(),
                    mandatoryKeySet,
                    optionalKeySet
            );
            transformKeys(rawData);
        }

        return rawData;
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.singletonList(Scope.ACTION);
    }

    @Override
    public String keyToTransform() {
        return SlangTextualKeys.JAVA_ACTION_KEY;
    }

    private void transformKeys(Map<String, String> rawData) {
        // snake_case -> camelCase
        rawData.put(
                ScoreLangConstants.JAVA_ACTION_CLASS_KEY,
                rawData.remove(SlangTextualKeys.JAVA_ACTION_CLASS_NAME_KEY)
        );
        rawData.put(
                ScoreLangConstants.JAVA_ACTION_METHOD_KEY,
                rawData.remove(SlangTextualKeys.JAVA_ACTION_METHOD_NAME_KEY)
        );
    }

}
